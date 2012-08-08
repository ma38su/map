package map.sdf25k;

import labeling.Label;
import map.Curve;

/**
 * 数値地図25000の駅
 * @author ma38su
 */
public class Station extends Curve implements Label {

	/**
	 * 駅名
	 */
	private String name;

	/**
	 * 駅クラスを生成します
	 * @param name 駅名
	 * @param curve 駅の曲線データ
	 */
	public Station(String name, Curve curve) {
		super(curve.getArrayX(), curve.getArrayY(), 0);
		this.name = name;
	}
	public String getName() {
		return this.name;
	}
	public int getLng() {
		return (super.x[0] + super.x[super.x.length - 1]) / 2;
	}
	public int getLat() {
		return (super.y[0] + super.y[super.y.length - 1]) / 2;		
	}
}
