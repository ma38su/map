package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.print.Printable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import util.Setting;

import labeling.Label;
import labeling.Labels;
import labeling.SimpleLabeling;
import map.DataCity;
import map.MapDataManager;
import map.Curve;
import map.Road;
import map.route.DirectDistance;
import map.route.RouteEntry;
import map.route.RouteNavigation;
import map.sdf2500.DataSdf2500;
import map.sdf2500.ExtendedPoint;
import map.sdf2500.ExtendedPolygon;
import map.sdf25k.DataSdf25k;
import map.sdf25k.Mesh;
import map.sdf25k.Node;

import jp.sourceforge.ma38su.gui.ExportableComponent;
import jp.sourceforge.ma38su.util.Log;

/**
 * 地図データを表示するためのパネル
 * @author ma38su
 */
public class MapPanel extends ExportableComponent implements Printable {

	/**
	 * フォントの種類（論理フォント）
	 * Serif, SansSerif, Monospaced, Dialog, DialogInput
	 */
	private static final String FONT_FAMILY = "SansSerif";

	private static final String FONT_FAMILY_CITY = "SansSerif";
	
	private static final String FONT_FAMILY_PREF = "Serif";
	
	/**
	 * ラベリングに用いるフォント
	 */
	public static final Font FONT_INFO = new Font(MapPanel.FONT_FAMILY, Font.PLAIN, 12);
	
	/**
	 * ラベリングに用いるフォント
	 */
	private static final Font FONT_LABEL = new Font(MapPanel.FONT_FAMILY, Font.PLAIN, 11);

	/**
	 * 駅のフォント
	 */
	private static final Font FONT_STATION = new Font(MapPanel.FONT_FAMILY, Font.BOLD, 14);
	
	/**
	 * 市区町村名表示フォントの最大サイズ
	 */
	private static final int FONTSIZE_CITY_MAX = 38;

	/**
	 * 都道府県名表示フォントの最大サイズ
	 */
	private static final int FONTSIZE_PREFECTURE_MAX = 60;

	/**
	 * 施設
	 */
	public static final int LABEL_FACILITY = 4;

	/**
	 * 地名
	 */
	public static final int LABEL_PLACE = 2;

	/**
	 * 市区町村名、都道府県名
	 */
	public static final int lABEL_PLACE_GOVT = 8;

	/**
	 * 駅名
	 */
	public static final int LABEL_STATION = 1;

	private static final int MAX_SCREEN_X = 154 * 3600000;

	private static final int MAX_SCREEN_Y = 46  * 3600000;
	
	private static final int MIN_SCREEN_X = 122 * 3600000;
	private static final int MIN_SCREEN_Y = 20  * 3600000;
	
	/**
	 * この倍率以下で国土数値情報の都道府県界を表示します。
	 */
	private static final float MODE_PREF_SCALE = 0.00025f;

	/**
	 * この倍率以下でFreeGISを表示します。
	 */
	private static final float MODE_WORLD_SCALE = 0.000020f;
	
	/**
	 * 表示倍率の上限
	 */
	private static final float SCALE_MAX = 0.1f;
	
	/**
	 * 表示倍率の下限
	 */
	private static final float SCALE_MIN = 0.000001f;

	/**
	 * 表示倍率の変更精度
	 */
	private static final float SCALE_SENSE = 0.08f;

	/**
	 * 道路区間，鉄道区間の描画の基本の幅
	 */
	private static final int STROKE_WIDTH = 75;

	private boolean isTsp = false;

	private Color COLOR_PARK;

	/**
	 * 市区町村境界のストローク
	 */
	private final Stroke border;

	/**
	 * ポリゴン描画のためのキャッシュ
	 */
	private List<Polygon> cachePolygon = new ArrayList<Polygon>();

	/**
	 * ポリゴン、折れ線描画のためのX座標のキャッシュ
	 */
	private int[] cacheX = new int[15000];

	/**
	 * ポリゴン、折れ線描画のためのY座標のキャッシュ
	 */
	private int[] cacheY = new int[15000];

	/**
	 * 市区町村の行政界
	 */
	private Color COLOR_CITY_BORDER;

	/**
	 * 国土数値情報による塗りつぶし色
	 */
	private Color COLOR_GROUND;

	/**
	 * 領域の境界
	 */
	private Color COLOR_GROUND_BORDER;

	/**
	 * 高速道路の塗りつぶし色
	 */
	private Color COLOR_HIGHWAY;

	/**
	 * 高速道路の境界色
	 */
	private Color COLOR_HIGHWAY_BORDER;

	/**
	 * 名称の（データが）ある道路の塗りつぶし色
	 */
	private Color COLOR_MAINROAD;

	/**
	 * 名称の（データが）ある道路の境界色
	 */
	private Color COLOR_MAINROAD_BORDER;

	/**
	 * JR以外の鉄道の塗りつぶし色
	 */
	private Color COLOR_OTHER_RAIL;

	private Color COLOR_ROAD;

	private Color COLOR_ROAD_BORDER;

	/**
	 * 一般道のルート色
	 */
	private Color COLOR_ROUTE;

	/**
	 * 高速道路のルート色
	 */
	private Color COLOR_ROUTE_HIGHWAY;
	
	/**
	 * 水域を塗りつぶす色
	 */
	private Color COLOR_SEA;

	private Color COLOR_SCHOOL;
	private Color COLOR_CEMETERY;
	private Color COLOR_SHRINE;
	private Color COLOR_OTHER;

	/**
	 * 水域の境界色
	 */
	private Color COLOR_SEA_BORDER;

	/**
	 * 駅の塗りつぶし色
	 */
	private Color COLOR_STATION;

	private Color COLOR_STATION_BORDER;

	/**
	 * 描画する道路のレベル
	 */
	private int drawlevel;
	
	/**
	 * アンチエイリアスのフラグ
	 */
	private boolean isAntialias;

	/**
	 * 経度・緯度の表示
	 */
	private boolean isAxis;

	/**
	 * 高速道路標示のフラグ
	 */
	private boolean isHighway;

	/**
	 * ラベル表示のフラグ
	 */
	private int isLabel;
	
	private boolean isLabelFailure;

	private Polygon[] island;

	/**
	 * メッシュ表示のフラグ
	 */
	private boolean isMesh;
	
	/**
	 * 頂点表示のフラグ
	 */
	private boolean isNodeView;

	/**
	 * マウス操作のフラグ
	 */
	private boolean isOperation;

	/**
	 * 鉄道表示のフラグ
	 */
	private boolean isRailway;
	
	/**
	 * 再描画フラグ
	 */
	private boolean isRepaint;

	/**
	 * 河川の表示
	 */
	private boolean isRiver;
	
	/**
	 * 道路の表示
	 */
	private boolean isRoadway;

	/**
	 * レンダリング
	 */
	private boolean isShadow;

	private boolean isTextAntialiasing;

	/**
	 * ラベリングアルゴリズム
	 */
	private SimpleLabeling labeling;

	private Polygon[] lake;

	/**
	 * 地図情報管理マップ
	 */
	private MapDataManager maps;
	
	/**
	 * 最短経路探索アルゴリズム
	 */
	private RouteNavigation navi;

	/**
	 * オフスクリーンイメージ
	 */
	private Image offs;
	
	/**
	 * 都道府県ポリゴン
	 */
	private Polygon[][] prefectures;
	
	/**
	 * 地図の表示倍率
	 */
	private float scale = 0.005f;
	
	/**
	 * スクリーンサイズ
	 */
	private Rectangle screen;

	private final int STROKE_CAP = BasicStroke.CAP_BUTT;

	private final int STROKE_JOIN = BasicStroke.JOIN_ROUND;

	/**
	 * 高速道路を許した経路探索を行う
	 */
	private boolean useHighway = true;

	/**
	 * 世界地図ポリゴン
	 */
	private Polygon[][] world;

	/**
	 * 世界地図の標準範囲
	 */
	private final Rectangle WORLD_SCREEN = new Rectangle(-648000000, -324000000, 1296000000, 648000000);

	private final Rectangle WORLD_SCREEN_EAST = new Rectangle(648000000, -324000000, 1296000000, 648000000);

	private Color COLOR_RAILBASE;
	
