package map.route;

import java.util.HashMap;
import java.util.Map;

import jp.sourceforge.ma38su.util.Log;

import map.MapDataManager;
import map.Road;
import map.sdf25k.Node;

public class AStar {
	private MapDataManager manager;
	public AStar(MapDataManager db) {
		this.manager = db;
	}
	public RouteEntry search(Node s, Node t) {
		Heap heap = new Heap(new HeuristicComparator(new DirectDistance(), t));
		Map<Node, Node> path = new HashMap<Node, Node>();
		Map<Long, Float> vp = new HashMap<Long, Float>();
		heap.put(s, 0);
		int count = 0;
		RouteEntry route = new RouteEntry(s, t);
		do {
			Heap.Entry entry = heap.poll();
			Node p = entry.getKey();
			float d = entry.getValue();

			// p を訪問済み頂点とする
			vp.put(p.getID(), d);
			if (t.equals(p)) {
				route.setCost(d);
				this.traceback(path, route);
				break;
			}

			// 隣接頂点までの距離を設定
			for (Map.Entry<Long, Road> edge : p.getEdge().entrySet()) {
				Long key = edge.getKey();
				if (!vp.containsKey(key)) {
					final Node v = this.manager.getNode(key);
					final float cost = this.getCost(edge.getValue());
					if(cost != Float.POSITIVE_INFINITY) {
						if(heap.put(v, d + cost)) {
							path.put(v, p);
						}
					}
				}
			}
		} while (!heap.isEmpty() || ++count < 5000);
		if (count == 5000) {
			System.out.println("out of search");
		}
		return route;
	}
	
	public void traceback(Map<Node, Node> path, RouteEntry route) {
		Node terminal = route.getTerminal();
		Node node = terminal;
		while (path.containsKey(node)) {
			Node next = path.get(node);
			if (next == null) {
				Log.out(this, node.getID() + " = null");
				break;
			}
			Road road = node.getEdge().get(next.getID());
			if(road != null) {
				if (road.getArrayX() == null) {
					throw new UnknownError();
				}
				route.add(road);
			}
			node = next;
		}
	}

	/**
	 * 道路のコストを求める
	 * @param road 道路
	 * @return 道路のコスト
	 */
	private float getCost(Road road) {
		if (road == null) {
			return 0;
		} else if (road.getType() == 3) {
			return Float.POSITIVE_INFINITY;
		}
		return road.getCost();
	}
}
