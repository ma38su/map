package map.route.tsp;

import java.util.List;

import map.sdf25k.Node;

/**
 * 改善法を順序を指定して適用するためのクラス
 * 改善法が適用できなくなれば、改めて最初の改善法から適用していきます。
 * @author ma38su
 */
public class ImproveRoutine implements TspImprovement {
	TspImprovement[] algorithm;
	
	public ImproveRoutine(TspImprovement... algorithm) {
		this.algorithm = algorithm;
	}

	public boolean method(List<Node> route, DistanceTable table) {
		boolean flag = false;
		boolean ret = false;
		for (int i = 0; i < this.algorithm.length; i++) {
			while (this.algorithm[i].method(route, table)) {
				flag = true;
				ret = true;
			}
			if (flag) {
				i = 0;
				flag = false;
			}
		}
		return ret;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		for (TspImprovement tsp : this.algorithm) {
			if (isFirst) {
				isFirst = false;
			} else {
				sb.append(" -> ");
			}
			sb.append(tsp);
		}
		return sb.toString();
	}
}
