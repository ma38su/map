package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.print.Printable;

import util.FixedPoint;
import util.Log;
import util.gui.ExportableComponent;

import labeling.SimpleLabeling;
import map.MapDataManager;
import map.ksj.BusDataset;
import map.ksj.BusRoute;
import map.ksj.CityAreas;
import map.ksj.GmlCurve;
import map.ksj.PrefectureDataset;
import map.ksj.RailroadLine;
import map.ksj.RailwayDataset;
import map.ksj.Station;


/**
 * 地図データを表示するためのパネル
 * @author ma38su
 */
public class MapPanel extends ExportableComponent implements Printable {

	/**
	 * 市区町村名、都道府県名
	 */
	public static final int lABEL_PLACE_GOVT = 8;

	private static final int MAX_SCREEN_X = 154 * FixedPoint.SHIFT;

	private static final int MAX_SCREEN_Y =  46 * FixedPoint.SHIFT;
	
	private static final int MIN_SCREEN_X = 122 * FixedPoint.SHIFT;
	
	private static final int MIN_SCREEN_Y =  20 * FixedPoint.SHIFT;
	
	/**
	 * 倍率に対応したモードの番号
	 * 0: この倍率以下でFreeGISを表示
	 * 1: この倍率以下で国土数値情報の都道府県界を表示
	 * 2: この倍率以下で鉄道を表示
	 * 3: この倍率以下でバス路線を表示
	 */
	private static final float[] MODE_SCALE = {
			0.000020f,	// 0
			0.000500f,	// 1
			0.002000f,	// 2
	};
	
	/**
	 * 倍率に対応したモードの説明
	 */
	private static final String[] MODE_LABEL = {
		"MODE: 世界地図",							// 0
		"MODE: 国土数値情報（都道府県）",				// 1
		"MODE: 国土数値情報（都道府県＋鉄道）",		// 2
		"MODE: 国土数値情報（都道府県＋鉄道＋バス）",	// 3
	};
	
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
	 * 国土数値情報による塗りつぶし色
	 */
	private Color COLOR_GROUND;

	/**
	 * 領域の境界
	 */
	private Color COLOR_GROUND_BORDER;

	/**
	 * JR以外の鉄道の塗りつぶし色
	 */
	private Color COLOR_OTHER_RAIL;

	private Color COLOR_ROAD;

	/**
	 * 水域を塗りつぶす色
	 */
	private Color COLOR_SEA;

	/**
	 * 水域の境界色
	 */
	private Color COLOR_SEA_BORDER;

	/**
	 * 駅の塗りつぶし色
	 */
	private Color COLOR_STATION;

	/**
	 * マウス操作のフラグ
	 */
	private boolean isOperation;

	/**
	 * 再描画フラグ
	 */
	private boolean isRepaint;

	/**
	 * アンチエイリアスのフラグ
	 */
	private boolean isAntialiasing;

	/**
	 * 経度・緯度の表示
	 */
	private boolean isAxis;

	/**
	 * 都道府県名ラベルの表示フラグ
	 */
	private boolean isCityLabelVisible;

	/**
	 * 鉄道表示のフラグ
	 */
	private boolean isRailwayVisible;

	/**
	 * 駅の表示フラグ
	 */
	private boolean isStationVisible;

	/**
	 * 駅のラベルの表示フラグ
	 */
	private boolean isStationLabelVisible;

	/**
	 * バス路線の表示フラグ
	 */
	private boolean isBusVisible;
	
	/**
	 * バス停のラベルの表示フラグ
	 */
	private boolean isBusLabelVisible;

	/**
	 * ラベリングアルゴリズム
	 */
	private SimpleLabeling labeling;

	/**
	 * 地図情報管理マップ
	 */
	private MapDataManager maps;
	
	/**
	 * オフスクリーンイメージ
	 */
	private Image offs;
	
	/**
	 * 世界地図ポリゴン
	 */
	private Polygon[][] world;

	private Polygon[][] prefectures;
	
	private Polygon[] lake;

	private Polygon[] island;
	
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
	 * 世界地図の標準範囲
	 */
	private final Rectangle WORLD_SCREEN = new Rectangle(-648000000, -324000000, 1296000000, 648000000);

	private Color COLOR_RAILBASE;

	/**
	 * 地図パネル
	 */
	public MapPanel() {
		this.isAxis = false;
		this.isRailwayVisible = false;
		this.isBusVisible = false;

		this.screen = new Rectangle();
		this.labeling = new SimpleLabeling(this.screen);
		this.setDefaultStyle();
	}
	
	private final AffineTransform trans = new AffineTransform();
	private final AffineTransform baseTrans = new AffineTransform(1, 0, 0, 1, 0, 0);
	
