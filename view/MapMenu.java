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
		
		JMenu menuViewLabel = new JMenu("ラベル表示");
		menuView.add(menuViewLabel);
		
		final JCheckBoxMenuItem menuViewLabelStationVisible = new JCheckBoxMenuItem("駅", panel.isStationLabelVisible());
		menuViewLabelStationVisible.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.setStationLabelVisible(menuViewLabelStationVisible.isSelected());
				panel.repaint();
			}
		});
		menuViewLabel.add(menuViewLabelStationVisible);

		final JCheckBoxMenuItem menuViewLabelBusVisible = new JCheckBoxMenuItem("バス", panel.isBusLabelVisible());
		menuViewLabelStationVisible.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.setBusLabelVisible(menuViewLabelBusVisible.isSelected());
				panel.repaint();
			}
		});
		menuViewLabel.add(menuViewLabelBusVisible);
		
		final JCheckBoxMenuItem menuViewLabelCityVisible = new JCheckBoxMenuItem("地名", true);
		menuViewLabelCityVisible.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.setCityLabelVisible(menuViewLabelCityVisible.isSelected());
				panel.repaint();
			}
		});
		menuViewLabel.add(menuViewLabelCityVisible);
		
		menuViewLabel.addSeparator();
	
		final JCheckBoxMenuItem menuViewLabelTextAntialiasing = new JCheckBoxMenuItem("テキストアンチエイリアス", panel.isTextAntialiasing());
		menuViewLabelTextAntialiasing.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.setTextAntialiasing(menuViewLabelTextAntialiasing.isSelected());
				panel.repaint();
			}
		});
		menuViewLabel.add(menuViewLabelTextAntialiasing);
		
		final JCheckBoxMenuItem menuViewLabelTextShadow = new JCheckBoxMenuItem("ラベルに影をつける", panel.isLabelShadowVisible());
		menuViewLabelTextShadow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.setLabelShadowVisible(menuViewLabelTextShadow.isSelected());
				panel.repaint();
			}
		});
		menuViewLabel.add(menuViewLabelTextShadow);

		menuViewLabel.addSeparator();
		
		final JCheckBoxMenuItem itemViewLabelFailure = new JCheckBoxMenuItem("配置失敗の表示", false);
		itemViewLabelFailure.addActionListener(control);
		itemViewLabelFailure.setActionCommand("label_failure");
		menuViewLabel.add(itemViewLabelFailure);

		menuView.addSeparator();
		
		final JCheckBoxMenuItem menuViewRailway = new JCheckBoxMenuItem("鉄道路線表示", panel.isRailwayVisible());
		menuViewRailway.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.setRailwayVisible(menuViewRailway.isSelected());
				panel.repaint();
			}
		});
		menuView.add(menuViewRailway);

		final JCheckBoxMenuItem menuViewStation = new JCheckBoxMenuItem("鉄道駅表示", panel.isStationVisible());
		menuViewStation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.setStationVisible(menuViewStation.isSelected());
				panel.repaint();
			}
		});
		menuView.add(menuViewStation);

		final JCheckBoxMenuItem menuViewBusVisible = new JCheckBoxMenuItem("バス表示", panel.isBusVisible());
		menuViewBusVisible.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.setBusVisible(menuViewBusVisible.isSelected());
				panel.repaint();
			}
		});
		menuView.add(menuViewBusVisible);

		final JCheckBoxMenuItem menuViewAxisVisible = new JCheckBoxMenuItem("経度・緯度の表示", panel.isAxisVisible());
		menuViewAxisVisible.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.setAxisVisible(menuViewAxisVisible.isSelected());
				panel.repaint();
			}
		});
		menuView.add(menuViewAxisVisible);

		menuView.addSeparator();
		
		final JCheckBoxMenuItem menuViewAntialiasing = new JCheckBoxMenuItem("アンチエイリアス", panel.isAntialiasing());
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
