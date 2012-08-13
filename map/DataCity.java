package map;

import java.awt.Point;
import java.awt.Polygon;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import labeling.Labels;

/**
 * 市区町村クラス
 * @author ma38su
 */
public class DataCity implements Serializable {

	/**
	 * 市町村番号フォーマット
	 */
	private final static NumberFormat CODE_FORMAT = new DecimalFormat("00000");
	
	/**
	 * 都道府県番号フォーマット
	 */
	private final static NumberFormat PREFECTURE_FORMAT = new DecimalFormat("00");
	
	/**
	 * 市区町村番号(int)を(String)に変換
	 * @param code 市区町村番号
	 * @return 市区町村番号
	 */
	public static String cityFormat(int code) {
		return DataCity.CODE_FORMAT.format(code);
	}

	/**
	 * 都道府県番号(int)を(String)に変換
	 * @param code 都道府県番号
	 * @return 都道府県番号
	 */
	public static String prefectureFormat(int code) {
		return DataCity.PREFECTURE_FORMAT.format(code);
	}
	
	/**
	 * 地域番号
	 */
	private final Integer code;

	/**
	 * 国土数値情報の市町村界
	 */
	private Polygon[] ksj;
	
	/**
	 * 市区町村名
	 */
	private String name;

	/**
	 * 店舗情報
	 */
	private transient Map<String, Labels> shop;

	private int x;
	
	private int y;

	public DataCity(int code, String name) {
		this.code = code;
		this.name = name;
	}
	
	/**
	 * 市区町村データのコンストラクタ
	 * @param code 市区町村番号
	 * @param polygon 行政界
	 */
	public DataCity(int code, Polygon[] polygon) {
		this.code = code;
		this.ksj = polygon;
		this.shop = new HashMap<String, Labels>();

		long x = 0;
		long y = 0;
		int size = 0;
		for (Polygon p : polygon) {
			size += p.npoints;
			for (int i = 0; i < p.npoints; i++) {
				x += p.xpoints[i];
				y += p.ypoints[i];
			}
		}
		this.x = (int) (x / size);
		this.y = (int) (y / size);
	}

	/**
	 * チェーン店情報を追加します。
	 * @param shop チェーン店名
	 * @param location 店舗の所在地（経緯度）
	 */
	public void addLabels(String shop, Collection<Point> location) {
		if (!this.shop.containsKey(shop)) {
			Labels labels = new Labels(shop);
			labels.add(location);
			this.shop.put(shop, labels);
		}
	}
	
	public boolean hasLabels() {
		return this.shop.size() > 0;
	}

	/**
	 * 市町村番号の取得
	 * @return 市町村番号
	 */
	public int getCode() {
		return this.code;
	}

	/**
	 * @return 複数ラベル
	 */
	public Collection<Labels> getLabels() {
		return this.shop.values();
	}

	/**
	 * 市区町村名
	 * @return 市区町村名
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * 行政界ポリゴンの取得
	 * @return 行政界ポリゴン(国土数値情報)
	 */
	public Polygon[] getPolygon() {
		return this.ksj;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}

	/**
	 * 名前を設定します。
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.getName() + "(" + DataCity.cityFormat(this.getCode()) + ")";
	}

	@Override
	public int hashCode() {
		return this.code;
	}

	@Override
	public boolean equals(Object obj) {
		return obj.equals(this.code);
	}
}