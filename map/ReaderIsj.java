package map;

import java.awt.Point;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jp.sourceforge.ma38su.util.Log;

import view.DialogFactory;

/**
 * 街区レベル位置参照情報の読み込み
 * @author ma38su
 */
public class ReaderIsj {

	/**
	 * 街区レベル位置参照情報の配布URL
	 */
	private final String BASE_URL = "http://nlftp.mlit.go.jp/isj/dls/data/";
	
	private final NumberFormat DOUBLE_NUMBER_FORMAT = new DecimalFormat("00");
	
	/**
	 * 取得する最新の年度
	 */
	private final int LATEST_YEAR = 2005;

	/**
	 * キャッシュファイル保存先ディレクトリ
	 */
	private final String CACHE_DIR;

	/**
	 * カンマ分割パターン
	 */
	private final Pattern ISJ_SPLITER = Pattern.compile(",");

	/**
	 * アドレスマッチング
	 */
	private final AddressMatching MATCHING;

	/**
	 * キャッシュファイル保存先ディレクトリを指定してインスタンスを生成する
	 * @param cacheDir キャッシュファイル保存先ディレクトリ
	 */
	public ReaderIsj(String cacheDir) {
		this.MATCHING = AddressMatching.getInstance();
		this.CACHE_DIR = cacheDir;
		final File dir = new File(cacheDir);
		if(!dir.isDirectory()) {
			dir.mkdirs();
		}
	}
	
	/**
	 * ZIPファイルからストリーミングで読み込みます。
	 * 必要なファイルがZIPファイル中、ひとつだけなのでダウンロードするより効率的です。
	 * @param url URL ZIPファイルのURL
	 * @throws IOException 入出力エラー
	 * @return 住所と経緯度の対応Map
	 */
	private Map<String, Point> getStreaming(URL url) throws IOException {
		Map<String, Point> map = new HashMap<String, Point>();
		ZipInputStream in = null;
		BufferedReader bi = null;
		try {
			in = new ZipInputStream(url.openStream());
			bi = new BufferedReader(new InputStreamReader(in, "SJIS"));
			ZipEntry entry = null;
			while ((entry = in.getNextEntry()) != null){
				String name = entry.getName();
				if (!entry.isDirectory() && name.endsWith(".csv")) {
					Log.out(this, "streaming read " + name);
					String line = bi.readLine();
					while ((line = bi.readLine()) != null) {
						String[] param = this.ISJ_SPLITER.split(line);
						String address = this.MATCHING.format(param[2] + param[3]);
						try {
							int x = (int)(Double.parseDouble(param[8]) * 3600000 + 0.5);
							int y = (int)(Double.parseDouble(param[7]) * 3600000 + 0.5);
							Point point = new Point(x, y);
							if (map.get(address) != null || Integer.parseInt(param[10]) == 1) {
								map.put(address, point);
							}
						} catch (NumberFormatException e) {
							throw new IllegalArgumentException("line:"+ line + ", url:"+ url, e);
						}
					}
				}
			}
		} finally {
			if (bi != null) {
				bi.close();
			} else if (in != null) {
				in.close();
			}
		}
		return map;
	}
	
