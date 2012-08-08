package map.sdf25k;

import java.awt.Color;
import java.io.Serializable;

/**
 * メッシュ標高
 * @author ma38su
 */
public class Mesh implements Serializable {

	/**
	 * メッシュの間隔
	 */
	public static final int SIZE = 2000;

	/**
	 * 標高
	 */
	private final int height;

	/**
	 * 色
	 */
	private final Color color;
	
	public Mesh(int height) {
		this.height = height;
		float n = (float) height / 1300;
		float n2 = (float) height / 3500;
		if (n > 1f) {
			n = 1f;
		}
		this.color = Color.getHSBColor(40 / 360f + n2 * 0.39f, 0.04f + n * 0.15f, 0.95f - n * 0.20f);
	}

	/**
	 * 標高を返します。
	 * @return 標高
	 */
	public int getHeight() {
		return this.height;
	}
	
	/**
	 * 描画色を返します。
	 * @return 描画色
	 */
	public Color getColor() {
		return this.color;
	}
	
    @Override
	public boolean equals(Object obj) {
    	return false;
    }

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
