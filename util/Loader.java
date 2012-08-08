package util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import jp.sourceforge.ma38su.util.GeneralFileFilter;
import jp.sourceforge.ma38su.util.Log;

import map.store.Store;

/**
 * クラスをロードするためのクラス
 * @author ma38su
 */
public class Loader {

	/**
	 * 外部JARをロードします。
	 * @param dir
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws NoSuchMethodException 
	 * @throws MalformedURLException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 */
	public static void addExternalJar(String dir) throws SecurityException, IllegalArgumentException, MalformedURLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		File library = new File(dir);
		if (library.isDirectory()) {
			for (File file : library.listFiles()) {
				if (file.getName().toLowerCase().endsWith(".jar")) {
					Loader.addClassPath(file.toURI().toURL());
				}
			}
		}
	}

	/**
	 * クラスパスを追加する。
	 * @param url URL 追加するパッケージのあるのURL
	 * @throws NoSuchMethodException 
	 * @throws SecurityException
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static void addClassPath(URL url) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// URLClassLoaderのprotectedメソッドaddURLを取得する
		Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
		// アクセス可能に変更する
		method.setAccessible(true);
		// システムクラスローダーをURLClassLoaderと仮定し、addURLをコールする
		method.invoke(ClassLoader.getSystemClassLoader(), new Object[] { url });
	}

	/**
	 * 店舗情報取得のためのプラグインをロードします。
	 * @param path プラグインのあるパス
	 * @param cacheDir 店舗情報のキャッシュディレクトリ
	 * @return 店舗情報のリスト
	 */
	public static List<Store> loadStorePlugin(String path, String cacheDir) {
		List<Store> list = new ArrayList<Store>();
		File dir = new File(path);
		if (dir.isDirectory()) {
			try {
				for (File file : dir.listFiles(new GeneralFileFilter("jar"))) {
					Loader.addClassPath(file.toURI().toURL());
					JarFile jar = new JarFile(file);
					Enumeration<JarEntry> e = jar.entries();
					while (e.hasMoreElements()) {
						String name = e.nextElement().getName();
						if (name.endsWith("Plugin.class")) {
							String title = name.substring(0, name.length() - 6);
							Store store = (Store) Class.forName(title.replaceAll("/", ".")).newInstance();
							store.setCacheDirectory(cacheDir);
							list.add(store);
							Log.out(Loader.class, "loaded "+ store.getClass().getName());
						}
					}
				}
			} catch (Throwable e) {
				Log.err(Loader.class, e);	
			}
		}
		return list;
	}
}
