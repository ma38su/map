package map.route;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import jp.sourceforge.ma38su.util.Log;

import map.MapDataManager;
import map.Road;
import map.route.tsp.DistanceTable;
import map.route.tsp.NearestNeighbor;
import map.route.tsp.Opt2;
import map.route.tsp.TspImprovement;
import map.sdf25k.Node;

/**
 * 単方向探索による最短経路探索を行うスレッド
 * @author ma38su
 */
public class RouteNavigation {

	private double distance;
	
	/**
	 * ヒューリスティックな距離関数
	 */
	private HeuristicDistanceFunction heuristic;
	
	/**
	 * 終点となる端点
	 */
	private Node last;

	/**
	 * 地図データ
	 */
	private final MapDataManager manager;

	/**
	 * 探索の端点
	 */
	private LinkedList<Node> nodes;

	/**
	 * 探索済みのルート
	 */
	private List<RouteEntry> route;

	/**
	 * これから探索を行うキュー
	 */
	final Queue<RouteEntry> task;

	/**
	 * 現在の探索スレッド
	 */
	private Thread thread;

	/**
	 * 高速道路利用のフラグ
	 */
	private boolean useHighway;

	/**
	 * 訪問頂点数
	 */
	private int vp;

	/**
	 * 単方向の検索スレッド
	 * @param name アルゴリズム名
	 * @param node 始点
	 * @param manager 地図データ
	 * @param heuristic 近似距離関数
	 * @param heap ヒープ
	 * @param useHighway 高速道路利用のフラグ
	 */
	public RouteNavigation(MapDataManager manager) {
		this.nodes = new LinkedList<Node>();
		this.manager = manager;
		this.useHighway = true;
		this.task = new LinkedList<RouteEntry>();
		this.route = new ArrayList<RouteEntry>();
		this.routeTsp = new ArrayList<RouteEntry>();
		this.tspImprove = new Opt2();
		manager.set(this);
	}

	/**
	 * ルートを削除します。
	 */
	public void clear() {
		this.thread = null;
		this.task.clear();
		this.nodes.clear();
		synchronized (this.route) {
			this.route.clear();
		}
		synchronized (this.routeTsp) {
			this.routeTsp.clear();
		}
		this.vp = 0;
		this.distance = 0;
	}

	/**
	 * 道路のコストを求める
	 * @param road 道路
	 * @return 道路のコスト
	 */
	private float getCost(Road road) {
		if (road == null) {
			return 0;
		} else if (!this.useHighway && road.getType() == 3) {
			return Float.POSITIVE_INFINITY;
		}
		return road.getCost();
	}

	
	private NearestNeighbor tsp = new NearestNeighbor();
	private TspImprovement tspImprove;
	
	public void searchTsp() {
		List<Node> list = new ArrayList<Node>(this.nodes);
		Log.out(this, "tsp start.");
		DistanceTable table = new DistanceTable(this.manager, list);
		Log.out(this, "DistanceTable maked.");
		List<Node> result = this.tsp.method(list, table);
		Log.out(this, this.tsp.toString() + " finish.");
		while(this.tspImprove.method(result, table));
		Log.out(this, this.tspImprove.toString() + " finish.");
		synchronized (this.routeTsp) {
			this.routeTsp.clear();
			this.routeTsp.addAll(table.getRouteEntry(result));
		}
		Log.out(this, "tsp finish.");
		this.manager.searchedFinished();
	}

	public void switchTsp() {
		this.isTsp = !this.isTsp;
	}
	/**
	 * 探索済みなら最短経路を返す
	 * @return 最短経路
	 */
	public List<RouteEntry> getRoute() {
		List<RouteEntry> route;
		if (this.isTsp) {
			synchronized (this.routeTsp) {
				route = new ArrayList<RouteEntry>(this.routeTsp);
			}
		} else {
			synchronized (this.route) {
				route = new ArrayList<RouteEntry>(this.route);
			}
		}
		return route;
	}
	
	List<RouteEntry> routeTsp;
	
	/**
	 * 端点のリストを返します。
	 * @return 端点のリスト
	 */
	public List<Node> getTerminal() {
		return this.nodes;
	}
	
	/**
	 * 訪問頂点数を返します。
	 * 
	 * @return 訪問頂点数
	 */
	public int getVP() {
		return this.vp;
	}

