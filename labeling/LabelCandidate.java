package labeling;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.Log;


/**
 * ラベル候補
 * @author ma38su
 */
public class LabelCandidate extends Rectangle {
	
	static List<Site> staticlist = new ArrayList<Site>();
	
	private Map<Site, Set<LabelCandidate>> conflict;
	
	public LabelCandidate (int x, int y, int width, int height) {
		super(x, y, width, height);
		this.conflict = new HashMap<Site, Set<LabelCandidate>>();
	}
	/**
	 * コンフリクトを記録
	 * @param site0 
	 * @param candidate
	 * @param site 
	 */
	public void conflict(Site site0, LabelCandidate candidate, Site site) {
		if(!this.conflict.containsKey(site)) {
			this.conflict.put(site, new HashSet<LabelCandidate>());
		}
		if(!candidate.conflict.containsKey(site0)) {
			candidate.conflict.put(site0, new HashSet<LabelCandidate>());
		}
		this.conflict.get(site).add(candidate);
		candidate.conflict.get(site0).add(this);
	}
	/**
	 * このラベルに対してのコンフリクトを解除する。
	 * @param site 
	 * @param set 
	 */
	public void disconflict(Site site, Set<Site> set) {
		Iterator<Map.Entry<Site, Set<LabelCandidate>>> itr = this.conflict.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<Site, Set<LabelCandidate>> entry = itr.next();
			Site key = entry.getKey();
			if(!key.equals(site)) {
				// なぜラベル候補が決まったものとコンフリクトしたのか？
				if(!key.isFixed()) {
					for (LabelCandidate candidate : key.getCandidates()) {
						candidate.conflict.remove(site);
					}
					for (LabelCandidate candidate : entry.getValue()) {
						candidate.conflict.remove(site);
					}
					set.add(key);
				} else {
					itr.remove();
					for (Site c : LabelCandidate.staticlist) {
						Log.out(this, c.getName());
					}
					throw new RuntimeException();
				}
			}
		}
	}
	/**
	 * コンフリクトの次数を返す
	 * @return コンフリクトの次数 
	 */
	public int conflictOrder() {
		return this.conflict.size();
	}
	/**
	 * ルール2のコンフリクトを探す
	 * @param site0 
	 * 
	 */
	public void checkRule2(Site site0) {
		for (Map.Entry<Site, Set<LabelCandidate>> entry : this.conflict.entrySet()) {
			Site site = entry.getKey();
			for (LabelCandidate c0 : site.getCandidates()) {
				Set<LabelCandidate> set = c0.conflict.get(site0);				
				for(LabelCandidate c1 : set) {
					if(c1.conflictOrder() == 1) {
						site0.fixed(this);
					}
				}
			}
		}
	}
}
