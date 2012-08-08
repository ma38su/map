package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import jp.sourceforge.ma38su.util.Log;

import util.Setting;

import controller.MapController;

/**
 * メニュークラス
 * @author ma38su
 */
public class MapMenu extends JMenuBar {
	public MapMenu(String styleDir, final MapController control) {

		JMenu menu0 = new JMenu("ファイル(F)");
		menu0.setMnemonic(KeyEvent.VK_F);

		JMenuItem item0_1 = new JMenuItem("印刷");
		item0_1.setActionCommand("print");
		item0_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
		item0_1.addActionListener(control);
		menu0.add(item0_1);

		JMenuItem item0_2 = new JMenuItem("エクスポート(PNG)");
		item0_2.setActionCommand("exportPNG");
		item0_2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		item0_2.addActionListener(control);
		menu0.add(item0_2);
		
		menu0.addSeparator();

		JMenuItem item0_5 = new JMenuItem("終了");
		item0_5.setActionCommand("exit");
		item0_5.addActionListener(control);
		menu0.add(item0_5);

		
		JMenu menu1 = new JMenu("ルート探索(S)");
		menu1.setMnemonic(KeyEvent.VK_S);

		ButtonGroup group = new ButtonGroup();

		JMenuItem item1_0 = new JMenuItem("クリア");
		item1_0.setActionCommand("navi_clear");
		item1_0.addActionListener(control);
		menu1.add(item1_0);
		
		menu1.addSeparator();
		
		JCheckBoxMenuItem item1_1 = new JCheckBoxMenuItem("Dijkstra", false);
		item1_1.setActionCommand("path_dijkstra");
		item1_1.addActionListener(control);
		menu1.add(item1_1);
		group.add(item1_1);

		JCheckBoxMenuItem item1_2 = new JCheckBoxMenuItem("A*", true);
		item1_2.setActionCommand("path_a*");
		item1_2.addActionListener(control);
		menu1.add(item1_2);
		group.add(item1_2);

		menu1.addSeparator();

		JCheckBoxMenuItem item1_3 = new JCheckBoxMenuItem("高速道路の利用", true);
		item1_3.setActionCommand("highway");
		item1_3.addActionListener(control);
		menu1.add(item1_3);

		menu1.addSeparator();

		JCheckBoxMenuItem item1_4 = new JCheckBoxMenuItem("巡回セールスマン問題", false);
		item1_4.setActionCommand("TSP");
		item1_4.addActionListener(control);
		menu1.add(item1_4);
		
		JMenu menu2 = new JMenu("表示設定(V)");
		menu2.setMnemonic(KeyEvent.VK_V);
		
		JMenu item2_0 = new JMenu("スタイル");
		menu2.add(item2_0);
		final JCheckBoxMenuItem item2_0_0 = new JCheckBoxMenuItem("デフォルト", true);
		item2_0.add(item2_0_0);
		item2_0_0.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				control.setDefaultStyle();
			}
		});
		File style = new File(styleDir);
		if (style.isDirectory()) {
			File[] styles = style.listFiles();
			if (styles.length > 0) {
				item2_0.addSeparator();
				ButtonGroup styleGroup = new ButtonGroup();
				styleGroup.add(item2_0_0);
				for (File file : styles) {
					try {
						String name = file.getName();
						JCheckBoxMenuItem item = new JCheckBoxMenuItem(name.substring(0, name.lastIndexOf('.')));
							item.setActionCommand(file.getCanonicalPath());
						item.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								try {
									control.setStyle(e.getActionCommand());
								} catch (IllegalArgumentException ex) {
									item2_0_0.doClick();
								}
							}
						});
						item2_0.add(item);
						styleGroup.add(item);
					} catch (IOException ex) {
						Log.err(this, ex);
					}
				}
			}
		}
		
		JMenu item2_1 = new JMenu("ラベル表示");
		menu2.add(item2_1);
		
		JCheckBoxMenuItem item2_1_1 = new JCheckBoxMenuItem("駅", true);
		item2_1_1.setActionCommand("labelStation");
		item2_1_1.addActionListener(control);
		item2_1.add(item2_1_1);
		
		JCheckBoxMenuItem item2_1_2 = new JCheckBoxMenuItem("地名", true);
		item2_1_2.setActionCommand("labelCity");
		item2_1_2.addActionListener(control);
		item2_1.add(item2_1_2);
		
		JCheckBoxMenuItem item2_1_3 = new JCheckBoxMenuItem("施設", true);
		item2_1_3.setActionCommand("labelFacility");
		item2_1_3.addActionListener(control);
		item2_1.add(item2_1_3);

		JCheckBoxMenuItem item2_1_4 = new JCheckBoxMenuItem("都道府県/市区町村", true);
		item2_1_4.setActionCommand("label_govt");
		item2_1_4.addActionListener(control);
		item2_1.add(item2_1_4);
		
		item2_1.addSeparator();
	
		final JCheckBoxMenuItem item2_1_5 = new JCheckBoxMenuItem("テキストアンチエイリアス", true);
		item2_1_5.addActionListener(control);
		item2_1_5.setActionCommand("label_antialiasing");
		item2_1.add(item2_1_5);
		
		final JCheckBoxMenuItem item2_1_6 = new JCheckBoxMenuItem("ラベルに影をつける", true);
		item2_1_6.addActionListener(control);
		item2_1_6.setActionCommand("label_shadow");
		item2_1.add(item2_1_6);

		item2_1.addSeparator();
		
		final JCheckBoxMenuItem item2_1_7 = new JCheckBoxMenuItem("配置失敗の表示", false);
		item2_1_7.addActionListener(control);
		item2_1_7.setActionCommand("label_failure");
		item2_1.add(item2_1_7);

		menu2.addSeparator();
		
		JCheckBoxMenuItem item2_2 = new JCheckBoxMenuItem("頂点表示", false);
		item2_2.setActionCommand("node");
		item2_2.addActionListener(control);
		menu2.add(item2_2);

		JCheckBoxMenuItem item2_3 = new JCheckBoxMenuItem("鉄道表示", true);
		item2_3.setActionCommand("show_railway");
		item2_3.addActionListener(control);
		menu2.add(item2_3);

		JCheckBoxMenuItem item2_4 = new JCheckBoxMenuItem("高速道路表示", true);
		item2_4.setActionCommand("show_highway");
		item2_4.addActionListener(control);
		menu2.add(item2_4);

		JCheckBoxMenuItem item2_5 = new JCheckBoxMenuItem("一般道表示", true);
		item2_5.setActionCommand("show_roadway");
		item2_5.addActionListener(control);
		menu2.add(item2_5);
		
		JCheckBoxMenuItem item2_7 = new JCheckBoxMenuItem("河川表示", true);
		item2_7.setActionCommand("show_river");
		item2_7.addActionListener(control);
		menu2.add(item2_7);
		
		JCheckBoxMenuItem item2_8 = new JCheckBoxMenuItem("標高メッシュ表示", true);
		item2_8.setActionCommand("show_mesh");
		item2_8.addActionListener(control);
		menu2.add(item2_8);

		JCheckBoxMenuItem item2_9 = new JCheckBoxMenuItem("経度・緯度の表示", true);
		item2_9.setActionCommand("show_axis");
		item2_9.addActionListener(control);
		menu2.add(item2_9);

		menu2.addSeparator();
		
		JCheckBoxMenuItem item2_10 = new JCheckBoxMenuItem("アンチエイリアス", true);
		item2_10.setActionCommand("alias");
		item2_10.addActionListener(control);
		menu2.add(item2_10);

		JMenu menu3 = new JMenu("移動(M)");
		menu3.setMnemonic(KeyEvent.VK_M);

		JMenu menu4 = new JMenu("ヘルプ(H)");
		menu4.setMnemonic(KeyEvent.VK_H);

		JMenuItem menu3_1 = new JMenuItem("日本全域を表示倍率");
		menu3_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.CTRL_DOWN_MASK));
		menu3_1.setActionCommand("move_home");
		menu3_1.addActionListener(control);
		menu3.add(menu3_1);

		JMenuItem menu3_2 = new JMenuItem("数値地図を表示倍率");
		menu3_2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COLON, InputEvent.CTRL_DOWN_MASK));
		menu3_2.setActionCommand("move_sdf");
		menu3_2.addActionListener(control);
		menu3.add(menu3_2);

		menu3.addSeparator();

		JMenuItem menu3_3 = new JMenuItem("表示位置を東へ移動");
		menu3_3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK));
		menu3_3.setActionCommand("move_right");
		menu3_3.addActionListener(control);
		menu3.add(menu3_3);

		JMenuItem menu3_4 = new JMenuItem("表示位置を西へ移動");
		menu3_4.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK));
		menu3_4.setActionCommand("move_left");
		menu3_4.addActionListener(control);
		menu3.add(menu3_4);

		JMenuItem menu3_5 = new JMenuItem("表示位置を南へ移動");
		menu3_5.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK));
		menu3_5.setActionCommand("move_down");
		menu3_5.addActionListener(control);
		menu3.add(menu3_5);

		JMenuItem menu3_6 = new JMenuItem("表示位置を北へ移動");
		menu3_6.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK));
		menu3_6.setActionCommand("move_up");
		menu3_6.addActionListener(control);
		menu3.add(menu3_6);
		
		menu3.addSeparator();
		
		JMenuItem menu3_7 = new JMenuItem("緯度経度を指定して移動");
		menu3_7.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
		menu3_7.setActionCommand("move_location");
		menu3_7.addActionListener(control);
		menu3.add(menu3_7);
		
		JMenuItem help4_1 = new JMenuItem("操作マニュアル");
		help4_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.CTRL_DOWN_MASK));
		help4_1.setActionCommand("web_manual");
		help4_1.addActionListener(control);
		menu4.add(help4_1);
		
		JMenuItem help4_2 = new JMenuItem("利用に際して");
		help4_2.setActionCommand("web_licence");
		help4_2.addActionListener(control);
		menu4.add(help4_2);
		
		menu4.addSeparator();

		JMenuItem help4_3 = new JMenuItem("ソフトウェアの更新を確認(O)");
		help4_3.setMnemonic(KeyEvent.VK_O);
		help4_3.setActionCommand("version");
		help4_3.addActionListener(control);
		menu4.add(help4_3);

		boolean isUpdate = "true".equalsIgnoreCase(control.getSetting().get(Setting.KEY_UPDATE));
		final JCheckBoxMenuItem help4_4 = new JCheckBoxMenuItem("起動時に更新を確認", isUpdate);
		help4_4.setActionCommand("check");
		help4_4.addActionListener(control);
		menu4.add(help4_4);

		menu4.addSeparator();
		
		final JMenuItem help4_5 = new JMenuItem("Digital Mapについて");
		help4_5.setActionCommand("about");
		help4_5.addActionListener(control);
		menu4.add(help4_5);
		
		control.setUpdateCheck(help4_4);

		this.add(menu0);
		this.add(menu1);
		this.add(menu2);
		this.add(menu3);
		this.add(menu4);
	}
}
