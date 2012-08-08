package map.sdf25k;

import java.io.Serializable;

import labeling.Label;

/**
 * 施設のラベル
 * @author ma38su
 */
public class Facility implements Label, Serializable {
	private String name;
	private static final String POST = "〒";
	private final int x, y;
	public Facility(String name, int x, int y){
		if (name.endsWith("郵便局")) {
			this.name = Facility.POST;
		} else {
			this.name = name;
		}
		this.x = x;
		this.y = y;
	}
	public String getName() {
		return this.name;
	}
	public int getLng() {
		return this.x;
	}
	public int getLat() {
		return this.y;
	}
}
