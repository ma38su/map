package index;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * セルを用いた中間表現
 * @author ma38su
 */
public class CellBounds implements Shape, Serializable {
	
	public int getOverlapArea(Rectangle rect) {
		long flag = this.getCellFlag(rect);
		int count = 0;
		do {
			if (flag % 2 == 1) {
				count++;
			}
		} while ((flag /= 2) > 0);
		return count;
	}

	/**
	 *  0  1  2  3  4  5  6  7
	 *  8  9 10 11 12 13 14 15
	 * 16 17 18 19 20
	 * 
	 */
	private static final long[] maxX = new long[]{0L, 72340172838076673L, 217020518514230019L, 506381209866536711L, 1085102592571150095L, 2242545357980376863L, 4557430888798830399L, 9187201950435737471L, -1L};
	
	private static final long[] maxY = new long[]{0L, 255L, 65535L, 16777215L, 4294967295L, 1099511627775L, 281474976710655L, 72057594037927935L, -1L};
	private static final long[] minX = new long[]{-1L, -72340172838076674L, -217020518514230020L, -506381209866536712L, -1085102592571150096L, -2242545357980376864L, -4557430888798830400L, -9187201950435737472L, 0L};
	private static final long[] minY = new long[]{-1L, -256L, -65536L, -16777216L, -4294967296L, -1099511627776L, -281474976710656L, -72057594037927936L, 0L};

	/**
	 * 領域と重なるセルの数
	 */
	private int cellCount;

	/**
	 * セルとの交差フラグ
	 */
	private final long flagCell;

	/**
	 * 外接長方形の高さ
	 */
	private final int h;

	/**
	 * 外接長方形の幅
	 */
	private final int w;

	/**
	 * X座標
	 */
	private final int x;

	/**
	 * Y座標
	 */
	private final int y;
	
	public CellBounds(DataInput disc) throws IOException {
		this.x = disc.readInt();
		this.y = disc.readInt();
		this.w = disc.readInt();
		this.h = disc.readInt();
		this.flagCell = disc.readLong();
	}

	/**
	 * ポリゴンから中間表現を作成します。
	 * @param polygons
	 */
	public CellBounds(List<Polygon> polygons) {
		int x0 = Integer.MAX_VALUE;
		int y0 = Integer.MAX_VALUE;
		int x1 = Integer.MIN_VALUE;
		int y1 = Integer.MIN_VALUE;
		for (Polygon p : polygons) {
			Rectangle rect = p.getBounds();
			if (x0 > rect.x) {
				x0 = rect.x;
			}
			if (y0 > rect.y) {
				y0 = rect.y;
			}
			if (x1 < rect.x + rect.width) {
				x1 = rect.x + rect.width;
			}
			if (y1 < rect.y + rect.height) {
				y1 = rect.y + rect.height;
			}
		}
		this.x = x0;
		this.y = y0;
		this.w = x1 - x0;
		this.h = y1 - y0;
		Rectangle cell = new Rectangle();
		if ((this.w % 8) > 0) {
			cell.width = (this.w >> 3) + 1;
		} else {
			cell.width = this.w >> 3;
		}
		if ((this.h % 8) > 0) {
			cell.height = (this.h >> 3) + 1;
		} else {
			cell.height = this.h >> 3;
		}
		
		/* セル */
		this.cellCount = 0;
		cell.y = this.y;
		long flag = 0L;
		for (int i = 0; i < 8; i++, cell.y += cell.height) {
			cell.x = this.x;
			for (int j = 0; j < 8; j++, cell.x += cell.width) {
				for (Polygon p : polygons) {
					if (p.intersects(cell)) {
						flag += 1L << (i * 8 + j);
						this.cellCount++;
						break;
					}
				}
			}
		}
		this.flagCell = flag;
	}
	
	public CellBounds(Polygon[] polygons) {
		this(Arrays.asList(polygons));
	}
	
	public boolean contains(double x, double y) {
		throw new UnsupportedOperationException("未実装");
	}
	
	public boolean contains(double x, double y, double w, double h) {
		throw new UnsupportedOperationException("未実装");
	}
	
	public boolean contains(Point2D p) {
		throw new UnsupportedOperationException("未実装");
	}
	
	public boolean contains(Rectangle2D r) {
		throw new UnsupportedOperationException("未実装");
	}

	/**
	 * 外接長方形を返します。
	 * 
	 * @return 外接長方形
	 */
	public Rectangle getBounds() {
		return new Rectangle(this.x, this.y, this.w, this.h);
	}
	
	/**
	 * 外接長方形を返します。
	 * 
	 * @return 外接長方形
	 */
	public Rectangle2D getBounds2D() {
		return this.getBounds();
	}

