package map.sdf25k;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.sourceforge.ma38su.util.Log;

import labeling.Label;
import map.DataCity;
import map.Curve;
import map.Road;
import database.FileDatabase;

/**
 * 数値地図25000を読み込むためのクラス
 * @author ma38su
 */
public class ReaderSdf25k {

	public static String CODE = "SJIS";
	private final int DEFAULT_BORDER_CAPACITY = 20;
	private final int DEFAULT_MESH_CAPACITY = 20000;
	private final int DEFAULT_NODE_CAPACITY = 1000;
	private final int DEFAULT_SLP_CAPACITY = 20000;
	private final String KEY_BORDER = "GK.sal";
	private final String KEY_RIVER = "KK.sal";

	/**
	 * ファイル読み込みのための正規表現
	 */
	private final Pattern SAL_PARSER = Pattern.compile("([A-Z]{2})" + "(?:"
			+ "(?:" + "\\(" + "([^)]*)" + "\\)" + ")" + "|" + "\\{"
			+ "([^\\}\\{]*)" + "\\}" + ")" + "(?:" + "\\{" + "(?:" + "("
			+ "(?:" + "\\d{6},)*" + "\\d{6}" + ")" + "|" + "(" + "(?:"
			+ "[A-Z]{2}" + "\\(" + "[^\\)]*" + "\\)" + ")*" + ")" + ")" + "\\}"
			+ ")?");

	/**
	 * 必要なファイルを取得するためのクラス
	 */
	private final FileDatabase db;

	/**
	 * コンストラクタ
	 * 
	 * @param storage
	 *            数値地図25000のデータ取得管理クラス
	 * @throws IOException 
	 */
	public ReaderSdf25k(FileDatabase storage) {
		this.db = storage;
	}

	/**
	 * グラフをつなぎあわせて頂点数を減らします。
	 * 次数2の頂点は削除して、次数1と、次数3の頂点のみ残します。
	 * @param nodes つなぎあわせる頂点
	 * @param set
	 */
	private void jointNode(Collection<ExtractNode> nodes, Collection<Curve> set) {
		Set<Curve> borderSet = new HashSet<Curve>();
		for (ExtractNode node : nodes) {
			if (node.getBorder().size() == 2) {
				for (int i = 0; i < 4; i++) {
					Curve border = node.connect(i);
					if (border != null) {
						set.add(border);
					}
				}
			}
		}
		for (ExtractNode node : nodes) {
			for (Curve border : node.getBorder()) {
				borderSet.add(border);
			}
		}
		for (Curve border : borderSet) {
			set.add(border);
		}
	}

	/**
	 * 多角形面の向き
	 * 
	 * @param x
	 *            多角形面を構成するx座標の配列
	 * @param y
	 *            多角形面を構成するy座標の配列
	 * @return 多角形から計算した外積のZ座標の値
	 */
	int polygonDirection(int[] x, int[] y) {
		int vector = 0;
		for (int i = 1; i < x.length - 1; i++) {
			int dx0 = x[i] - x[0];
			int dy0 = y[i] - y[0];

			int dx1 = x[i + 1] - x[0];
			int dy1 = y[i + 1] - y[0];

			vector += dx1 * dy0 - dy1 * dx0;
		}
		return (vector >= 0) ? 1 : -1;
	}

	private String getSerializeFilePath(int code) {
		return "sdf25k" + File.separatorChar + DataCity.prefectureFormat(code / 1000) + File.separator + DataCity.cityFormat(code) + ".obj";
	}
	