	/**
	 * 地図パネル
	 * @param db 市区町村番号データベース
	 * @param styledir スタイルファイルのディレクトリ
	 * @param ver バージョン
	 */
	public MapPanel(String styledir) {
		this.isShadow = true;
		this.isTextAntialiasing = true;
		this.border = new BasicStroke(0.5f);
		this.isAxis = true;
		this.isRailway = true;
		this.isMesh = true;
		this.isRiver = true;
		this.isRoadway = true;
		this.screen = new Rectangle();
		this.labeling = new SimpleLabeling(this.screen);
		this.isHighway = true;
		this.isLabelFailure = false;
		this.setDefaultStyle();
	}
	
	public void clearNavigation() {
		this.navi.clear();
	}
	/**
	 * 地図の描画
	 * @param g
	 */
	@Override
	public void draw(Graphics2D g) {
		this.labeling.init(g, this.scale, this.getWidth(), this.getHeight(), this.isTextAntialiasing, this.isShadow, this.isLabelFailure);

		Stroke defaultStroke = g.getStroke();
		String mode = null;
		switch (this.mode()) {
		case 0 :
			mode = "MODE: 世界地図";
			break;
		case 1 : 
			mode = "MODE: 国土数値情報（都道府県）";
			break;
		case 2 :
			mode = "MODE: 国土数値情報（都道府県＋市区町村）";
			break;
		case 3 :
			mode = "MODE: 数値地図25000";
			break;
		default :
			mode = "MODE: 数値地図2500";
		}
		g.setFont(MapPanel.FONT_INFO);
		this.labeling.set(mode, 5, 2);
		
		StringBuilder sb = new StringBuilder("SCALE: ");
		sb.append((int)(this.scale / 10 * 1000000 * 10) / 10f);
		sb.append("µ");
		this.labeling.set(sb.toString(), 5, 17);

		g.setColor(this.getBackground());
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		g.setColor(this.COLOR_SEA);
		this.drawBackground(g);

		this.setPrefectrueFont(g);

		g.setColor(this.COLOR_GROUND);
		this.fillPolygonWorld(g, this.world[0]);
		
		if (this.scale < MapPanel.MODE_WORLD_SCALE) {

			g.setColor(this.COLOR_GROUND);
			this.fillPolygonWorld(g, this.world[1]);
			if (!this.isOperation) {
				if (this.isAntialias) {
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				}
			}
		} else if(this.scale < MapPanel.MODE_PREF_SCALE) {
			if ((this.isLabel & MapPanel.lABEL_PLACE_GOVT) != 0) {
				for (int i = 0; i < this.prefectures.length; i++) {
					this.fillPrefectures(g, this.maps.getPrefecture(i), this.prefectures[i]);
				}
				this.fillPolygon(g, this.lake, this.COLOR_SEA, this.COLOR_SEA_BORDER);
				this.fillPolygon(g, this.island, this.COLOR_GROUND, this.COLOR_GROUND_BORDER);
			} else {
				for (int i = 0; i < this.prefectures.length; i++) {
					this.fillPolygon(g, this.prefectures[i], this.COLOR_GROUND, this.COLOR_GROUND_BORDER);
				}
				this.fillPolygon(g, this.lake, this.COLOR_SEA, this.COLOR_SEA_BORDER);
				this.fillPolygon(g, this.island, this.COLOR_GROUND, this.COLOR_GROUND_BORDER);
			}
			if (!this.isOperation) {
				if (this.isAntialias) {
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				}
			}
		} else {
			if (!this.isOperation) {
				if (this.isAntialias) {
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				}
			}
			DataCity[] cities = this.maps.getDataCity();
			g.setColor(this.COLOR_GROUND);

			g.setStroke(new BasicStroke(1.5f, this.STROKE_CAP, this.STROKE_JOIN));
			if ((this.isLabel & MapPanel.lABEL_PLACE_GOVT) != 0) {
				for (int i = 0; i < this.prefectures.length; i++) {
					this.fillPrefectures(g, this.maps.getPrefecture(i), this.prefectures[i]);
				}
				this.fillPolygon(g, this.lake, this.COLOR_SEA, this.COLOR_SEA_BORDER);
				this.fillPolygon(g, this.island, this.COLOR_GROUND, this.COLOR_GROUND_BORDER);
			} else {
				for (int i = 0; i < this.prefectures.length; i++) {
					this.fillPolygon(g, this.prefectures[i], this.COLOR_GROUND, this.COLOR_GROUND_BORDER);
				}
			}
			this.setCityFont(g);
			g.setStroke(this.border);
			for (DataCity city : cities) {
				this.drawBorder(g, city);
			}
			g.setStroke(defaultStroke);
			this.drawRoute(g);

		}

		this.drawRuler(g);
		g.setColor(Color.BLACK);
		if (this.isAxis) {
			this.drawAxis(g);
		}
		this.labeling.draw();
	}

	/**
	 * 経度緯度を描画します。
	 * @param g 
	 */
	private void drawAxis(Graphics2D g) {
		int step;
		switch (this.mode()) {
			case 0: step = 10;
			break;
			case 3: step = 1;
			break;
			default: step = 5;
		}
		step *= 3600000;
		int height = this.getHeight();
		int width = this.getWidth();
		int sy = height + (int)((324000000 + this.screen.y) * this.scale);
		int ey = height - (int)((324000000 - this.screen.y) * this.scale);
		for (int y = -324000000; y <= 324000000; y += step) {
			int ty = height - (int)((y - this.screen.y) * this.scale);
			if (ty < 0) {
				break;
			} else if (ty > height){
				continue;
			}
			g.drawLine(0, ty, width, ty);
		}
		for (int x = -648000000;; x += step) {
			int tx = (int)((x - this.screen.x) * this.scale);
			if (tx < 0) {
				continue;
			} else if (tx > width){
				break;
			}
			g.drawLine(tx, sy, tx, ey);
		}
	}
	
	/**
	 * 背景の描画
	 * @param g
	 */
	private void drawBackground(Graphics2D g) {
		int height = this.getHeight();
		int sy = height - (int)((90 * 3600000 - this.screen.y) * this.scale);
		if (sy < 0) {
			sy = 0;
		}
		int ey = height - (int)((-90 * 3600000 - this.screen.y) * this.scale);
		if (ey > height) {
			ey = height;
		}
		g.fillRect(0, sy, this.getWidth(), (ey - sy));
	}

	/**
	 * 市区町村の行政界と市区町村名を描画します。
	 * 塗りつぶしは行いません。
	 * @param g 描画するGraphics2D
	 * @param city 描画する行政界
	 */
	private void drawBorder(Graphics2D g, DataCity city) {
		Polygon[] polygons = city.getPolygon();
		if (polygons != null) {
			g.setColor(this.COLOR_GROUND_BORDER);
			for (Polygon polygon : polygons) {
				if(polygon.intersects(this.screen)) {
					int[] aryX = polygon.xpoints;
					int[] aryY = polygon.ypoints;

					for (int j = 0; j < polygon.npoints; j++) {
						this.cacheX[j] = (int)((aryX[j] - this.screen.x) * this.scale);
						this.cacheY[j] = this.getHeight() - (int)((aryY[j] - this.screen.y) * this.scale);
					}
					g.drawPolygon(this.cacheX, this.cacheY, polygon.npoints);
				}
			}
			g.setColor(Color.BLACK);
			String name = city.getName();
			if (name != null && (this.isLabel & MapPanel.lABEL_PLACE_GOVT) != 0) {
				int x = (int) ((city.getX() - this.screen.x) * this.scale);
				int y = this.getHeight() - (int) ((city.getY() - this.screen.y) * this.scale);
				this.labeling.add(name, x, y, true);
			}
		}
	}

