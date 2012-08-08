package map.route.tsp;

import java.util.List;

import map.sdf25k.Node;

/**
 * 2-Optによる巡回セールスマン問題の改善法です。
 * @author ma38su
 */
public class Opt2 implements TspImprovement {
	public boolean method(List<Node> route, DistanceTable table) {
		int length = route.size();
		for (int i = 1; i <= length; i++) {
			Node s1 = route.get(i - 1);
			Node t1 = route.get(i % length);
			double d1 = table.getCost(s1, t1);
			for (int j = i + 2; j < i + length - 2; j++) {
				Node s2 = route.get((j - 1) % length);
				Node t2 = route.get(j % length);
				double before = d1 + table.getCost(s2, t2);
				double after = table.getCost(s1, s2) + table.getCost(t1, t2);
				if (before > after) {
					for (int k = 0; k < (j - i) / 2; k++) {
						Node tmp = route.get((k + i) % length);
						route.set((k + i) % length, route.get((j - k - 1) % length));
						route.set((j - k - 1) % length, tmp);
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "2-Opt";
	}
}