	/**
	 * 指定した市区町村番号の数値地図25000を読み込みます。
	 * @param code 市区町村番号
	 * @return 数値地図25000
	 */
	public DataSdf25k readData(int code) {
		DataSdf25k data = null;
		String path = this.getSerializeFilePath(code);
		try {
			Object obj = this.db.readSerializable(path);
			if (obj instanceof DataSdf25k) {
				data = (DataSdf25k) obj;
			}
		} catch (Throwable e) {
			data = null;
		}
		try {
			if (data == null) {
				File dir = null;
				Rectangle area = this.readSLM(dir);
				data = new DataSdf25k(code, area);
				
				// 標高メッシュ */
				Mesh[][] mesh = this.readMesh(dir, area);
				data.setMesh(mesh);
				
				Point[] slp = this.readSLP(dir, area);
				
				// 行政界の読み込み
				Curve[] border = this.readBorder(dir, slp, this.KEY_BORDER);
				data.setBorder(border);
				
				// 水域界の読み込み
				Curve[] coast = this.readCoast(dir, slp);
				data.setCoast(coast);
				
				// 河川区間の読み込み
				Curve[] river = this.readBorder(dir, slp, this.KEY_RIVER);;
				data.setRiver(river);
				
				// 鉄道の読み込み
				Map<Integer, Curve> railMap = new HashMap<Integer, Curve>();
				Curve[][] rail = this.readRail(dir, slp, railMap);
				data.setRailway(rail[0], rail[1]);
				
				// 駅区間
				Station[] station = this.readStation(dir, railMap);
				data.setStation(station);
				
				// 道路接点の読み込み
				Map<Integer, Node> nodes = this.readNode(dir, slp);
				data.setNodes(nodes);
				
				// 道路区間の読み込み
				Road[][] road = this.readRoad(dir, slp, nodes, true);
				data.setRoad(road);
				
				Map<String, Label[]> label = new HashMap<String, Label[]>();
				label.put(Label.KEY_CM, this.readFacility(dir, slp, "CM.sal"));
				label.put(Label.KEY_KO, this.readFacility(dir, slp, "KO.sal"));
				data.setLabel(label);

				this.db.writeSerializable(path, data);
			}
		} catch (IOException e) {
			Log.err(this, e);
		}
		return data;
	}

	public Point readPoint(int code) throws IOException {
		File dir = null;
		Rectangle area = this.readSLM(dir);
		Point[] slp = this.readSLP(dir, area);
		return this.readRepresentative(dir, slp);
	}

	/**
	 * 境界の読み込み
	 * @param dir
	 * @param slp
	 * @param key
	 * @return 境界の配列
	 * @throws IOException 入出力エラー
	 */
	private Curve[] readBorder(File dir, Point[] slp, String key) {
		List<Curve> border = new ArrayList<Curve>(this.DEFAULT_BORDER_CAPACITY);
		try {
			File file = new File(dir.getCanonicalPath() + File.separatorChar + dir.getName() + key);
			if (file.isFile()) {
				BufferedReader bi = null;
				try {
					bi = new BufferedReader(new InputStreamReader(new FileInputStream(file), ReaderSdf25k.CODE));
					Pattern csv = Pattern.compile(",");
					while (bi.ready()) {
						String line = bi.readLine();
						Matcher matcher = this.SAL_PARSER.matcher(line.substring(17));
						int[] curveX = null;
						int[] curveY = null;
						while (matcher.find()) {
							String id = matcher.group(1);
							if (id.equals("CV")) {
								// カーブメモリ
								String[] ref = csv.split(matcher.group(4));
								curveX = new int[ref.length];
								curveY = new int[ref.length];
								for (int i = 0; i < ref.length; i++) {
									Point point = slp[Integer.parseInt(ref[i]) - 1];
									curveX[i] = point.x;
									curveY[i] = point.y;
								}
							}
						}
						Curve b = new Curve(curveX, curveY, 0);
						border.add(b);
					}
				} finally {
					if (bi != null) {
						bi.close();
					}
				}
			}
		} catch (Throwable e) {
			Log.err(this, e);
		}
		return border.toArray(new Curve[]{});
	}

