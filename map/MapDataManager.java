package map;

import index.CellBounds;
import index.CellMethod;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import map.KsjDataManager;
import map.ksj.PrefectureDataset;
import map.ksj.RailwayDataset;
import view.MapPanel;
import view.StatusBar;

/**
 * 地図データ管理クラス
 * @author ma38su
 */
public class MapDataManager extends Thread {

	/**
	 * セル型の地域検索クラス
	 */
	private final CellMethod cell;
	
	/**
	 * 都道府県名
	 */
	private final String[] name = new String[]{"北海道", "青森県", "岩手県", "宮城県", "秋田県", "山形県", "福島県", "茨城県", "栃木県", "群馬県", "埼玉県", "千葉県", "東京都", "神奈川県", "新潟県", "富山県", "石川県", "福井県", "山梨県", "長野県", "岐阜県", "静岡県", "愛知県", "三重県", "滋賀県", "京都府", "大阪府", "兵庫県", "奈良県", "和歌山県", "鳥取県", "島根県", "岡山県", "広島県", "山口県", "徳島県", "香川県", "愛媛県", "高知県", "福岡県", "佐賀県", "長崎県", "熊本県", "大分県", "宮崎県", "鹿児島県", "沖縄県"};

	/**
	 * 地図パネルクラス
	 */
	private final MapPanel panel;

	private PrefectureDataset[] prefecture;

	private final KsjDataManager ksjMgr;
	
	private Rectangle screen;
	
	private final RailwayDataset railway;
	
	/**
	 * ステータスバー
	 */
	private final StatusBar statusbar;

	public MapDataManager(String dir, MapPanel panel, final CellMethod cell, StatusBar statusbar) {
		this.ksjMgr = new KsjDataManager(dir + File.separatorChar + "org", dir + File.separatorChar + "csv");
		this.panel = panel;
		this.cell = cell;
		this.statusbar = statusbar;
		this.prefecture = new PrefectureDataset[47];

		this.railway = this.ksjMgr.getRailwayDataset();

		this.screen = this.panel.getScreen();
	}
	
	public RailwayDataset getRailwayCollection() {
		return this.railway;
	}

	/**
	 * 引数の都道府県番号に含まれていないデータの解放
	 * @param codes 都道府県番号
	 */
	public synchronized void dumpPrefecture(Set<Integer> codes) {
//		for (Integer code : codes) {
//			int prefCode = code / 1000;
//			this.prefecture[prefCode - 1] = null;
//		}
		this.statusbar.finishReading();
	}
	
	public String getPrefecture(int i) {
		return this.name[i];
	}

	public PrefectureDataset[] getPrefectureDatas() {
		return this.prefecture;
	}

	/**
	 * 指定した都道府県番号の国土数値情報を読み込みます。
	 * @param prefCode 都道府県番号
	 * @throws IOException 入出力エラー
	 */
	private void readPrefecture(final int prefCode) {
		if (this.prefecture[prefCode - 1] == null) {
			this.statusbar.startReading("READ PREF: " + this.name[prefCode - 1]);
			this.prefecture[prefCode - 1] = ksjMgr.getPrefectureData(prefCode);
			this.panel.repaint();
			this.statusbar.finishReading();
		}
	}

	@Override
	public void run() {
		Set<Integer> prefSet = new HashSet<Integer>();
		while (true) {
			try {
				if (this.panel.getMode() > 1) {
					Rectangle rect;
					do {
						rect = new Rectangle(this.screen);
						this.statusbar.startReading("SEARCH AREA");
						Map<CellBounds, Integer> map = this.cell.search2(rect);
						for (int val : map.values()) {
							prefSet.add(val / 1000);
						}
						if (!this.panel.isOperation()) {
							this.statusbar.startReading("DUMP PREF");
							this.dumpPrefecture(prefSet);
						}
						for (int prefCode : prefSet) {
							this.readPrefecture(prefCode);
						}
					} while (!this.screen.equals(rect));
				}
				synchronized (this.cell) {
					this.cell.wait();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			prefSet.clear();
		}
	}

	/**
	 * FreeGISの世界地図データ
	 * [0] 日本以外
	 * [1] 日本のみ
	 */
	private static final String worldPolygon = "/.data/freegis.dat";

	public void wakeup() {
		synchronized (this.cell) {
			this.cell.notifyAll();
		}
	}
	
	/**
	 * 都道府県界のデータ
	 */
	private static final String prefPolygon = "/.data/prefecture.dat";

	/**
	 * 世界地図データの取得
	 * @return 世界地図データ
	 */
	public Polygon[][] getWorldPolygon() {
		return readSerializableArchive(worldPolygon, Polygon[][].class);
	}

	/**
	 * 日本の都道府県データを取得します。
	 * @return 都道府県データ
	 */
	public Polygon[][] getPrefecturePolygon() {
		return readSerializableArchive(prefPolygon, Polygon[][].class);
	}

	public Polygon[] getJapan() {
		return this.ksjMgr.getJapanPolygon();
	}
	
	/**
	 * @param path
	 * @param c
	 * @return 読み込んだインスタンス
	 */
	public static <T> T readSerializableArchive(String path, Class<T> c) {
		T obj = null;
		try {
			ObjectInputStream in = null;
			try {
				in = new ObjectInputStream(System.class.getResourceAsStream(path));
				obj = c.cast(in.readObject());
			} finally {
				if (in != null) {
					in.close();
				}
			}
		} catch (Exception e) {
			obj = null;
		}
		return obj;
	}
}
