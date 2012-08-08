package map;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import database.FileDatabase;

import jp.sourceforge.ma38su.util.Log;

/**
 * 国土数値情報を読み込むクラス
 * @author ma38su
 */
public class ReaderKsj {
	public static void main(String[] args) throws IOException {
		FileDatabase db = new FileDatabase("d:/java/.digital_map/");
		ReaderKsj reader = new ReaderKsj(db);
		Polygon[][] polygons = new Polygon[47][];
		for (int i = 1; i <= 47; i++) {
			System.out.println("read: "+ i);
			List<Polygon> list = reader.readPrefecturePolygons(i);
			polygons[i - 1] = list.toArray(new Polygon[]{});
		}
		if (polygons != null) {
			ObjectOutputStream out = null;
			try {
				out = new ObjectOutputStream(new FileOutputStream("prefecture.dat"));
				out.writeObject(polygons);
				out.flush();
			} finally {
				if (out != null) {
					out.close();
					out = null;
				}
			}
			System.out.println("read finish");
		}
	}
	
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
			in = new ObjectInputStream(ReaderKsj.class.getResourceAsStream("/.data/" + code + "/ksj.dat"));
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
	
	/**
	 * AreaをPolygonのListに変換します。
	 * @param area 変換するAreaインスタンス
	 * @return 変換したPolygonのList
	 */
	public static List<Polygon> transformPolygon(Area area) {
		List<Polygon> list = new ArrayList<Polygon>();
		PathIterator itr = area.getPathIterator(new AffineTransform());
		List<Integer> x = new ArrayList<Integer>();
		List<Integer> y = new ArrayList<Integer>();
		while (!itr.isDone()) {
			double[] coords = new double[6];
			int type = itr.currentSegment(coords);
			switch (type) {
				case PathIterator.SEG_MOVETO: 
				{
					if (x.size() != y.size() || x.size() != 0) {
						throw new IllegalArgumentException("エラー");
					}
					x.add((int) (coords[0] + 0.5f));
					y.add((int) (coords[1] + 0.5f));
					break;
				}
				case PathIterator.SEG_CUBICTO:
				case PathIterator.SEG_QUADTO:
				case PathIterator.SEG_LINETO:
				{
					if (x.size() != y.size() || x.size() == 0) {
						throw new IllegalArgumentException("エラー");
					}
					x.add((int) (coords[0] + 0.5));
					y.add((int) (coords[1] + 0.5));
					break;
				}
				case PathIterator.SEG_CLOSE:
				{
					int[] aryX = new int[x.size()];
					int[] aryY = new int[y.size()];
					for (int i = 0; i < aryX.length; i++) {
						aryX[i] = x.get(i);
						aryY[i] = y.get(i);
					}
					list.add(new Polygon(aryX, aryY, aryX.length));
					x.clear();
					y.clear();
					break;
				}
				default:
					System.out.println("error");
			}
			itr.next();
		}
		return list;
	}
	
	/**
	 * 地図データファイルを管理するクラス
	 */
	private FileDatabase db;

	public ReaderKsj(FileDatabase db) {
		this.db = db;
	}
	
	/**
	 * 文字列を切り出してintに変換します。
	 * @param str 変換する文字列
	 * @param s 切り出す始点
	 * @param t 切り出す終点
	 * @return 変換後のint
	 */
	private int parseInt(String str, int s, int t) {
		return Integer.parseInt(str.substring(s, t).trim());
	}
	
	/**
	 * 国土数値情報の行政界を読み込みます。
	 * 行政界は都道府県単位で読み込みます。
	 * ファイルが存在しない場合には、国土数値情報から取得します。
	 * 
	 * @param code 都道府県番号
	 * @return 市区町村番号対応した行政界を持つMap
	 */
	public Map<Integer, List<Polygon>> readKsjBorder(int code) {
		Map<Integer, List<Polygon>> ksjMap = null;
		try {
			File file = this.db.getKsjBoder(code);
			ksjMap = this.readKsjFace(file);
		} catch (IOException e) {
			ksjMap = null;
		}
		return ksjMap;
	}