	/**
	 * 水域界の読み込み
	 * @param seaborder 海岸線
	 * @param dir ファイルディレクトリ
	 * @param slp
	 * @return 水域界データ
	 */
	private Curve[] readCoast(File dir, Point[] slp) {
		List<Curve> coast = new ArrayList<Curve>();
		try {
			File file = new File(dir.getCanonicalPath() + File.separatorChar + dir.getName() + "SK.sal");
			if (file.isFile()) {
				BufferedReader bi = null;
				try {
					bi = new BufferedReader(new InputStreamReader(new FileInputStream(file), ReaderSdf25k.CODE));
					Pattern csv = Pattern.compile(",");
					while (bi.ready()) {
						String line = bi.readLine();
						Matcher matcher = this.SAL_PARSER.matcher(line.substring(17));
						int type = 0;
						int[] curveX = null;
						int[] curveY = null;
						while (matcher.find()) {
							String id = matcher.group(1);
							if (id.equals("CV")) {
								// カーブメモリ
								String[] ref = csv.split(matcher.group(4));
								curveX = new int[ref.length];
								curveY = new int[ref.length];
								for (int i = 0; i < ref.length; i++) {
									Point point = slp[Integer.parseInt(ref[i]) - 1];
									curveX[i] = point.x;
									curveY[i] = point.y;
								}
							} else if (id.equals("SR")) {
								/**
								 * 51 水涯線または湖岸線 52 海岸線 53 河口 54 湖沼と河川の境界
								 */
								type = Integer.parseInt(matcher.group(3)) - 51;
								if (type != 1) {
									type = 0;
								}
								if (type == 3) {
									Log.out(this, "SR 54 ? " + file);
								}
							}
						}
						coast.add(new Curve(curveX, curveY, type));
					}
				} finally {
					if (bi != null) {
						bi.close();
					}
				}
			}
		} catch (Throwable e) {
			Log.err(this, e);
		}
		return coast.toArray(new Curve[]{});
	}

	/**
	 * 施設情報の読み込み
	 * @param dir ファイルディレクトリ
	 * @param slp
	 * @param key 
	 * @return 施設情報配列
	 * @throws IOException 入出力エラー
	 */
	private Facility[] readFacility(File dir, Point[] slp, String key) {
		List<Facility> facility = new ArrayList<Facility>();
		try {
			File file = new File(dir.getCanonicalPath() + File.separatorChar + dir.getName() + key);
			BufferedReader bi = null;
			try {
				bi = new BufferedReader(new InputStreamReader(new FileInputStream(
						file), ReaderSdf25k.CODE));
				while (bi.ready()) {
					String line = bi.readLine();
					Matcher matcher = this.SAL_PARSER.matcher(line.substring(17));
					Point point = null;
					boolean isNM = false;
					String name = null;
					while (matcher.find()) {
						String param = matcher.group(1);
						if (param.equals("NM")) {
							name = matcher.group(3);
							isNM = true;
						} else if (param.equals("PT")) {
							point = slp[Integer.parseInt(matcher.group(4)) - 1];
						}
						if (isNM && point != null) {
							facility.add(new Facility(name, point.x, point.y));
						}
					}
				}
			} finally {
				if (bi != null) {
					bi.close();
				}
			}
		} catch (Throwable e) {
			Log.err(this, e);
		}
		return facility.toArray(new Facility[] {});
	}
	
	/**
	 * 行政代表点を取得します。
	 * @param dir 行政代表点のファイル
	 * @param slp
	 * @return 行政代表店
	 */
	private Point readRepresentative(File dir, Point[] slp) {
		Point point = null;
		try {
			File file = new File(dir.getCanonicalPath() + File.separatorChar + dir.getName() + "GD.sal");
			BufferedReader bi = null;
			try {
				bi = new BufferedReader(new InputStreamReader(new FileInputStream(file), ReaderSdf25k.CODE));
				while (bi.ready()) {
					String line = bi.readLine();
					Matcher matcher = this.SAL_PARSER.matcher(line.substring(17));
					while (matcher.find()) {
						String param = matcher.group(1);
						if (param.equals("PT")) {
							point = slp[Integer.parseInt(matcher.group(4)) - 1];
						}
					}
				}
			} finally {
				if (bi != null) {
					bi.close();
				}
			}
		} catch (Throwable e) {
			Log.err(this, e);
		}
		return point;
	}

