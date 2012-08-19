package index;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * セルメソッドを拡張した空間データベース
 * @author ma38su
 */
public class CellMethod {
	
	/**
	 * セルデータへのポインタ
	 */
	private RandomAccessFile index;
	
	/**
	 * セルの原点のX座標
	 */
	private int x;
	
	/**
	 * セルの原点のY座標
	 */
	private int y;
	
	/**
	 * セルの幅
	 */
	private int cellWidth;

	/**
	 * セルの高さ
	 */
	private int cellHeight;
	
	/**
	 * セルのX方向の分割数
	 */
	private int cx;
	
	/**
	 * セルのY方向の分割数
	 */
	private int cy;
	
	/**
	 * コンストラクタ
	 * @param dir セルのインデックスデータのディレクトリ
	 * @throws IOException 
	 */
	public CellMethod(String dir) throws IOException {
		this.index = new RandomAccessFile(dir + File.separatorChar + "sdf25k.idx", "r");
		try {
			this.x = this.index.readInt();
			this.y = this.index.readInt();
			int width = this.index.readInt();
			int height = this.index.readInt();
			this.cx = this.index.readUnsignedShort();
			this.cy = this.index.readUnsignedShort();
			if ((width % this.cx) == 0) {
				this.cellWidth = width / this.cx;
			} else {
				this.cellWidth = width / this.cx + 1;
			}
			if ((height % this.cy) == 0) {
				this.cellHeight = height / this.cy;
			} else {
				this.cellHeight = height / this.cy + 1;
			}
		} catch (IOException e) {
			this.index.close();
			throw new IllegalStateException(e);
		}
		this.disc = new RandomAccessFile(dir + File.separatorChar + "sdf25k.cell", "r");
	}
	
	/**
	 * セルデータ
	 */
	private RandomAccessFile disc;

	/**
	 * 範囲検索
	 * @param rect 検索範囲
	 * @return 検索範囲内の市区町村番号の配列
	 */
	public SortedMap<CellBounds, Integer> search2(final Rectangle rect) {
		SortedMap<CellBounds, Integer> map = new TreeMap<CellBounds, Integer>(new Comparator<CellBounds>() {
			public int compare(CellBounds o1, CellBounds o2) {
				return o1.getOverlapArea(rect) - o2.getOverlapArea(rect);
			}
		});
		int x = rect.x - this.x;
		int y = rect.y - this.y;
		int cx0 = x / this.cellWidth;
		int cy0 = y / this.cellHeight;
		int cx1 = (x + rect.width) / this.cellWidth;
		int cy1 = (y + rect.height) / this.cellHeight;
		if (cx0 < 0) {
			cx0 = 0;
		} else if (cx0 >= this.cx) {
			cx0 = this.cx - 1;
			return map;
		}
		if (cy0 < 0) {
			cy0 = 0;
		} else if (cy0 >= this.cy) {
			cy0 = this.cy - 1;
			return map;
		}
		if (cx1 >= this.cx) {
			cx1 = this.cx - 1;
		} else if (cx1 < 0) {
			cx1 = 0;
			return map;
		}
		if (cy1 >= this.cy) {
			cy1 = this.cy - 1;
		} else if (cy1 < 0) {
			cy1 = 0;
			return map;
		}
		try {
			for (int i = cy0; i <= cy1; i++) {
				for (int j = cx0; j <= cx1; j++) {
					this.index.seek(9 * (i * this.cx + j) + 20);
					long pointer = this.index.readLong();
					int size = this.index.readUnsignedByte();
					if (size > 0) {
						this.disc.seek(pointer);
						for (int k = 0; k < size; k++) {
							int code = this.disc.readUnsignedShort();
							CellBounds bounds = new CellBounds(this.disc);
							if (bounds.intersects(rect)) {
								map.put(bounds, code);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			map.clear();
		}
		return map;
	}

	/**
	 * 範囲検索
	 * @param rect 検索範囲
	 * @return 検索範囲内の市区町村番号の配列
	 */
	public Map<Integer, Set<Integer>> search(Rectangle rect) {
		HashMap<Integer, Set<Integer>> map = new HashMap<Integer, Set<Integer>>();
		int x = rect.x - this.x;
		int y = rect.y - this.y;
		int cx0 = x / this.cellWidth;
		int cy0 = y / this.cellHeight;
		int cx1 = (x + rect.width) / this.cellWidth;
		int cy1 = (y + rect.height) / this.cellHeight;
		if (cx0 < 0) {
			cx0 = 0;
		} else if (cx0 >= this.cx) {
			cx0 = this.cx - 1;
			return map;
		}
		if (cy0 < 0) {
			cy0 = 0;
		} else if (cy0 >= this.cy) {
			cy0 = this.cy - 1;
			return map;
		}
		if (cx1 >= this.cx) {
			cx1 = this.cx - 1;
		} else if (cx1 < 0) {
			cx1 = 0;
			return map;
		}
		if (cy1 >= this.cy) {
			cy1 = this.cy - 1;
		} else if (cy1 < 0) {
			cy1 = 0;
			return map;
		}
		try {
			for (int i = cy0; i <= cy1; i++) {
				for (int j = cx0; j <= cx1; j++) {
					this.index.seek(9 * (i * this.cx + j) + 20);
					long pointer = this.index.readLong();
					int size = this.index.readUnsignedByte();
					if (size > 0) {
						this.disc.seek(pointer);
						for (int k = 0; k < size; k++) {
							int code = this.disc.readUnsignedShort();
							CellBounds bounds = new CellBounds(this.disc);
							if (bounds.intersects(rect)) {
								int prefCode = code / 1000;
								if (!map.containsKey(prefCode)) {
									map.put(prefCode, new HashSet<Integer>());
								}
								map.get(prefCode).add(code);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			map.clear();
		}
		return map;
	}

	/**
	 * ファイルを閉じる。
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (this.disc != null) {
			this.disc.close();
			this.disc = null;
		}
		if (this.index != null) {
			this.index.close();
			this.index = null;
		}
	}
	
	/**
	 * インスタンスを破棄する際に開いたファイルを閉じる。
	 */
	@Override
	protected void finalize() throws Throwable {
		if (this.disc != null) {
			this.disc.close();
			this.disc = null;
		}
		if (this.index != null) {
			this.index.close();
			this.index = null;
		}
	}
}
