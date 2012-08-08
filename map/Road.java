package map;



/**
 * 道路の区間
 * @author ma38su
 */
public class Road extends Curve {

	/**
	 * 道路の距離
	 */
	private final float cost;

	/**
	 * 道路種別
	 * type = 16 - 12 = 4 高速道路
	 *        15 - 12 = 3 幹線道路
	 *        15 - 13 = 2 一般道
	 *        14 - 13 = 1 石段
	 *        13 - 13 = 0 庭園
	 */
	private final int width;
	private final String name;
	public String getName() {
		return this.name;
	}
	public Road() {
		super(null, null, 0);
		this.name = null;
		this.cost = 0;
		this.width = 0;
	}
	public Road(String name, int[] aryX, int[] aryY, int type, int width, float cost) {
		super(aryX, aryY, type);
		this.name = name;
		assert aryX.length == aryY.length;
		this.cost = cost;
		this.width = width;
	}
	
	public Road(int type, int width, float cost) {
		super(null, null, type);
		this.name = null;
		this.cost = cost;
		this.width = width;
	}
	/**
	 * 幅員を取得
	 * 0 - 5
	 * @return 幅員
	 */
	public int getWidth() {
		return this.width;
	}
	public float getCost() {
		return this.cost;
	}
}