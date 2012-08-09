package map.sdf2500;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JComponent;

import jp.sourceforge.ma38su.util.Log;

import map.Curve;
import map.Road;

/**
 * 数値地図2500を読み込むためのクラス
 * @author ma38su
 */
public class ReaderSdf2500 {

	private final String CHARSET = "SJIS";

	public ReaderSdf2500() {
	}
	
	/**
	 * グラフをつなぎあわせて頂点数を減らします。
	 * 次数2の頂点は削除して、次数1と、次数3の頂点のみ残します。
	 * @param nodes つなぎあわせる頂点
	 * @param set
	 */
	private void jointNode(Collection<ArcNode> nodes, Collection<Arc> set) {
		Set<Arc> borderSet = new HashSet<Arc>();
		for (ArcNode node : nodes) {
			if (node.getBorder().size() == 2) {
				for (int i = 0; i < 4; i++) {
					Arc border = node.connect(i);
					if (border != null) {
						set.add(border);
					}
				}
			}
		}
		for (ArcNode node : nodes) {
			for (Arc border : node.getBorder()) {
				borderSet.add(border);
			}
		}
		for (Arc border : borderSet) {
			set.add(border);
		}
	}
	
	/**
	 * アークデータを読み込みます。
	 * @param file ファイル
	 * @return アークデータ
	 * @throws IOException 入出力エラー
	 */
	private Map<Integer, Arc> readArc(File file) throws IOException {
		BufferedReader in = null;
		Map<Integer, Arc> map = new HashMap<Integer, Arc>();
		Pattern csv = Pattern.compile(",");
		try {
			in = new BufferedReader(new FileReader(file));
			String line = in.readLine();
			String[] param = csv.split(line);
//			int flag = Integer.parseInt(param[5]);
//			int kei = Integer.parseInt(param[6]);
			double x0 = Double.parseDouble(param[8]); // 8
			double y0 = Double.parseDouble(param[9]); // 9
			int length = 0;
			double[] aryX = null;
			double[] aryY = null;
			int count = 0;
			int code = 0;
			int tag = 0;
			Integer id = null;
			while ((line = in.readLine()) != null) {
				param = csv.split(line);
				if (length == count) {
					if (count > 0 && code != 0) {
						map.put(id, new Arc(aryX, aryY, code, tag));
					}
					count = 0;
					code = Integer.parseInt(param[0].substring(1));
					tag = Integer.parseInt(param[1]); // 線種タグ
					id = Integer.valueOf(param[2]); // 個別番号
					length = Integer.parseInt(param[3]);
					aryX = new double[length];
					aryY = new double[length];
				} else {
					double x = x0 + Double.parseDouble(param[0]);
					double y = y0 + Double.parseDouble(param[1]);
					aryX[count] = x;
					aryY[count] = y;
					count++;
				}
			}
			// 最後のひとつを追加
			if (id != null && count == length && count > 0) {
				map.put(id, new Arc(aryX, aryY, code, tag));
			}
		} catch (IOException e) {
			map = null;
			throw e;
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return map;
	}
	
	/**
	 * 属性の読み込みを行います。
	 * @param file 属性ファイル
	 * @param map
	 */
	private void readAttribute(File file, Map<Integer, ? extends HasAttribute> map) {
		BufferedReader in = null;
		try {
			try {
				in = new BufferedReader(new InputStreamReader(new FileInputStream(file), this.CHARSET));
				Pattern csv = Pattern.compile(",");
				String line = in.readLine();
				while ((line = in.readLine()) != null) {
					String[] param = csv.split(line);
					int id = Integer.valueOf(param[1]);
					HasAttribute arc = map.get(id);
					int size = Integer.parseInt(param[2]);
					if (size > 0) {
						String[] attribute = new String[size];
						for (int i = 0; i < attribute.length && i < param.length - 3; i++) {
							attribute[i] = param[3 + i];
						}
						arc.setAttribute(attribute);
					}
				}
			} finally {
				if (in != null) {
					in.close();
				}
			}
		} catch (IOException e) {
			Log.err(this, e);
		}
	}

	/**
	 * 数値地図2500を読み込みます。
	 * @param code 市区町村番号
	 * @param set
	 * @param comp 
	 */
	public void readData(int code, Set<DataSdf2500> set, JComponent comp) {
	}
	
	private final String serializeDir = "sdf2500";

	public String getSerializeFilePath(String name) {
		return this.serializeDir + File.separatorChar + name.substring(0, 2) + File.separatorChar + name + ".obj";
	}

	/**
	 * 市区町村ポリゴン、街区ポリゴンの読み込み
	 * 外郭単位で読み込みます。
	 * @param data 読み込むデータ
	 * @param dir 読み込むディレクトリ
	 */
	void readGyousei(DataSdf2500 data, File dir) {
		ExtendedPolygon[] townPolygon = null;
		ExtendedPolygon[] cityPolygon = null;
		if (townPolygon == null || cityPolygon == null) {
			List<Curve> list = new ArrayList<Curve>();
			try {
				Collection<ExtendedPolygon> town = new ArrayList<ExtendedPolygon>();
				Collection<ExtendedPolygon> city = new ArrayList<ExtendedPolygon>();
				String path = dir.getCanonicalPath() + File.separatorChar + "gyousei" + File.separatorChar;
				File dataFile = new File(path + "gyousei.arc");
				if (dataFile.isFile()) {
					Map<Integer, Arc> arcs = this.readArc(dataFile);
					dataFile = new File(path + "tyome.pgn");
					Log.out(this, "read "+ dataFile);
					Map<Integer, ExtendedPolygon> map = this.readPolygon(dataFile, arcs);
					town.addAll(map.values());
					for (Arc arc : arcs.values()) {
						list.add(arc.transformCurve());
					}
					dataFile = new File(path + "si_tyo.pgn");
					Log.out(this, "read "+ dataFile);
					map = this.readPolygon(dataFile, arcs);
					city.addAll(map.values());
					for (Arc arc : arcs.values()) {
						list.add(arc.transformCurve());
					}
				}
				if (town.size() > 0) {
					townPolygon = town.toArray(new ExtendedPolygon[]{});
				}
				if (city.size() > 0) {
					cityPolygon = city.toArray(new ExtendedPolygon[]{});
				}
			} catch (IOException e) {
				Log.err(this, e);
				townPolygon = null;
				cityPolygon = null;
			}
		}
		if (cityPolygon != null || townPolygon != null) {
			data.setGyuosei(cityPolygon, townPolygon);
		}
	}

	/**
	 * 内水面ポリゴン、内水面アーク
	 * @param data
	 * @param dir
	 */
	void readMizu(DataSdf2500 data, File dir) {
		ExtendedPolygon[] mizuPgn = null;
		Curve[] mizuArc = null;
		if (mizuPgn == null || mizuArc == null) {
			Collection<Curve> arcList = new ArrayList<Curve>();
			Collection<ExtendedPolygon> pgnList = new ArrayList<ExtendedPolygon>();
			try {
				String path = dir.getCanonicalPath() + File.separatorChar + "mizu" + File.separatorChar;
				File dataFile = new File(path + "mizu.arc");
				if (dataFile.isFile()) {
					Map<Integer, Arc> arcs = this.readArc(dataFile);
					Log.out(this, "read "+ dataFile);
					dataFile = new File(path + "mizu.pgn");
					Map<Integer, ExtendedPolygon> map = this.readPolygon(dataFile, arcs);
					pgnList.addAll(map.values());
					Log.out(this, "read "+ dataFile);
					for (Arc arc : arcs.values()) {
						if (arc.getTag() < 4) {
							arcList.add(arc.transformCurve());
						}
					}
				}
				if (pgnList.size() > 0) {
					mizuPgn = pgnList.toArray(new ExtendedPolygon[]{});
				}
				if (arcList.size() > 0) {
					mizuArc = arcList.toArray(new Curve[]{});
				}
			} catch (IOException e) {
				Log.err(this, e);
				mizuPgn = null;
				mizuArc = null;
			}
		}
		if (mizuPgn != null && mizuArc != null) {
			data.setMizu(mizuArc, mizuPgn);
		}
	}

	void readOthers(DataSdf2500 data, File dir) {
		try {
			String path = dir.getCanonicalPath() + File.separatorChar + "others" + File.separatorChar;
			File dataFile = new File(path + "others.arc");
			if (dataFile.isFile()) {
				Map<Integer, Arc> arcs = this.readArc(dataFile);
				dataFile= new File(path + "tetudou.atr");
				if (dataFile.isFile()) {
					this.readAttribute(dataFile, arcs);
				}
				dataFile = new File(path + "zyouti.pgn");
				if (dataFile.isFile()) {
					readOthersPgn(data, path, dataFile, arcs);
				}
				readRailwayArc(data, arcs);
				ExtendedPoint[] stationPnt = readStation(path);
				if (stationPnt != null) {
					data.setStation(stationPnt);
				}
			}
		} catch (IOException e) {
			Log.err(this, e);
		}
	}

	/**
	 * 公園等場地ポリゴンを読み込みます。
	 * @param data 
	 * @param path
	 * @param dataFile
	 * @param arcs
	 * @throws IOException
	 */
	private void readOthersPgn(DataSdf2500 data, String path, File dataFile, Map<Integer, Arc> arcs) throws IOException {
		Collection<ExtendedPolygon> park = new ArrayList<ExtendedPolygon>();
		Collection<ExtendedPolygon> school = new ArrayList<ExtendedPolygon>();
		Collection<ExtendedPolygon> cemetery = new ArrayList<ExtendedPolygon>();
		Collection<ExtendedPolygon> shrine = new ArrayList<ExtendedPolygon>();
		Collection<ExtendedPolygon> other = new ArrayList<ExtendedPolygon>();
		Collection<ExtendedPolygon> railbase = new ArrayList<ExtendedPolygon>();
		Map<Integer, ExtendedPolygon> map = this.readPolygon(dataFile, arcs);
		dataFile = new File(path + "zyouti.atr");
		this.readAttribute(dataFile, map);
		for (ExtendedPolygon polygon : map.values()) {
			switch (polygon.getCode()) {
			case 6241: // 鉄道敷
				railbase.add(polygon);
				break;
			case 6242: // 都市公園
				park.add(polygon);
				break;
			case 6243: // 学校敷地
				school.add(polygon);
				break;
			case 6244: // 神社・寺院境内
				shrine.add(polygon);
				break;
			case 6215: // 墓地
				cemetery.add(polygon);
				break;
			case 6200: // その他の場地
				other.add(polygon);
				break;
			default:
				System.out.println(polygon.getCode());
			}
		}
		if (railbase.size() > 0) {
			data.setRailbase(railbase.toArray(new ExtendedPolygon[]{}));
		}
		if (park.size() > 0) {
			data.setPark(park.toArray(new ExtendedPolygon[]{}));
		}
		if (school.size() > 0) {
			data.setSchool(school.toArray(new ExtendedPolygon[]{}));
		}
		if (cemetery.size() > 0) {
			data.setCemetery(cemetery.toArray(new ExtendedPolygon[]{}));
		}
		if (shrine.size() > 0) {
			data.setShrine(shrine.toArray(new ExtendedPolygon[]{}));
		}
		if (other.size() > 0) {
			data.setOther(other.toArray(new ExtendedPolygon[]{}));
		}
	}

	private Map<Integer, ExtendedPoint> readPoint(File file) {
		BufferedReader in = null;
		Map<Integer, ExtendedPoint> map = new HashMap<Integer, ExtendedPoint>();
		try {
			Pattern csv = Pattern.compile(",");
			try {
				in = new BufferedReader(new InputStreamReader(new FileInputStream(file), this.CHARSET));
				String line = in.readLine();
				String[] param = csv.split(line);
				// int flag = Integer.parseInt(param[5]);
//				int kei = Integer.parseInt(param[6]);
				double x0 = Double.parseDouble(param[8]); // 8
				double y0 = Double.parseDouble(param[9]); // 9
				while ((line = in.readLine()) != null) {
					param = csv.split(line);
					if (Integer.parseInt(param[0].substring(1)) == 2420) {
						int id = Integer.parseInt(param[1]);
						Point2D p = new Point2D.Double(x0 + Double.parseDouble(param[2]), y0 + Double.parseDouble(param[3]));
						map.put(id, new ExtendedPoint((int)(p.getX() * 3600000 + 0.5), (int)(p.getY() * 3600000 + 0.5)));
					}
				}
			} finally {
				if (in != null) {
					in.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}

	
	
	/**
	 * ポリゴンデータを読み込みます。
	 * @param polygon 読み込むポリゴンデータ
	 * @param file 
	 * @param arcs アークデータ
	 * @return ポリゴンデータ
	 * @throws IOException 入出力エラー
	 */
	private Map<Integer, ExtendedPolygon> readPolygon(File file, Map<Integer, Arc> arcs) throws IOException {
		Map<Integer, ExtendedPolygon> map = new HashMap<Integer, ExtendedPolygon>();
		BufferedReader bi = null;
		try {
			bi = new BufferedReader(new InputStreamReader(new FileInputStream(file), this.CHARSET));
			// ヘッダを読み飛ばす
			bi.readLine();
			int length = 0;
			int count = 0;
			Integer id = null;
			Arc[] aryArc = null;
			Pattern csv = Pattern.compile(",");
			int code = 0;
			while (bi.ready()) {
				String line = bi.readLine();
				if (length == count) {
					if (count > 0 && id != null) {
						map.put(id, Arc.transformPolygon(code, aryArc));
					}
					String[] param = csv.split(line);
					count = 0;
					code = Integer.parseInt(param[0].substring(1));
					id = Integer.valueOf(param[1]);
					length = Integer.parseInt(param[2]);
					aryArc = new Arc[length];
				} else {
					int key = Integer.parseInt(line);
					Arc arc;
					if (key < 0) {
						arc = arcs.get(- key).reverse();
					} else {
						arc = arcs.get(key);
					}
					if (arc == null) {
						throw new IllegalArgumentException("エラー");
					}
					aryArc[count] = arc;
					count++;
				}
			}
			if (count > 0 && length == count && id != null) {
				map.put(id, Arc.transformPolygon(code, aryArc));
			}
		} catch (IOException e) {
			map.clear();
			throw e;
		} finally {
			if (bi != null) {
				bi.close();
			}
		}
		return map;
	}
	
	/**
	 * 鉄道アークを読み込みます。
	 * アークの属性は先に読み込んでおかなければなりません。
	 * 鉄道アークはできる限り少ないくなるようつなげます。
	 * つないだ後、JRとその他の鉄道を分けます。
	 * @param data 地図データ
	 * @param arcs アークのマップ
	 */
	private void readRailwayArc(DataSdf2500 data, Map<Integer, Arc> arcs) {
		Set<Arc> arcSet = new HashSet<Arc>();
		List<Curve> other = new ArrayList<Curve>();
		List<Curve> jr = new ArrayList<Curve>();
		Map<Point2D, ArcNode> nodeMap = new HashMap<Point2D, ArcNode>();
		for (Arc arc : arcs.values()) {
			switch (arc.getCode()) {
			case 2300: // 鉄道
				double[] curveX = arc.getArrayX();
				double[] curveY = arc.getArrayY();
				ArcNode p1 = new ArcNode(curveX[0], curveY[0]);
				ArcNode p2 = new ArcNode(curveX[curveX.length - 1], curveY[curveX.length - 1]);
				if (p1.equals(p2)) {
					arcSet.add(arc);
				} else {
					if (nodeMap.containsKey(p1)) {
						p1 = nodeMap.get(p1);
					} else {
						nodeMap.put(p1, p1);
					}
					if (nodeMap.containsKey(p2)) {
						p2 = nodeMap.get(p2);
					} else {
						nodeMap.put(p2, p2);
					}
					Arc b = p1.put(p2, arc);
					if (b != null) {
						arcSet.add(b);
					}
				}
				break;
			case 6241: // 鉄道敷
			case 6242: // 都市公園
			case 6243: // 学校敷地
			case 6244: // 神社・寺院境内
			case 6215: // 墓地
			case 6200: // その他の場地
				break;
			default:
				System.out.println(arc.getCode());
			}
		}
		this.jointNode(nodeMap.values(), arcSet);
		for (Arc arc : arcSet) {
			Railway rail = arc.transformRailway();
			if (rail.getType() == 0) {
				jr.add(rail);
			} else {
				other.add(rail);
			}
		}
		if (jr.size() > 0) {
			data.setJR(jr.toArray(new Railway[]{}));
		}
		if (other.size() > 0) {
			data.setRailway(other.toArray(new Railway[]{}));
		}
	}

	/**
	 * 道路線を読み込みます。
	 * @param data
	 * @param dir
	 */
	void readRoad(DataSdf2500 data, File dir) {
		Road[][] roads = null;
		if (roads == null) {
			int maxWidth = 4;
			List<List<Curve>> normal = new ArrayList<List<Curve>>(maxWidth);
			List<List<Curve>> main = new ArrayList<List<Curve>>(maxWidth);
			List<List<Curve>> highway = new ArrayList<List<Curve>>(maxWidth);
			for (int i = 0; i < maxWidth; i++) {
				normal.add(new ArrayList<Curve>());
				main.add(new ArrayList<Curve>());
				highway.add(new ArrayList<Curve>());
			}
			try {
				String path = dir.getCanonicalPath() + File.separatorChar + "road" + File.separatorChar;
				File arcFile = new File(path + "roadntwk.arc");
				if (arcFile.isFile()) {
					Map<Integer, Arc> arcs = this.readArc(arcFile);
					File atrFile = new File(path + "road.atr");
					if (atrFile.isFile()) {
						this.readAttribute(atrFile, arcs);
					}
					for (Arc arc : arcs.values()) {
						Road road = arc.transformRoad();
						if (road.getType() == 4) {
							highway.get(road.getWidth() - 1).add(road);
						} else if (road.getType() == 3) {
							main.get(road.getWidth() - 1).add(road);
						} else {
							normal.get(road.getWidth() - 1).add(road);
						}
					}
				}
				if ((highway.size() + main.size() + normal.size()) > 0) {
					roads = new Road[maxWidth * 3][];
					int index = 0;
					for (int i = 0; i < normal.size(); i++) {
						roads[i] = normal.get(i).toArray(new Road[]{});
					}
					index += normal.size();
					for (int i = 0; i < main.size(); i++) {
						roads[i + index] = main.get(i).toArray(new Road[]{});
					}
					index += main.size();
					for (int i = 0; i < highway.size(); i++) {
						roads[i + index] = highway.get(i).toArray(new Road[]{});
					}
				}
			} catch (IOException e) {
				Log.err(this, e);
				roads = null;
			}
		}
		if (roads != null) {
			data.setRoad(roads);
		}
	}

	/**
	 * 駅ポイントを読み込みます。
	 * @param path
	 * @return 駅ポイント
	 */
	private ExtendedPoint[] readStation(String path) {
		File dataFile = new File(path + "eki.pnt");
		if (dataFile.isFile()) {
			Map<Integer, ExtendedPoint> map = this.readPoint(dataFile);
			File atrFile = new File(path + "eki.atr");
			if (atrFile.isFile()) {
				this.readAttribute(atrFile, map);
			}
			if (map.size() > 0) {
				return map.values().toArray(new ExtendedPoint[]{});
			}
		}
		return null;
	}

	/**
	 * 建物ポリゴンを読み込みます。
	 * @param data
	 * @param file
	 * @return 読み込みに成功すればtrue、失敗すればfalseを返します。
	 */
	boolean readTatemono(DataSdf2500 data, File file) {
		List<ExtendedPolygon> tatemono = new ArrayList<ExtendedPolygon>();
		List<Curve> arcList = new ArrayList<Curve>();
		try {
			String path = file.getCanonicalPath() + File.separatorChar + "tatemono" + File.separatorChar;
			File dataFile = new File(path + "tatemono.arc");
			if (dataFile.isFile()) {
				Map<Integer, Arc> arcs = this.readArc(dataFile);
				dataFile = new File(path + "tatemono.pgn");
				Log.out(this, "read "+ dataFile);
				Map<Integer, ExtendedPolygon> map = this.readPolygon(dataFile, arcs);
				dataFile = new File(path + "tatemono.atr");
				this.readAttribute(dataFile, map);
				tatemono.addAll(map.values());
				for (Arc arc : arcs.values()) {
					if (arc.getTag() < 4) {
						arcList.add(arc.transformCurve());
					}
				}
			}
		} catch (IOException e) {
			Log.err(this, e);
			tatemono.clear();
		}
		if (tatemono.size() > 0) {
			data.setBuilding(tatemono.toArray(new ExtendedPolygon[]{}), arcList.toArray(new Curve[]{}));
			return true;
		}
		return false;
	}
}
