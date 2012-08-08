package map.sdf2500;

import java.awt.Point;

import labeling.Label;

public class ExtendedPoint extends Point implements HasAttribute, Label {
	private String name;
	public ExtendedPoint(int x, int y) {
		super(x, y);
	}
	public String getName() {
		return this.name;
	}
	public void setAttribute(String[] attribute) {
		if (attribute != null && attribute.length > 0) {
			this.name = attribute[0];
		}
	}
	public int getLat() {
		return super.y;
	}

	public int getLng() {
		return super.x;
	}
}
