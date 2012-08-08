package labeling;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

/**
 * 配置済みラベル
 * @author ma38su
 */
public class FixedLabel {

	/**
	 * ラベルの文字列
	 */
	private final String name;

	/**
	 * ラベルのX座標
	 */
	private final int x;

	/**
	 * ラベルのY座標
	 */
	private final int y;
	
	/**
	 * コンストラクタ
	 * @param name 文字列
	 * @param x X座標
	 * @param y Y座標
	 */
	public FixedLabel(String name, int x, int y) {
		this.name = name;
		this.x = x;
		this.y = y;
	}
	
	/**
	 * 白抜き文字を描画します。
	 * @param g
	 * @param border 
	 * @param fill 
	 * @param isShape 
	 */
	void draw(Graphics2D g, Color fill, Color border, boolean isShape) {
		Font font = g.getFont();
		FontRenderContext render = g.getFontRenderContext();
		GlyphVector glyph = font.createGlyphVector(render, this.name);
		g.setColor(border);
		// TODO
		g.setStroke(new BasicStroke(4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		Shape shape = glyph.getOutline(this.x, this.y);
		g.draw(shape);
		g.setColor(fill);

		BasicStroke stroke = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		if (isShape) {
			g.fill(shape);
			g.setStroke(stroke);
		} else {
			g.setStroke(stroke);
			g.drawString(this.name, this.x, this.y);
		}
	}

	/**
	 * ラベルを描画します。
	 * @param g
	 * @param border 
	 * @param fill 
	 */
	public void draw(Graphics2D g) {
		g.drawString(this.name, this.x, this.y);
	}

	/**
	 * ラベルを描画します。
	 * @param g
	 * @param color 
	 * @param shadow 
	 */
	public void drawSimple(Graphics2D g, Color color, Color shadow) {
		g.setColor(shadow);
		g.drawString(this.name, this.x + 1, this.y + 1);
		g.drawString(this.name, this.x + 1, this.y);
		g.drawString(this.name, this.x, this.y + 1);
		g.setColor(color);
		g.drawString(this.name, this.x, this.y);
	}

	
	/**
	 * ラベルの文字列を取得します。
	 * @return ラベルの文字列
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * X座標を取得します。
	 * @return X座標
	 */
	public int getX() {
		return this.x;
	}
	
	/**
	 * Y座標を取得します。
	 * @return Y座標
	 */
	public int getY() {
		return this.y;
	}
}
