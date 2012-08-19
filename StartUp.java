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
import java.awt.Polygon;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;
import javax.swing.UIManager;

import map.FileDatabase;
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

		new JRootPane();
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
		
		JMenuBar menu = new MapMenu(panel, controller);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(statusbar, BorderLayout.SOUTH);
		frame.add(panel, BorderLayout.CENTER);
		frame.add(menu, BorderLayout.NORTH);

		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		frame.setBounds(env.getMaximumWindowBounds());
		frame.setLocationRelativeTo(null);

		frame.setVisible(true);

		try {
			statusbar.startReading("初期設定");

			final MapDataManager maps = new MapDataManager(panel, new CellMethod(".data" + File.separatorChar + "index"), statusbar);
			statusbar.finishReading();

			statusbar.startReading("READ OpenGIS Worlddata");
			Polygon[][] world = FileDatabase.getWorldPolygon();

			statusbar.startReading("READ 国土数値情報 都道府県界");
			Polygon[][] prefectures = FileDatabase.getPrefecturePolygon();
			Polygon[] prefectureLake = new Polygon[]{prefectures[45][278], prefectures[18][1], prefectures[19][0], prefectures[7][2], prefectures[7][3], prefectures[0][84], prefectures[4][13], prefectures[1][30], prefectures[0][288], prefectures[6][9], prefectures[24][7], prefectures[31][85]};
			Polygon[] island = new Polygon[]{prefectures[24][5], prefectures[0][82], prefectures[0][83]};
			statusbar.finishReading();

			panel.init(maps, world, prefectures, prefectureLake, island);
			panel.repaint();

		} catch (IOException e) {
			statusbar.startReading("ERROR "+ e.getMessage());
		}

		statusbar.finishReading();
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