	/**
	 * 曲線の描画
	 * 
	 * @param g 描画するGraphics
	 * @param curves 描画する行政界
	 */
	private void drawCurve(Graphics2D g, final Curve[] curves) {
		if (curves != null) {
			for (final Curve curve : curves) {
				final int[] aryX = curve.getArrayX();
				final int[] aryY = curve.getArrayY();
				int x0 = (int) ((aryX[0] - this.screen.x) * this.scale);
				int y0 = this.getHeight() - (int) ((aryY[0] - this.screen.y) * this.scale);
				for (int k = 1; k < aryX.length; k++) {
					int x = (int) ((aryX[k] - this.screen.x) * this.scale);
					int y = this.getHeight() - (int) ((aryY[k] - this.screen.y) * this.scale);
					// 表示領域内
					if (this.screen.intersectsLine(aryX[k - 1], aryY[k - 1], aryX[k], aryY[k])) {
						g.drawLine(x0, y0, x, y);
					}
					x0 = x;
					y0 = y;
				}
			}
		}
	}
	
	public void drawLabeling(Graphics2D g, DataCity[] data) {
		g.setFont(MapPanel.FONT_LABEL);
		synchronized (this.maps) {
			for (DataCity map : data) {
				Collection<Labels> labels = map.getLabels();
				for (Labels label : labels) {
					this.labeling.add(label);
				}
			}
		}
	}
	
	/**
	 * 線分を描画します。
	 * @param g 描画するGraphics2D
	 * @param x1 始点のX座標
	 * @param y1 始点のY座標
	 * @param x2 終点のX座標
	 * @param y2 終点のY座標
	 * @param line 境界色
	 */
	public void drawLine(Graphics2D g, int x1, int y1, int x2, int y2) {
		int tx1 = (int)((x1 - this.screen.x) * this.scale);
		int ty1 = this.getHeight() - (int)((y1 - this.screen.y) * this.scale);
		int tx2 = (int)((x2 - this.screen.x) * this.scale);
		int ty2 = this.getHeight() - (int)((y2 - this.screen.y) * this.scale);
		g.drawLine(tx1, ty1, tx2, ty2);
	}

	/**
	 * メッシュを描画します。
	 * @param g
	 * @param size サイズ
	 * @param area 描画範囲
	 * @param mesh メッシュ
	 */
	private void drawMesh(Graphics2D g, int size, Rectangle area, Mesh[][] mesh) {
		int meshX = area.x;
		for (int x = 0; x < mesh.length; x++) {
			int meshY = area.y;
			for (int y = 0; y < mesh[x].length; y++) {
				if (mesh[x][y] != null) {
					if (this.screen.intersects(meshX, meshY, Mesh.SIZE, Mesh.SIZE)) {
						int tx = (int) ((meshX - this.screen.x) * this.scale);
						int ty = this.getHeight() - (int) ((meshY + Mesh.SIZE - this.screen.y) * this.scale);
						g.setColor(mesh[x][y].getColor());
						g.fillRect(tx, ty, size, size);
					}
				}
				meshY += Mesh.SIZE;
			}
			meshX += Mesh.SIZE;
		}
	}
	
	public void drawOval(Graphics2D g, Point[] points, int r) {
		for (Point p : points) {
			int x = (int) ((p.getX() - this.screen.x) * this.scale) - r;
			int y = this.getHeight() - (int) ((p.getY() - this.screen.y) * this.scale) - r;
			g.setColor(this.COLOR_STATION);
			g.fillOval(x, y, r * 2, r * 2);
			g.setColor(this.COLOR_STATION_BORDER);
			g.drawOval(x, y, r * 2, r * 2);
		}
	}

	/**
	 * ポリゴンを描画します。
	 * @param g 描画するGraphics2D
	 * @param polygons 描画するポリゴン
	 */
	void drawPolygon(Graphics2D g, Polygon[] polygons) {
		if (polygons == null) {
			return;
		}
		for (Polygon polygon : polygons) {
			if(polygon.intersects(this.screen)) {
				int[] aryX = polygon.xpoints;
				int[] aryY = polygon.ypoints;

				for (int j = 0; j < polygon.npoints; j++) {
					this.cacheX[j] = (int)((aryX[j] - this.screen.x) * this.scale);
					this.cacheY[j] = this.getHeight() - (int)((aryY[j] - this.screen.y) * this.scale);
				}
				g.drawPolygon(this.cacheX, this.cacheY, polygon.npoints);
			}
		}
	}

	/**
	 * 折れ線を描画します。
	 * @param g 
	 * @param curves 曲線
	 */
	void drawPolyline(Graphics2D g, Curve[] curves) {
		if (curves != null) {
			for (Curve curve : curves) {
				int[] aryX = curve.getArrayX();
				int[] aryY = curve.getArrayY();
				for (int i = 0; i < aryX.length; i++) {
					this.cacheX[i] = (int) ((aryX[i] - this.screen.x) * this.scale);
					this.cacheY[i] = this.getHeight() - (int) ((aryY[i] - this.screen.y) * this.scale);
				}
				g.drawPolyline(this.cacheX, this.cacheY, aryX.length);
			}
		}
	}

	/**
	 * 折れ線の描画をおこないます。
	 * @param g
	 * @param listX
	 * @param listY
	 * @param color
	 * @param stroke
	 */
	private void drawPolyLine(Graphics2D g, List<int[]> listX, List<int[]> listY) {
		for (int i = 0; i < listX.size(); i++) {
			int[] aryX = listX.get(i);
			g.drawPolyline(aryX, listY.get(i), aryX.length);
		}
	}

	/**
	 * Rectangleを描画します。
	 * @param g 描画するGraphics2D
	 * @param rect 描画するRectangle
	 * @param polygons 描画するポリゴン
	 * @param bg 背景色
	 * @param line 境界色
	 */
	void drawRectangle(Graphics2D g, Rectangle rect) {
		int rectX = (int)((rect.x - this.screen.x) * this.scale);
		float height = rect.height * this.scale;
		int rectY = this.getHeight() - (int)((rect.y - this.screen.y) * this.scale + height);
		g.drawRect(rectX, rectY, (int)(rect.width * this.scale), (int) (height));
	}
	
	/**
	 * 道路の描画
	 * 
	 * @param g Graphics2D
	 * @param path 
	 * @param width 
	 */
	private void drawRoadway(Graphics2D g, GeneralPath[] path, int[] width) {
		if (this.isRoadway) {
			g.setColor(this.COLOR_ROAD_BORDER);
			for (int i = 0; i < width.length; i++) {
				if(path[i] != null) {
					g.setStroke(new BasicStroke(width[i] + 2, this.STROKE_CAP, this.STROKE_JOIN));
					g.draw(path[i]);
				}
			}
			g.setColor(this.COLOR_MAINROAD_BORDER);
			for (int i = 0; i < width.length; i++) {
				if(path[i] != null) {
					g.setStroke(new BasicStroke(width[i] + 2, this.STROKE_CAP, this.STROKE_JOIN));
					g.draw(path[i + width.length]);
				}
			}

			g.setColor(this.COLOR_ROAD);
			for (int i = 0; i < width.length; i++) {
				if(path[i] != null) {
					g.setStroke(new BasicStroke(width[i], this.STROKE_CAP, this.STROKE_JOIN));
					g.draw(path[i]);
				}
			}

			g.setColor(this.COLOR_MAINROAD);
			for (int i = 0; i < width.length; i++) {
				if(path[i] != null) {
					g.setStroke(new BasicStroke(width[i], this.STROKE_CAP, this.STROKE_JOIN));
					g.draw(path[i + width.length]);
				}
			}
		}
		
		if (this.isHighway) {
			g.setColor(this.COLOR_HIGHWAY_BORDER);
			for (int i = 0; i < width.length; i++) {
				g.setStroke(new BasicStroke(width[i] + 3, this.STROKE_CAP, this.STROKE_JOIN));
				g.draw(path[i + width.length * 2]);
			}
			g.setColor(this.COLOR_HIGHWAY);
			for (int i = 0; i < width.length; i++) {
				g.setStroke(new BasicStroke(width[i] + 1, this.STROKE_CAP, this.STROKE_JOIN));
				g.draw(path[i + width.length * 2]);
			}
		}
	}