	public synchronized void reroute() {
		Node n1 = null;
		if (this.nodes.size() >= 2) {
			this.task.clear();
			if (this.isTsp) {
				this.thread = new Thread() {
					@Override
					public void run() {
						searchTsp();
					}
				};
			} else {
				for (Node node : this.nodes) {
					if (n1 != null) {
						this.task.add(new RouteEntry(n1, node));
					}
					n1 = node;
				}
				this.thread = new Thread() {
					@Override
					public void run() {
						while (!RouteNavigation.this.task.isEmpty()) {
							search(RouteNavigation.this.task.poll(), this);
						}
					}
				};
			}
			this.thread.setPriority(Thread.MIN_PRIORITY);
			this.thread.start();
		}
	}
	
	private boolean isTsp;

	/**
	 * 探索する頂点を追加します。
	 * @param node
	 */
	public synchronized void put(Node node) {
		if (this.isTsp) {
			this.nodes.add(node);
			if (this.nodes.size() > 1) {
				this.thread = new Thread() {
					@Override
					public void run() {
						searchTsp();
					}
				};
				this.thread.setPriority(Thread.MIN_PRIORITY);
				this.thread.start();
			}
		} else {
			if (this.nodes.size() > 0) {
				Node n1 = this.nodes.getLast();
				if (n1.equals(node)) {
					return;
				}
				this.task.add(new RouteEntry(n1, node));
			}
			this.nodes.add(node);
			if (this.thread == null || (!this.thread.isAlive() && !node.equals(this.last))) {
				this.thread = new Thread() {
					@Override
					public void run() {
						while (!RouteNavigation.this.task.isEmpty()) {
							search(RouteNavigation.this.task.poll(), this);
						}
					}
				};
				this.thread.setPriority(Thread.MIN_PRIORITY);
				this.thread.start();
			}
		}
	}
	
	/**
	 * ２頂点間の経路探索を行います。
	 * @param route ルート探索のエントリー
	 * @param thread 探索を行うスレッド
	 */
	void search(RouteEntry route, Thread thread) {
		Node start = route.getStart();
		Node terminal = route.getTerminal();
		this.last = terminal;
		double distance = 0;
		Heap heap = this.heuristic == null ? new Heap() : new Heap(new HeuristicComparator(this.heuristic, terminal));
		Map<Long, Long> path = new HashMap<Long, Long>();
		Map<Long, Float> vp = new HashMap<Long, Float>();
		try {
			long t = terminal.getID();
			// 初期化
			heap.put(start, 0);
			do {
				Heap.Entry entry = heap.poll();
				Node p = entry.getKey();
				float d = entry.getValue();
				if (p.getID() == t) {
					distance = d;
					long r = terminal.getID();
					while (path.containsKey(r)) {
						Long next = path.get(r);
						if (next == null) {
							break;
						}
						Node n = this.manager.getNode(r);
						if (n == null) {
							Log.out(this, r + " > "+ next);
							break;
						}
						Road road = n.getEdge().get(next);
						if(road != null) {
							if (road.getArrayX() == null) {
								throw new IOException();
							}
							route.add(road);
						}
						r = next;
					}
					break;
				}
				// p を訪問済み頂点とする
				vp.put(p.getID(), d);

				// 隣接頂点までの距離を設定
				for (Map.Entry<Long, Road> edge : p.getEdge().entrySet()) {
					Long key = edge.getKey();
					if (vp.containsKey(key)) {
						continue;
					}
					final Node v = this.manager.getNode(key);
					final float cost = this.getCost(edge.getValue());

					if(cost == Float.POSITIVE_INFINITY) {
						continue;
					}

					if(heap.put(v, d + cost)) {
						path.put(v.getID(), p.getID());
					}
				}
			} while (!heap.isEmpty());
		} catch (IOException e) {
			Log.err(this, e);
			distance = -1;
			route.clear();
		} catch (OutOfMemoryError e) {
			distance = -2;
			route.clear();
		} finally {
			if (thread.equals(this.thread)) {
				this.vp += vp.size();
				route.setCost(distance);
				this.distance += distance;
				synchronized (this.route) {
					this.route.add(route);
				}
				this.manager.searchedFinished();
			}
		}
	}

	/**
	 * 評価関数を設定します。
	 * @param heurisitc
	 */
	public void setFunction(HeuristicDistanceFunction heurisitc) {
		this.heuristic = heurisitc;
	}

	/**
	 * 高速道路利用の有無を切り替えます。
	 */
	public void switchHighway() {
		this.useHighway = !this.useHighway;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (this.thread != null && this.thread.isAlive()) {
			sb.append(" / SEARCHING");
		} else if (this.nodes.size() >= 2) {
			sb.append(" / DISTANCE: ");
			sb.append((int) this.distance / 1000D);
			sb.append("km");
		}
		return sb.toString();
	}
}