package labeling;

import java.awt.Color;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import view.MapPanel;

import map.ksj.BusStop;
import map.ksj.CityAreas;
import map.ksj.CityInfo;
import map.ksj.Station;

/**
 * 単純なアルゴリズムのラベル配置
 * @author ma38su
 */
public class SimpleLabeling {
	
	public static int RENDERING_NONE = 0;

	public static int RENDERING_SHADOW = 1;
	
	public static final Color SHADOW = new Color(0xEEEEEE);

	private Graphics2D g;

	private boolean isShadow;
	
	/**
	 * 配置済みラベル
	 */
	private final Map<Font, List<FixedLabel>> label;

	/**
	 * ラベルの重なり
	 */
	private final List<Rectangle> lap;

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
	 * 失敗表示フラグ
	 */
	private boolean isFailure;

	private boolean isTextAntialiasing;

	/**
	 * コンストラクタ
	 * @param screen 描画変換の前の表示領域
	 */
	public SimpleLabeling(Rectangle screen) {
		this.screen0 = screen;
		this.screen = new Rectangle();
		this.lap = new ArrayList<Rectangle>();
		this.label = new HashMap<Font, List<FixedLabel>>();
	}
	
	/**
	 * ラベルを追加します。
	 * 同じフォントで表示することになります。
	 * @param data 追加するラベルデータ
	 */
	public void add(Label[] data) {
		FontMetrics metrics = this.g.getFontMetrics();
		int fontAscent = metrics.getAscent();
		int fontHeight = metrics.getHeight();
		Font font = this.g.getFont();
		List<FixedLabel> list = this.label.get(font);
		if (list == null) {
			list = new ArrayList<FixedLabel>();
			this.label.put(font, list);
		}
		for (final Label label : data) {
			String name = label.getName();
			if (name == null) {
				continue;
			}
			// 表示領域内のであれば描画する
			if (this.screen0.contains(label.getLng(), label.getLat())) {
				int x = (int) ((label.getLng() - this.screen0.x) * this.scale);
				int y = this.screen.height - (int) ((label.getLat() - this.screen0.y) * this.scale);
		
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
					for (Rectangle rect : this.lap) {
						if (rect.intersects(labelCandidate)) {
							isLap = true;
							break;
						}
					}

					// 重なるか，スクリーン内からはみでる場合は再計算
					if (isLap || !this.screen.contains(labelCandidate)) {
						continue;
					}

					this.lap.add(labelCandidate);
					list.add(new FixedLabel(name, labelCandidate.x + 2 + (i / 2) * (labelCandidate.width - fontWidth - 4), labelCandidate.y + fontAscent + (i % 2) * 3));
					flag = true;
					break;
				}
				if (!(label instanceof Station)) {
					if (flag) {
						this.g.setColor(Color.BLACK);
						this.g.fillOval(x - 2, y - 2, 4, 4);
					} else if (this.isFailure && this.screen.contains(x, y)) {
						this.g.setColor(Color.GRAY);
						this.g.drawLine(x - 2, y - 2, x + 2, y + 2);
						this.g.drawLine(x + 2, y - 2, x - 2, y + 2);
					}
				}
			}
		}
	}

	/**
	 * 同一ラベルで複数地点に配置するラベルを追加します。
	 * チェーン店などの追加に用います。
	 * @param data 追加するラベル
	 */
	public void add(Labels data) {
		FontMetrics metrics = this.g.getFontMetrics();
		int fontAscent = metrics.getAscent();
		int fontHeight = metrics.getHeight();
		Font font = this.g.getFont();
		List<FixedLabel> list = this.label.get(font);
		if (list == null) {
			list = new ArrayList<FixedLabel>();
			this.label.put(font, list);
		}
		for (Point point : data.getLocation()) {
		
			String name = data.getName();
			
			// 表示領域内のであれば描画する
			if (this.screen0.contains(point.getX(), point.getY())) {

				int x = (int) ((point.getX() - this.screen0.x) * this.scale);
				int y = this.screen.height - (int) ((point.getY() - this.screen0.y) * this.scale);
		
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
					for (Rectangle rect : this.lap) {
						if (rect.intersects(labelCandidate)) {
							isLap = true;
							break;
						}
					}

					// 重なるか，スクリーン内からはみでる場合は再計算
					if (isLap || !this.screen.contains(labelCandidate)) {
						continue;
					}
					this.lap.add(labelCandidate);
					list.add(new FixedLabel(name, labelCandidate.x + 2 + (i / 2) * (labelCandidate.width - fontWidth - 4), labelCandidate.y + fontAscent + (i % 2) * 3));
					flag = true;
					break;
				}
				if (flag) {
					this.g.setColor(Color.BLACK);
					this.g.fillOval(x - 2, y - 2, 4, 4);
				} else if (this.isFailure && this.screen.contains(x, y)) {
					this.g.setColor(Color.GRAY);
					this.g.drawLine(x - 2, y - 2, x + 2, y + 2);
					this.g.drawLine(x + 2, y - 2, x - 2, y + 2);
				}
			}
		}
	}

	public void addCenter(Label[] data) {
		FontMetrics metrics = this.g.getFontMetrics();
		int fontAscent = metrics.getAscent();
		int fontHeight = metrics.getHeight();
		Font font = this.g.getFont();
		List<FixedLabel> list = this.label.get(font);
		if (list == null) {
			list = new ArrayList<FixedLabel>();
			this.label.put(font, list);
		}
		for (final Label label : data) {
			String name = label.getName();
			if (name == null || "".equals(name)) {
				continue;
			}
			// 表示領域内のであれば描画する
			if (this.screen0.contains(label.getLng(), label.getLat())) {
				int x = (int) ((label.getLng() - this.screen0.x) * this.scale);
				int y = this.screen.height - (int) ((label.getLat() - this.screen0.y) * this.scale);
		
				int fontWidth = metrics.stringWidth(name);

				boolean isLap = false;
				Rectangle labelCandidate = new Rectangle();
				labelCandidate.width = fontWidth + 8;
				labelCandidate.height = fontHeight + 3;
				labelCandidate.x = x - labelCandidate.width / 2;
				labelCandidate.y = y - labelCandidate.height / 2;
				if (!this.screen.contains(labelCandidate)) {
					isLap = true;
				}
				for (Rectangle rect : this.lap) {
					if (rect.intersects(labelCandidate)) {
						isLap = true;
						break;
					}
				}
				// 重なるか，スクリーン内からはみでる場合は再計算
				if (!isLap) {
					this.lap.add(labelCandidate);
					list.add(new FixedLabel(name, labelCandidate.x + 2 + (labelCandidate.width - fontWidth - 4) / 2, labelCandidate.y + fontAscent + 3 / 2));
				} else {
					isLap = false;
					for (int i = 0; i < 4; i++) {
						labelCandidate.x = x - (i / 2) * labelCandidate.width;
						labelCandidate.y = y - (i % 2) * labelCandidate.height;
	
						if (!this.screen.contains(labelCandidate)) {
							continue;
						}
						for (Rectangle rect : this.lap) {
							if (rect.intersects(labelCandidate)) {
								isLap = true;
								break;
							}
						}
						if (!isLap) {
							this.lap.add(labelCandidate);
							list.add(new FixedLabel(name, labelCandidate.x + 2 + (i / 2) * (labelCandidate.width - fontWidth - 4), labelCandidate.y + fontAscent + (i % 2) * 3));
							break;
						}
					}
				}
			}
		}
		
	}
	
	public void add(CityAreas[] areas) {
		Font font = this.g.getFont();
		FontMetrics metrics = this.g.getFontMetrics();
		int fontAscent = metrics.getAscent();
		int fontHeight = metrics.getHeight();
		
		List<FixedLabel> list = this.label.get(font);
		if (list == null) {
			list = new ArrayList<FixedLabel>();
			this.label.put(font, list);
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
					
					for (Rectangle rect : this.lap) {
						if (labelCandidate.intersects(rect)) {
							isLap = true;
							break;
						}
					}

					// 重なるか，スクリーン内からはみでる場合は再計算
					if (isLap || !this.screen.contains(labelCandidate)) {
						continue;
					}

					this.lap.add(labelCandidate);
					list.add(new FixedLabel(name, labelCandidate.x + 2 + (i / 2) * (labelCandidate.width - fontWidth - 4), labelCandidate.y + fontAscent + (i % 2) * 3));
					flag = true;
					break;
				}
				if (flag) {
					this.g.setColor(Color.DARK_GRAY);
					this.g.fillOval(lng - r, lat - r, r2, r2);
				} else if (this.isFailure) {
					this.g.setColor(Color.GRAY);
					this.g.drawLine(x - r, y - r, x + r, y + r);
					this.g.drawLine(x + r, y - r, x - r, y + r);
				}
			}
		}
	}

	public void add(Station[] stations) {
		Font font = this.g.getFont();
		FontMetrics metrics = this.g.getFontMetrics();
		int fontAscent = metrics.getAscent();
		int fontHeight = metrics.getHeight();
		
		List<FixedLabel> list = this.label.get(font);
		if (list == null) {
			list = new ArrayList<FixedLabel>();
			this.label.put(font, list);
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
					
					for (Rectangle rect : this.lap) {
						if (labelCandidate.intersects(rect)) {
							isLap = true;
							break;
						}
					}

					// 重なるか，スクリーン内からはみでる場合は再計算
					if (isLap || !this.screen.contains(labelCandidate)) {
						continue;
					}

					this.lap.add(labelCandidate);
					list.add(new FixedLabel(name, labelCandidate.x + 2 + (i / 2) * (labelCandidate.width - fontWidth - 4), labelCandidate.y + fontAscent + (i % 2) * 3));
					flag = true;
					break;
				}
				if (flag) {
					this.g.setColor(Color.DARK_GRAY);
					this.g.fillOval(lng - r, lat - r, r2, r2);
				} else if (this.isFailure) {
					this.g.setColor(Color.GRAY);
					this.g.drawLine(x - r, y - r, x + r, y + r);
					this.g.drawLine(x + r, y - r, x - r, y + r);
				}
			}
		}
	}

	public void add(BusStop[] stops) {
		Font font = this.g.getFont();
		FontMetrics metrics = this.g.getFontMetrics();
		int fontAscent = metrics.getAscent();
		int fontHeight = metrics.getHeight();
		
		List<FixedLabel> list = this.label.get(font);
		if (list == null) {
			list = new ArrayList<FixedLabel>();
			this.label.put(font, list);
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
					
					for (Rectangle rect : this.lap) {
						if (labelCandidate.intersects(rect)) {
							isLap = true;
							break;
						}
					}

					// 重なるか，スクリーン内からはみでる場合は再計算
					if (isLap || !this.screen.contains(labelCandidate)) {
						continue;
					}

					this.lap.add(labelCandidate);
					list.add(new FixedLabel(name, labelCandidate.x + 2 + (i / 2) * (labelCandidate.width - fontWidth - 4), labelCandidate.y + fontAscent + (i % 2) * 3));
					flag = true;
					break;
				}
				if (flag) {
					this.g.setColor(Color.DARK_GRAY);
					this.g.fillOval(lng - r, lat - r, r2, r2);
				} else if (this.isFailure) {
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
		
		List<FixedLabel> list = this.label.get(font);
		if (list == null) {
			list = new ArrayList<FixedLabel>();
			this.label.put(font, list);
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
				
				for (Rectangle rect : this.lap) {
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

				this.lap.add(labelCandidate);
				list.add(new FixedLabel(name, labelCandidate.x + 2 + (i / 2) * (labelCandidate.width - fontWidth - 4), labelCandidate.y + fontAscent + (i % 2) * 3));
				flag = true;
				break;
			}
			if (flag) {
				this.g.setColor(Color.DARK_GRAY);
				this.g.fillOval(lng - r, lat - r, r2, r2);
			} else if (this.isFailure) {
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
		for (Map.Entry<Font, List<FixedLabel>> entry : this.label.entrySet()) {
			Font font = entry.getKey();
			this.g.setFont(font);
			if (font == MapPanel.FONT_INFO) {
				for (FixedLabel label : entry.getValue()) {
					label.draw(this.g, Color.DARK_GRAY, Color.WHITE, flag);
				}
			} else if (font.getFamily().equals("Serif")) {
				for (FixedLabel label : entry.getValue()) {
					label.draw(this.g, Color.DARK_GRAY, Color.WHITE, flag);
				}
			} else if (font.getStyle() == Font.BOLD){
				// 市区町村名
				if (this.isShadow) {
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
				if (this.isShadow) {
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
		FontMetrics metrics = this.g.getFontMetrics();
		int fontAscent = metrics.getAscent();
		int fontHeight = metrics.getHeight();
		Font font = this.g.getFont();
		List<FixedLabel> list = this.label.get(font);
		if (list == null) {
			list = new ArrayList<FixedLabel>();
			this.label.put(font, list);
		}
		// 表示領域内のであれば描画する
		if (this.screen.contains(x, y)) {
			int fontWidth = metrics.stringWidth(name);
			Rectangle labelCandidate = new Rectangle();
			labelCandidate.width = fontWidth + 8;
			labelCandidate.height = fontHeight + 3;
			labelCandidate.x = x;
			labelCandidate.y = y;
			this.lap.add(labelCandidate);
			list.add(new FixedLabel(name, labelCandidate.x + 2, labelCandidate.y + fontAscent));
		}
	}

	public void init(Graphics2D g, float scale, int width, int height, boolean isTextAntialiasing, boolean isShadow, boolean isFailure) {
		this.isShadow = isShadow;
		this.g = g;
		this.isTextAntialiasing = isTextAntialiasing;
		this.scale = scale;
		this.screen.width = width;
		this.screen.height = height;
		this.lap.clear();
		this.label.clear();
		this.isFailure = isFailure;
	}
}
