package labeling;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * ラベル配置のためのサイト
 * @author ma38su
 */
public class Site {
	private String name;
	private final int x, y;
	private List<LabelCandidate> candidates;
	private LabelCandidate label;
	public Site(String name, int x, int y){
		this.name = name;
		this.x = x;
		this.y = y;
	}
	public int conflictSize() {
		return this.candidates.size();
	}
	public String getName() {
		return this.name;
	}
	public int getX() {
		return this.x;
	}
	public int getY() {
		return this.y;
	}
	public void add(List<LabelCandidate> candidates) {
		this.candidates = candidates;
	}
	public Iterator<LabelCandidate> getCandidateIterator() {
		return this.candidates.iterator();
	}
	public Collection<LabelCandidate> getCandidates() {
		return this.candidates;
	}
	public boolean isFixed() {
		return this.label != null;
	}
	public LabelCandidate getLabel() {
		return this.label;
	}
	/**
	 * コンフリクトしていないラベル候補を決定するrule1を実行。
	 * @return 適用できればtrue
	 *
	 */
	public boolean rule1 () {
		LabelCandidate.staticlist.add(this);
		if(this.isFixed()) {
			throw new RuntimeException();
		}
		for (LabelCandidate candidate : this.candidates) {
			if(candidate.conflictOrder() == 0) {
				this.fixed(candidate);
				return true;
			}
		}
		return false;
	}
	/**
	 * このサイトのラベル配置を決定する。
	 * @param candidate 決定したラベル候補
	 */
	public void fixed(LabelCandidate candidate) {
		if(this.isFixed()) {
			throw new RuntimeException();
		}
		this.label = candidate;
		Set<Site> tmp = new HashSet<Site>();
		// このサイトのラベル候補に対して
		for (LabelCandidate c : this.candidates) {
			// 決定したラベル候補以外に対して
			if(!c.equals(candidate)) {
				// コンフリクトしているサイトを更新する。
				c.disconflict(this, tmp);
			}
		}
		this.candidates = null;
		for (Site site : tmp) {
			if(!site.isFixed()) {
				site.rule1();
			}
		}
	}
}
