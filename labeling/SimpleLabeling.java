package labeling;

import java.awt.Color;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import map.ksj.BusStop;
import map.ksj.CityAreas;
import map.ksj.CityInfo;
import map.ksj.Station;

/**
 * 単純なアルゴリズムのラベル配置
 * @author ma38su
 */
public class SimpleLabeling {
	
	/**
	 * 駅のフォント
	 */
	private static final Font FONT_STATION = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

	/**
	 * 駅のフォント
	 */
	private static final Font FONT_BUSSTOP = new Font(Font.SANS_SERIF, Font.PLAIN, 10);

	/**
	 * 市区町村名表示フォントの最大サイズ
	 */
	private static final int FONTSIZE_CITY_MAX = 38;

	/**
	 * 都道府県名表示フォントの最大サイズ
	 */
	private static final int FONTSIZE_PREFECTURE_MAX = 60;
	
	/**
	 * ラベリングに用いるフォント
	 */
	private static final Font FONT_INFO = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

	public static final Color SHADOW = new Color(0xEEEEEE);

	private Graphics2D g;

	/**
	 * ラベルに陰をつけるかどうかのフラグ
	 */
	private boolean isLabelShadowVisible;
	
	/**
	 * 配置済みラベル
	 */
	private final Map<Font, List<FixedLabel>> fixedLabelMap;

	/**
	 * ラベルの重なり
	 */
	private final List<Rectangle> lapList;

	/**
	 * 表示倍率
	 */
	private float scale;
		
	/**
	 * 描画変換後の表示領域
	 */
	private final Rectangle screen;
	
	/**
	 * 描画変換前の表示領域
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

	/**
	 * コンストラクタ
	 * @param screen 描画変換の前の表示領域
	 */
	public SimpleLabeling(Rectangle screen) {
		this.screen0 = screen;
		this.screen = new Rectangle();
		this.lapList = new ArrayList<Rectangle>();
		this.fixedLabelMap = new HashMap<Font, List<FixedLabel>>();

		this.isTextAntialiasing = false;
		this.isLabelFailureVisible = false;
		this.isLabelShadowVisible = false;
	}
	
