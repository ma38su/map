/*
 * KSJ Map
 * 地図データの閲覧のためのプログラムです。
 * Copyright(C) 2005-2012 ma38su
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
import index.CellMethod;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.UIManager;

import map.MapDataManager;

import util.Log;
import util.Version;
import view.MapMenu;
import view.MapPanel;
import view.StatusBar;
import controller.MapController;

/**
 * 地図描画アプリケーション起動のためのクラス
 * @author ma38su
 */
public class StartUp {

	/**
	 * @param mapDir 
	 */
	public static void startup(String mapDir) {

		Log.isDebug = false;

		try {
			String lf = UIManager.getSystemLookAndFeelClassName();
			Log.out(StartUp.class, "set Look&Fell: "+ lf);
			UIManager.setLookAndFeel(lf);
		} catch (Exception e) {
			Log.err(StartUp.class, e);
		}
		JFrame frame = new JFrame();

		String version = Version.get("/history.txt");
		String title = version == null ? "KSJ Map" : "KSJ Map ver." + version;
		frame.setTitle(title);

		StatusBar statusbar = new StatusBar(" ");
		MapPanel panel = new MapPanel();

		MapController controller = new MapController(panel);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(statusbar, BorderLayout.SOUTH);
		frame.add(panel, BorderLayout.CENTER);
		frame.add(new MapMenu(panel, controller), BorderLayout.NORTH);

		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		frame.setBounds(env.getMaximumWindowBounds());
		frame.setLocationRelativeTo(null);

		frame.setVisible(true);

		try {
			statusbar.startReading("初期設定");
			panel.init(new MapDataManager(mapDir, panel, new CellMethod(mapDir + File.separatorChar + "index"), statusbar));
		} catch (IOException e) {
			statusbar.startReading("ERROR "+ e.getMessage());
		}
		panel.addMouseListener(controller);
		panel.addMouseMotionListener(controller);
		panel.addMouseWheelListener(controller);

		statusbar.setThreadPriority(Thread.MIN_PRIORITY);
	}
	
	/**
	 * インスタンス生成不要
	 */
	private StartUp() {
	}
}