	/**
	 * 地図の描画
	 * @param g
	 */
	@Override
	public void draw(Graphics2D g) {
		
		this.labeling.init(g, this.scale, this.getWidth(), this.getHeight());

		Stroke defaultStroke = g.getStroke();

		int mode = getMode();

		this.labeling.set(MODE_LABEL[mode], 5, 2);
		this.labeling.set(String.format("SCALE: %.1fµ", (this.scale * 1000 * 1000)), 5, 17);

		g.setColor(this.COLOR_SEA);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		this.trans.setTransform(scale, 0, 0, - scale, - scale * this.screen.x, this.screen.y * this.scale + this.getHeight());
		g.setTransform(this.trans);

		g.setColor(this.COLOR_GROUND);
		
		if (mode == 0) {
			g.setColor(this.COLOR_GROUND);
			this.fillPolygonWorld(g, this.world[0]);
			this.fillPolygonWorld(g, this.world[1]);
			if (!this.isOperation) {
				if (this.isAntialiasing) {
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				}
			}
		} else if (mode == 1) {

			this.fillPolygonWorld(g, this.world[0]);

			for (int i = 0; i < this.prefectures.length; i++) {
				this.fillPrefectures(g, this.maps.getPrefecture(i), this.prefectures[i]);
			}
			this.fillPolygon(g, this.lake, this.COLOR_SEA, this.COLOR_SEA_BORDER);
			this.fillPolygon(g, this.island, this.COLOR_GROUND, this.COLOR_GROUND_BORDER);
			if (!this.isOperation && this.isAntialiasing) {
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			}
		} else {
			if (!this.isOperation && this.isAntialiasing) {
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			}

			float w1 = 3 / this.scale;
			float w2 = 2 / this.scale;
			float w5 = 1 / this.scale;
//			float w6 = 1.5f / this.scale;
			
			Stroke roadStroke = new BasicStroke(w5, this.STROKE_CAP, this.STROKE_JOIN);
//			Stroke roadBorderStroke = new BasicStroke(w6, this.STROKE_CAP, this.STROKE_JOIN);

			Stroke borderStroke = new BasicStroke(w1, this.STROKE_CAP, this.STROKE_JOIN); 
			Stroke dashStroke = new BasicStroke(w2, this.STROKE_CAP, this.STROKE_JOIN, 10f, new float[]{w2 * 6, w2 * 6}, 0);

			g.setStroke(new BasicStroke(0.5f / this.scale, this.STROKE_CAP, this.STROKE_JOIN));

			for (PrefectureDataset data : this.maps.getPrefectureDatas()) {
				if (data != null && this.screen.intersects(data.getBounds())) {
					g.setColor(COLOR_GROUND);
					for (Polygon p : data.getPolygons()) {
						if (this.screen.intersects(p.getBounds())) {
							g.fillPolygon(p);
						}
					}
					g.setColor(COLOR_GROUND_BORDER);
					for (Polygon p : data.getPolygons()) {
						if (this.screen.intersects(p.getBounds())) {
							g.drawPolygon(p);
						}
					}
				}
			}

			this.fillPolygon(g, this.lake, this.COLOR_SEA, this.COLOR_SEA_BORDER);
			this.fillPolygon(g, this.island, this.COLOR_GROUND, this.COLOR_GROUND_BORDER);

			if (this.isBusVisible && mode >= 3) {
				g.setColor(COLOR_ROAD);
				g.setStroke(roadStroke);
				for (PrefectureDataset data : this.maps.getPrefectureDatas()) {
					if (data != null && this.screen.intersects(data.getBounds())) {
						BusDataset bus = data.getBusDataset();
						if (bus != null) {
							for (BusRoute route : bus.getBusRoute()) {
								if (this.screen.intersects(route.getBounds())) {
									route.draw(g);
								}
							}
							if (this.isBusLabelVisible) {
								this.labeling.add(bus.getBusStops());
							}
						}
					}
				}
			}
			
			if (mode >= 2) {
				RailwayDataset railway = this.maps.getRailwayCollection();
				Station[] stations = railway.getStations();
				
				if (this.isRailwayVisible) {
					
					//float w3 = 7 / this.scale;
					float w4 = 5 / this.scale;

					for (RailroadLine line : railway.getOtherLines()) {
						for (GmlCurve curve : line.getCurves()) {
							if (this.screen.intersects(curve.getBounds())) {
								g.setStroke(borderStroke);
								g.setColor(COLOR_OTHER_RAIL);
								curve.draw(g);
							}
						}
					}

					for (RailroadLine line : railway.getJrLines()) {
						for (GmlCurve curve : line.getCurves()) {
							if (this.screen.intersects(curve.getBounds())) {
								g.setStroke(borderStroke);
								g.setColor(COLOR_RAILBASE);
								curve.draw(g);
								g.setColor(Color.WHITE);
								g.setStroke(dashStroke);
								curve.draw(g);
							}
						}
					}

					if (this.isStationVisible) {
						g.setColor(this.COLOR_STATION);
						g.setStroke(new BasicStroke(w4, this.STROKE_CAP, this.STROKE_JOIN));
						for (Station station : stations) {
							if (this.screen.intersects(station.getBounds())) {
								station.draw(g);
							}
						}
						if (this.isStationLabelVisible) {
							this.labeling.add(stations);
						}
					}
				}
				g.setStroke(defaultStroke);
				g.setTransform(this.baseTrans);
				
				if (this.isCityLabelVisible) {
					for (PrefectureDataset data : this.maps.getPrefectureDatas()) {
						if (data != null && this.screen.intersects(data.getBounds())) {
							CityAreas[] areas = data.getAreas();
							if (areas != null) {
								this.labeling.add(areas);
							}
						}
					}
				}
			}
		}

		g.setTransform(this.baseTrans);
		g.setStroke(defaultStroke);

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
		switch (this.getMode()) {
			case 0: step = 10;
			break;
			case 3: step = 1;
			break;
			default: step = 5;
		}
		step *= FixedPoint.SHIFT;
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
		FontMetrics metrics = g.getFontMetrics();
		String rule;
		if (distance >= 1000) {
			rule = Integer.toString((int) (distance / 1000)) + "km";
		} else {
			rule = Integer.toString((int) distance) + "m";
		}
		int center = (x0 + x - metrics.stringWidth(rule)) / 2 + 5;
		this.labeling.set(rule, center, y - 3);
	}
	
