package map.route.tsp;

import java.util.List;

import map.sdf25k.Node;

/**
 * 3-Optによる巡回セールスマン問題の改善法です。
 * @author ma38su
 */
public class Opt3 implements TspImprovement {
	public boolean method(List<Node> route, DistanceTable table) {
		int length = route.size();
		for (int i = 1; i <= length; i++) {
			Node s1 = route.get(i - 1);
			Node t1 = route.get(i % length);
			double d1 = table.getCost(s1, t1);
			for (int j = i + 2; j < i + length - 4; j++) {
				Node s2 = route.get((j - 1) % length);
				Node t2 = route.get(j % length);
				double d2 = table.getCost(s2, t2);
				for (int k = j + 2; k < i + length - 2; k++) {
					Node s3 = route.get((k - 1) % length);
					Node t3 = route.get(k % length);
					double before = d1 + d2 + table.getCost(s3, t3);
					double after = table.getCost(s1, t2) + table.getCost(s3, t1) + table.getCost(s2, t3);
					if (before > after) {
						// リストの回転を行います。
						this.reverse(route, i, j - 1);
						this.reverse(route, j, k - 1);
						this.reverse(route, i, k - 1);
						return true;
					}
					after = table.getCost(s1, t2) + table.getCost(s3, s2) + table.getCost(t1, t3);
					if (before > after) {
						this.reverse(route, j, k - 1);
						this.reverse(route, i, k - 1);
						return true;
					}
					after = table.getCost(s1, s3) + table.getCost(t2, t1) + table.getCost(s2, t3);
					if (before > after) {
						this.reverse(route, i, j - 1);
						this.reverse(route, i, k - 1);
						return true;
					}
					after = table.getCost(s1, s2) + table.getCost(t1, s3) + table.getCost(t2, t3);
					if (before > after) {
						this.reverse(route, i, j - 1);
						this.reverse(route, j, k - 1);
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 指定したインデックス間の要素を逆順に並べ替えます。
	 * @param route リスト 
	 * @param s 並べ替える要素の最小のインデックス
	 * @param t 並べ替える要素の最大のインデックス
	 */
	public void reverse(List<Node> route, int s, int t) {
		int length = route.size();
		for (int i = (t - s) / 2; i >= 0; i--) {
			Node tmp = route.get((s + i) % length);
			route.set((s + i) % length, route.get((t - i) % length));
			route.set((t - i) % length, tmp);
		}
	}
	
	@Override
	public String toString() {
		return "3-Opt";
	}
}
