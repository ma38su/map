package map.route;
import map.sdf25k.Node;

/**
 * 2頂点間のユークリッド距離を求めるためのクラスです。
 * ヒューリスティックな距離関数の条件を満たすために、
 * 少し短めに見積もっています。
 * 経路探索のためのヒープで利用します。
 * @author ma38su
 */
public class DirectDistance implements HeuristicDistanceFunction {
	/**
	 * Heuristic 距離を求める
	 * @param n1
	 * @param n2
	 * @return Heuristic 距離
	 */
	public double get(Node n1, Node n2) {
		double dx = 6378137 * (double) (n1.getX() - n2.getX()) / 3600000 / 180 * Math.PI * Math.cos((double) ((long) n1.getY() + (long) n2.getY()) / 2 / 3600000 * Math.PI / 180);
		double dy = 6378137 * (double) (n1.getY() - n2.getY()) / 3600000 / 180 * Math.PI;
		return Math.sqrt(dx * dx + dy * dy);
	}
	@Override
	public String toString() {
		return "直線距離";
	}
}