	public double distance(int x0, int x, int y) {
		double sx0 = x0 / this.scale + this.screen.x;
		double sx = x / this.scale + this.screen.x;
		double sy = (this.getHeight() - y) / this.scale + this.screen.y;
		return Math.abs(6378137 * (sx0 - sx) / FixedPoint.SHIFT / 180 * Math.PI * Math.cos(sy / FixedPoint.SHIFT * Math.PI / 180));
	}
	
	/**
	 * ポリゴンを描画します。
	 * @param g 描画するGraphics2D
	 * @param polygons 描画するポリゴン
	 * @param bg 背景色
	 * @param line 境界色
	 */
	private void fillPolygon(Graphics2D g, Polygon[] polygons, Color bg, Color line) {
		if (polygons == null) {
			return;
		}
		g.setColor(bg);
		for (Polygon polygon : polygons) {
			if(polygon.intersects(this.screen)) {
				g.fillPolygon(polygon);
			}
		}
		// 境界を描画つぶします。
		g.setColor(line);
		for (Polygon polygon : polygons) {
			if(polygon.intersects(this.screen)) {
				g.drawPolygon(polygon);
			}
		}
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
		for (Polygon polygon : polygons) {
			if(polygon.intersects(this.screen)) {
				g.fillPolygon(polygon);
			}
		}
		g.setColor(this.COLOR_GROUND_BORDER);
		for (Polygon polygon : polygons) {
			if(polygon.intersects(this.screen)) {
				g.drawPolygon(polygon);
			}
		}
	}

	/**
	 * ポリゴンを描画します。
	 * @param g 描画するGraphics2D
	 * @param name 名前
	 * @param prefecture 描画するポリゴン
	 */
	private void fillPrefectures(Graphics2D g, String name, Polygon[] prefecture) {
		g.setColor(this.COLOR_GROUND);
		for (Polygon polygon : prefecture) {
			if(polygon.intersects(this.screen)) {
				g.fillPolygon(polygon);
			}
		}
		g.setColor(this.COLOR_GROUND_BORDER);
		for (Polygon polygon : prefecture) {
			if(polygon.intersects(this.screen)) {
				g.drawPolygon(polygon);
			}
		}
	}

	public double getLocationX(int x) {
		return (this.screen.x + x / this.scale) / FixedPoint.SHIFT;
	}

	public double getLocationCenterX() {
		return (this.screen.x + this.screen.width / 2) / FixedPoint.SHIFT;
	}

	
	public double getLocationY(int y) {
		return (this.screen.y + (this.getHeight() - y) / this.scale) / FixedPoint.SHIFT;
	}

