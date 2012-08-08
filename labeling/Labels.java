package labeling;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 同じ文字列で複数配置するラベル
 * コンビニなどのチェーン店の配置のためのラベル
 * @author ma38su
 */
public class Labels {

	/**
	 * ラベルの文字列
	 */
	private final String label;

	/**
	 * 座標
	 */
	private final Collection<Point> point;

	public Labels(String label) {
		this.label = label;
		this.point = new ArrayList<Point>();
	}

	/**
	 * ラベルの座標を追加する
	 * @param points 座標Collection
	 */
	public void add(Collection<Point> points) {
		this.point.addAll(points);
	}
	
	/**
	 * 
	 * @return ラベル
	 */
	public String getName() {
		return this.label;
	}

	/**
	 * 
	 * @return 座標配列
	 */
	public Collection<Point> getLocation() {
		return this.point;
	}
}
