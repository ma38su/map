package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.print.PrintException;
import javax.swing.AbstractButton;
import javax.swing.JCheckBoxMenuItem;

import jp.sourceforge.ma38su.gui.Output;
import jp.sourceforge.ma38su.util.Log;

import util.Setting;
import util.Version;
import view.DialogFactory;
import view.MapPanel;

/**
 * マウス、キーボードによる操作クラス
 * @author ma38su
 */
public class MapController implements MouseListener, MouseMotionListener, MouseWheelListener, ActionListener {

	/**
	 * キーボードによる平行移動感度
	 */
	private static final int MOVE_SENSE = 8;

	/**
	 * マウスがWindow内に入っているかどうか
	 */
	private boolean isMouseEntered;

	/**
	 * 地図表示パネル
	 */
	private final MapPanel panel;

	/**
	 * 設定
	 */
	private Setting setting;

	private JCheckBoxMenuItem updateCheckItem;

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
	public MapController(final MapPanel panel, Setting setting) {
		this.panel = panel;
		this.setting = setting;
		this.x = panel.getWidth() / 2;
		this.y = panel.getHeight() / 2;
	}

	public void setStyle(String file) throws IllegalArgumentException {
		this.panel.readStyle(file);
		this.panel.repaint();
	}
	public void setDefaultStyle() {
		this.panel.setDefaultStyle();
		this.panel.repaint();
	}
	
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals("exit")) {
			// 即終了しても大丈夫だろうか・・・
			System.exit(0);
		} else if (command.equals("menu_help")) {
			this.updateCheckItem.setVisible(true);
		} else if (command.equals("about")) {
			DialogFactory.aboutDialog();
		} else if (command.startsWith("export")) {
			try {
				while (!this.panel.isLoaded()) {
						Thread.sleep(2L);
				}
				if (command.endsWith("PNG")) {
					Output.exportPng(this.panel);
				}
			} catch (InterruptedException ex) {
				Log.err(this, ex);
			}
		} else if (command.equals("print")) {
			try {
				while (!this.panel.isLoaded()) {
					Thread.sleep(100L);
				}
				Output.print(this.panel);
			} catch (InterruptedException ex) {
				Log.err(this, ex);
			} catch (PrintException ex) {
				Log.err(this, ex);
			}
		} else if (command.startsWith("navi_")) {
			if (command.endsWith("clear")) {
				this.panel.clearNavigation();
				this.panel.repaint();
			}
		} else if (command.startsWith("path_")) {
			this.panel.switchShortestPathAlgorithm(command);
		} else if (command.startsWith("label")) {
			if (command.endsWith("govt")) {
				this.panel.switchLabel(MapPanel.lABEL_PLACE_GOVT);
			} else if (command.endsWith("Station")) {
				this.panel.switchLabel(MapPanel.LABEL_STATION);
			} else if (command.endsWith("City")) {
				this.panel.switchLabel(MapPanel.LABEL_PLACE);
			} else if (command.endsWith("Facility")) {
				this.panel.switchLabel(MapPanel.LABEL_FACILITY);
			} else if (command.endsWith("label_antialiasing")) {
				this.panel.switchTextAntialiasing();
				this.panel.repaint();
			} else if (command.endsWith("shadow")) {
				this.panel.switchShadow();
				this.panel.repaint();
			} else if (command.endsWith("failure")) {
				this.panel.switchLabelFailure();
			}
			this.panel.repaint();
		} else if (command.equals("node")) {
			this.panel.switchNodeView();
		} else if (command.equals("alias")) {
			this.panel.switchRendering();
		} else if (command.equals("highway")) {
			this.panel.switchUseHighway();
			this.panel.reroute();
		} else if (command.startsWith("move_")) {
			if (command.equals("move_location")) {
				DialogFactory.locationDialog(this.panel, this);
			} else {
				if (command.equals("move_home")) {
					this.panel.moveDefault();
				} else if (command.equals("move_right")) {
					this.panel.moveLocation(-MapController.MOVE_SENSE, 0);
				} else if (command.equals("move_left")) {
					this.panel.moveLocation(MapController.MOVE_SENSE, 0);
				} else if (command.equals("move_up")) {
					this.panel.moveLocation(0, MapController.MOVE_SENSE);
				} else if (command.equals("move_down")) {
					this.panel.moveLocation(0, -MapController.MOVE_SENSE);
				} else if (command.equals("move_in")) {
					this.panel.zoom(this.x, this.y, 1);
				} else if (command.equals("move_out")) {
					this.panel.zoom(this.x, this.y, -1);
				}
				this.panel.repaint(); 
			}
		} else if (command.equals("version")) {
			String version = Version.getVersion();
			DialogFactory.versionDialog(version, this.panel, this);
		} else if (command.equals("check")) {
			Object source = e.getSource();
			boolean state = ((AbstractButton) source).isSelected();
			this.setting.set(Setting.KEY_UPDATE, Boolean.toString(state));
			this.updateCheckItem.setSelected(state);
		} else if (command.startsWith("show")) {
			if (command.endsWith("roadway")) {
				this.panel.switchRoadway();
				this.panel.repaint();
			} else if (command.endsWith("highway")) {
					this.panel.switchHighway();
					this.panel.repaint();
			} else if (command.endsWith("railway")) {
				this.panel.switchRailway();
				this.panel.repaint();
			} else if (command.endsWith("river")) {
				this.panel.switchRiver();
				this.panel.repaint();
			} else if (command.endsWith("mesh")) {
				this.panel.switchMesh();
				this.panel.repaint();
			} else if (command.endsWith("axis")) {
				this.panel.switchAxis();
				this.panel.repaint();
			}
		} else if (command.equals("TSP")) {
			this.panel.switchTsp();
			this.panel.repaint();
		}
	}

	public float getLocationMouseX() {
		return (float) this.panel.getLocationX(this.x);
	}

	public float getLocationMouseY() {
		return (float) this.panel.getLocationY(this.y);
	}

	public Setting getSetting() {
		return this.setting;
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			boolean flag = (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0;
			this.panel.searchBoundary(e.getX(), e.getY(), flag);
		} else {
			// this.panel.test();
		}
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
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		int d = e.getWheelRotation();
		this.panel.zoom(e.getX(), e.getY(), d);
		this.panel.repaint();
	}

	/**
	 * 起動時に更新を確認するためのメニュー
	 * @param item
	 */
	public void setUpdateCheck(JCheckBoxMenuItem item) {
		this.updateCheckItem = item;
	}
}
