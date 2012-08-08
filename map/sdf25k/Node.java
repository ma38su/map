package map.sdf25k;

import java.awt.Point;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import map.Road;


/**
 * 数値地図25000の道路の頂点
 * @author ma38su
 */
public class Node implements Serializable {

	/**
	 * 頂点番号
	 */
	private final long id;

	/**
	 * 座標
	 */
	private final Point point;
	/**
	 * 隣接頂点と辺の情報
	 */
	private final Map<Long, Road> edge;
	
	/**
	 * 接続している最低レベルの辺
	 */
	private int level;

	/**
	 * 頂点クラスを作成
	 * @param id 頂点番号
	 * @param p 座標
	 */
	public Node(long id, Point p) {
		this.id = id;
		this.point = p;
		this.edge = new HashMap<Long, Road>();
	}
	/**
	 * 頂点クラスを作成
	 * @param id 頂点番号
	 * @param x X座標
	 * @param y Y座標
	 */
	public Node(long id, int x, int y) {
		this.id = id;
		this.point = new Point(x, y);
		this.edge = new HashMap<Long, Road>();
	}
	/**
	 * 隣接頂点を追加する
	 * @param node 隣接頂点番号
	 * @param road 隣接頂点までの辺番号
	 */
	public void connect(Long node, Road road) {
		if (road != null && this.level < road.getWidth()) {
			this.level = road.getWidth();
		}
		this.edge.put(node, road);
	}
	/**
	 * 頂点番号を返す
	 * @return 頂点番号
	 */
	public long getID() {
		return this.id;
	}
	/**
	 * 頂点のX座標を返す
	 * @return 頂点のX座標
	 */
	public int getX() {
		return this.point.x;
	}
	/**
	 * 頂点のY座標を返す
	 * @return 頂点のY座標
	 */
	public int getY() {
		return this.point.y;
	}
	/**
	 * 隣接頂点情報を返す
	 * @return 隣接頂点情報
	 */
	public Map<Long, Road> getEdge() {
		return this.edge;
	}
	
	@Override
	public int hashCode() {
		return (int)(this.id % Integer.MAX_VALUE);
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof Node) {
			Node n = (Node) o;
			return this.id == n.id;
		}
		return false;
	}
	@Override
	public String toString() {
		return this.point.toString();
	}
	
	public double distance (Node n) {
		long dx = this.point.x - n.point.x;
		long dy = this.point.y - n.point.y;
		return Math.sqrt(dx * dx + dy * dy);
	}
	public double distance (int x, int y, int level) {
		if (level <= this.level) {
			long dx = this.point.x - x;
			long dy = this.point.y - y;
			return Math.sqrt(dx * dx + dy * dy);
		} else {
			return Double.POSITIVE_INFINITY;
		}
	}
}