	/**
	 * 探索済み頂点を描画する
	 * 
	 * @param g
	 */
	private void drawRoute(Graphics2D g) {
		List<RouteEntry> route = this.navi.getRoute();
		if (route != null) {
			int[] width = new int[12];
			for (int i = 0; i < 6; i++) {
				width[i] = (int)((i + 1) * MapPanel.STROKE_WIDTH * this.scale) + 1;
			}
			Iterator<RouteEntry> itr = route.iterator();
			while (itr.hasNext()) {
				RouteEntry entry = itr.next();
				for (Road road : entry.getRoute()) {
					int[] aryX = road.getArrayX();
					int[] aryY = road.getArrayY();
		
					int x0 = (int) ((aryX[0] - this.screen.x) * this.scale);
					int y0 = this.getHeight() - (int) ((aryY[0] - this.screen.y) * this.scale);

					if (road.getType() == 4) {
						g.setColor(this.COLOR_ROUTE_HIGHWAY);
					} else {
						g.setColor(this.COLOR_ROUTE);
					}
					g.setStroke(new BasicStroke(width[road.getWidth()], this.STROKE_CAP, this.STROKE_JOIN));
		
					for (int k = 1; k < aryX.length; k++) {
		
						int x = (int) ((aryX[k] - this.screen.x) * this.scale);
						int y = this.getHeight() - (int) ((aryY[k] - this.screen.y) * this.scale);

						// 表示領域内
						if (this.screen.intersectsLine(aryX[k - 1], aryY[k - 1], aryX[k], aryY[k])) {
							g.drawLine(x0, y0, x, y);
						}
						x0 = x;
						y0 = y;
					}
				}
			}
		}
		
		List<Node> terminal = this.navi.getTerminal();
		if (this.isTsp && terminal.size() >= 2) {
			terminal = new ArrayList<Node>();
			for (RouteEntry entry : route) {
				terminal.add(entry.getStart());
			}
		}
		if (terminal.size() > 0) {
			int r = (int) (this.scale * 500) + 2;
			g.setStroke(new BasicStroke(r / 2f, this.STROKE_CAP, this.STROKE_JOIN));
			for (int i = 0; i < terminal.size(); i++) {
				Node node = terminal.get(i);
				int x = (int) ((node.getX() - this.screen.x) * this.scale) - r;
				int y = this.getHeight() - (int) ((node.getY() - this.screen.y) * this.scale) - r;
				if (i == terminal.size() - 1) {
					g.setColor(Color.RED);
				} else {
					g.setColor(Color.YELLOW);
				}
				g.fillOval(x, y, r * 2, r * 2);
				g.setColor(Color.BLACK);
				g.drawOval(x, y, r * 2, r * 2);
				g.drawString(Integer.toString(i), x, y);
			}
		}
	}
	/**
	 * 数値地図2500を描画します。
	 * @param g
	 */
	void drawSdf2500(Graphics2D g) {
		Collection<DataSdf2500> sdf2500 = this.maps.getSdf2500(this.screen);
		g.setFont(MapPanel.FONT_LABEL);
		for (DataSdf2500 data : sdf2500) {
			ExtendedPoint[] points = data.getStation();
			if (points != null) {
				this.labeling.addCenter(points);
			}

			g.setColor(this.COLOR_GROUND);
			this.fillPolygon(g, data.getCityPgn());

			g.setColor(this.COLOR_RAILBASE);
			ExtendedPolygon[] railbase = data.getRailbase();
			if (railbase != null) {
				this.fillPolygon(g, railbase);
			}

			g.setColor(this.COLOR_PARK);
			ExtendedPolygon[] park = data.getPark();
			if (park != null) {
				this.fillPolygon(g, park);
				this.labeling.addCenter(park);
			}

			g.setColor(this.COLOR_SCHOOL);
			ExtendedPolygon[] school = data.getSchool();
			if (school != null) {
				this.fillPolygon(g, school);
				this.labeling.addCenter(school);
			}

			g.setColor(this.COLOR_SHRINE);
			ExtendedPolygon[] shrine = data.getShrine();
			if (shrine != null) {
				this.fillPolygon(g, shrine);
				this.labeling.addCenter(shrine);
			}

			g.setColor(this.COLOR_CEMETERY);
			ExtendedPolygon[] cemetery = data.getCemetery();
			if (cemetery != null) {
				this.fillPolygon(g, cemetery);
				this.labeling.addCenter(cemetery);
			}

			g.setColor(this.COLOR_OTHER);
			ExtendedPolygon[] other = data.getOther();
			if (other != null) {
				this.fillPolygon(g, other);
				this.labeling.addCenter(other);
			}


			g.setColor(this.COLOR_SEA);
			this.fillPolygon(g, data.getMizuPgn());
			
			g.setColor(this.COLOR_SEA_BORDER);
			this.drawCurve(g, data.getMizuArc());

		}
		{
			int[] roadWidth = new int[4];
			GeneralPath[] road = new GeneralPath[18];
			GeneralPath jr = new GeneralPath();
			List<int[]> otherX = new ArrayList<int[]>();
			List<int[]> otherY = new ArrayList<int[]>();
			for (int i = roadWidth.length - 1; i >= 0; i--) {
				float width = (i + 1) * MapPanel.STROKE_WIDTH * this.scale;
				if(width > 1) {
					roadWidth[i] = (int)(width);
					road[i] = new GeneralPath();
					this.drawlevel = i;
				} else if (width > 0.5f){
					roadWidth[i] = 1;
					road[i] = new GeneralPath();
					this.drawlevel = i;
				}
				road[i + roadWidth.length] = new GeneralPath();
				road[i + roadWidth.length * 2] = new GeneralPath();
			}
			for (DataSdf2500 data : sdf2500) {
				Road[][] roads = data.getRoad();
				for (int i = roads.length - 1; i >= 0; i--) {
					this.extractRoadway(road[i], roads[i]);
				}
				this.extractGeneralPath(data.getJR(), jr);
				this.extractPolyLine(data.getRailway(), otherX, otherY);
			}
			this.drawRoadway(g, road, roadWidth);
			if (this.isRailway) {
				int w = (int)(MapPanel.STROKE_WIDTH * this.scale * 4f + 0.5f);
				if (w == 0) {
					w = 1;
				}

				Stroke stroke = new BasicStroke(w + 2, this.STROKE_CAP, this.STROKE_JOIN);
				g.setColor(this.COLOR_OTHER_RAIL);
				g.setStroke(stroke);
				this.drawPolyLine(g, otherX, otherY);
				
				g.setColor(Color.BLACK);
				g.draw(jr);
				
				g.setColor(Color.WHITE);
				g.setStroke(new BasicStroke(w, this.STROKE_CAP, this.STROKE_JOIN, 10f, new float[]{w * 8, w * 8}, 0));
				g.draw(jr);
			}
		}
		g.setStroke(new BasicStroke(1));
		int r = (int) (5 * MapPanel.STROKE_WIDTH * this.scale);
		for (DataSdf2500 data : sdf2500) {
			ExtendedPolygon[] pgn = data.getBuildingPgn();
			Curve[] arc = data.getBuildingArc();
			if (pgn != null && arc != null) {
				g.setColor(Color.LIGHT_GRAY);
				this.fillPolygon(g, pgn);
				g.setColor(Color.GRAY);
				this.drawCurve(g, arc);
				this.labeling.addCenter(pgn);
			}
			ExtendedPoint[] points = data.getStation();
			if (points != null) {
				this.drawOval(g, points, r);
			}
		}
	}

	
	public void drawRuler(Graphics2D g) {
		g.setColor(Color.BLACK);
		int width = 100;
		int y = this.getHeight() - 20;
		int y1 = y - 5;
		int x = this.getWidth() - 20;
		int x0 = x - width;
		g.drawLine(x0, y, x0, y1);
		g.drawLine(x0, y, x, y);
		g.drawLine(x, y, x, y1);
		double distance = this.distance(x0, x, y);
		g.setFont(FONT_INFO);
		FontMetrics metrics = g.getFontMetrics();
		String rule;
		if (distance >= 1000) {
			rule = Integer.toString((int) (distance / 1000)) + "km";
		} else {
			rule = Integer.toString((int) distance) + "m";
		}
		int center = (x0 + x - metrics.stringWidth(rule)) / 2 + 5;
		g.drawString(rule, center, y -3);
	}
	