	/**
	 * メッシュ標高の読み込み
	 * @param dir ファイルディレクトリ
	 * @param area 読み込む範囲
	 * @return メッシュ標高のリスト
	 */
	private Mesh[][] readMesh(File dir, Rectangle area) {
		Mesh[][] mesh = null;
		List<Point> points = new ArrayList<Point>(this.DEFAULT_MESH_CAPACITY);
		try {
			File slp = new File(dir.getCanonicalPath() + File.separatorChar + dir.getName() + "MH.slp");
			File sal = new File(dir.getCanonicalPath() + File.separatorChar + dir.getName() + "MH.sal");
			BufferedReader bi = null;
			try {
				bi = new BufferedReader(new FileReader(slp));
				while (bi.ready()) {
					String line = bi.readLine();
					int x = Integer.parseInt(line.substring(0, 7)) + area.x;
					int y = Integer.parseInt(line.substring(8, 15)) + area.y;
					points.add(new Point(x, y));
				}
			} finally {
				if (bi != null) {
					bi.close();
				}
			}
			try {
				mesh = new Mesh[area.width / Mesh.SIZE + 1][area.height / Mesh.SIZE + 1];
				bi = new BufferedReader(new InputStreamReader(new FileInputStream(sal), ReaderSdf25k.CODE));
				String line;
				while ((line = bi.readLine()) != null) {
					Matcher matcher = this.SAL_PARSER.matcher(line.substring(17));
					Point point = null;
					int height = 0;
					while (matcher.find()) {
						String param = matcher.group(1);
						if (param.equals("PT")) {
							point = points
							.get(Integer.parseInt(matcher.group(4)) - 1);
						} else if (param.equals("HK")) {
							height = Integer.parseInt(matcher.group(3));
						}
					}
					mesh[(point.x - area.x) / Mesh.SIZE][(point.y - area.y) / Mesh.SIZE] = new Mesh(height);
				}
			} finally {
				if (bi != null) {
					bi.close();
				}
			}
		} catch (Throwable e) {
			mesh = null;
			Log.err(this, e);
		}
		return mesh;
	}

	/**
	 * 接点の読み込み
	 * @param dir ファイルディレクトリ
	 * @param slp
	 * @return 頂点番号をkey、頂点をvalueとしたMap
	 */
	private Map<Integer, Node> readNode(File dir, Point[] slp) {
		Map<Integer, Node> node = new HashMap<Integer, Node>(this.DEFAULT_NODE_CAPACITY);
		try {
			File file = new File(dir.getCanonicalPath() + File.separatorChar + dir.getName() + "DS.sal");
			if (file.isFile()) {
				BufferedReader bi = null;
				try {
					bi = new BufferedReader(new InputStreamReader(new FileInputStream(file), ReaderSdf25k.CODE));
					String line;
					while ((line = bi.readLine()) != null) {
						Matcher matcher = this.SAL_PARSER.matcher(line.substring(17));
						String id = null;
						Point point = null;
						int slpindex = -1;
						while (matcher.find()) {
							String param = matcher.group(1);
							if (param.equals("PT")) {
								slpindex = Integer.parseInt(matcher.group(4)) - 1;
								point = slp[slpindex];
							} else if (param.equals("ND")) {
								id = matcher.group(2).substring(5, 11);
							}
						}
						if (point != null && id != null) {
							// 接続できてなかった頂点を調節（瀬戸大橋など）
							int code = Integer.parseInt(dir.getName());
							if (code == 40101) {
								if (id.equals("000985")) {
									point.setLocation(471453115, 122261236);
								} else if (id.equals("001020")) {
									point.setLocation(471457816, 122265788);
								}
							} else if (code == 38202 && id.equals("001867")) {
								point.setLocation(478795248, 122834185);
							}
							node.put(Integer.parseInt(id), new Node(Long.parseLong(code + id), point));
						}
					}
				} finally {
					if (bi != null) {
						bi.close();
					}
				}
			}
		} catch (Throwable e) {
			Log.err(this, e);
		}
		return node;
	}

