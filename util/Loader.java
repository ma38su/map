package util;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * クラスをロードするためのクラス
 * @author ma38su
 */
public class Loader {

	/**
	 * 外部JARをロードします。
	 * @param dirPath 外部Jar格納ディレクトリ
	 * @return 外部Jarの設定の成否
	 */
	public static boolean addExternalJar(String dirPath) {
		boolean ret = true;
		File dir = new File(dirPath);
		if (dir.isDirectory()) {
			FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".jar");
				}
			};
			for (String name : dir.list(filter)) {
				if (!Loader.addClassPath(new File(name))) {
					ret = false;
				}
			}
		}
		return ret;
	}

	/**
	 * @param file External Jar Library
	 * @return クラスパスを追加の成否
	 */
	public static boolean addClassPath(File file) {
		boolean ret = true;
		try {
			// URLClassLoaderのprotectedメソッドaddURLを取得する
			Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
			// アクセス可能に変更する
			method.setAccessible(true);
			// システムクラスローダーをURLClassLoaderと仮定し、addURLをコールする
			method.invoke(ClassLoader.getSystemClassLoader(), new Object[] { file.toURI().toURL() });
		} catch (Exception e) {
			ret = false;
			e.printStackTrace();
		}
		return ret;
	}

}
