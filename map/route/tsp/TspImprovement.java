package map.route.tsp;

import java.util.List;

import map.sdf25k.Node;

/**
 * 巡回セールスマン問題の改善法のインターフェースです。
 * 改善法では既存の巡回路からより小さいコストの巡回路を求めます。
 * @author ma38su
 */
public interface TspImprovement {
	public boolean method(List<Node> route, DistanceTable table);
}
