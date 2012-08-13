package util.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

import javax.swing.JComponent;

/**
 * エクスポート可能なパネルクラス
 * @author ma38su
 */
public abstract class ExportableComponent extends JComponent implements Printable {
	
	/**
	 * Printable インターフェースの実装
	 * by Kumano
	 * 
	 * @param graphics
	 * @param pageFormat
	 * @param pageIndex
	 * @return 状態
	 */
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
		if (pageIndex == 0) {
			final Graphics2D g = (Graphics2D) graphics;
			g.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
			this.paintTranslate(g, pageFormat.getImageableWidth(), pageFormat.getImageableHeight());
			return Printable.PAGE_EXISTS;
		} else {
			return Printable.NO_SUCH_PAGE;
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		Image offs = this.createImage(this.getWidth(), this.getHeight());
		Graphics2D offg = (Graphics2D) offs.getGraphics();
		super.paintComponent(offg);
		this.draw(offg);
		g.drawImage(offs, 0, 0, null);
	}

	/**
	 * 変換して描画します。
	 * @param g 
	 * @param width 
	 * @param height 
	 */
	public void paintTranslate(Graphics2D g, double width, double height) {
		final double newScale = Math.min(width / this.getWidth(), height / this.getHeight());
		g.scale(newScale, newScale);
		g.setClip(0, 0, this.getWidth(), this.getHeight());
		this.draw(g);
	}
	
	/**
	 * 描画を行うメソッド
	 * @param g 
	 */
	public abstract void draw(Graphics2D g);
}
