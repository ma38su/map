package map;

import java.io.Serializable;


/**
 * 数値地図での曲線
 * @author ma38su
 */
public class Curve implements Serializable {

	/**
	 * X座標の配列
	 */
	protected int[] x;

	/**
	 * Y座標の配列
	 */
	protected int[] y;

	/**
	 * 鉄道種別
	 * type = 43 - 43 普通鉄道(JR)
	 *        44 - 43 普通鉄道
	 *        45 - 43 路面電車
	 *        46 - 43 地下式鉄道
	 *        47 - 43 その他
	 * 道路種別
	 * type = 16 - 13 高速道路
	 *        15 - 13 一般道
	 *        14 - 13 石段
	 *        13 - 13 庭園
	 */
	private final int type;

	/**
	 * 曲線
	 * @param curveX
	 * @param curveY
	 * @param type
	 */
	public Curve(int[] curveX, int[] curveY, int type) {
		this.x = curveX;
		this.y = curveY;
		this.type = type;
	}
	
	/**
	 * X座標の配列を取得
	 * @return X座標配列
	 */
	public int[] getArrayX() {
		return this.x;
	}
	/**
	 * Y座標の配列を取得
	 * @return Y座標配列
	 */
	public int[] getArrayY() {
		return this.y;
	}
	/**
	 * 鉄道種別
	 * type = 43 - 43 普通鉄道(JR)
	 *        44 - 43 普通鉄道
	 *        45 - 43 路面電車
	 *        46 - 43 地下式鉄道
	 *        47 - 43 その他
	 * 道路種別
	 * type = 16 高速道路
	 *        15 一般道
	 *        14 石段
	 *        13 庭園
	 * @return 各種別
	 */
	public int getType() {
		return this.type;
	}
	public static final int RAILWAY_JR = 0;
	
	/**
	 * 駅の接続のためのメソッド
	 * @param curve
	 * @return 接続に成功すればtrue
	 */
	public boolean connect(Curve curve) {
		if (curve.x[0] == this.x[0] && curve.y[0] == this.y[0]) {
			int[] newX = new int[curve.x.length + this.x.length];
			int[] newY = new int[curve.y.length + this.y.length];
			for (int i = 0; i < this.x.length; i++) {
				newX[i] = this.x[this.x.length - i - 1];
				newY[i] = this.y[this.x.length - i - 1];
			}
			for (int i = 0; i < curve.x.length; i++) {
				newX[i + this.x.length] = curve.x[i];
				newY[i + this.x.length] = curve.y[i];
			}
			this.x = newX;
			this.y = newY;
			return true;
		} else if (curve.x[0] == this.x[this.x.length - 1] && curve.y[0] == this.y[this.x.length - 1]) {
			int[] newX = new int[curve.x.length + this.x.length];
			int[] newY = new int[curve.y.length + this.y.length];
			for (int i = 0; i < this.x.length; i++) {
				newX[i] = this.x[i];
				newY[i] = this.y[i];
			}
			for (int i = 0; i < curve.x.length; i++) {
				newX[i + this.x.length] = curve.x[i];
				newY[i + this.x.length] = curve.y[i];
			}
			this.x = newX;
			this.y = newY;
			return true;
		} else if (curve.x[curve.x.length - 1] == this.x[this.x.length - 1] && curve.y[curve.y.length - 1] == this.y[this.x.length - 1]) {
			int[] newX = new int[curve.x.length + this.x.length];
			int[] newY = new int[curve.y.length + this.y.length];
			for (int i = 0; i < this.x.length; i++) {
				newX[i] = this.x[i];
				newY[i] = this.y[i];
			}
			for (int i = 0; i < curve.x.length; i++) {
				newX[i + this.x.length] = curve.x[curve.x.length - i - 1];
				newY[i + this.x.length] = curve.y[curve.x.length - i - 1];
			}
			this.x = newX;
			this.y = newY;
			return true;
		} else if (curve.x[curve.x.length - 1] == this.x[0] && curve.y[curve.y.length - 1] == this.y[0]) {
			int[] newX = new int[curve.x.length + this.x.length];
			int[] newY = new int[curve.y.length + this.y.length];
			for (int i = 0; i < this.x.length; i++) {
				newX[i] = this.x[this.x.length - i - 1];
				newY[i] = this.y[this.x.length - i - 1];
			}
			for (int i = 0; i < curve.x.length; i++) {
				newX[i + this.x.length] = curve.x[curve.x.length - i - 1];
				newY[i + this.x.length] = curve.y[curve.x.length - i - 1];
			}
			this.x = newX;
			this.y = newY;
			return true;
		}
		return false;
	}
}
