package map.route.tsp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import map.sdf25k.Node;

/**
 * nearest neighborによる巡回セールスマン問題の構築法の実装
 * @author ma38su
 */
public class NearestNeighbor {
	public List<Node> method(Collection<Node> place, DistanceTable table) {
		Set<Node> nodes = new HashSet<Node>(place);
		List<Node> route = new ArrayList<Node>(nodes.size() + 1);
		Set<Node> vp = new HashSet<Node>(nodes.size());
		Iterator<Node> itr = nodes.iterator();
		if (itr.hasNext()) {
			Node node = itr.next();
			while (!nodes.isEmpty()) {
				route.add(node);
				vp.add(node);
				if (nodes.remove(node) && nodes.isEmpty()) {
					break;
				}
				Node tmp = null;
				double min = Double.POSITIVE_INFINITY;
				for (Node terminal : nodes) {
					if (!node.equals(terminal) && !vp.contains(terminal)) {
						double distance = table.getCost(node, terminal);
						if (min > distance) {
							min = distance;
							tmp = terminal;
						}
					}
				}
				if (tmp == null) {
					System.out.println("NULL");
				} else if (tmp.equals(node)) {
					System.out.println("EQUAL");
				} else {
					node = tmp;
				}
			}
		}
		return route;
	}

	@Override
	public String toString() {
		return "Nearest Neighbor";
	}
}
