package map.route;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import map.sdf25k.Node;

/**
 * 経路探索のためのヒープです。
 * キーに対して値を持たせ、値の比較によって、ヒープ（優先度付キュー）を構築します。
 * Comparatorをコンストラクタに与えなければ、要素は最小の値を根にしてヒープを構成します。
 * 
 * キーに対する値を更新する場合には、以前の値よりも根に近い（小さい）と評価される場合のみ
 * 更新されます。
 *
 * @author ma38su
 *
 */
public class Heap {

	/**
	 * 標準の初期容量
	 */
	private static final int DEFAULT_CAPACITY = 10;

	/**
	 * ソートされるオブジェクト
	 */
	private Entry[] entries;

	/**
	 * ヒープのサイズ
	 */
	private int size;

	/**
	 * キーの管理のためのMap
	 */
	private final Map<Long, Integer> table;
	
	/**
	 * 順序付け
	 */
	private final Comparator<Entry> comparator;

	/**
	 * コンストラクタ
	 *
	 */
	public Heap() {
		this(null);
	}

	/**
	 * コンストラクタ
	 * @param initialCapacity 初期容量
	 */
	public Heap(int initialCapacity) {
		this(initialCapacity, null);
	}
	/**
	 * コンストラクタ
	 * @param comparator
	 */
	public Heap(Comparator<Entry> comparator) {
		this(Heap.DEFAULT_CAPACITY, comparator);
	}

	/**
	 * コンストラクタ
	 * @param initialCapacity 初期容量
	 * @param comparator
	 */
	public Heap (int initialCapacity, Comparator<Entry> comparator) {
		if (initialCapacity < 1) {
			throw new IllegalArgumentException();
		}
		this.size = 0;
		this.entries = new Entry[initialCapacity + 1];
		this.table = new HashMap<Long, Integer>();
		this.comparator = comparator;
	}

	/**
	 * keyが存在していればvalue更新、keyが存在してなければ場合は挿入する
	 * @param key 挿入する key
	 * @param value 挿入する value
	 * @return 更新または挿入がおこなえればtrue
	 */
	public boolean put(Node key, float value) {
		Entry entry = new Entry(key, value);
		Integer pointer = this.table.get(key.getID());
		if (pointer != null) {
			int index = pointer.intValue();
			if (this.comparator == null) {
				if(((Comparable<Entry>)this.entries[index]).compareTo(entry) > 0) {
					this.entries[index] = entry;
					this.fixUp(index);
				} else {
					return false;
				}
			} else {
				if (this.comparator.compare(this.entries[index], entry) > 0) {
					this.entries[index] = entry;
					this.fixUp(index);
				} else {
					return false;
				}
			}
		} else {
			this.grow(++this.size);
			this.table.put(key.getID(), this.size);
			this.entries[this.size] = entry;
			this.fixUp(this.size);
		}
		return true;
	}

	/**
	 * 入れ替える
	 * @param index1
	 * @param index2
	 */
	private void swap(int index1, int index2) {
		final Entry tmp = this.entries[index1];
		this.entries[index1] = this.entries[index2];
		this.entries[index2] = tmp;
		this.table.put(this.entries[index1].getKey().getID(), index1);
		this.table.put(this.entries[index2].getKey().getID(), index2);
	}

	/**
	 * ヒープの先頭（根）の要素を削除して取り出す
	 * @return ヒープの先頭の要素
	 */
	public Entry poll() {
		if (this.size == 0) {
			return null;
		}

		final Entry entry = this.entries[1];
		this.table.remove(entry.getKey());
		if (this.size > 1) {
			this.entries[1] = this.entries[this.size];
			this.table.put(this.entries[1].getKey().getID(), 1);
		}
		this.entries[this.size] = null;
		if (--this.size > 1) {
			this.fixDown(1);
		}
		return entry;
	}

	/**
	 * 削除せずにヒープの先頭（根）の要素を取り出す
	 * @return ヒープの先頭の要素
	 */
	public Entry peek() {
		return this.entries[1];
	}