	/**
	 * 検証のためのメソッドです。
	 * 
	 * @return 領域と重なるセルの数
	 */
	public int getCellAccuracy() {
		return this.cellCount;
	}

	/**
	 * 検索範囲とセルとの交差フラグを計算します。
	 * フラグの位置はX軸方向へとカウントし、Y軸方向へ繰り上げます。
	 * @param r 検索範囲
	 * @return セルとの交差フラグ
	 */
	private long getCellFlag(Rectangle r) {
		int cw = this.w >> 3;
		int ch = this.h >> 3;
		int bx = this.x - cw;
		int by = this.y - ch;
		int x0 = (r.x - bx) / cw - 1;
		int y0 = (r.y - by) / ch - 1;
		int x1 = (r.x + r.width - bx) / cw;
		int y1 = (r.y + r.height - by) / ch;
		return this.flagCell & this.getFlag(x0, x1, y0, y1);
	}

	/**
	 * セルの高さを返します。
	 * @return セルの高さ
	 */
	public int getCellHeight() {
		return this.h >> 3;
	}

	/**
	 * セルの幅を返します。
	 * @return セルの幅
	 */
	public int getCellWidth() {
		return this.w >> 3;
	}

	private long getFlag(int x0, int x1, int y0, int y1) {
		if (x0 < 0) {
			x0 = 0;
		} else if (x0 > 8) {
			x0 = 8;
		}
		if (y0 < 0) {
			y0 = 0;
		} else if (y0 > 8) {
			y0 = 8;
		}
		if (x1 > 8) {
			x1 = 8;
		} else if (x1 < 0) {
			x1 = 0;
		}
		if (y1 > 8) {
			y1 = 8;
		} else if (y1 < 0) {
			y1 = 0;
		}
		return CellBounds.minX[x0] & CellBounds.maxX[x1] & CellBounds.minY[y0] & CellBounds.maxY[y1];
	}

	public PathIterator getPathIterator(AffineTransform at) {
		throw new UnsupportedOperationException("未実装");
	}
	
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		throw new UnsupportedOperationException("未実装");
	}


	/**
	 * 最小のX座標を返します。
	 * @return 最小のX座標
	 */
	public int getX() {
		return this.x;
	}
	
	/**
	 * 最小のY座標を返します。
	 * @return 最小のY座標
	 */
	public int getY() {
		return this.y;
	}

	public boolean intersects(double x, double y, double w, double h) {
		throw new UnsupportedOperationException("未実装");
	}

	public boolean intersects(Rectangle r) {
		return this.intersectsCell(r);
	}

	public boolean intersects(Rectangle2D r) {
		throw new UnsupportedOperationException("未実装");
	}
	
	/**
	 * 外接長方形との交差判定を行います。
	 * @param r 交差判定を行うRectangle
	 * @return 交差していればtrue
	 */
	public boolean intersectsBounds(Rectangle r) {
		int tw = this.w;
		int th = this.h;
		int rw = r.width;
		int rh = r.height;
		if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
			return false;
		}
		int tx = this.x;
		int ty = this.y;
		int rx = r.x;
		int ry = r.y;
		rw += rx;
		rh += ry;
		tw += tx;
		th += ty;
		return ((rw < rx || rw > tx) && (rh < ry || rh > ty)
				&& (tw < tx || tw > rx) && (th < ty || th > ry));
	}
	
	/**
	 * セルを用いて領域の交差判定を行います。
	 * @param r 交差判定を行う領域
	 * @return 交差していればtrue
	 */
	public boolean intersectsCell(Rectangle r) {
		return this.getCellFlag(r) != 0;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("x:");
		sb.append(this.x);
		sb.append("\ny:");
		sb.append(this.y);
		sb.append("\nw:");
		sb.append(this.w);
		sb.append("\nh:");
		sb.append(this.h);
		sb.append("\nf:");
		sb.append(this.flagCell);
		return sb.toString();
	}

	/**
	 * セルを用いた中間表現を書き込みます。
	 * @param disc
	 * @throws IOException
	 */
	public void writeDisc(DataOutput disc) throws IOException {
		this.writeDiscMBR(disc);
		disc.writeLong(this.flagCell);
	}

	/**
	 * 外接長方形を書き込みます。
	 * @param disc
	 * @throws IOException
	 */
	public void writeDiscMBR(DataOutput disc) throws IOException {
		disc.writeInt(this.x);
		disc.writeInt(this.y);
		disc.writeInt(this.w);
		disc.writeInt(this.h);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CellBounds) {
			CellBounds bounds = (CellBounds) obj;
			return bounds.x == this.x && bounds.y == this.y && bounds.w == this.w && bounds.h == this.h && bounds.flagCell == bounds.flagCell;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.x + this.y;
	}
}
