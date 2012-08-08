package map.store;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jp.sourceforge.ma38su.util.Log;

import map.AddressMatching;
import map.DataCity;

/**
 * チェーン店のためのクラス
 * @author ma38su
 */
public abstract class ChainStore implements Store {

	/**
	 * 読み込むページ数の上限
	 */
	protected static final int MAX_PAGE = 10;

	/**
	 * アドレスマッチング
	 */
	private final AddressMatching ADDRESS_MATCHING;

	/**
	 * 店舗名
	 */
	private final String NAME;

	/**
	 * キャッシュ保存先ディレクトリ
	 */
	private String cacheDir;

	/**
	 * エンコーディング
	 */
	protected final String ENCODING;

	/**
	 * Pluginとしてロードするためのコンストラクタ
	 * ロード後キャッシュディレクトリを設定する必要がある。
	 * 
	 * @param name 店舗名
	 */
	public ChainStore(String name) {
		this(name, "SJIS");
	}

	/**
	 * @param name 店舗名
	 * @param encode エンコード
	 */
	public ChainStore(String name, String encode) {
		this.ENCODING = encode;
		this.ADDRESS_MATCHING = AddressMatching.getInstance();
		this.NAME = name;
	}
	
	/**
	 * キャッシュディレクトリを設定する
	 * @param dir ディレクトリ
	 */
	public void setCacheDirectory(String dir) {
		this.cacheDir = dir;
	}

	/**
	 * ページを解析して住所の一覧を取得します。
	 * @param city 市区町村データ
	 * @return 住所と店舗名の対応表
	 * @throws IOException 入出力エラー
	 */
	public List<String> getAddress(DataCity city) throws IOException {
		File file = new File(this.cacheDir + DataCity.prefectureFormat(city.getCode() / 1000) + File.separatorChar + this.getClass().getSimpleName() + DataCity.cityFormat(city.getCode()) + ".csv");
		List<String> list = null;
		if (file.exists()) {
			list = this.readFile(file);
		}
		if (list == null) {
			list = new ArrayList<String>();
			for (int i = 1; i < ChainStore.MAX_PAGE; i++) {
				final URL url = this.getURL(city, i);
				if (url != null) {
					BufferedReader in = null;
					try {
						in = new BufferedReader(new InputStreamReader(url.openStream(), this.ENCODING));
						boolean flag = false;
						String line = null;
						while ((line = in.readLine()) != null) {
							String address = this.parseAddress(line);
							if (address != null) {
								list.add(address);
								flag = true;
							}
						}
						if (!flag) {
							i = ChainStore.MAX_PAGE;
						}
					} finally {
						if (in != null) {
							in.close();
						}
					}
				}
			}
			if (this.cacheDir != null) {
				if (!file.getParentFile().isDirectory()) {
					file.getParentFile().mkdirs();
				}
				this.saveFile(file, list);
			}
		}
		return list;
	}

	protected abstract String parseAddress(String input);
	
	/**
	 * 市区町村番号から店舗の経緯度を取得する
	 * @param city 市区町村データ
	 * @param locationMap
	 * @return 店舗の経緯度
	 * @throws IOException 
	 */
	public List<Point> getLocation(DataCity city, Map<String, Point> locationMap) throws IOException {
		List<Point> list = new ArrayList<Point>();
		int count = 0;
		final List<String> address = this.getAddress(city);
		for (String value : address) {
			String cityarea = this.ADDRESS_MATCHING.parse(value);
			Point point = locationMap.get(cityarea);
			if (point != null) {
				list.add(point);
			} else {
				String cityarea2 = this.ADDRESS_MATCHING.chopNumber(cityarea);
				if (cityarea2 != null) {
					point = locationMap.get(cityarea2);
					if (point != null) {
						list.add(point);
					} else {
						String cityarea3 = this.ADDRESS_MATCHING.chopNumber(cityarea2);
						if (cityarea3 != null) {
							point = locationMap.get(cityarea3);
							if (point != null) {
								list.add(point);
							} else {
								count++;
								Log.out(this, city + " Not found : " + value + " (" + cityarea + ")");
								Log.out(this, city + " Not found : " + value + " (" + cityarea2 + ")");
								Log.out(this, city + " Not found : " + value + " (" + cityarea3 + ")");
							}
						} else {
							count++;
							Log.out(this, city + " Not found : " + value + " (" + cityarea + ")");
							Log.out(this, city + " Not found : " + value + " (" + cityarea2 + ")");							
						}
					}
				} else {
					count++;
					Log.out(this, city + " Not found : " + value + " (" + cityarea + ")");					
				}
			}
		}
		Log.out(this, city +" Matching Result " + (address.size() - count) + " / "+ address.size());
		return list;
	}

	/**
	 * 店舗名を返します。
	 * @return 店舗名
	 */
	public String getName() {
		return this.NAME;
	}

	protected abstract URL getURL(DataCity city, int page);
	

	/**
	 * 文字列配列をファイルから読み込む
	 * @param file 読み込むファイル
	 * @return 文字列配列
	 */
	private List<String> readFile(File file) {
		BufferedReader in = null;
		List<String> list = new ArrayList<String>();
		try {
			try {
				in = new BufferedReader(new InputStreamReader(new FileInputStream(file), this.ENCODING));
				String line = null;
				while((line = in.readLine()) != null) {
					list.add(line);
				}
			} finally {
				if (in != null) {
					in.close();
				}
			}
		} catch (IOException e) {
			Log.err(this, e);
			if (file.exists()) {
				file.delete();
			}
		}
		return list;
	}

	/**
	 * 文字列配列をファイルに保存する
	 * @param file 保存するファイル
	 * @param address 文字列配列
	 */
	private void saveFile(File file, List<String> address) {
		if (!file.getParentFile().isDirectory()) {
			file.getParentFile().mkdirs();
		}
		
		BufferedWriter out = null;
		try {
			try {
				out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), this.ENCODING));
				for (String value : address) {
					out.write(value);
					out.newLine();
				}
				out.flush();
			} finally {
				if (out != null) {
					out.close();
				}
			}
		} catch (IOException e) {
			if (file.exists()) {
				file.delete();
			}
		}
	}
	
	/**
	 * 直列化されたオブジェクトを読み込みます。
	 * @param file
	 * @return 座標のリスト
	 */
	@SuppressWarnings("unchecked")
	protected List<Point> readSerialize(File file) {
		List<Point> points = null;
		ObjectInputStream in = null;
		try {
			try {
				in = new ObjectInputStream(new FileInputStream(file));
				points = (List<Point>) in.readObject();
			} finally {
				if (in != null) {
					in.close();
				}
			}
		} catch (Exception e) {
			if (file.exists()) {
				file.delete();
			}
		}
		return points;
	}

	/**
	 * 直列化して保存します。
	 * @param file
	 * @param points
	 */
	protected void saveSerialize(File file, List<Point> points) {
		ObjectOutputStream out = null;
		try {
			try {
				out = new ObjectOutputStream(new FileOutputStream(file));
				out.writeObject(points);
			} finally {
				out.close();
			}
		} catch (IOException e) {
			if (file.exists()) {
				file.delete();
			}
		}
	}
}
