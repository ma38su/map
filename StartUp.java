/*
 * Digital Map
 * 地図データの閲覧のためのプログラムです。
 * Copyright(C) 2005-2006 ma38su
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
import java.awt.Polygon;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;
import javax.swing.UIManager;

import jp.sourceforge.ma38su.util.Log;

import map.MapDataManager;
import map.ReaderIsj;
import map.store.Store;
import util.Loader;
import util.Setting;
import util.Version;
import view.DialogFactory;
import view.MapMenu;
import view.MapPanel;
import view.StatusBar;
import controller.MapController;
import database.CodeDatabase;
import database.FileDatabase;

/**
 * 地図描画アプリケーション起動のためのクラス
 * @author ma38su
 */
public class StartUp {

	/**
	 * フレーム高さ
	 */
	private static final int F_WIDTH = 800;

	/**
	 * フレーム幅
	 */
	private static final int F_HEIGHT = 600;

	/**
	 * args[0]には地図データのルートディレクトリの位置を指定します。
	 * args[1]にはプラグインのディレクトリの位置を指定します。
	 * @param libDir 
	 * @param pluginDir 
	 * @param mapDir 
	 * @param styleDir
	 */
	public static void startup(String libDir, String pluginDir, String mapDir, String styleDir) {

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
		Setting.setVersion(version);
		String title = version == null ? "Digital Map" : "Digital Map ver." + version;
		frame.setTitle(title);

		final Setting setting = new Setting(mapDir + "setting.ini");

		CodeDatabase codeDB;
		try {
			codeDB = new CodeDatabase("/.data/city.csv", mapDir + "city.idx", mapDir + "city.dat");
		} catch (IOException e) {
			codeDB = null;
			DialogFactory.errorDialog(null, e);
			return;
		}
		StatusBar statusbar = new StatusBar(" ");
		MapPanel panel = new MapPanel(styleDir);

		MapController controller = new MapController(panel, setting);
		
		JMenuBar menu = new MapMenu(styleDir, controller);

		boolean isFirst = !"true".equalsIgnoreCase(setting.get(Setting.KEY_TERMS));
		if (isFirst) {
			DialogFactory.termsDialog(setting);
		}

		if (!"false".equalsIgnoreCase(setting.get(Setting.KEY_UPDATE))) {
			try {
				String latest = Version.getLatest("ma38su", "Digital Map");
				if (latest != null && version != null && !version.equals(latest)) {
					DialogFactory.versionDialog(latest, frame, controller);
				}
			} catch (IOException ex) {
				DialogFactory.errorDialog(panel, ex);
			}
		}


		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(statusbar, BorderLayout.SOUTH);
		frame.add(panel, BorderLayout.CENTER);
		frame.add(menu, BorderLayout.NORTH);

		
		frame.setSize(StartUp.F_WIDTH, StartUp.F_HEIGHT);
		frame.setLocationRelativeTo(null);

		frame.setVisible(true);

		try {
			statusbar.startReading("初期設定");

			Collection<Store> store = Loader.loadStorePlugin(pluginDir, mapDir + "store" + File.separatorChar);
			
			FileDatabase fileDB = new FileDatabase(mapDir);
			fileDB.addObserver(statusbar);
			if (isFirst) {
				Log.out(StartUp.class, "delete Cache Files");
				fileDB.clearCache();
			}
			final MapDataManager maps = new MapDataManager(panel, new CellMethod(".data/index/"), fileDB, codeDB, new ReaderIsj(mapDir + "isj" + File.separatorChar), store, statusbar);
			statusbar.finishReading();

			statusbar.startReading("READ OpenGIS Worlddata");
			Polygon[][] world = fileDB.getWorldPolygon();

			statusbar.startReading("READ 国土数値情報 都道府県界");
			Polygon[][] prefectures = fileDB.getPrefecturePolygon();
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