	public void add(CityAreas[] areas) {
		Font font = this.g.getFont();
		FontMetrics metrics = this.g.getFontMetrics();
		int fontAscent = metrics.getAscent();
		int fontHeight = metrics.getHeight();
		
		List<FixedLabel> list = this.fixedLabelMap.get(font);
		if (list == null) {
			list = new ArrayList<FixedLabel>();
			this.fixedLabelMap.put(font, list);
		}
		for (CityAreas area : areas) {
			CityInfo info = area.getInfo();
			String name = info.getCn2();
			
			int lat = area.getY();
			int lng = area.getX();
			// 表示領域内のであれば描画する
			if (this.screen0.contains(lng, lat)) {
				
				int r = (int) (2 / scale + 0.5);
				int r2 = (int) (4 / scale + 0.5);
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
				if (flag) {
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

	public void add(Station[] stations) {
		this.g.setFont(FONT_STATION);
		Font font = this.g.getFont();
		FontMetrics metrics = this.g.getFontMetrics();
		int fontAscent = metrics.getAscent();
		int fontHeight = metrics.getHeight();
		
		List<FixedLabel> list = this.fixedLabelMap.get(font);
		if (list == null) {
			list = new ArrayList<FixedLabel>();
			this.fixedLabelMap.put(font, list);
		}
		for (Station st : stations) {
			String name = st.getName();
			int lat = st.getY();
			int lng = st.getX();
			// 表示領域内のであれば描画する
			if (this.screen0.contains(lng, lat)) {
				
				int r = (int) (2 / scale + 0.5);
				int r2 = (int) (4 / scale + 0.5);
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
				if (flag) {
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

	public void add(BusStop[] stops) {
		this.g.setFont(FONT_BUSSTOP);
		Font font = this.g.getFont();
		FontMetrics metrics = this.g.getFontMetrics();
		int fontAscent = metrics.getAscent();
		int fontHeight = metrics.getHeight();
		
		List<FixedLabel> list = this.fixedLabelMap.get(font);
		if (list == null) {
			list = new ArrayList<FixedLabel>();
			this.fixedLabelMap.put(font, list);
		}
		for (BusStop st : stops) {
			String name = st.getName();
			int lat = st.getY();
			int lng = st.getX();
			// 表示領域内のであれば描画する
			if (this.screen0.contains(lng, lat)) {
				
				int r = (int) (2 / scale + 0.5);
				int r2 = (int) (4 / scale + 0.5);
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
				if (flag) {
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
	 * 市区町村名ラベルを追加します。
	 * @param name 
	 * @param lng X座標
	 * @param lat Y座標
	 * @param isPoint ラベルの地点の表示の有無
	 */
	public void add(String name, int lng, int lat, boolean isPoint) {

		Font font = this.g.getFont();
		FontMetrics metrics = this.g.getFontMetrics();
		int fontAscent = metrics.getAscent();
		int fontHeight = metrics.getHeight();
		
		List<FixedLabel> list = this.fixedLabelMap.get(font);
		if (list == null) {
			list = new ArrayList<FixedLabel>();
			this.fixedLabelMap.put(font, list);
		}
		// 表示領域内のであれば描画する
		if (this.screen0.contains(lng, lat)) {
			
			int r = (int) (2 / scale + 0.5);
			int r2 = (int) (4 / scale + 0.5);
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
						System.out.println(i);
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
			if (flag) {
				this.g.setColor(Color.DARK_GRAY);
				this.g.fillOval(lng - r, lat - r, r2, r2);
			} else if (this.isLabelFailureVisible) {
				this.g.setColor(Color.GRAY);
				this.g.drawLine(x - r, y - r, x + r, y + r);
				this.g.drawLine(x + r, y - r, x - r, y + r);
			}
		}
	}

	/**
	 * 配置したラベルを描画します。
	 * @param rendering 
	 */
	public void draw() {
		if (this.isTextAntialiasing) {
			this.g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		} else {
			this.g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		}
		boolean flag = this.isTextAntialiasing && RenderingHints.VALUE_ANTIALIAS_ON.equals(this.g.getRenderingHint(RenderingHints.KEY_ANTIALIASING));
		for (Map.Entry<Font, List<FixedLabel>> entry : this.fixedLabelMap.entrySet()) {
			Font font = entry.getKey();
			this.g.setFont(font);
			if (font == FONT_INFO) {
				for (FixedLabel label : entry.getValue()) {
					label.draw(this.g, Color.DARK_GRAY, Color.WHITE, flag);
				}
			} else if (font.getFamily().equals("Serif")) {
				for (FixedLabel label : entry.getValue()) {
					label.draw(this.g, Color.DARK_GRAY, Color.WHITE, flag);
				}
			} else if (font.getStyle() == Font.BOLD){
				// 市区町村名
				if (this.isLabelShadowVisible) {
					for (FixedLabel label : entry.getValue()) {
						label.draw(this.g, Color.BLACK, Color.WHITE, flag);
					}
				} else {
					this.g.setColor(Color.BLACK);
					for (FixedLabel label : entry.getValue()) {
						label.draw(this.g);
					}
				}
			} else {
				if (this.isLabelShadowVisible) {
					for (FixedLabel label : entry.getValue()) {
						label.draw(this.g, Color.BLACK, SimpleLabeling.SHADOW, flag);
					}
				} else {
					this.g.setColor(Color.BLACK);
					for (FixedLabel label : entry.getValue()) {
						label.draw(this.g);
					}
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
		this.g.setFont(FONT_INFO);
		FontMetrics metrics = this.g.getFontMetrics();
		int fontAscent = metrics.getAscent();
		int fontHeight = metrics.getHeight();
		Font font = this.g.getFont();
		List<FixedLabel> list = this.fixedLabelMap.get(font);
		if (list == null) {
			list = new ArrayList<FixedLabel>();
			this.fixedLabelMap.put(font, list);
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
		this.g = g;
		this.scale = scale;
		this.screen.width = width;
		this.screen.height = height;
		this.lapList.clear();
		this.fixedLabelMap.clear();
	}
}
