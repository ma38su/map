package labeling;

import java.awt.Color;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import map.Label;
import map.ksj.BusStop;
import map.ksj.CityAreas;
import map.ksj.Station;

/**
 * 単純なアルゴリズムのラベル配置
 * @author ma38su
 */
public class SimpleLabeling {
	
	private static final int LABEL_INFO = 0;
	private static final int LABEL_CITY = 1;
	private static final int LABEL_STATION = 2;
	private static final int LABEL_BUSSTOP = 3;
	
	/**
	 * フォント
	 */
	private final Font[] FONTS = {
		new Font(Font.SANS_SERIF, Font.PLAIN, 12), // 情報のフォント
		new Font(Font.SANS_SERIF, Font.PLAIN, 12), // 行政区画のフォント
		new Font(Font.SANS_SERIF, Font.PLAIN, 11), // 駅のフォント
		new Font(Font.SANS_SERIF, Font.PLAIN, 10), // バス停のフォント
	};

	/**
	 * フォント
	 */
	private final Color[] FOREGROUND_COLORS = {
		new Color(0x000000), // 情報のフォント
		new Color(0x000000), // 情報のフォント
		new Color(0x000000), // 情報のフォント
		new Color(0x000000), // 情報のフォント
	};

	/**
	 * フォント
	 */
	private final Color[] SHADOW_COLORS = {
		new Color(0xEEEEEE), // 情報のフォント
		new Color(0xEEEEEE), // 情報のフォント
		new Color(0xEEEEEE), // 情報のフォント
		new Color(0xEEEEEE), // 情報のフォント
	};

	
	/**
	 * フォントメトリクス
	 */
	private FontMetrics[] METRICS;
	
	private Graphics2D g;

	/**
	 * ラベルに陰をつけるかどうかのフラグ
	 */
	private boolean isLabelShadowVisible;
	
	/**
	 * 配置済みラベル
	 */
	private final Map<Integer, List<FixedLabel>> fixedLabelMap;

	/**
	 * ラベルの重なり
	 */
	private final List<Rectangle> lapList;

	/**
	 * 表示倍率
	 */
	private float scale;
		
	/**
	 * スクリーン座標系の表示領域
	 */
	private final Rectangle screen;
	
	/**
	 * 緯度経度座標系（描画変換前）の表示領域
	 */
	private final Rectangle screen0;

	/**
	 * ラベル失敗表示フラグ
	 */
	private boolean isLabelFailureVisible;

	/**
	 * テキストアンチエイリアス適用のフラグ
	 */
	private boolean isTextAntialiasing;
	
	private int r;
	private int r2;

	/**
	 * コンストラクタ
	 * @param screen 描画変換の前の表示領域
	 */
	public SimpleLabeling(Rectangle screen) {
		this.screen0 = screen;
		this.screen = new Rectangle();

		this.lapList = new ArrayList<Rectangle>();
		this.fixedLabelMap = new HashMap<Integer, List<FixedLabel>>();
		
		this.isTextAntialiasing = false;
		this.isLabelFailureVisible = false;
		this.isLabelShadowVisible = false;
	}
	
	public void add(CityAreas[] areas) {
		add(LABEL_CITY, areas);
	}

	public void add(Station[] stations) {
		add(LABEL_STATION, stations);
	}
	
	public void add(BusStop[] stops) {
		add(LABEL_BUSSTOP, stops);
	}
		
	private void add(int type, Label[] labels) {
		FontMetrics metrics = METRICS[type];
		int fontAscent = metrics.getAscent();
		int fontHeight = metrics.getHeight();
		
		List<FixedLabel> list = this.fixedLabelMap.get(type);
		if (list == null) {
			list = new ArrayList<FixedLabel>();
			this.fixedLabelMap.put(type, list);
		}
		
		for (Label st : labels) {

			String name = st.getName();
			int lat = st.getY();
			int lng = st.getX();

			// 表示領域内のであれば描画する
			if (this.screen0.contains(lng, lat)) {
				
				int x = (int) ((lng - this.screen0.x) * this.scale);
				int y = this.screen.height - (int) ((lat - this.screen0.y) * this.scale);

				int fontWidth = metrics.stringWidth(name);

				// 施設の位置を描画する
				boolean flag = false;
				Rectangle labelCandidate = new Rectangle();
				labelCandidate.width = fontWidth + 8;
				labelCandidate.height = fontHeight + 3;
				
				for (int i = 0; i < 4; i++) {
					boolean isLap = false;
					labelCandidate.x = x - (i / 2) * labelCandidate.width;
					labelCandidate.y = y - (i % 2) * labelCandidate.height;
					
					for (Rectangle rect : this.lapList) {
						if (labelCandidate.intersects(rect)) {
							isLap = true;
							break;
						}
					}

					// 重なるか，スクリーン内からはみでる場合は再計算
					if (isLap || !this.screen.contains(labelCandidate)) {
						continue;
					}

					this.lapList.add(labelCandidate);
					list.add(new FixedLabel(name, labelCandidate.x + 2 + (i / 2) * (labelCandidate.width - fontWidth - 4), labelCandidate.y + fontAscent + (i % 2) * 3));
					flag = true;
					break;
				}
				if (flag && type != LABEL_STATION) {
					this.g.setColor(Color.DARK_GRAY);
					this.g.fillOval(lng - r, lat - r, r2, r2);
				} else if (this.isLabelFailureVisible) {
					this.g.setColor(Color.GRAY);
					this.g.drawLine(x - r, y - r, x + r, y + r);
					this.g.drawLine(x + r, y - r, x - r, y + r);
				}
			}
		}
	}
	