	/**
	 * 国土数値情報の面ポリゴンを読み込みます
	 * @param file 国土数値情報のファイル
	 * @return 市区町村番号に対応した面ポリゴン
	 * @throws IOException 入出力エラー
	 */
	public Map<Integer, List<Polygon>> readKsjFace(File file) throws IOException {
		Map<Integer, Map<Integer, Point[]>> points = new HashMap<Integer, Map<Integer, Point[]>>();
		Map<Integer, List<Polygon>> polygons = new LinkedHashMap<Integer, List<Polygon>>();
		BufferedReader bi = null;
		try {
			bi = new BufferedReader(new InputStreamReader(new FileInputStream(file), "SJIS"));
			String line;
			while((line = bi.readLine()) != null) {
				switch (line.charAt(0)) {
					case 'L': {
							final int mesh = this.parseInt(line, 3, 9);
							final int linkID = this.parseInt(line, 27, 33);
							int length = this.parseInt(line, 45, 51);
							Point[] np = new Point[length];
							Point[] rp = new Point[length];
							// 中間点データの読み込み
							int index = 0;
							int end = (length - 1) / 5 + 1;
							for(int j = 0; j < end; j++) {
								line = bi.readLine();
								StringTokenizer st = new StringTokenizer(line);
								while(st.hasMoreTokens()) {
									Point p = new Point(Integer.parseInt(st.nextToken()) * 100, Integer.parseInt(st.nextToken()) * 100);
									np[index] = p;
									rp[length - 1 - index] = p;
									index++;
								}
							}
							if(length != index) {
								throw new UnknownError("length:"+ length +", index:"+ index +"\n");
							}
							if(!points.containsKey(mesh)) {
								points.put(mesh, new HashMap<Integer, Point[]>());
							}
							points.get(mesh).put( linkID, np);
							points.get(mesh).put(-linkID, rp);
						break;
					}
					case 'A' : {
						final int code = this.parseInt(line, 40, 45);
						final int length = this.parseInt(line, 45, 51);
						int end = (length - 1) / 5 + 1;
						List<Integer> aryX = new ArrayList<Integer>();
						List<Integer> aryY = new ArrayList<Integer>();
						for (int j = 0; j < end; j++) {
							line = bi.readLine();
							for (int k = 0; k < 5; k++) {
								int index = k * 14;
								if(12 + index >= line.length()) {
									break;
								}
								int meshL = this.parseInt(line, 0 + index, 6 + index);
								int idL = this.parseInt(line, 6 + index, 12 + index);
								for(Point p : points.get(meshL).get(idL)) {
									aryX.add(p.x);
									aryY.add(p.y);
								}
							}
						}
						int[] x = new int[aryX.size()];
						int[] y = new int[aryY.size()];
						for (int j = 0; j < aryX.size(); j++) {
							x[j] = aryX.get(j);
							y[j] = aryY.get(j);
						}
						if(!polygons.containsKey(code)) {
							polygons.put(code, new ArrayList<Polygon>());
						}
						polygons.get(code).add(new Polygon(x, y, x.length));
						break;
					}
				}
			}
		} finally {
			if (bi != null) {
				bi.close();
			}
		}
		return polygons;
	}

	/**
	 * 都道府県界
	 * @param code 都道府県番号
	 * @return 都道府県界ポリゴン
	 */
	public List<Polygon> readPrefecturePolygons(int code) {
		Map<Integer, List<Polygon>> map = readKsjBorder(code);
		Log.out(this, "finish reading Ksj Polygon");
		List<Polygon> list = null;
		if (map != null) {
			Area area = new Area();
			for (List<Polygon> polygons : map.values()) {
				for (Polygon polygon : polygons) {
					area.add(new Area(polygon));
				}
			}
			list = transformPolygon(area);
		}
		Log.out(this, "finish making Ksj Prefecture Polygon");
		return list;
	}
}
