package map.route;

import map.sdf25k.Node;

/**
 * ヒューリスティックな距離関数
 * @author ma38su
 */
public interface HeuristicDistanceFunction {
	/**
	 * 頂点間のヒューリスティック評価値を求める
	 * @param n1 頂点
	 * @param n2 頂点
	 * @return 頂点間の距離のヒューリスティック評価値
	 */
	public double get(Node n1, Node n2);
}
