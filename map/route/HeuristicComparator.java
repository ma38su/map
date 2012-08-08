package map.route;

import java.util.Comparator;

import map.sdf25k.Node;

/**
 * ヒューリスティック関数を用いてコレクションの「全体順序付け」を行う比較関数です。
 *
 * @author ma38su
 *
 */
public class HeuristicComparator implements Comparator<Heap.Entry> {
	private HeuristicDistanceFunction heuristic;
	private Node terminal;
	public HeuristicComparator(HeuristicDistanceFunction heuristic, Node terminal) {
		this.heuristic = heuristic;
		this.terminal = terminal;
	}
	public int compare(Heap.Entry o1, Heap.Entry o2) {
		double d1 = o1.getValue() + this.heuristic.get(o1.getKey(), this.terminal);
		double d2 = o2.getValue() + this.heuristic.get(o2.getKey(), this.terminal);
		if(d1 < d2) {
			return -1;
		} else if(d1 > d2) {
			return 1;
		} else {
			return 0;
		}
	}
}