	/**
	 * 水域界の読み込み
	 * @param dir 
	 * @param slp
	 * @param map
	 * @return 水域界データ
	 * @throws IOException 入出力エラー
	 */
	private Curve[][] readRail(File dir, Point[] slp, Map<Integer, Curve> map) {
		Collection<Curve> jr = new HashSet<Curve>();
		Collection<Curve> other = new HashSet<Curve>();
		Map<Point, ExtractNode> points = new HashMap<Point, ExtractNode>();
		try {
			File file = new File(dir.getCanonicalPath() + File.separatorChar + dir.getName() + "TK.sal");
			if (file.isFile()) {
				BufferedReader bi = null;
				try {
					bi = new BufferedReader(new InputStreamReader(new FileInputStream(file), ReaderSdf25k.CODE));
					Pattern csv = Pattern.compile(",");
					while (bi.ready()) {
						String line = bi.readLine();
						Matcher matcher = this.SAL_PARSER.matcher(line.substring(17));
						int type = 0;
						int[] curveX = null;
						int[] curveY = null;
						while (matcher.find()) {
							String id = matcher.group(1);
							if (id.equals("CV")) {
								// カーブメモリ
								String[] ref = csv.split(matcher.group(4));
								curveX = new int[ref.length];
								curveY = new int[ref.length];
								for (int i = 0; i < ref.length; i++) {
									Point point = slp[Integer.parseInt(ref[i]) - 1];
									curveX[i] = point.x;
									curveY[i] = point.y;
								}
							} else if (id.equals("SB")) {
								type = Integer.parseInt(matcher.group(3)) - 43;
							}
						}
						Curve border = new Curve(curveX, curveY, type);
						map.put(Integer.valueOf(line.substring(8, 14)), border);
						ExtractNode p1 = new ExtractNode(curveX[0], curveY[0]);
						ExtractNode p2 = new ExtractNode(curveX[curveX.length - 1],
								curveY[curveX.length - 1]);
						if (border.getType() != 0) {
							other.add(border);
						} else if (p1.equals(p2)) {
							jr.add(border);
						} else {
							if (points.containsKey(p1)) {
								p1 = points.get(p1);
							} else {
								points.put(p1, p1);
							}
							if (points.containsKey(p2)) {
								p2 = points.get(p2);
							} else {
								points.put(p2, p2);
							}
							Curve b = p1.put(p2, border);
							if (b != null) {
								jr.add(b);
							}
						}
					}
					this.jointNode(points.values(), jr);
				} finally {
					if (bi != null) {
						bi.close();
					}
				}
			}
		} catch (Throwable e) {
			Log.err(this, e);
		}
		return new Curve[][] { jr.toArray(new Curve[] {}), other.toArray(new Curve[] {}) };
	}
	