	public double distance(int x0, int x, int y) {
		double sx0 = x0 / this.scale + this.screen.x;
		double sx = x / this.scale + this.screen.x;
		double sy = (this.getHeight() - y) / this.scale + this.screen.y;
		return Math.abs(6378137 * (sx0 - sx) / 3600000 / 180 * Math.PI * Math.cos(sy / 3600000 * Math.PI / 180));
	}
	/**
	 * 数値地図を描画する
	 * @param g
	 */
	public void drawSdf25k(Graphics2D g) {
		Collection<DataSdf25k> sdf25k = this.maps.getSdf25k(this.screen);
		for (int code = 1; code <= 47; code++) {
			if (!this.maps.hasPrefecture(code)) {
				this.fillPrefectures(g, this.maps.getPrefecture(code - 1), this.prefectures[code - 1]);
			}
		}
		if (this.isMesh) {
			for (DataSdf25k data : sdf25k) {
				int meshSize = (int) (Mesh.SIZE * this.scale) + 1;
				Rectangle area = data.getArea();
				Mesh[][] mesh = data.getMesh();
				this.drawMesh(g, meshSize, area, mesh);
			}
		}

		for (DataSdf25k data : sdf25k) {
			if (this.isRiver) {
				g.setColor(this.COLOR_SEA_BORDER);
				this.drawCurve(g, data.getCoast());

				g.setColor(this.COLOR_SEA_BORDER);
				this.drawCurve(g, data.getRiver());
			}
		}
		{
			int[] roadWidth = new int[6];

			GeneralPath[] roadpath = new GeneralPath[18];
			for (int i = 5; i >= 0; i--) {
				float width = (i + 1) * MapPanel.STROKE_WIDTH * this.scale;
				if(width > 1) {
					roadWidth[i] = (int)(width);
					roadpath[i] = new GeneralPath();
					this.drawlevel = i;
				} else if (width > 0.5f){
					roadWidth[i] = 1;
					roadpath[i] = new GeneralPath();
					this.drawlevel = i;
				}
				roadpath[i + 6] = new GeneralPath();
				roadpath[i + 12] = new GeneralPath();
			}

			GeneralPath jr = new GeneralPath();
			List<int[]> otherX = new ArrayList<int[]>();
			List<int[]> otherY = new ArrayList<int[]>();
			List<int[]> stationX = new ArrayList<int[]>();
			List<int[]> stationY = new ArrayList<int[]>();

			for (DataSdf25k data : sdf25k) {
				Road[][] roads = data.getRoad();
				for (int i = 17; i >= 0; i--) {
					this.extractRoadway(roadpath[i], roads[i]);
				}
				this.extractGeneralPath(data.getJr(), jr);
				this.extractPolyLine(data.getRailway(), otherX, otherY);
				this.extractPolyLine(data.getStation(), stationX, stationY);
			}
			
			this.drawRoadway(g, roadpath, roadWidth);

			if (this.isRailway) {
				int w = (int)(MapPanel.STROKE_WIDTH * this.scale * 4f + 0.5f);
				if (w == 0) {
					w = 1;
				}

				Stroke stroke = new BasicStroke(w + 2, this.STROKE_CAP, this.STROKE_JOIN);
				g.setColor(this.COLOR_OTHER_RAIL);
				g.setStroke(stroke);
				this.drawPolyLine(g, otherX, otherY);
				
				g.setColor(Color.BLACK);
				g.draw(jr);
				
				g.setColor(Color.WHITE);
				g.setStroke(new BasicStroke(w, this.STROKE_CAP, this.STROKE_JOIN, 10f, new float[]{w * 8, w * 8}, 0.25f));
				g.draw(jr);

				w *= 3;
				g.setColor(this.COLOR_STATION_BORDER);
				g.setStroke(new BasicStroke(w + 2, BasicStroke.CAP_SQUARE, this.STROKE_JOIN));
				this.drawPolyLine(g, stationX, stationY);
				
				g.setColor(this.COLOR_STATION);
				g.setStroke(new BasicStroke(w, BasicStroke.CAP_SQUARE, this.STROKE_JOIN));
				this.drawPolyLine(g, stationX, stationY);
			}
		}
		g.setStroke(this.border);
		for (final DataSdf25k data : sdf25k) {
			g.setColor(this.COLOR_CITY_BORDER);
			this.drawCurve(g, data.getBorder());
		}

		this.drawRoute(g);
		g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		g.setFont(MapPanel.FONT_LABEL);
		if (this.isLabel > 0) {
			for (DataSdf25k map : sdf25k) {
				Map<String, Label[]> label = map.getLabel();
				if ((this.isLabel & MapPanel.LABEL_STATION) != 0) {
					Font df = g.getFont();
					g.setFont(MapPanel.FONT_STATION);
					this.labeling.addCenter(map.getStation());
					g.setFont(df);
				}
				if ((this.isLabel & MapPanel.LABEL_PLACE) != 0) {
					this.labeling.add(label.get(Label.KEY_CM));
				}
				if ((this.isLabel & MapPanel.LABEL_FACILITY) != 0) {
					this.labeling.add(label.get(Label.KEY_KO));
				}
			}
		}
	}

	/**
	 * GeneralPathに展開
	 * 
	 * @param curves 展開する曲線の配列
	 * @param path 展開されたGeneralPath
	 */
	private void extractGeneralPath(final Curve[] curves, final GeneralPath path) {
		if (curves != null) {
			for (final Curve curve : curves) {
				int[] aryX = curve.getArrayX();
				int[] aryY = curve.getArrayY();

				int x = (int) ((aryX[0] - this.screen.x) * this.scale);
				int y = this.getHeight() - (int) ((aryY[0] - this.screen.y) * this.scale);
				path.moveTo(x, y);

				for (int i = 1; i < aryX.length; i++) {
					x = (int) ((aryX[i] - this.screen.x) * this.scale);
					y = this.getHeight() - (int) ((aryY[i] - this.screen.y) * this.scale);
					path.lineTo(x, y);
				}
			}
		}
	}

	/**
	 * 折れ線に展開
	 * 
	 * @param curves
	 * @param polyX 
	 * @param polyY 
	 */
	private void extractPolyLine(final Curve[] curves, final List<int[]> polyX, final List<int[]> polyY) {
		if (curves != null) {
			for (final Curve curve : curves) {
				int[] aryX = curve.getArrayX();
				int[] aryY = curve.getArrayY();
				int[] lineX = new int[aryX.length];
				int[] lineY = new int[aryY.length];
				for (int i = 0; i < aryX.length; i++) {
					lineX[i] = (int) ((aryX[i] - this.screen.x) * this.scale);
					lineY[i] = this.getHeight() - (int) ((aryY[i] - this.screen.y) * this.scale);
				}
				polyX.add(lineX);
				polyY.add(lineY);
			}
		}
	}

	/**
	 * 道路データをGeneralPathに展開する
	 * @param path 展開先のGeneralPath
	 * @param curves 道路データ
	 */
	private void extractRoadway(GeneralPath path, Curve[] curves) {
		if (path != null && curves != null) {
			for (Curve curve : curves) {
				int[] aryX = curve.getArrayX();
				int[] aryY = curve.getArrayY();
	
				int x0 = (int) ((aryX[0] - this.screen.x) * this.scale);
				int y0 = this.getHeight() - (int) ((aryY[0] - this.screen.y) * this.scale);
	
				path.moveTo(x0, y0);
				
				for (int k = 1; k < aryX.length; k++) {
	
					int x = (int) ((aryX[k] - this.screen.x) * this.scale);
					int y = this.getHeight() - (int) ((aryY[k] - this.screen.y) * this.scale);

					// 表示領域外であれば次へ
					if (this.screen.intersectsLine(aryX[k - 1], aryY[k - 1], aryX[k], aryY[k])) {
						path.lineTo(x, y);
					} else {
						path.moveTo(x, y);
					}
				}
			}
		}
	}

