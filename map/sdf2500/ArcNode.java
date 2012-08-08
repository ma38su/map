package map.sdf2500;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 曲線連結のための接点
 * JR路線や駅を連結するためのクラス
 * @author ma38su
 */
class ArcNode extends Point2D.Double {

	private Map<ArcNode, Arc> border;

	ArcNode(double x, double y) {
		super(x, y);
		this.border = new HashMap<ArcNode, Arc>();
	}	

	public Arc connect(ArcNode node, Arc border) {
		int index = border.getCode();
		Arc border0 = this.border.get(node);
		double[] aryX0 = border0.getArrayX();
		double[] aryY0 = border0.getArrayY();
		double[] aryX1 = border.getArrayX();
		double[] aryY1 = border.getArrayY();
		int length = aryX0.length + aryX1.length;
		double[] x = new double[length];
		double[] y = new double[length];

		if (super.x == aryX0[0] && super.y == aryY0[0]) {
			for (int i = 0; i < aryX0.length; i++) {
				x[i] = aryX0[aryX0.length - i - 1];
				y[i] = aryY0[aryX0.length - i - 1];
			}
		} else if(super.x == aryX0[aryX0.length - 1] && super.y == aryY0[aryX0.length - 1]){
			for (int i = 0; i < aryX0.length; i++) {
				x[i] = aryX0[i];
				y[i] = aryY0[i];
			}
		} else {
			throw new IllegalArgumentException("DEBUG error : "+ border.getCode());
		}

		if (super.x == aryX1[0] && super.y == aryY1[0]) {
			for (int i = 0; i < aryX1.length; i++) {
				x[i + aryX0.length] = aryX1[i];
				y[i + aryX0.length] = aryY1[i];
			}
		} else if(super.x == aryX1[aryX1.length - 1] && super.y == aryY1[aryX1.length - 1]){
			for (int i = 0; i < aryX1.length; i++) {
				x[i + aryX0.length] = aryX1[aryX1.length - i - 1];
				y[i + aryX0.length] = aryY1[aryX1.length - i - 1];
			}
		} else {
			throw new IllegalArgumentException("DEBUG error : "+ border.getCode());
		}

		this.border.remove(node);
		node.border.remove(this);
		Arc arc = new Arc(x, y, index, border.getTag());
		Set<String> attribute = new HashSet<String>();
		for (String str : border.getAttribute()) {
			attribute.add(str);
		}
		for (String str : border0.getAttribute()) {
			attribute.add(str);
		}
		arc.setAttribute(attribute.toArray(new String[]{}));
		return arc;
	}
	public Arc connect(int k) {
		if (this.border.size() != 2) {
			return null;
		}
		int j = 0;
		ArcNode[] node = new ArcNode[2];
		Arc[] borders = new Arc[2];
		for (Map.Entry<ArcNode, Arc> entry : this.border.entrySet()) {
			node[j] = entry.getKey();
			borders[j++] = entry.getValue();
		}
		double[] aryX0 = borders[0].getArrayX();
		double[] aryY0 = borders[0].getArrayY();
		double[] aryX1 = borders[1].getArrayX();
		double[] aryY1 = borders[1].getArrayY();
		int length = aryX0.length + aryX1.length;
		double[] x = new double[length];
		double[] y = new double[length];

		if (super.x == aryX0[0] && super.y == aryY0[0]) {
			for (int i = 0; i < aryX0.length; i++) {
				x[i] = aryX0[aryX0.length - i - 1];
				y[i] = aryY0[aryX0.length - i - 1];
			}
		} else if(super.x == aryX0[aryX0.length - 1] && super.y == aryY0[aryX0.length - 1]){
			for (int i = 0; i < aryX0.length; i++) {
				x[i] = aryX0[i];
				y[i] = aryY0[i];
			}
		} else {
			throw new IllegalArgumentException("DEBUG error : "+ k);
		}

		if (super.x == aryX1[0] && super.y == aryY1[0]) {
			for (int i = 0; i < aryX1.length; i++) {
				x[i + aryX0.length] = aryX1[i];
				y[i + aryX0.length] = aryY1[i];
			}
		} else if(super.x == aryX1[aryX1.length - 1] && super.y == aryY1[aryX1.length - 1]){
			for (int i = 0; i < aryX1.length; i++) {
				x[i + aryX0.length] = aryX1[aryX1.length - i - 1];
				y[i + aryX0.length] = aryY1[aryX1.length - i - 1];
			}
		} else {
			throw new IllegalArgumentException("DEBUG error : "+ k);
		}

		node[0].border.remove(this);
		node[1].border.remove(this);
		this.border.clear();

		Arc arc = new Arc(x, y, k, borders[0].getTag());
		Set<String> attributeSet = new HashSet<String>();
		String[] attribute = borders[0].getAttribute();
		if (attribute != null) {
			for (String str : attribute) {
				attributeSet.add(str);
			}
		}
		attribute = borders[1].getAttribute();
		if (attribute != null) {
			for (String str : attribute) {
				attributeSet.add(str);
			}
		}
		arc.setAttribute(attributeSet.toArray(new String[]{}));
		
		return node[0].put(node[1], arc);
	}
	public Collection<Arc> getBorder() {
		return this.border.values();
	}
	public Arc put(ArcNode node, Arc border) {
		int flag = 0;
		if(this.border.containsKey(node)) {
			flag++;
		}
		if(node.border.containsKey(this)) {
			flag++;
		}
		if(flag != 0) {
			return this.connect(node, border);
		}
		this.border.put(node, border);
		node.border.put(this, border);
		return null;
	}
}
