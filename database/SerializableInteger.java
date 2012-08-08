package database;
import java.io.Serializable;

/**
 * 数値データ
 * Serializable可能なInteger型
 * @author ma38su
 */
class SerializableInteger implements Serializable {

	/**
	 * 数値
	 */
	private int n;
	
	/**
	 * コンストラクタ
	 * @param str
	 */
	SerializableInteger(String str) {
		this.n = Integer.parseInt(str);
	}

	/**
	 * コンストラクタ
	 * @param pointer
	 */
	SerializableInteger(long pointer) {
		this.n = (int) (pointer % Integer.MAX_VALUE);
	}

	SerializableInteger() {
		this.n = 0;
	}
	
	/**
	 * 値を設定します。
	 * @param n 数値
	 */
	void setValue(int n) {
		this.n = n;
	}
	
	/**
	 * 値を取得します。
	 * @return 数値
	 */
	int getValue() {
		return this.n;
	}
	
	@Override
	public String toString() {
		return Integer.toString(this.n);
	}

	/**
     * Integer のハッシュコードを返します。
     * 
     * @return  このオブジェクトのハッシュコード値。この Integer
     *          オブジェクトが表すプリミティブ型 int 値に等しい
     */
    @Override
	public int hashCode() {
        return this.n;
    }
    
    /**
     * このオブジェクトを指定されたオブジェクトと比較します。結果が true
     * になるのは、引数が null ではなく、このオブジェクトと
     * 同じ int 値を含む Integer オブジェクトである
     * 場合だけです。
     * 
     * @param   obj 比較対象のオブジェクト
     * @return  オブジェクトが同じである場合は true、そうでない場合は
     *          false
     */
    @Override
	public boolean equals(Object obj) {
        if (obj instanceof Integer) {
            return this.n == ((Integer) obj).intValue();
        } else if (obj instanceof SerializableInteger) {
        	return this.n == ((SerializableInteger) obj).n;
        }
        return false;
    }
}