	/**
	 * 市区町村の行政界を描画します。
	 * @param g 描画するGraphics2D
	 * @param city 描画する行政界
	 * @param bg 背景色
	 * @param line 境界色
	 */
	void fillBorder(Graphics2D g, DataCity city) {
		Polygon[] polygons = city.getPolygon();
		if (polygons != null) {
			Polygon[] tmp = new Polygon[polygons.length];
			g.setColor(this.COLOR_GROUND);
			for (int i = 0; i < polygons.length; i++) {
				if(polygons[i].getBounds().intersects(this.screen)) {
					int[] aryX = polygons[i].xpoints;
					int[] aryY = polygons[i].ypoints;
	
					int[] polygonX = new int[polygons[i].npoints];
					int[] polygonY = new int[polygons[i].npoints];
	
					for (int j = 0; j < polygons[i].npoints; j++) {
						polygonX[j] = (int)((aryX[j] - this.screen.x) * this.scale);
						polygonY[j] = this.getHeight() - (int)((aryY[j] - this.screen.y) * this.scale);
					}
					tmp[i] = new Polygon(polygonX, polygonY, polygonX.length);
					g.fillPolygon(tmp[i]);
				}
			}
			g.setColor(this.COLOR_GROUND_BORDER);
			for (Polygon p : tmp) {
				if (p != null) {
					g.drawPolygon(p);
				}
			}
			g.setColor(Color.BLACK);
			String name = city.getName();
			if (name != null && (this.isLabel & MapPanel.lABEL_PLACE_GOVT) != 0) {
				int x = (int) ((city.getX() - this.screen.x) * this.scale);
				int y = this.getHeight() - (int) ((city.getY() - this.screen.y) * this.scale);
				this.labeling.add(name, x, y, true);
			}
		}
	}

	/**
	 * ポリゴンを描画します。
	 * @param g 描画するGraphics2D
	 * @param polygons 描画するポリゴン
	 */
	private void fillPolygon(Graphics2D g, Polygon[] polygons) {
		if (polygons == null) {
			return;
		}
		for (Polygon polygon : polygons) {
			if(polygon.intersects(this.screen)) {
				int[] aryX = polygon.xpoints;
				int[] aryY = polygon.ypoints;

				for (int j = 0; j < polygon.npoints; j++) {
					this.cacheX[j] = (int)((aryX[j] - this.screen.x) * this.scale);
					this.cacheY[j] = this.getHeight() - (int)((aryY[j] - this.screen.y) * this.scale);
				}
				g.fillPolygon(this.cacheX, this.cacheY, polygon.npoints);
			}
		}
	}

	/**
	 * ポリゴンを描画します。
	 * @param g 描画するGraphics2D
	 * @param polygons 描画するポリゴン
	 * @param bg 背景色
	 * @param line 境界色
	 */
	void fillPolygon(Graphics2D g, Polygon[] polygons, Color bg, Color line) {
		if (polygons == null) {
			return;
		}
		g.setColor(bg);
		for (Polygon polygon : polygons) {
			if(polygon.intersects(this.screen)) {
				int[] aryX = polygon.xpoints;
				int[] aryY = polygon.ypoints;
				for (int j = 0; j < polygon.npoints; j++) {
					this.cacheX[j] = (int)((aryX[j] - this.screen.x) * this.scale);
					this.cacheY[j] = this.getHeight() - (int)((aryY[j] - this.screen.y) * this.scale);
				}
				Polygon p = new Polygon(this.cacheX, this.cacheY, polygon.npoints);
				this.cachePolygon.add(new Polygon(this.cacheX, this.cacheY, polygon.npoints));
				g.fillPolygon(p);
			}
		}
		// 境界を描画つぶします。
		g.setColor(line);
		for (Polygon p : this.cachePolygon) {
			g.drawPolygon(p);
		}
		this.cachePolygon.clear();
	}

	/**
	 * ポリゴンを描画します。
	 * @param g 描画するGraphics2D
	 * @param polygons 描画するポリゴン
	 */
	private void fillPolygonWorld(Graphics2D g, Polygon[] polygons) {
		if (polygons == null) {
			return;
		}
		for (int i = 0; i < polygons.length; i++) {
			if(polygons[i].intersects(this.screen)) {
				int[] aryX = polygons[i].xpoints;
				int[] aryY = polygons[i].ypoints;
								
				for (int j = 0; j < polygons[i].npoints; j++) {
					this.cacheX[j] = (int)((aryX[j] - this.screen.x) * this.scale);
					this.cacheY[j] = this.getHeight() - (int)((aryY[j] - this.screen.y) * this.scale);
				}
				Polygon p = new Polygon(this.cacheX, this.cacheY, polygons[i].npoints);
				g.fillPolygon(p);
				this.cachePolygon.add(p);
			}
		}
		if (!this.WORLD_SCREEN.contains(this.screen)) {
			if (this.WORLD_SCREEN_EAST.intersects(this.screen)) {
				Rectangle screen = new Rectangle(this.screen);
				screen.x -= this.WORLD_SCREEN.width;
				long screenX = 1296000000 - this.screen.x;
				for (Polygon polygon : polygons) {
					if(polygon.intersects(screen)) {
						int[] aryX = polygon.xpoints;
						int[] aryY = polygon.ypoints;
						for (int j = 0; j < polygon.npoints; j++) {
							float x = (aryX[j] + screenX) * this.scale;
							this.cacheX[j] = (x >= Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) x;
							this.cacheY[j] = this.getHeight() - (int)((aryY[j] - this.screen.y) * this.scale);
						}
						Polygon p = new Polygon(this.cacheX, this.cacheY, polygon.npoints);
						g.fillPolygon(p);
						this.cachePolygon.add(p);
					}
				}
			}
		}
		g.setColor(this.COLOR_GROUND_BORDER);
		for (Polygon p : this.cachePolygon) {
			g.drawPolygon(p);
		}
		this.cachePolygon.clear();
	}

	/**
	 * ポリゴンを描画します。
	 * @param g 描画するGraphics2D
	 * @param name 名前
	 * @param prefecture 描画するポリゴン
	 */
	private void fillPrefectures(Graphics2D g, String name, Polygon[] prefecture) {
		g.setColor(this.COLOR_GROUND);
		int maxN = 0;
		int lx = 0;
		int ly = 0;
		for (Polygon polygon : prefecture) {
			if(polygon.intersects(this.screen)) {
				int centerX = 0;
				int centerY = 0;
				int[] aryX = polygon.xpoints;
				int[] aryY = polygon.ypoints;
				for (int j = 0; j < polygon.npoints; j++) {
					this.cacheX[j] = (int)((aryX[j] - this.screen.x) * this.scale);
					this.cacheY[j] = this.getHeight() - (int)((aryY[j] - this.screen.y) * this.scale);
					centerX += this.cacheX[j];
					centerY += this.cacheY[j];
				}
				if (maxN < polygon.npoints) {
					maxN = polygon.npoints;
					lx = centerX / maxN;
					ly = centerY / maxN;
				}
				Polygon p = new Polygon(this.cacheX, this.cacheY, polygon.npoints);
				this.cachePolygon.add(new Polygon(this.cacheX, this.cacheY, polygon.npoints));
				g.fillPolygon(p);
			}
		}
		g.setColor(this.COLOR_GROUND_BORDER);
		for (Polygon p : this.cachePolygon) {
			g.drawPolygon(p);
		}
		if (maxN > 0) {
			this.labeling.add(name, lx, ly, false);
		}
		this.cachePolygon.clear();
	}

	public double getLocationX(int x) {
		return (this.screen.x + x / this.scale) / 3600000;
	}
	
	public double getLocationY(int y) {
		return (this.screen.y + (this.getHeight() - y) / this.scale) / 3600000;
	}

	/**
	 * スクリーン情報の取得
	 * 
	 * @return スクリーンのRectangle
	 */
	public Rectangle getScreen() {
		return this.screen;
	}
	
	/**
	 * 初期設定
	 * @param setting 設定
	 * @param map 地図データ管理クラス
	 * @param world 世界（日本以外）国境ポリゴン
	 * @param japan 日本国境ポリゴン
	 * @param prefectures 都道府県ポリゴン
	 * @param lake 都道府県ポリゴンと重なる水域ポリゴン
	 * @param island 水域ポリゴンと重なる島などのポリゴン
	 */
	public void init(MapDataManager map, Polygon[][] world, Polygon[][] prefectures, Polygon[] lake, Polygon[] island) {
		Log.out(this, "init called.");
		this.navi = new RouteNavigation(map);
		this.navi.setFunction(new DirectDistance());

		this.maps = map;
		this.maps.start();
		
		this.world = world;
		this.prefectures = prefectures;
		this.lake = lake;
		this.island = island;
		this.isLabel = 15;
		this.isNodeView = false;
		this.isAntialias = true;
		this.isOperation = false;
		this.moveDefault();
		this.repaint();
	}

