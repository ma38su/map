package database;

import java.awt.Polygon;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 数値地図のデータ管理
 * @author ma38su
 */
public class FileDatabase {

	/**
	 * 都道府県界のデータ
	 */
	private static final String prefPolygon = "/.data/prefecture.dat";

	/**
	 * FreeGISの世界地図データ
	 * [0] 日本以外
	 * [1] 日本のみ
	 */
	private static final String worldPolygon = "/.data/freegis.dat";

	/**
	 * 日本の都道府県データを取得します。
	 * @return 都道府県データ
	 */
	public static Polygon[][] getPrefecturePolygon() {
		return readSerializableArchive(FileDatabase.prefPolygon, Polygon[][].class);
	}

	/**
	 * 世界地図データの取得
	 * @return 世界地図データ
	 */
	public static Polygon[][] getWorldPolygon() {
		return readSerializableArchive(FileDatabase.worldPolygon, Polygon[][].class);
	}

	/**
	 * @param path パス
	 * @param obj 
	 * @return 書き込み成否
	 */
	public static <T> boolean writeSerializableArchive(String path, T obj) {
		boolean ret = true;
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));
			try {
				out.writeObject(obj);
			} finally {
				out.close();
			}
		} catch (Exception e) {
			ret = false;
		}
		return ret;
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
