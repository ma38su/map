package map;

import index.CellBounds;
import index.CellMethod;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.sourceforge.ma38su.util.Log;

import map.ksj.KsjPrefecture;
import map.ksj.RailwayCollection;
import map.route.RouteNavigation;
import map.sdf25k.Node;
import map.sdf25k.ReaderSdf25k;
import view.MapPanel;
import view.StatusBar;
import database.FileDatabase;
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

	private KsjPrefecture[] prefecture;

	/**
	 * 数値地図25000を読み込むためのクラス
	 */
	private final ReaderSdf25k readerSdf25k;

	private final KsjDataManager ksjMgr;
	
	private Rectangle screen;
	
	private final RailwayCollection railway;
	
	/**
	 * ステータスバー
	 */
	private final StatusBar statusbar;

	public MapDataManager(MapPanel panel, final CellMethod cell, FileDatabase storage, CodeDatabase db, StatusBar statusbar) {
		this.db = db;
		this.ksjMgr = new KsjDataManager(".data"+ File.separatorChar + "org", ".data" + File.separatorChar + "csv", ".data" + File.separatorChar + "serialize");
		this.mapCity = new HashMap<Integer, DataCity>();
		this.panel = panel;
		this.cell = cell;
		this.readerSdf25k = new ReaderSdf25k(storage);
		this.statusbar = statusbar;
		this.prefecture = new KsjPrefecture[47];

		this.railway = this.ksjMgr.getRailwayCollection();
		this.prefecture[0] = this.ksjMgr.getPrefectureData(1);

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
		synchronized (this.mapCity) {
			/*
			Iterator<Map.Entry<Integer, DataCity>> itr = this.mapCity.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry<Integer, DataCity> entry = itr.next();
				int prefCode = entry.getKey() / 1000;
				if (!codes.contains(prefCode)) {
//					this.prefecture[prefCode - 1] = null;
//					this.statusbar.startReading("DUMP "+ this.name[prefCode - 1]);
					itr.remove();
				}
			}
			*/
		}
		this.statusbar.finishReading();
	}
	
	public DataCity[] getDataCity() {
		synchronized (this.mapCity) {
			return this.mapCity.values().toArray(new DataCity[]{});
		}
	}

	/**
	 * 指定した頂点番号の頂点を取得します。
	 * @param id 頂点番号
	 * @param isDetailRoad 詳細な道路区間データを取得する場合true
	 * @return 頂点
	 * @throws IOException 頂点を取得できなかった場合
	 */
	public Node getNode(long id) {
		Node ret = null;
		return ret;
	}
	
	public Collection<Node> getNodes(int code) {
		return null;
	}

	/**
	 * とりあえず行政代表点を求めます。
	 * @param code 市区町村番号
	 * @return 市区町村番号に対応した座標
	 */
	public Point getPoint(int code) {
		Point point = null;
		try {
			point = this.readerSdf25k.readPoint(code);
		} catch (Exception e) {
			point = null;
			Log.err(this, e);
			Log.err(this, "code: "+ Integer.toString(code));
		}
		return point;
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
	
	public KsjPrefecture[] getPrefectureDatas() {
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
	private void readPrefecture(final int prefCode) throws IOException {
		if (this.prefecture[prefCode - 1] == null) {
			this.statusbar.startReading("READ PREF: " + this.name[prefCode - 1]);
			final List<DataCity> list = new ArrayList<DataCity>();
			
			String format = DataCity.prefectureFormat(prefCode);
			
			Map<Integer, List<Polygon>> polygonMap = ReaderKsj.readSerializeKsj(format);
			for (Map.Entry<Integer, List<Polygon>> entry : polygonMap.entrySet()) {
				Integer code = entry.getKey();
				DataCity city = new DataCity(code, entry.getValue().toArray(new Polygon[] {}));
				list.add(city);
				synchronized (this.mapCity) {
					this.mapCity.put(code, city);
				}
			}
//			this.prefecture[prefCode - 1] = ksjMgr.getPrefectureData(prefCode);

			Thread thread = new Thread() {
				@Override
				public void run() {
					for (DataCity city : list) {
						city.setName(MapDataManager.this.db.get(city.getCode()));
					}
					MapDataManager.this.panel.repaint();
				}
			};
			thread.setPriority(1);
			thread.start();
			this.statusbar.finishReading();
		}
	}

	@Override
	public void run() {
		Set<Integer> prefSet = new HashSet<Integer>();
		while (true) {
			Log.out(this, "running...");
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
						Log.out(this, "codes = " + codes);
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
					Log.out(this, "wait");
					this.cell.wait();
				}
			} catch (Exception e) {
				Log.err(this, e);
			}
			prefSet.clear();
		}
	}

	public void searchedFinished() {
		this.panel.repaint();
	}

	public void set(RouteNavigation navi) {
		this.statusbar.set(navi);
	}

	public void wakeup() {
		synchronized (this.cell) {
			this.cell.notifyAll();
		}
	}
}
