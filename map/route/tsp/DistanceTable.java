package map.route.tsp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import map.MapDataManager;
import map.route.RouteEntry;
import map.sdf25k.Node;

/**
 * 有向グラフにおける任意の頂点の距離テーブル
 * @author ma38su
 */
public class DistanceTable {

	/**
	 * 距離取得のためのキャッシュ
	 */
	private RouteEntry cache;
	
	/**
	 * 距離テーブルのマップ
	 */
	private Map<RouteEntry, RouteEntry> map;

	public DistanceTable(MapDataManager db, Collection<Node> nodes) {
		this.cache = new RouteEntry();
		this.map = new HashMap<RouteEntry, RouteEntry>();
		AStar path = new AStar(db);
		for (Node s : nodes) {
			for (Node t : nodes) {
				if (!s.equals(t)) {
					RouteEntry route = path.search(s, t);
					this.map.put(route, route);
				}
			}
		}
	}
	
	/**
	 * 始点と終点から距離を取得します。
	 * @param s 始点
	 * @param t 終点
	 * @return 距離
	 */
	public RouteEntry get(Node s, Node t) {
		synchronized (this.cache) {
			this.cache.setRoute(s, t);
			RouteEntry route = this.map.get(this.cache);
			return route;
		}
	}

	/**
	 * 2頂点間の最短経路を求めます。
	 * @param s 始点
	 * @param t 終点
	 * @return 終点
	 */
	public Double getCost(Node s, Node t) {
		return this.get(s, t).getCost();
	}
	
	/**
	 * 頂点配列からルート配列を取得します。
	 * @param nodes 頂点配列
	 * @return ルート配列
	 */
	public List<RouteEntry> getRouteEntry(List<Node> nodes) {
		List<RouteEntry> route = new ArrayList<RouteEntry>(nodes.size() + 1);
		Node n0 = nodes.get(nodes.size() - 1);
		int count = 0;
		synchronized (this.cache) {
			for (Node node : nodes) {
				this.cache.setRoute(n0, node);
				RouteEntry entry = this.map.get(this.cache);
				if (entry == null) {
					System.out.println(count + " / "+ nodes.size());
					System.out.println(n0.getID() + " : "+ node.getID());
				} else {
					route.add(entry);
				}
				n0 = node;
				count++;
			}
		}
		return route;
	}
}
