package map.sdf2500;

import java.awt.Polygon;
import java.awt.Rectangle;

import labeling.Label;

public class ExtendedPolygon extends Polygon implements HasAttribute, Label {
	private String name;
	private int code;
	public ExtendedPolygon(int[] x, int y[], int n, int code) {
		super(x, y, n);
		this.code = code;
	}
	public int getCode() {
		return this.code;
	}

	public void setAttribute(String[] attribute) {
		if (attribute != null && attribute.length > 0) {
			if (attribute.length > 1) {
				this.name = attribute[1];
			} else {
				this.name = attribute[0];
			}
		}
	}

	public String getName() {
		return this.name;
	}
	public int getLat() {
		Rectangle rect = this.getBounds();
		return rect.y + rect.height / 2;
	}
	public int getLng() {
		Rectangle rect = this.getBounds();
		return rect.x + rect.width / 2;
	}
}
