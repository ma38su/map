package map.store;

import java.awt.Point;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import map.DataCity;

/**
 * 店舗情報のクラス
 * @author ma38su
 */
public interface Store {
	public String getName();
	public List<Point> getLocation(DataCity city, Map<String, Point> locationMap) throws IOException;
	public void setCacheDirectory(String cacheDir);
}
