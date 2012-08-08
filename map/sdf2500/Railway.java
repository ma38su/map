package map.sdf2500;

import map.Curve;

public class Railway extends Curve {
	private String name;
	public Railway(String name, int[] curveX, int[] curveY, int type) {
		super(curveX, curveY, type);
		this.name = name;
	}
	public String getName() {
		return this.name;
	}
}