	/**
	 * key から value を取得
	 * @param key
	 * @return value
	 */
	public float get(Object key){
		return this.entries[this.table.get(key)].getValue();
	}

	/**
	 * @param key 確認する key
	 * @return keyが含まれていれば true
	 */
	public boolean containsKey(Object key) {
		return this.table.containsKey(key);
	}

	/**
	 * 子との状態の比較
	 * @param index
	 */
	private void fixDown(int index) {
		int son;
		if (this.comparator == null) {
			while ((son = index << 1) <= this.size) {
				if (son < this.size && this.entries[son].compareTo(this.entries[son+1]) > 0) {
					son++;
				}
				if (this.entries[index].compareTo(this.entries[son]) <= 0) {
					break;
				}
				this.swap(index, son);
				index = son;
			}
		} else {
			while ((son = index << 1) <= this.size) {
				if (son < this.size && this.comparator.compare(this.entries[son], this.entries[son+1]) > 0) {
					son++;
				}
				if (this.comparator.compare(this.entries[index], this.entries[son]) <= 0) {
					break;
				}
				this.swap(index, son);
				index = son;
			}
		}
	}

	/**
	 * 親との状態を確認
	 * @param index
	 */
	private void fixUp(int index) {
		int parent;
		if (this.comparator == null) {
			while ((parent = index >> 1) > 0) {
				if (this.entries[index].compareTo(this.entries[parent]) >= 0) {
					break;
				}
				this.swap(index, parent);
				index = parent;
			}
		} else {
			while ((parent = index >> 1) > 0) {
				if (this.comparator.compare(this.entries[index], this.entries[parent]) >= 0) {
					break;
				}
				this.swap(index, parent);
				index = parent;
			}
		}
	}

	/**
	 * ヒープが空でないか確かめる。
	 * @return ヒープに要素がなければtrue
	 */
	public boolean isEmpty() {
		return this.size == 0;
	}

	/**
	 * 配列のサイズを拡張する
	 * @param index
	 */
	private void grow(int index) {
		int newLength = this.entries.length;
		if (index < newLength) {
			return;
		}
		if (index == Integer.MAX_VALUE) {
			throw new OutOfMemoryError();
		}
		while (newLength <= index) {
			if (newLength >= Integer.MAX_VALUE / 2) {
				newLength = Integer.MAX_VALUE;
			} else {
				newLength <<= 2;
			}
		}
		final Entry[] newEntrys = new Entry[newLength];
		System.arraycopy(this.entries, 0, newEntrys, 0, this.entries.length);

		this.entries = newEntrys;
	}

	/**
	 * 値を返すためのクラス
	 * 
	 * @author ma38su
	 *
	 */
	public class Entry implements Comparable<Entry> {

		/**
		 * 頂点
		 */
		private final Node key;
		
		/**
		 * 頂点に対応する値
		 */
		private final float value;

		public Entry(Node key, float value) {
			this.key = key;
			this.value = value;
		}

		/**
		 * キーを取得
		 * @return キー
		 */
		public Node getKey() {
			return this.key;
		}

		/**
		 * 値を取得
		 * @return 値
		 */
		public float getValue() {
			return this.value;
		}

		@Override
		public String toString() {
			return this.key +"->"+ Float.toString(this.value);
		}

		public int compareTo(Entry o) {
			if (this.value > o.value) {
				return 1;
			} else if (this.value < o.value) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	@Override
	public String toString() {
		if (this.size == 0) {
			return "";
		}
		final StringBuilder sb = new StringBuilder(this.entries[1].toString());
		for(int i = 2; i <= this.size; i++) {
			sb.append("," + this.entries[i].toString());
		}
		return sb.toString();
	}

	/**
	 * ヒープのサイズを返します。
	 * @return ヒープのサイズ
	 */
	public int size() {
		return this.size;
	}
}
