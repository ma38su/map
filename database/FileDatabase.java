package database;

import java.awt.Polygon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Observable;
import java.util.regex.Pattern;

import jp.sourceforge.ma38su.util.Log;

/**
 * 数値地図のデータ管理
 * @author ma38su
 */
public class FileDatabase extends Observable {

	/**
	 * 都道府県データの相対位置リスト
	 */
	private static final String prefData = "/.data/prefecture.csv";

	/**
	 * 都道府県界のデータ
	 */
	private static final String prefPolygon = "/.data/prefecture.dat";

	public void clearCache() {
		File file = new File(this.SERIALIZE_DIR);
		if (file.delete()) {
			Log.err(this, "failure: delete "+ this.SERIALIZE_DIR);
		}
	}

	/**
	 * FreeGISの世界地図データ
	 * [0] 日本以外
	 * [1] 日本のみ
	 */
	private static final String worldPolygon = "/.data/freegis.dat";

	private final NumberFormat cityFormat;

	/**
	 * 都道府県のURL補助
	 */
	private final String[] prefecture;

	/**
	 * 直列化したファイルの保存先ディレクトリ
	 */
	private final String SERIALIZE_DIR;
	
	/**
	 * コンストラクタ
	 * @param cacheDir データ格納ディレクトリ
	 * @param status ステータスバー
	 * @throws IOException 入出力エラー
	 */
	public FileDatabase(String cacheDir) throws IOException {

		this.cityFormat = new DecimalFormat("00000");
		
		File dirSdf25k = new File(cacheDir + "sdf25k");
		File dirSdf2500 = new File(cacheDir + "sdf2500");
		File dirKsj = new File(cacheDir + "ksj");
		File dirSerializable = new File(cacheDir + "serialize");
		this.SERIALIZE_DIR = dirSerializable.getCanonicalPath() + File.separatorChar;
		
		Log.out(this, "init Cache Directory "+ dirSdf25k.getCanonicalPath());
		if(!dirSdf25k.isDirectory()) {
			dirSdf25k.mkdirs();
		}
		Log.out(this, "init Cache Directory "+ dirSdf2500.getCanonicalPath());
		if(!dirSdf2500.isDirectory()) {
			dirSdf2500.mkdirs();
		}
		Log.out(this, "init Cache Directory "+ dirKsj.getCanonicalPath());
		if(!dirKsj.isDirectory()) {
			dirKsj.mkdirs();
		}
		Log.out(this, "init Cache Serialize Directory "+ dirSerializable.getCanonicalPath());
		if(!dirSerializable.isDirectory()) {
			dirSerializable.mkdirs();
		}

		// 都道府県番号（都道府県数47だが、北海道は1のため）
		this.prefecture = new String[48];
		BufferedReader out = null;
		try {
			out = new BufferedReader(new InputStreamReader(FileDatabase.class.getResourceAsStream(FileDatabase.prefData), "SJIS"));
			int i = 1;
			Pattern csv = Pattern.compile(",");
			while(out.ready()) {
				String line = out.readLine();
				this.prefecture[i++] = csv.split(line)[1];
			}
		} finally {
			if(out != null) {
				out.close();
			}
		}
		this.setChanged();
	}
	
	private String cityFormat(int code) {
		return this.cityFormat.format(code);
	}
	
	@Override
	protected synchronized void clearChanged() {
	}
	
	/**
	 * 頂点の外部への接続情報の取得
	 * @param code 市町村番号
	 * @return 市町村番号に対応する頂点の外部への接続情報
	 */
	public InputStream getBoundaryNode(int code) {
		String codeStr = this.cityFormat(code);
		return this.getClass().getResourceAsStream("/.data/" + codeStr.substring(0, 2) + "/" + codeStr +".nod");
	}
	
	/**
	 * 日本の都道府県データを取得します。
	 * @return 都道府県データ
	 */
	public Polygon[][] getPrefecturePolygon() {
		return (Polygon[][]) this.readSerializableArchive(FileDatabase.prefPolygon);
	}

	/**
	 * 世界地図データの取得
	 * @return 世界地図データ
	 */
	public Polygon[][] getWorldPolygon() {
		return (Polygon[][]) this.readSerializableArchive(FileDatabase.worldPolygon);
	}
	
	private String getSerializablePath(String path) {
		return this.SERIALIZE_DIR + path;
	}
	
	/**
	 * 直列化されたメッシュ標高を読み込みます。
	 * synchronizedは経路探索処理と、地図データ表示のスレッドの衝突を防ぐため。
	 * @param path パス
	 * @return メッシュ標高
	 */
	public Object readSerializableArchive(String path) {
		Log.out(this, "read Serialize "+ path);
		Object obj = null;
		try {
			ObjectInputStream in = null;
			try {
				in = new ObjectInputStream(System.class.getResourceAsStream(path));
				obj = in.readObject();
			} finally {
				if (in != null) {
					in.close();
				}
			}
		} catch (Exception e) {
			obj = null;
			Log.err(this, e);
		}
		return obj;
	}

	/**
	 * 直列化されたメッシュ標高を読み込みます。
	 * synchronizedは経路探索処理と、地図データ表示のスレッドの衝突を防ぐため。
	 * @param path パス
	 * @return メッシュ標高
	 */
	public Object readSerializable(String path) {
		Object obj = null;
		File file = new File(this.getSerializablePath(path));
		if (file.isFile()) {
			try {
				Log.out(this, "read Serialize "+ file.getCanonicalPath());
				ObjectInputStream in = null;
				try {
					in = new ObjectInputStream(new FileInputStream(file));
					obj = in.readObject();
				} finally {
					if (in != null) {
						in.close();
					}
				}
			} catch (Throwable e) {
				Log.err(this, e);
				if (!file.delete()) {
					Log.err(this, "failure delete: "+ file);
				}
				obj = null;
			}
		}
		return obj;
	}

	/**
	 * オブジェクトを直列化して保存します。
	 * 衝突を避けるため一度.tmpファイルに保存して後にリネームします。
	 * @param path パス
	 * @param obj 直列化可能なオブジェクト
	 */
	public void writeSerializable(String path, Object obj) {
		String key = this.getSerializablePath(path);
		Log.out(this, "save "+ key);
		File file = new File(key + ".tmp");
		if (!file.getParentFile().isDirectory()) {
			if (!file.getParentFile().mkdirs()) {
				Log.err(this, "mkdir failure: "+ file.getParent());
			}
		}
		try {
			ObjectOutputStream out = null;
			try {
				out = new ObjectOutputStream(new FileOutputStream(file));
				out.writeObject(obj);
				out.flush();
			} finally {
				if (out != null) {
					out.close();
				}
			}
			if (!file.renameTo(new File(key))) {
				Log.err(this, "rename failure: "+ key);
				if (!file.delete()) {
					Log.err(this, "delete failure: "+ file);
				}
			}
		} catch (Exception e) {
			Log.err(this, e);
			if (file.exists()) {
				file.delete();
			}
		}
	}
}
