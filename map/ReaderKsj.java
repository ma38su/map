package map;

import java.awt.Polygon;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;

import jp.sourceforge.ma38su.util.Log;

/**
 * 国土数値情報を読み込むクラス
 * @author ma38su
 */
public class ReaderKsj {

	/**
	 * 直列化して保存した行政界を読み込みます。
	 * @param name 入力ストリーム
	 * @return Polygon配列
	 * @throws IOException 入出力エラー
	 */
	public static Polygon[][] readPolygon(String name) throws IOException {
		Polygon[][] polygon = null;
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(System.class.getResourceAsStream(name));
			polygon = (Polygon[][]) in.readObject();
		} catch (ClassNotFoundException e) {
			polygon = null;
			Log.err(ReaderKsj.class, e);
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return polygon;
	}

	/**
	 * 都道府県ポリゴンを読み込みます。
	 * @param name 読み込むファイル
	 * @return 都道府県ポリゴン
	 * @throws IOException
	 */
	public static Polygon[][] readPrefecturePolygons(String name) throws IOException {
		ObjectInputStream in = null;
		Polygon[][] polygons = null;
		try {
			in = new ObjectInputStream(System.class.getResourceAsStream(name));
			Object obj = in.readObject();
			if (obj instanceof Polygon[][]) {
				polygons = (Polygon[][]) obj;
			}
		} catch (ClassNotFoundException e) {
			polygons = null;
		} finally {
			if (in != null) {
				in.close();
				in = null;
			}
		}
		return polygons;
	}
	
	/**
	 * 都道府県内の各市区町村に対応した国土数値情報を読み込みます。
	 * 読み込めるのは、直列化して保存したデータに限ります。
	 * 
	 * @param code 都道府県番号
	 * @return 市区町村に対応した国土数値情報の行政界
	 * @throws IOException 入出力エラー
	 * @throws ClassNotFoundException 
	 */
	@SuppressWarnings("unchecked")
	public static Map<Integer, List<Polygon>> readSerializeKsj(String code) throws IOException {
		Map<Integer, List<Polygon>> map = null;
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(ReaderKsj.class.getResourceAsStream("/.data/ksj/" + code + "/ksj.dat"));
			map = (Map<Integer, List<Polygon>>) in.readObject();
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return map;
	}
	
}
