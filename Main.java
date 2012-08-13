import java.io.File;

import util.Loader;

/**
 * アプリケーション起動のためのクラス
 * 
 * 外部JARにクラスパスを通して、UIの設定を行います。
 * その後、StartUpクラスを呼び出します。
 * 
 * 外部JARにパスを通さないと、実行は難しいです。
 * 
 * @author ma38su
 */
public class Main {
	public static void main(String[] args) {
		String mapDir = ".data" + File.separatorChar;
		String libDir = "lib" + File.separatorChar;

		// 外部JARをロードします。
		if (Loader.addExternalJar(libDir)) {
			StartUp.startup(mapDir);
		}
	}
}
