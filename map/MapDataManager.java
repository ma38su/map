package map;

import index.CellBounds;
import index.CellMethod;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.sourceforge.ma38su.util.Log;

import map.route.RouteNavigation;
import map.sdf2500.DataSdf2500;
import map.sdf25k.DataSdf25k;
import map.sdf25k.Node;
import map.sdf25k.ReaderSdf25k;
import map.store.Store;
import view.DialogFactory;
import view.MapPanel;
import view.StatusBar;
import database.FileDatabase;
import database.CodeDatabase;

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
	 * 街区レベル位置参照情報ファクトリー
	 */
	private final ReaderIsj isjFactory;

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

	/**
	 * 都道府県ごとの市区町村の読み込み状態
	 * true  -> 既読
	 * false -> 未読
	 */
	private boolean[] prefecture;

	/**
	 * 数値地図25000を読み込むためのクラス
	 */
	private final ReaderSdf25k readerSdf25k;

	private Rectangle screen;
	
	/**
	 * 数値地図2500
	 */
	private final Set<DataSdf2500> sdf2500;

	private final Map<Integer, DataSdf25k> sdf25k;

	/**
	 * 店舗情報ファクトリー
	 */
	private final Collection<Store> shops;
	
	/**
	 * ステータスバー
	 */
	private final StatusBar statusbar;

	public MapDataManager(MapPanel panel, final CellMethod cell, FileDatabase storage, CodeDatabase db, ReaderIsj isjFactory, Collection<Store> shop, StatusBar statusbar) {
		this.db = db;
		this.mapCity = new HashMap<Integer, DataCity>();
		this.sdf2500 = new HashSet<DataSdf2500>();
		this.sdf25k = new HashMap<Integer, DataSdf25k>();
		this.panel = panel;
		this.cell = cell;
		this.shops = shop;
		this.readerSdf25k = new ReaderSdf25k(storage);
		this.isjFactory = isjFactory;
		this.statusbar = statusbar;
		this.prefecture = new boolean[47];
		this.screen = this.panel.getScreen();
	}

	/**
	 * 引数の都道府県番号に含まれていないデータの解放
	 * @param codes 都道府県番号
	 */
	public synchronized void dumpPrefecture(Set<Integer> codes) {
		synchronized (this.mapCity) {
			Iterator<Map.Entry<Integer, DataCity>> itr = this.mapCity.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry<Integer, DataCity> entry = itr.next();
				int prefCode = entry.getKey() / 1000;
				if (!codes.contains(prefCode)) {
					this.prefecture[prefCode - 1] = false;
					this.statusbar.startReading("DUMP "+ this.name[prefCode - 1]);
					itr.remove();
				}
			}
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
		int code = (int) (id / 1000000);
		DataSdf25k map = this.sdf25k.get(code);
		if (map != null) {
			map = this.readerSdf25k.readData(code);
			Map<Integer, Node> nodes = map.getNodes();
			ret = nodes.get((int) (id % 1000000));
		}
		return ret;
	}
	
	/**
	 * 頂点のCollectionを取得
	 * @param code 市区町村番号
	 * @return 頂点のCollection
	 */
	public Collection<Node> getNodes(int code) {
		return this.sdf25k.get(code).getNodes().values();
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
	 * 表示範囲内の数値地図2500を取得します。
	 * @param screen 表示範囲
	 * @return 数値地図2500
	 */
	public Collection<DataSdf2500> getSdf2500(Rectangle screen) {
		Collection<DataSdf2500> list = new ArrayList<DataSdf2500>();
		synchronized (this.sdf2500) {
			Iterator<DataSdf2500> itr = this.sdf2500.iterator();
			while (itr.hasNext()) {
				DataSdf2500 data = itr.next();
				if (data.intersects(screen)) {
					list.add(data);
				} else {
					itr.remove();
				}
			}
		}
		return list;
	}

	/**
	 * 表示範囲内の数値地図2500を取得します。
	 * @param screen 表示範囲
	 * @return 数値地図25000
	 */
	public Collection<DataSdf25k> getSdf25k(Rectangle screen) {
		Collection<DataSdf25k> list = new ArrayList<DataSdf25k>();
		synchronized (this.sdf25k) {
			Iterator<Map.Entry<Integer, DataSdf25k>> itr = this.sdf25k.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry<Integer, DataSdf25k> entry = itr.next();
				DataSdf25k data = entry.getValue();
				if (data.intersects(screen)) {
					list.add(data);
				} else {
					itr.remove();
				}
			}
		}
		return list;
	}
	
	/**
	 * 都道府県データ
	 * @param code
	 * @return 既読ならtrue、未読ならfalse
	 */
	public boolean hasPrefecture(int code) {
		return this.prefecture[code - 1];
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
		if (!city.hasLabels()) {
			Log.out(this, "read ISJ: "+ code);
			Map<String, Point> locationMap = this.isjFactory.productStreaming(code);
			// TODO 富士山頂付近でnullが返ることがある。
			if (locationMap != null) {
				for (Store shop : this.shops) {
					try {
						List<Point> location = shop.getLocation(city, locationMap);
						city.addLabels(shop.getName(), location);
					} catch (Throwable e) {
						DialogFactory.errorDialog(this.panel, e);
					}
				}
			}
		}
	}

	/**
	 * 指定した都道府県番号の国土数値情報を読み込みます。
	 * @param prefCode 都道府県番号
	 * @throws IOException 入出力エラー
	 */
	private void readPrefecture(int prefCode) throws IOException {
		if (!this.prefecture[prefCode - 1]) {
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
			this.prefecture[prefCode - 1] = true;
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
