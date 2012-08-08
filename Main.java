import java.io.File;

import javax.swing.JOptionPane;

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
		String pluginDir = "plugin" + File.separatorChar;
		String libDir = "lib" + File.separatorChar;
		String styleDir = ".style" + File.separatorChar;
		switch (args.length) {
			case 4: styleDir = args[3];
			case 3: libDir = args[2];
			case 2: pluginDir = args[1];
			case 1: mapDir = args[0];
		}

		try {
			// 外部JARをロードします。
			Loader.addExternalJar(libDir);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "必要なライブラリが見つかりません。");
			return;
		}
		StartUp.startup(libDir, pluginDir, mapDir, styleDir);
	}
}
