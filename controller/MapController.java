package controller;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import view.MapPanel;

/**
 * マウス、キーボードによる操作クラス
 * @author ma38su
 */
public class MapController implements MouseListener, MouseMotionListener, MouseWheelListener {

	/**
	 * マウスがWindow内に入っているかどうか
	 */
	private boolean isMouseEntered;

	/**
	 * 地図表示パネル
	 */
	private final MapPanel panel;

	/**
	 * 操作の中心位置（マウス位置）のX座標
	 */
	private int x;

	/**
	 * 操作の中心位置（マウス位置）のY座標
	 */
	private int y;
	
	/**
	 * @param panel 地図表示のためのパネル
	 * @param setting 設定
	 */
	public MapController(final MapPanel panel) {
		this.panel = panel;
		this.x = panel.getWidth() / 2;
		this.y = panel.getHeight() / 2;
	}

	public float getLocationMouseX() {
		return (float) this.panel.getLocationX(this.x);
	}

	public float getLocationMouseY() {
		return (float) this.panel.getLocationY(this.y);
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		// ポインター座標変化量を計算
		int dx = e.getX() - this.x;
		int dy = e.getY() - this.y;
		// 変化時のポインター座標
		this.x = e.getX();
		this.y = e.getY();
		// 平行移動
		this.panel.setOperation(true);
		this.panel.moveLocation(dx, dy);
		this.panel.repaint();
	}

	public void mouseEntered(MouseEvent e) {
		this.isMouseEntered = true;
	}

	public void mouseExited(MouseEvent e) {
		this.isMouseEntered = false;
		if (!this.panel.isOperation()) {
			this.x = this.panel.getWidth() / 2;
			this.y = this.panel.getHeight() / 2;
		}
	}

	public void mouseMoved(MouseEvent e) {
		if (this.isMouseEntered) {
			this.x = e.getX();
			this.y = e.getY();
		}
	}

	public void mousePressed(MouseEvent e) {
		this.x = e.getX();
		this.y = e.getY();
		this.panel.setOperation(true);
	}

	public void mouseReleased(MouseEvent e) {
		if (!this.isMouseEntered
				&& (e.getModifiersEx() & (InputEvent.BUTTON1_DOWN_MASK
						| InputEvent.BUTTON2_DOWN_MASK | InputEvent.BUTTON3_DOWN_MASK)) == 0) {
			this.x = this.panel.getWidth() / 2;
			this.y = this.panel.getHeight() / 2;
		}
		this.panel.setOperation(false);
		this.panel.repaint();
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		int d = e.getWheelRotation();
		this.panel.zoom(e.getX(), e.getY(), d);
		this.panel.repaint();
	}

}
