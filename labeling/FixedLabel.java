package labeling;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

/**
 * 配置済みラベル
 * @author ma38su
 */
public class FixedLabel {

	private static final Stroke SHADOW_STROKE = new BasicStroke(4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
	
	private static final Stroke BASIC_STROKE = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

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
	 * 影をつけてラベルを描画
	 * @param g
	 * @param fill フォントの色
	 * @param border 影の色
	 */
	void draw(Graphics2D g, Color fill, Color border) {
		Font font = g.getFont();
		FontRenderContext render = g.getFontRenderContext();
		GlyphVector glyph = font.createGlyphVector(render, this.name);
		g.setColor(border);

		g.setStroke(SHADOW_STROKE);
		Shape shape = glyph.getOutline(this.x, this.y);
		g.draw(shape);
		g.setColor(fill);

		g.setStroke(BASIC_STROKE);
		g.drawString(this.name, this.x, this.y);
	}

	/**
	 * ラベルを描画
	 * @param g
	 * @param border 
	 * @param fill 
	 */
	public void draw(Graphics2D g) {
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
