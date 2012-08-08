package map.sdf2500;

import map.Curve;
import map.Road;

/**
 * 数値地図2500のアークデータ
 * @author Masayasu Fujiwara
 */
public class Arc implements HasAttribute {

	public static ExtendedPolygon transformPolygon(int code, Arc[] arcs) {
		int length = 0;
		for (Arc arc : arcs) {
			length += arc.x.length;
		}
		int[] x = new int[length];
		int[] y = new int[length];
		int count = 0;
		for (Arc arc : arcs) {
			for (int i = 0; i < arc.x.length; i++) {
				x[count] = (int)(arc.x[i] * 3600000 + 0.5);
				y[count] = (int)(arc.y[i] * 3600000 + 0.5);
				count++;
			}
		}
		return new ExtendedPolygon(x, y, length, code);
	}

	public double[] getArrayX() {
		return this.x;
	}
	public double[] getArrayY() {
		return this.y;
	}
	
	public int getTag() {
		return this.tag;
	}

	/**
	 * 図式分類コード
	 */
	private int code;

	/**
	 * 線種タグ
	 */
	private int tag;

	private double[] x;
	
	private double[] y;
	
	private String[] attribute;
	
	public Arc(double[] x, double[] y, int code, int tag) {
		this.x = x;
		this.y = y;
		this.code = code;
		this.tag = tag;
	}
	
	public String[] getAttribute() {
		return this.attribute;
	}

	/**
	 * @return 図式分類コード
	 */
	public int getCode() {
		return this.code;
	}
	
	public void setAttribute(String[] obj) {
		this.attribute = obj;
	}
	/**
	 * 反対方向のアークのを新たに作成します。
	 * @return 反対方向のアーク
	 */
	public Arc reverse() {
		int length = this.x.length;
		double[] reverseX = new double[length];
		double[] reverseY = new double[length];
		for (int i = 0; i < length; i++) {
			reverseX[i] = this.x[length - i - 1];
			reverseY[i] = this.y[length - i - 1];
		}
		return new Arc(reverseX, reverseY, this.code, this.tag);
	}
	
	@Override
	public String toString() {
		int length = this.x.length;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(this.x[i]);
			sb.append(", ");
			sb.append(this.y[i]);
			sb.append('\n');
		}
		return sb.toString();
	}

	public Road transformRoad() {
		int length = this.x.length;
		int[] aryX = new int[length];
		int[] aryY = new int[length];
		double cost = 0;
		int type = 0;
		int x0 = 0;
		int y0 = 0;
		for (int i = 0; i < length; i++) {
			aryX[i] = (int)(this.x[i] * 3600000 + 0.5);
			aryY[i] = (int)(this.y[i] * 3600000 + 0.5);
			if (i == 0) {
				x0 = aryX[0];
				x0 = aryY[0];
			} else {
				double dx = 6378137 * (double) (x0 - aryX[i]) / 3600000 / 180 * Math.PI * Math.cos((double) ((long) y0 + (long) aryY[i]) / 2 / 3600000 * Math.PI / 180);
				double dy = 6378137 * (double) (y0 - aryY[i]) / 3600000 / 180 * Math.PI;
				cost += Math.sqrt(dx * dx + dy * dy);
				x0 = aryX[i];
				x0 = aryY[i];
			}
		}
		int width = 1;
		String name = null;
		if (this.attribute != null) {
			name = this.attribute[0];
			if (name != null) {
				if (name.startsWith("主要地方道")) {
					type = 3;
					width = 2;
				} else if (name.startsWith("県道")) {
					type = 2;
					width = 2;
				} else if (name.startsWith("国道")) {
					type = 3;
					width = 3;
				} else if (name.endsWith("有料道路")) {
					type = 4;
					width = 3;
				} else if (name.contains("高速")) {
					type = 4;
					width = 4;
				} else {
					type = 2;
				}
			}
		}
		return new Road(name, aryX, aryY, type, width, (float) cost);
	}
	public Railway transformRailway() {
		int length = this.x.length;
		int[] aryX = new int[length];
		int[] aryY = new int[length];
		for (int i = 0; i < length; i++) {
			aryX[i] = (int)(this.x[i] * 3600000 + 0.5);
			aryY[i] = (int)(this.y[i] * 3600000 + 0.5);
		}
		int type = 1;
		String name = null;
		if (this.attribute != null) {
			name = this.attribute[0];
			if (name.startsWith("ＪＲ")) {
				type = Curve.RAILWAY_JR;
			} else {
				type = 1;
			}
		}
		return new Railway(name, aryX, aryY, type);
	}

	public Curve transformCurve() {
		int length = this.x.length;
		int[] aryX = new int[length];
		int[] aryY = new int[length];
		for (int i = 0; i < length; i++) {
			aryX[i] = (int)(this.x[i] * 3600000 + 0.5);
			aryY[i] = (int)(this.y[i] * 3600000 + 0.5);
		}
		return new Curve(aryX, aryY, 0);
	}
}