	public double getLocationCenterY() {
		return (this.screen.y + this.screen.height / 2) / FixedPoint.SHIFT;
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

		this.maps = map;
		this.maps.start();
		
		this.world = world;
		this.prefectures = prefectures;
		this.lake = lake;
		this.island = island;

		this.isAntialiasing = false;
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
	 * <li>2 : 国土数値情報（市区町村）</li>
	 * <li>1 : 国土数値情報（都道府県）</li>
	 * <li>0 : FreeGIS（世界地図）</li>
	 * </ul>
	 * @return 地図の表示状態
	 */
	public int getMode() {
		int i = 0;
		do {
			if (Float.compare(this.scale, MapPanel.MODE_SCALE[i]) < 0) {
				break;
			}
		} while (++i < MapPanel.MODE_SCALE.length);
		return i;
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
			offg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
			// オフスクリーンバッファ
			offg.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
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

	@Override
	public void repaint() {
		if (this.maps != null) {
			this.maps.wakeup();
		}
		this.isRepaint = true;
		super.repaint();
	}

	public void setDefaultStyle() {
		this.COLOR_GROUND = new Color(242, 239, 233);
		this.COLOR_GROUND_BORDER = new Color(128, 128, 128);
		this.COLOR_OTHER_RAIL = new Color(110, 110, 110);
		this.COLOR_SEA = new Color(153, 179, 204);
		this.COLOR_SEA_BORDER = this.COLOR_SEA.darker();
		this.COLOR_STATION = new Color(242, 133, 133);
		this.COLOR_ROAD = Color.LIGHT_GRAY;
		this.COLOR_RAILBASE = Color.BLACK;
	}

	/**
	 * 地図の中心経緯度を指定して表示位置を移動させます。
	 * @param x 東経
	 * @param y 北緯
	 */
	public void setMapLocation(double x, double y) {
		this.screen.x = (int)(x * FixedPoint.SHIFT) - (int)(this.getWidth() / 2 / this.scale + 0.5f);
		this.screen.y = (int)(y * FixedPoint.SHIFT) - (int)(this.getHeight() / 2 / this.scale + 0.5f);
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
	}
	
	public void setAxisVisible(boolean flag) {
		this.isAxis = flag;
	}
	
	public boolean isAxisVisible() {
		return this.isAxis;
	}

	public boolean isLabelFailureVisible() {
		return this.labeling.isLabelFailureVisible();
	}
	
	public void setLabelFailureVisible(boolean flag) {
		this.labeling.setLabelFailureVisible(flag);
	}

	/**
	 * @param flag 鉄道表示フラグ
	 */
	public void setRailwayVisible(boolean flag) {
		this.isRailwayVisible = flag;
	}
	
	public boolean isRailwayVisible() {
		return this.isRailwayVisible;
	}

	/**
	 * @param flag アンチエイリアスの有無
	 */
	public void setAntialiasing(boolean flag) {
		this.isAntialiasing = flag;
	}
	
	public boolean isAntialiasing() {
		return this.isAntialiasing;
	}
	
	/**
	 * @return バスの表示有無
	 */
	public boolean isBusVisible() {
		return this.isBusVisible;
	}

	/**
	 * @param flag バス路線の表示有無
	 */
	public void setBusVisible(boolean flag) {
		this.isBusVisible = flag;
	}

	/**
	 * @return 駅の表示有無
	 */
	public boolean isStationVisible() {
		return this.isStationVisible;
	}
	
	public void setStationVisible(boolean flag) {
		this.isStationVisible = flag;
	}

	/**
	 * @return 駅のラベルの表示有無
	 */
	public boolean isStationLabelVisible() {
		return this.isStationLabelVisible;
	}

	/**
	 * @param flag 駅のラベルの表示
	 */
	public void setStationLabelVisible(boolean flag) {
		this.isStationLabelVisible = flag;
	}

	/**
	 * @return バスのラベル表示有無
	 */
	public boolean isBusLabelVisible() {
		return this.isBusLabelVisible;
	}

	/**
	 * @param flag バスのラベル表示有無
	 */
	public void setBusLabelVisible(boolean flag) {
		this.isBusLabelVisible = flag;
	}

	/**
	 * @return 行政区画のラベルの表示有無
	 */
	public boolean isCityLabelVisible() {
		return this.isCityLabelVisible;
	}

	/**
	 * @param flag 行政区画のラベル表示有無
	 */
	public void setCityLabelVisible(boolean flag) {
		this.isCityLabelVisible = flag;
	}

	public boolean isLabelShadowVisible() {
		return this.labeling.isLabelFailureVisible();
	}
	
	public void setLabelShadowVisible(boolean flag) {
		this.labeling.setLabelShadowVisible(flag);
	}
	
	/**
	 * @param flag テキストアンチエイリアスのフラグ
	 */
	public void setTextAntialiasing(boolean flag) {
		this.labeling.setTextAntialiasing(flag);
	}
	
	/**
	 * @return テキストアンチエイリアスの有無
	 */
	public boolean isTextAntialiasing() {
		return this.labeling.isTextAntialiasing();
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
		if (Float.compare(newScale, MapPanel.SCALE_MAX) > 0) {
			newScale = MapPanel.SCALE_MAX;
		} else if (Float.compare(newScale, MapPanel.SCALE_MIN) < 0) {
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