	/**
	 * 道路の読み込み
	 * @param dir ファイルディレクトリ
	 * @param slp slpファイルの読み込んだもの
	 * @param node
	 * @param isDetail
	 * @return 道路ファイル
	 * @throws IOException 入出力エラー
	 */
	private Road[][] readRoad(File dir, Point[] slp, Map<Integer, Node> node, boolean isDetail) {
		List<List<Curve>> border = new ArrayList<List<Curve>>(6);
		List<List<Curve>> border2 = new ArrayList<List<Curve>>(6);
		List<List<Curve>> highway = new ArrayList<List<Curve>>(6);
		if (isDetail) {
			for (int i = 0; i < 6; i++) {
				border.add(new ArrayList<Curve>());
				border2.add(new ArrayList<Curve>());
				highway.add(new ArrayList<Curve>());
			}
		}
		try {
			File file = new File(dir.getCanonicalPath() + File.separatorChar + dir.getName() + "DK.sal");
			if (file.isFile()) {
				int code = Integer.parseInt(dir.getName());
				BufferedReader in = null;
				try {
					Pattern csv = Pattern.compile(",");
					in = new BufferedReader(new InputStreamReader(new FileInputStream(file), ReaderSdf25k.CODE));
					while (in.ready()) {
						String line = in.readLine();
						Matcher matcher = this.SAL_PARSER.matcher(line.substring(17));
						int[] curveX = null;
						int[] curveY = null;
						int type = 0;
						int width = 0;
						int n0 = -1;
						int n1 = -1;
						String[] ref = null;
						double cost = 0;
						String name = null;
						while (matcher.find()) {
							String id = matcher.group(1);
							if (id.equals("CV")) {
								// カーブメモリ
								ref = csv.split(matcher.group(4));
								curveX = new int[ref.length];
								curveY = new int[ref.length];
								Point p0 = null;
								for (int i = 0; i < ref.length; i++) {
									Point point = slp[Integer.parseInt(ref[i]) - 1];
									curveX[i] = point.x;
									curveY[i] = point.y;
									if (i == 0) {
										p0 = point;
									} else {
										double dx = 6378137 * (double) (p0.x - point.x) / 3600000 / 180 * Math.PI * Math.cos((double) ((long) p0.y + (long) point.y) / 2 / 3600000 * Math.PI / 180);
										double dy = 6378137 * (double) (p0.y - point.y) / 3600000 / 180 * Math.PI;
										cost += Math.sqrt(dx * dx + dy * dy);
										p0 = point;
									}
								}
							} else if (id.equals("EG")) {
								String edge = matcher.group(5);
								n0 = Integer.parseInt(edge.substring(13, 19));
								n1 = Integer.parseInt(edge.substring(34, 40));
							} else if (id.equals("SB")) {
								type = Integer.parseInt(matcher.group(3)) - 13;
								if (type < 0) {
									type = 0;
								}
							} else if (id.equals("FI")) {
								// 17から16進数で与えられる
								width = Integer.parseInt(matcher.group(3), 16) - 23;
								if (width == 7) {
									width = 5;
								}
							} else if (id.equals("NM")) {
								name = matcher.group(3);
								if ("".equals(name)) {
									name = null;
								}
							}
						}
						Road b;
						if (type == 3 || name != null) {
							type += 1;
						}
						if (isDetail) {
							b = new Road(name, curveX, curveY, type, width, (float) cost);
						} else {
							b = new Road(type, width, (float) cost);
						}
						Node node0 = node.get(n0);
						Node node1 = node.get(n1);
						node0.connect(code * 1000000L + n1, b);
						node1.connect(code * 1000000L + n0, b);
						if (isDetail) {
							if (type == 4) {
								highway.get(width).add(b);
							} else if (type == 3){
								border2.get(width).add(b);
							} else {
								border.get(width).add(b);
							}
						}
					}
				} finally {
					if (in != null) {
						in.close();
					}
				}
				InputStream stream = this.db.getBoundaryNode(code);
				if (stream != null) {
					DataInputStream disc = null;
					try {
						disc = new DataInputStream(new BufferedInputStream(stream));
						while (disc.available() >= 12) {
							node.get(disc.readInt()).connect(disc.readLong(), null);
						}
					} finally {
						if (disc != null) {
							disc.close();
							disc = null;
							stream = null;
						}
						if (stream != null) {
							stream.close();
							stream = null;
						}
					}
				}
			} 
		} catch (Throwable e) {
			Log.err(this, e);
		}
		Road[][] road = new Road[18][];
		if (isDetail) {
			for (int i = 0; i < 6; i++) {
				road[i] = border.get(i).toArray(new Road[]{});
			}
			for (int i = 0; i < 6; i++) {
				road[i + 6] = border2.get(i).toArray(new Road[]{});
			}
			for (int i = 0; i < 6; i++) {
				road[i + 12] = highway.get(i).toArray(new Road[]{});
			}
			return road;
		} else {
			return null;
		}
	}

	/**
	 * 領域など基本情報の読み込み
	 * @param code 市区町村番号
	 * @param dir slmファイルの親ディレクトリ
	 * @return 市区町村の領域
	 * @throws IOException 入出力エラー
	 */
	private Rectangle readSLM(File dir) {
		Rectangle rect = null;
		if (dir.isDirectory()) {
			try {
				File file = new File(dir.getCanonicalPath() + File.separatorChar + dir.getName() + ".slm");
				if (file.isFile()) {
					Log.out(this, "read: "+ file.getCanonicalPath());
					BufferedReader bi = null;
					try {
						bi = new BufferedReader(new FileReader(file));
						// 原点読み込み
						StringTokenizer st = new StringTokenizer(bi.readLine(), ",");
						int x = (int) (Long.parseLong(st.nextToken()) / 10);
						int y = (int) (Long.parseLong(st.nextToken()) / 10);
						
						// 領域読み込み
						st = new StringTokenizer(bi.readLine(), ",");
						int width = Integer.parseInt(st.nextToken()) / 10;
						int height = Integer.parseInt(st.nextToken()) / 10;
						rect = new Rectangle(x, y, width, height);
					} finally {
						if (bi != null) {
							bi.close();
						}
					}
				}
			} catch (Throwable e) {
				Log.err(this, e);
			}
		}
		return rect;
	}