	/**
	 * 配置したラベルを描画します。
	 * @param rendering 
	 */
	public void draw() {
		this.g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				this.isTextAntialiasing ? RenderingHints.VALUE_TEXT_ANTIALIAS_GASP : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

		for (Map.Entry<Integer, List<FixedLabel>> entry : this.fixedLabelMap.entrySet()) {
			int type = entry.getKey();
			Font font = FONTS[type];
			this.g.setFont(font);
			Color foreColor = FOREGROUND_COLORS[type];
			if (this.isLabelShadowVisible) {
				Color shadowColor = SHADOW_COLORS[type];
				for (FixedLabel label : entry.getValue()) {
					label.draw(this.g, foreColor, shadowColor);
				}
			} else {
				this.g.setColor(foreColor);
				for (FixedLabel label : entry.getValue()) {
					label.draw(this.g);
				}
			}
		}
	}

	/**
	 * 領域内であれば確実に配置します。
	 * @param name 
	 * @param x 
	 * @param y 
	 */
	public void set(String name, int x, int y) {
		FontMetrics metrics = METRICS[LABEL_INFO];
		int fontAscent = metrics.getAscent();
		int fontHeight = metrics.getHeight();

		List<FixedLabel> list = this.fixedLabelMap.get(LABEL_INFO);
		if (list == null) {
			list = new ArrayList<FixedLabel>();
			this.fixedLabelMap.put(LABEL_INFO, list);
		}
		// 表示領域内のであれば描画する
		if (this.screen.contains(x, y)) {
			int fontWidth = metrics.stringWidth(name);
			Rectangle labelCandidate = new Rectangle();
			labelCandidate.width = fontWidth + 8;
			labelCandidate.height = fontHeight + 3;
			labelCandidate.x = x;
			labelCandidate.y = y;
			this.lapList.add(labelCandidate);
			list.add(new FixedLabel(name, labelCandidate.x + 2, labelCandidate.y + fontAscent));
		}
	}

	public boolean isTextAntialiasing() {
		return this.isTextAntialiasing;
	}
	
	public void setTextAntialiasing(boolean flag) {
		this.isTextAntialiasing = flag;
	}
	
	public void setLabelFailureVisible(boolean flag) {
		this.isLabelFailureVisible = flag;
	}
	
	public boolean isLabelFailureVisible() {
		return this.isLabelFailureVisible;
	}
	
	public void setLabelShadowVisible(boolean flag) {
		this.isLabelShadowVisible = flag;
	}
	
	public boolean isLabelShadowVisible() {
		return this.isLabelFailureVisible;
	}
	
	public void init(Graphics2D g, float scale, int width, int height) {
		if (this.METRICS == null) {
			AffineTransform transform = g.getTransform();
			if (Double.compare(transform.getScaleX(), 1) != 0 || Double.compare(transform.getScaleY(), 1) != 0) {
				throw new IllegalStateException("Illegal Transform");
			}

			this.METRICS = new FontMetrics[FONTS.length];
			Font defaultFont = g.getFont();
			for (int i = 0; i < FONTS.length; i++) {
				g.setFont(FONTS[i]);
				this.METRICS[i] = g.getFontMetrics();
			}
			g.setFont(defaultFont);
		}

		this.g = g;
		this.scale = scale;
		this.screen.width = width;
		this.screen.height = height;
		this.lapList.clear();
		this.fixedLabelMap.clear();
		
		this.r = (int) (2 / this.scale + 0.5);
		this.r2 = (int) (4 / this.scale + 0.5);
	}
}
