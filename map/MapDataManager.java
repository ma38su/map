package map;

import index.CellBounds;
import index.CellMethod;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import map.ksj.PrefectureCollection;
import map.ksj.RailwayCollection;
import view.MapPanel;
import view.StatusBar;
import database.CodeDatabase;
import database.KsjDataManager;

/**
 * 地図データ管理クラス
 * @author ma38su
 */
public class MapDataManager extends Thread {

	/**
	 * セル型の地域検索クラス
	 */
	private final CellMethod cell;

	final CodeDatabase db;
	
	/**
	 * 市区町村データのMap
	 */
	private final Map<Integer, DataCity> mapCity;
	
	/**
	 * 都道府県名
	 */
	private final String[] name = new String[]{"北海道", "青森県", "岩手県", "宮城県", "秋田県", "山形県", "福島県", "茨城県", "栃木県", "群馬県", "埼玉県", "千葉県", "東京都", "神奈川県", "新潟県", "富山県", "石川県", "福井県", "山梨県", "長野県", "岐阜県", "静岡県", "愛知県", "三重県", "滋賀県", "京都府", "大阪府", "兵庫県", "奈良県", "和歌山県", "鳥取県", "島根県", "岡山県", "広島県", "山口県", "徳島県", "香川県", "愛媛県", "高知県", "福岡県", "佐賀県", "長崎県", "熊本県", "大分県", "宮崎県", "鹿児島県", "沖縄県"};

	/**
	 * 地図パネルクラス
	 */
	final MapPanel panel;

	private PrefectureCollection[] prefecture;

	private final KsjDataManager ksjMgr;
	
	private Rectangle screen;
	
	private final RailwayCollection railway;
	
	/**
	 * ステータスバー
	 */
	private final StatusBar statusbar;

	public MapDataManager(MapPanel panel, final CellMethod cell, CodeDatabase db, StatusBar statusbar) {
		this.db = db;
		this.ksjMgr = new KsjDataManager(".data"+ File.separatorChar + "org", ".data" + File.separatorChar + "csv");
		this.mapCity = new HashMap<Integer, DataCity>();
		this.panel = panel;
		this.cell = cell;
		this.statusbar = statusbar;
		this.prefecture = new PrefectureCollection[47];

		this.railway = this.ksjMgr.getRailwayCollection();

		this.screen = this.panel.getScreen();
	}
	
	public RailwayCollection getRailwayCollection() {
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

	/**
	 * 都道府県データ
	 * @param code
	 * @return 既読ならtrue、未読ならfalse
	 */
	public boolean hasPrefecture(int code) {
		return this.prefecture[code - 1] != null;
	}
	
	public PrefectureCollection[] getPrefectureDatas() {
		return this.prefecture;
	}

	/**
	 * 指定した市区町村番号のラベルを読み込みます。
	 * @param code 市区町村番号
	 */
	private void readLabel(int code) {
		DataCity city = this.mapCity.get(code);
		if (city == null) {
			String name = this.db.get(code);
			city = new DataCity(code, name);
			synchronized (this.mapCity) {
				this.mapCity.put(code, city);
			}
		}
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
				if (this.panel.mode() > 1) {
					Rectangle rect;
					do {
						rect = new Rectangle(this.screen);
						this.statusbar.startReading("SEARCH AREA");
						Map<CellBounds, Integer> codes = this.cell.search2(rect);
						for (int val : codes.values()) {
							prefSet.add(val / 1000);
						}
						if (!this.panel.isOperation()) {
							this.statusbar.startReading("DUMP PREF");
							this.dumpPrefecture(prefSet);
						}
						for (int prefCode : prefSet) {
							this.readPrefecture(prefCode);
						}
						if (this.panel.mode() > 2) {
							for (Map.Entry<CellBounds, Integer> entry : codes.entrySet()) {
								if (!rect.intersects(this.screen)) {
									break;
								}
								CellBounds bounds = entry.getKey();
								Integer code = entry.getValue();
								String strCode = DataCity.cityFormat(code);
								if (bounds.intersects(this.screen)) {
									this.statusbar.startReading("READ LABELS: "+ strCode);
									this.readLabel(code);
									this.statusbar.finishReading();
								}
							}
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

	public void searchedFinished() {
		this.panel.repaint();
	}

	public void wakeup() {
		synchronized (this.cell) {
			this.cell.notifyAll();
		}
	}
}