	/**
	 * SLPファイルの読み込み
	 * 地図を構成する座標を取得します。
	 * @param dir SLPファイルの親ディレクトリ
	 * @param area 読み込む範囲
	 * @return 座標
	 * @throws IOException 入出力エラー
	 */
	private Point[] readSLP(File dir, Rectangle area) {
		List<Point> slp = new ArrayList<Point>(this.DEFAULT_SLP_CAPACITY);
		if (dir.isDirectory()) {
			try {
				File file = new File(dir.getCanonicalPath() + File.separatorChar + dir.getName() + ".slp");
				if (file.isFile()) {
					BufferedReader bi = null;
					try {
						bi = new BufferedReader(new FileReader(file));
						String line;
						while ((line = bi.readLine()) != null) {
							int x = Integer.parseInt(line.substring(0, 7)) + area.x;
							int y = Integer.parseInt(line.substring(8, 15)) + area.y;
							slp.add(new Point(x, y));
						}
					} finally {
						if (bi != null) {
							bi.close();
						}
					}
				}
			} catch (Throwable e) {
				Log.err(this, e);
			}
		}
		return slp.toArray(new Point[] {});
	}

	/**
	 * 駅区間の読み込み
	 * @param dir
	 * @param railway 鉄道区間
	 * @return 駅区間の配列
	 * @throws IOException 入出力エラー
	 */
	private Station[] readStation(File dir, Map<Integer, Curve> railway) throws IOException {
		List<Station> station = new ArrayList<Station>();
		Map<String, List<Curve>> stationMap = new HashMap<String, List<Curve>>();
		BufferedReader bi = null;
		if (dir.isDirectory()) {
			File file = new File(dir.getCanonicalPath() + File.separatorChar + dir.getName() + "EK.sal");
			try {
				bi = new BufferedReader(new InputStreamReader(new FileInputStream(file), ReaderSdf25k.CODE));
				String line;
				while ((line = bi.readLine()) != null) {
					Matcher matcher = this.SAL_PARSER.matcher(line.substring(17));
					String name = null;
					Curve curve = null;
					while (matcher.find()) {
						String id = matcher.group(1);
						if (id.equals("NM")) {
							// 駅名
							name = matcher.group(3);
						} else if (id.equals("KN")) {
							curve = railway.get(Integer.valueOf(matcher.group(2).substring(10, 16)));
						}
					}
					List<Curve> cache = stationMap.get(name);
					if (cache == null) {
						List<Curve> list = new ArrayList<Curve>();
						list.add(curve);
						stationMap.put(name, list);
					} else {
						boolean isConnect = false;
						for (Curve c : cache) {
							if(c.connect(curve)) {
								isConnect = true;
								break;
							}
						}
						if (!isConnect) {
							cache.add(curve);
						}
					}
				}
			} finally {
				if (bi != null) {
					bi.close();
				}
			}
		}
		for (Map.Entry<String, List<Curve>> entry : stationMap.entrySet()) {
			boolean isConnect;
			do {
				isConnect = false;
				List<Curve> curves = entry.getValue();
				for (int i = 0; i < curves.size(); i++) {
					Curve curve = curves.get(i);
					ListIterator<Curve> itr = curves.listIterator(i + 1);
					while (itr.hasNext()) {
						if (curve.connect(itr.next())) {
							itr.remove();
							isConnect = true;
						}
					}
				}
			} while (isConnect);
			for (Curve curve : entry.getValue()) {
				station.add(new Station(entry.getKey(), curve));
			}
		}
		return station.toArray(new Station[] {});
	}
	
	
}
