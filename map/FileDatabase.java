package map;

import java.awt.Polygon;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 数値地図のデータ管理
 * @author ma38su
 */
public class FileDatabase {

	/**
	 * @param path パス
	 * @param obj 
	 * @return 書き込み成否
	 */
	public static <T> boolean writeSerializableArchive(String path, T obj) {
		boolean ret = true;
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));
			try {
				out.writeObject(obj);
			} finally {
				out.close();
			}
		} catch (Exception e) {
			ret = false;
		}
		return ret;
	}


}
