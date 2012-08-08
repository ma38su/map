package map.sdf25k;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import map.Curve;

/**
 * 曲線連結のための接点
 * JR路線や駅を連結するためのクラス
 * @author ma38su
 */
class ExtractNode extends Point {

	private Map<ExtractNode, Curve>[] border;

	@SuppressWarnings("unchecked")
	ExtractNode(int x, int y) {
		super(x, y);
		this.border = new HashMap[4];
		this.border[0] = new HashMap<ExtractNode, Curve>();
		this.border[1] = new HashMap<ExtractNode, Curve>();
		this.border[2] = new HashMap<ExtractNode, Curve>();
		this.border[3] = new HashMap<ExtractNode, Curve>();		
	}	

	public Curve connect(ExtractNode node, Curve border) {
		int index = border.getType();
		Curve border0 = this.border[index].get(node);
		int[] aryX0 = border0.getArrayX();
		int[] aryY0 = border0.getArrayY();
		int[] aryX1 = border.getArrayX();
		int[] aryY1 = border.getArrayY();
		int length = aryX0.length + aryX1.length;
		int[] x = new int[length];
		int[] y = new int[length];

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
			throw new IllegalArgumentException("DEBUG error : "+ border.getType());
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
			throw new IllegalArgumentException("DEBUG error : "+ border.getType());
		}

		this.border[index].remove(node);
		node.border[index].remove(this);
		return new Curve(x, y, index);
	}

	public Curve connect(int k) {
		if(this.border[k].size() != 2) {
			return null;
		}
		int j = 0;
		ExtractNode[] node = new ExtractNode[2];
		Curve[] borders = new Curve[2];
		for (Map.Entry<ExtractNode, Curve> entry : this.border[k].entrySet()) {
			node[j] = entry.getKey();
			borders[j++] = entry.getValue();
		}
		int[] aryX0 = borders[0].getArrayX();
		int[] aryY0 = borders[0].getArrayY();
		int[] aryX1 = borders[1].getArrayX();
		int[] aryY1 = borders[1].getArrayY();
		int length = aryX0.length + aryX1.length;
		int[] x = new int[length];
		int[] y = new int[length];

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

		Curve newborder = new Curve(x, y, k);
		
		node[0].border[k].remove(this);
		node[1].border[k].remove(this);
		this.border[k].clear();
		return node[0].put(node[1], newborder);
	}

	public Collection<Curve> getBorder() {
		List<Curve> list = new ArrayList<Curve>();
		for (Map<ExtractNode, Curve> map : this.border) {
			list.addAll(map.values());
		}
		return list;
	}
	public Curve put(ExtractNode node, Curve border) {
		int flag = 0;
		int index = border.getType();
		if(this.border[index].containsKey(node)) {
			flag++;
		}
		if(node.border[index].containsKey(this)) {
			flag++;
		}
		if(flag != 0) {
			return this.connect(node, border);
		}
		this.border[index].put(node, border);
		node.border[index].put(this, border);
		return null;
	}
}
