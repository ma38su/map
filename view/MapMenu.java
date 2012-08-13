package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import controller.MapController;

/**
 * メニュークラス
 * @author ma38su
 */
public class MapMenu extends JMenuBar {
	public MapMenu(final MapPanel panel, final MapController control) {

		JMenu menuFile = new JMenu("ファイル(F)");
		menuFile.setMnemonic(KeyEvent.VK_F);

		JMenuItem menuFilePrint = new JMenuItem("印刷");
		menuFilePrint.setActionCommand("print");
		menuFilePrint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
		menuFilePrint.addActionListener(control);
		menuFile.add(menuFilePrint);

		JMenuItem menuFileExport = new JMenuItem("エクスポート(PNG)");
		menuFileExport.setActionCommand("exportPNG");
		menuFileExport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		menuFileExport.addActionListener(control);
		menuFile.add(menuFileExport);
		
		menuFile.addSeparator();

		JMenuItem menuFileExit = new JMenuItem("終了");
		menuFileExit.setActionCommand("exit");
		menuFileExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO スレッドをすべて止めてから終了すべき
				System.exit(0);
			}
		});
		menuFile.add(menuFileExit);
		
		JMenu menuView = new JMenu("表示設定(V)");
		menuView.setMnemonic(KeyEvent.VK_V);
		
		JMenu itemViewLabel = new JMenu("ラベル表示");
		menuView.add(itemViewLabel);
		
		JCheckBoxMenuItem itemViewLabelStation = new JCheckBoxMenuItem("駅", true);
		itemViewLabelStation.setActionCommand("labelStation");
		itemViewLabelStation.addActionListener(control);
		itemViewLabel.add(itemViewLabelStation);
		
		JCheckBoxMenuItem item2_1_2 = new JCheckBoxMenuItem("地名", true);
		item2_1_2.setActionCommand("labelCity");
		item2_1_2.addActionListener(control);
		itemViewLabel.add(item2_1_2);
		
		JCheckBoxMenuItem menuViewLabelArea = new JCheckBoxMenuItem("都道府県/市区町村", true);
		menuViewLabelArea.setActionCommand("label_govt");
		menuViewLabelArea.addActionListener(control);
		itemViewLabel.add(menuViewLabelArea);
		
		itemViewLabel.addSeparator();
	
		final JCheckBoxMenuItem menuViewLabelTextAntialiasing = new JCheckBoxMenuItem("テキストアンチエイリアス", true);
		menuViewLabelTextAntialiasing.addActionListener(control);
		menuViewLabelTextAntialiasing.setActionCommand("label_antialiasing");
		itemViewLabel.add(menuViewLabelTextAntialiasing);
		
		final JCheckBoxMenuItem menuViewLabelTextShadow = new JCheckBoxMenuItem("ラベルに影をつける", true);
		menuViewLabelTextShadow.addActionListener(control);
		menuViewLabelTextShadow.setActionCommand("label_shadow");
		itemViewLabel.add(menuViewLabelTextShadow);

		itemViewLabel.addSeparator();
		
		final JCheckBoxMenuItem itemViewLabelFailure = new JCheckBoxMenuItem("配置失敗の表示", false);
		itemViewLabelFailure.addActionListener(control);
		itemViewLabelFailure.setActionCommand("label_failure");
		itemViewLabel.add(itemViewLabelFailure);

		menuView.addSeparator();
		
		final JCheckBoxMenuItem menuViewRailway = new JCheckBoxMenuItem("鉄道表示", panel.isRailwayVisible());
		menuViewRailway.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.setRailwayVisible(menuViewRailway.isSelected());
				panel.repaint();
			}
		});
		menuView.add(menuViewRailway);

		JCheckBoxMenuItem item2_4 = new JCheckBoxMenuItem("高速道路表示", true);
		item2_4.setActionCommand("show_highway");
		item2_4.addActionListener(control);
		menuView.add(item2_4);

		JCheckBoxMenuItem item2_5 = new JCheckBoxMenuItem("一般道表示", true);
		item2_5.setActionCommand("show_roadway");
		item2_5.addActionListener(control);
		menuView.add(item2_5);
		
		JCheckBoxMenuItem item2_7 = new JCheckBoxMenuItem("河川表示", true);
		item2_7.setActionCommand("show_river");
		item2_7.addActionListener(control);
		menuView.add(item2_7);
		
		JCheckBoxMenuItem item2_8 = new JCheckBoxMenuItem("標高メッシュ表示", true);
		item2_8.setActionCommand("show_mesh");
		item2_8.addActionListener(control);
		menuView.add(item2_8);

		JCheckBoxMenuItem item2_9 = new JCheckBoxMenuItem("経度・緯度の表示", true);
		item2_9.setActionCommand("show_axis");
		item2_9.addActionListener(control);
		menuView.add(item2_9);

		menuView.addSeparator();
		
		final JCheckBoxMenuItem menuViewAntialiasing = new JCheckBoxMenuItem("アンチエイリアス", true);
		menuViewAntialiasing.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.setAntialiasing(menuViewAntialiasing.isSelected());
				panel.repaint();
			}
		});
		menuView.add(menuViewAntialiasing);

		JMenu menuMove = new JMenu("移動(M)");
		menuMove.setMnemonic(KeyEvent.VK_M);

		JMenuItem menu3_1 = new JMenuItem("日本全域を表示倍率");
		menu3_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.CTRL_DOWN_MASK));
		menu3_1.setActionCommand("move_home");
		menu3_1.addActionListener(control);
		menuMove.add(menu3_1);

		JMenuItem menu3_2 = new JMenuItem("数値地図を表示倍率");
		menu3_2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COLON, InputEvent.CTRL_DOWN_MASK));
		menu3_2.setActionCommand("move_sdf");
		menu3_2.addActionListener(control);
		menuMove.add(menu3_2);

		menuMove.addSeparator();

		JMenuItem menu3_3 = new JMenuItem("表示位置を東へ移動");
		menu3_3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK));
		menu3_3.setActionCommand("move_right");
		menu3_3.addActionListener(control);
		menuMove.add(menu3_3);

		JMenuItem menu3_4 = new JMenuItem("表示位置を西へ移動");
		menu3_4.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK));
		menu3_4.setActionCommand("move_left");
		menu3_4.addActionListener(control);
		menuMove.add(menu3_4);

		JMenuItem menu3_5 = new JMenuItem("表示位置を南へ移動");
		menu3_5.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK));
		menu3_5.setActionCommand("move_down");
		menu3_5.addActionListener(control);
		menuMove.add(menu3_5);

		JMenuItem menu3_6 = new JMenuItem("表示位置を北へ移動");
		menu3_6.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK));
		menu3_6.setActionCommand("move_up");
		menu3_6.addActionListener(control);
		menuMove.add(menu3_6);
		
		menuMove.addSeparator();
		
		JMenuItem menu3_7 = new JMenuItem("緯度経度を指定して移動");
		menu3_7.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
		menu3_7.setActionCommand("move_location");
		menu3_7.addActionListener(control);
		menuMove.add(menu3_7);

		this.add(menuFile);
		this.add(menuView);
		this.add(menuMove);
	}
}