	/**
	 * 市区町村を指定して、街区レベル位置参照情報をストリーミングで生成します。
	 * @param code 市区町村番号
	 * @return 指定した街区レベル位置参照情報
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Point> productStreaming(int code) {
		String label = this.DOUBLE_NUMBER_FORMAT.format(code / 1000);
		Map<String, Point> map = null;
		File serializeFile = new File(this.CACHE_DIR + label + File.separatorChar + DataCity.cityFormat(code) + ".obj");
		if (serializeFile.exists()) {
			map = (Map<String, Point>) this.readSerializeObject(serializeFile);
		}
		if (map == null) {
			int year = this.LATEST_YEAR;
			do {
				try {
					URL url = this.getURL(code, year);
					Log.out(this, "get "+ url);
					map = this.getStreaming(url);
					break;
				} catch (IOException e) {
					year--;
					Log.out(this, "failed.");
				} catch (Throwable e) {
					DialogFactory.errorDialog(null, e);
				}
			} while (year > 2000);
			this.saveSerializeObject(serializeFile, map);
		}
		return map;
	}
	
	/**
	 * ZIPファイルを展開して、CSVファイルを取得します。
	 * @param url URL
	 * @throws IOException 
	 * @return CSVファイル
	 */
	private File getCSV(URL url) throws IOException {
		ZipInputStream in = null;
		try {
			in = new ZipInputStream(url.openStream());
			ZipEntry entry = null;
			while ((entry = in.getNextEntry()) != null){
				BufferedOutputStream out = null;
				String name = entry.getName();
				if (!entry.isDirectory() && name.endsWith(".csv")) {
					Log.out(this, "download " + name);
					File file = new File(this.CACHE_DIR + entry.getName());
					try {
						out = new BufferedOutputStream(new FileOutputStream(file));
						byte[] buf = new byte[1024];
						int size = 0;
				        while ((size = in.read(buf)) != -1) {
				          out.write(buf, 0, size);
				          out.flush();
				        }
					} finally {
						if (out != null) {
							out.close();
						}
					}
					return file;
				}
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return null;
	}

	/**
	 * 市区町村を指定して、街区レベル位置参照情報を取得
	 * @param code 市区町村番号
	 * @return 指定した街区レベル位置参照情報
	 */
	private File getFile(int code) {
		int year = this.LATEST_YEAR;
		File file = null;
		do {
			try {
				URL url = this.getURL(code, year);
				Log.out(this, " : get "+ url);
				file = this.getCSV(url);
				break;
			} catch (IOException e) {
				year--;
				Log.out(this, " : failed.");
			}
		} while (year > 2000);
		return file;
	}
	
	/**
	 * 街区レベル位置参照情報ダウンロードサービスのURLを生成
	 * @param code 市区町村番号
	 * @param year 年度（西暦）
	 * @return 街区レベル位置参照情報ダウンロードサービスのURL
	 * @throws MalformedURLException 
	 */
	private URL getURL(int code, int year) throws MalformedURLException {
		String label = this.DOUBLE_NUMBER_FORMAT.format(year - 2001) + ".0a";
		String name = DataCity.cityFormat(code) + '-' + label + ".zip";
		return new URL(this.BASE_URL + label + '/' + name);
	}

	/**
	 * 街区レベル位置参照情報を生成します。
	 * @param code 市区町村番号
	 * @return 街区レベル一参照情報から生成したMap
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Point> product(int code) {
		String label = this.DOUBLE_NUMBER_FORMAT.format(code / 1000);
		Map<String, Point> map = null;
		File serializeFile = new File(this.CACHE_DIR + label + File.separatorChar + DataCity.cityFormat(code) + ".obj");
		if (serializeFile.exists()) {
			map = (Map<String, Point>) this.readSerializeObject(serializeFile);
		}
		if (map == null) {
			File file = this.getFile(code);
			if (!serializeFile.getParentFile().exists()) {
				serializeFile.getParentFile().mkdirs();
			}
			if (file != null) {
				map = new LinkedHashMap<String, Point>();
				try {
					Log.out(this, "read "+ file.getCanonicalPath());
					BufferedReader in = null;
					try {
						in = new BufferedReader(new FileReader(file));
						String line = null;
						while ((line = in.readLine()) != null) {
							String[] param = this.ISJ_SPLITER.split(line);
							String address = this.MATCHING.format(param[2] + param[3]);
							try {
								int x = (int)(Double.parseDouble(param[8]) * 3600000 + 0.5);
								int y = (int)(Double.parseDouble(param[7]) * 3600000 + 0.5);
								Point point = new Point(x, y);
								map.put(address, point);
							} catch (NumberFormatException e) {
							}
						}
						this.saveSerializeObject(serializeFile, map);
					} finally {
						if (in != null) {
							in.close();
						}
					}
				} catch (IOException e) {
					Log.err(ReaderIsj.class, e);
					if (file.exists()) {
						file.delete();
					}
				}
			}
		}
		return map;
	}

	/**
	 * ファイルに保存
	 * @param file 保存するファイル
	 * @param locationMap 住所に経緯度を対応させたMap
	 */
	private void saveSerializeObject(File file, Map<String, Point> locationMap) {
		ObjectOutputStream out = null;
		if (!file.getParentFile().isDirectory()) {
			file.getParentFile().mkdirs();
		}
		try {
			try {
				out = new ObjectOutputStream(new FileOutputStream(file));
				out.writeObject(locationMap);
			} finally {
				if (out != null) {
					out.close();
				}
			}
		} catch (IOException e) {
			Log.err(ReaderIsj.class, e);
			if (file.exists()) {
				file.delete();
			}
		}
	}
	
	/**
	 * ファイルから読み込み
	 * @param file 読み込むファイル
	 * @return 読み込んだオブジェクト
	 */
	private Object readSerializeObject(File file) {
		ObjectInputStream out = null;
		Object obj = null;
		try {
			try {
				out = new ObjectInputStream(new FileInputStream(file));
				obj = out.readObject();
			} finally {
				if (out != null) {
					out.close();
				}
			}
		} catch (Exception e) {
			Log.err(ReaderIsj.class, e);
			if (file.exists()) {
				file.delete();
			}
		}
		return obj;
	}
}