	/**
	 * 地図データの読み込みを確認します。
	 * @return 地図データを読み込んでいればtrueを返します。
	 */
	public boolean isLoaded() {
		return this.maps != null;
	}
	
	/**
	 * 操作しているかどうか確認します。
	 * @return 操作していればtrue
	 */
	public boolean isOperation() {
		return this.isOperation;
	}

	/**
	 * 地図の表示状態を確認します。
	 * <ul>
	 * <li>4 : 数値地図2500</li>
	 * <li>3 : 数値地図25000</li>
	 * <li>2 : 国土数値情報（市区町村）</li>
	 * <li>1 : 国土数値情報（都道府県）</li>
	 * <li>0 : FreeGIS（世界地図）</li>
	 * </ul>
	 * @return 地図の表示状態
	 */
	public int mode() {
		if (this.scale > MapPanel.MODE_PREF_SCALE) {
			return 2;
		} else if (this.scale > MapPanel.MODE_WORLD_SCALE) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * 表示位置を初期値へ
	 */
	public void moveDefault() {
		double widthScale = (double) this.getWidth() / (MapPanel.MAX_SCREEN_X - MapPanel.MIN_SCREEN_X);
		double heightScale = (double) this.getHeight() / (MapPanel.MAX_SCREEN_Y - MapPanel.MIN_SCREEN_Y);
		this.scale = (float) ((widthScale < heightScale) ? widthScale : heightScale);
		this.screen.x = MapPanel.MIN_SCREEN_X;
		this.screen.y = MapPanel.MIN_SCREEN_Y;
		this.screen.width = (int) (this.getWidth() / this.scale);
		this.screen.height = (int) (this.getHeight() / this.scale);
	}
	
	/**
	 * 表示位置を平行移動をさせます。
	 * @param dx X軸方向の移動量
	 * @param dy Y軸方向の移動量
	 */
	public void moveLocation(int dx, int dy) {
		this.screen.x -= dx / this.scale;
		this.screen.y += dy / this.scale;
		if (this.screen.x < this.WORLD_SCREEN.x) {
			this.screen.x += this.WORLD_SCREEN.width;
		} else if (this.screen.x > this.WORLD_SCREEN.x + this.WORLD_SCREEN.width) {
			this.screen.x -= this.WORLD_SCREEN.width;
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		if(this.maps == null) {
			this.isRepaint = false;
			this.offs = this.createImage(this.getWidth(), this.getHeight());
			Graphics2D offg = (Graphics2D) this.offs.getGraphics();
			super.paintComponent(offg);
			// オフスクリーンバッファ
			offg.setFont(new Font(MapPanel.FONT_FAMILY, Font.PLAIN, 20));
			String msg = "Now Loading...";
			FontMetrics metrics = offg.getFontMetrics();
			offg.drawString(msg, (this.getWidth() - metrics.stringWidth(msg)) / 2, (this.getHeight() - metrics.getHeight()) / 2);
			offg.dispose();
		} else if (this.isRepaint) {
			this.isRepaint = false;
			if (this.offs == null) {
				this.offs = this.createImage(this.getWidth(), this.getHeight());
			}
			if (this.getWidth() != this.offs.getWidth(null) || this.getHeight() != this.offs.getHeight(null)) {
				int centerX = this.screen.x + this.screen.width / 2;
				int centerY = this.screen.y + this.screen.height / 2;
				// Windowサイズが変わった
				this.offs.flush();
				// オフスクリーンバッファ
				this.offs = this.createImage(this.getWidth(), this.getHeight());
				this.setMapLocation(centerX, centerY);
			}
			Graphics2D offg = (Graphics2D) this.offs.getGraphics();
			this.draw(offg);
			offg.dispose();
		}
		g.drawImage(this.offs, 0, 0, null);
	}

	public void readStyle(String stylefile) {
		File file = new File(stylefile);
		if (file.isFile()) {
			Setting setting = new Setting(file);
			try {
				Color CITY_BORDER = new Color(Integer.parseInt(setting.get("CITY_BORDER"), 16));
				Color GROUND = new Color(Integer.parseInt(setting.get("GROUND"), 16));
				Color GROUND_BORDER = new Color(Integer.parseInt(setting.get("GROUND_BORDER"), 16));
				Color HIGHWAY = new Color(Integer.parseInt(setting.get("HIGHWAY"), 16));
				Color HIGHWAY_BORDER = new Color(Integer.parseInt(setting.get("HIGHWAY_BORDER"), 16));
				Color MAINROAD = new Color(Integer.parseInt(setting.get("MAINROAD"), 16));
				Color MAINROAD_BORDER = new Color(Integer.parseInt(setting.get("MAINROAD_BORDER"), 16));
				Color OTHER_RAIL = new Color(Integer.parseInt(setting.get("OTHER_RAIL"), 16));
				Color ROUTE = new Color(Integer.parseInt(setting.get("ROUTE"), 16));
				Color ROUTE_HIGHWAY = new Color(Integer.parseInt(setting.get("ROUTE_HIGHWAY"), 16));
				Color SEA = new Color(Integer.parseInt(setting.get("SEA"), 16));
				Color SEA_BORDER = new Color(Integer.parseInt(setting.get("SEA_BORDER"), 16));
				Color STATION = new Color(Integer.parseInt(setting.get("STATION"), 16));
				Color STATION_BORDER = new Color(Integer.parseInt(setting.get("STATION_BORDER"), 16));
				Color ROAD = new Color(Integer.parseInt(setting.get("ROAD"), 16));
				Color ROAD_BORDER = new Color(Integer.parseInt(setting.get("ROAD_BORDER"), 16));
				Color SCHOOL = new Color(Integer.parseInt(setting.get("SCHOOL"), 16));
				Color CEMETERY = new Color(Integer.parseInt(setting.get("CEMETERY"), 16));
				Color SHRINE = new Color(Integer.parseInt(setting.get("SHRINE"), 16));
				Color RAILBASE = new Color(Integer.parseInt(setting.get("RAILBASE"), 16));
				Color OTHER = new Color(Integer.parseInt(setting.get("OTHER"), 16));
				Color PARK = new Color(Integer.parseInt(setting.get("PARK"), 16));
				this.COLOR_CITY_BORDER = CITY_BORDER;
				this.COLOR_GROUND = GROUND;
				this.COLOR_GROUND_BORDER = GROUND_BORDER;
				this.COLOR_HIGHWAY = HIGHWAY;
				this.COLOR_HIGHWAY_BORDER = HIGHWAY_BORDER;
				this.COLOR_MAINROAD = MAINROAD;
				this.COLOR_MAINROAD_BORDER = MAINROAD_BORDER;
				this.COLOR_OTHER_RAIL = OTHER_RAIL;
				this.COLOR_ROUTE = ROUTE;
				this.COLOR_ROUTE_HIGHWAY = ROUTE_HIGHWAY;
				this.COLOR_SEA = SEA;
				this.COLOR_SEA_BORDER = SEA_BORDER;
				this.COLOR_STATION = STATION;
				this.COLOR_STATION_BORDER = STATION_BORDER;
				this.COLOR_ROAD = ROAD;
				this.COLOR_ROAD_BORDER = ROAD_BORDER;
				this.COLOR_PARK = PARK;
				this.COLOR_OTHER = OTHER;
				this.COLOR_RAILBASE = RAILBASE;
				this.COLOR_SHRINE = SHRINE;
				this.COLOR_CEMETERY = CEMETERY;
				this.COLOR_SCHOOL = SCHOOL;
			} catch (Throwable e) {
				this.setDefaultStyle();
				throw new IllegalArgumentException("スタイルファイルが異常です", e);
			}
		}
	}
	
	@Override
	public void repaint() {
		if (this.maps != null) {
			this.maps.wakeup();
		}
		this.isRepaint = true;
		super.repaint();
	}

	/**
	 * ルートの再計算を行います。
	 */
	public void reroute() {
		this.navi.reroute();
	}

	/**
	 * 探索端点の変更を行います。
	 * @param ex マウスのX座標
	 * @param ey マウスのY座標
	 * @param flag trueなら終点の変更、falseなら始点の変更
	 */
	public void searchBoundary(final int ex, final int ey, final boolean flag) {
		int x = (int)(ex / this.scale + this.screen.x);
		int y = (int)((this.getHeight() - ey) / this.scale + this.screen.y);
		Node point = null;
		double nodeDist = Double.POSITIVE_INFINITY;
		if (this.mode() == 3) {
			for (DataSdf25k data : this.maps.getSdf25k(this.screen)) {
				for (final Node node : data.getNodes().values()) {
					final double d = node.distance(x, y, this.drawlevel);
					if (nodeDist > d) {
						point = node;
						nodeDist = d;
					}
				}
			}
		}
		if (point != null) {
			if (flag) {
				this.navi.clear();
			}
			this.navi.put(point);
		}
	}

	/**
	 * 市区町村表示のフォントを設定します。
	 * @param g
	 */
	private void setCityFont(Graphics2D g) {
		int fontSize = 11 + (int)((this.scale - MapPanel.MODE_PREF_SCALE) * 4000);
		if (fontSize > MapPanel.FONTSIZE_CITY_MAX) {
			fontSize = MapPanel.FONTSIZE_CITY_MAX;
		}
		if (this.mode() == 3) {
			g.setFont(new Font("Serif", Font.PLAIN, fontSize + 7));
		} else {
			g.setFont(new Font(MapPanel.FONT_FAMILY_CITY, Font.PLAIN, fontSize));
		}
	}
	
	public void setDefaultStyle() {
		this.COLOR_CITY_BORDER = new Color(161, 159, 156);
		this.COLOR_GROUND = new Color(242, 239, 233);
		this.COLOR_GROUND_BORDER = new Color(128, 128, 128);
		this.COLOR_HIGHWAY = new Color(150, 163, 230);
		this.COLOR_HIGHWAY_BORDER = new Color(104, 118, 190);
		this.COLOR_MAINROAD = new Color(255, 247, 165);
		this.COLOR_MAINROAD_BORDER = new Color(175, 163, 143);
		this.COLOR_OTHER_RAIL = new Color(110, 110, 110);
		this.COLOR_ROUTE = Color.YELLOW;
		this.COLOR_ROUTE_HIGHWAY = Color.RED;
		this.COLOR_SEA = new Color(153, 179, 204);
		this.COLOR_SEA_BORDER = this.COLOR_SEA.darker();
		this.COLOR_STATION = new Color(242, 133, 133);
		this.COLOR_STATION_BORDER = new Color(169, 93, 93);
		this.COLOR_ROAD = Color.WHITE;
		this.COLOR_ROAD_BORDER = Color.LIGHT_GRAY;
		this.COLOR_SCHOOL = new Color(232, 214, 194);
		this.COLOR_CEMETERY = new Color(232, 214, 194);
		this.COLOR_OTHER = new Color(232, 214, 194);
		this.COLOR_RAILBASE = Color.LIGHT_GRAY;
		this.COLOR_SHRINE = new Color(232, 214, 194);
		this.COLOR_PARK = new Color(167, 204, 149);
	}

	/**
	 * 地図の中心経緯度を指定して表示位置を移動させます。
	 * @param x 東経
	 * @param y 北緯
	 */
	public void setMapLocation(double x, double y) {
		this.screen.x = (int)(x * 3600000) - (int)(this.getWidth() / 2 / this.scale + 0.5f);
		this.screen.y = (int)(y * 3600000) - (int)(this.getHeight() / 2 / this.scale + 0.5f);
	}

	/**
	 * 地図の中心座標を指定して表示位置を移動させます。
	 * @param x 中心のX座標
	 * @param y 中心のY座標
	 */
	public void setMapLocation(int x, int y) {
		this.screen.x = x - (int)(this.getWidth() / 2 / this.scale + 0.5f);
		this.screen.y = y - (int)(this.getHeight() / 2 / this.scale + 0.5f);
		this.screen.width = (int) (this.getWidth() / this.scale);
		this.screen.height = (int) (this.getHeight() / this.scale);
	}

	/**
	 * マウス操作の状態を設定する
	 * @param flag マウス操作していればtrue
	 */
	public void setOperation(boolean flag) {
		this.isOperation = flag;
		if(!flag) {
			this.repaint();
		}
	}
	
	/**
	 * 市区町村表示のフォントを設定します。
	 * @param g
	 */
	private void setPrefectrueFont(Graphics2D g) {
		int fontSize = 12 + (int)(this.scale * 50000);
		if (fontSize > MapPanel.FONTSIZE_PREFECTURE_MAX) {
			fontSize = MapPanel.FONTSIZE_PREFECTURE_MAX;
		}
		if (this.mode() == 1) {
			g.setFont(new Font("Serif", Font.PLAIN, fontSize));
		} else {
			g.setFont(new Font(MapPanel.FONT_FAMILY_PREF, Font.BOLD, fontSize + 9));
		}
	}
	
	public void switchAxis() {
		this.isAxis = !this.isAxis;
	}

	/**
	 * 道路表示を切り替えます。
	 */
	public void switchHighway() {
		this.isHighway = !this.isHighway;
	}

	/**
	 * ラベル表示を切り替える
	 * @param n 
	 */
	public void switchLabel(int n) {
		if ((this.isLabel & n) == 0) {
			this.isLabel += n;
		} else {
			this.isLabel -= n;
		}
	}

	public void switchLabelFailure() {
		this.isLabelFailure = !this.isLabelFailure;
	}

	/**
	 * メッシュの表示を切り替えます。
	 */
	public void switchMesh() {
		this.isMesh = !this.isMesh;
	}

	/**
	 * 頂点表示を切り替える
	 */
	public void switchNodeView() {
		this.isNodeView = !this.isNodeView;
		this.repaint();
	}
	
	/**
	 * 鉄道表示を切り替えます。
	 */
	public void switchRailway() {
		this.isRailway = !this.isRailway;
	}

	/**
	 * アンチエイリアスを切り替える
	 */
	public void switchRendering() {
		this.isAntialias = !this.isAntialias;
		this.repaint();
	}
	
	/**
	 * 河川表示を切り替えます。
	 */
	public void switchRiver() {
		this.isRiver = !this.isRiver;
	}
	
	/**
	 * TSP
	 */
	public void switchTsp() {
		this.navi.switchTsp();
		this.navi.reroute();
	}

	/**
	 * 道路表示を切り替えます。
	 */
	public void switchRoadway() {
		this.isRoadway = !this.isRoadway;
	}

	public void switchShadow() {
		this.isShadow = !this.isShadow;
	}
	
	/**
	 * 最短経路を求めるアルゴリズムを切り替える
	 * @param algorithm 切り替えるための文字列
	 */
	public void switchShortestPathAlgorithm(String algorithm) {
		if(algorithm.endsWith("_a*")) {
			this.navi.setFunction(new DirectDistance());
		} else if (algorithm.endsWith("_dijkstra")) {
			this.navi.setFunction(null);
		}
		this.reroute();
	}
	
	public void switchTextAntialiasing() {
		this.isTextAntialiasing = !this.isTextAntialiasing;
	}

	/**
	 * 道路表示を切り替えます。
	 */
	public void switchUseHighway() {
		this.useHighway = !this.useHighway;
	}
	
	/**
	 * 拡大縮小を行う
	 * 
	 * @param x
	 * @param y
	 * @param d
	 */
	public void zoom(int x, int y, int d) {
		float newScale = this.scale * (1 + d * MapPanel.SCALE_SENSE);
		if (newScale > MapPanel.SCALE_MAX) {
			newScale = MapPanel.SCALE_MAX;
		} else if (newScale < MapPanel.SCALE_MIN) {
			newScale = MapPanel.SCALE_MIN;
		}
		y = this.getHeight() - y;
		this.screen.x = (int) (this.screen.x + x / this.scale - x / newScale);
		this.screen.y = (int) (this.screen.y + y / this.scale - y / newScale);
		this.screen.width = (int) (this.getWidth() / newScale);
		this.screen.height = (int) (this.getHeight() / newScale);
		this.scale = newScale;
	}
}