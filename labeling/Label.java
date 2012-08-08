package labeling;

/**
 * ラベル
 * @author ma38su
 */
public interface Label {

	public static final String KEY_CM = "city";

	public static final String KEY_KO = "facility";

	/**
	 * 
	 * @return ラベル名
	 */
	public String getName();

	/**
	 * 
	 * @return ラベル位置のX座標
	 */
	public int getLng();
	/**
	 * 
	 * @return ラベル位置のY座標
	 */
	public int getLat();
}
