package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.sourceforge.ma38su.util.Log;

/**
 * 設定ファイル
 * @author ma38su
 */
public class Setting {

	private static String CODE = "UTF-8";

	/**
	 * バグレポートのキー
	 */
	public static final String KEY_REPORT = "REPORT";
	
	/**
	 * 利用規約確認のキー
	 */
	public static final String KEY_TERMS = "TERMS";

	/**
	 * 更新確認のキー
	 */
	public static final String KEY_UPDATE = "UPDATE";
		
	/**
	 * 設定ファイルのバージョンのキー
	 */
	public static final String KEY_VERSION = "VERSION";

	public static String version;

	public static String getVersion() {
		return Setting.version;
	}

	public static void setVersion(String version) {
		Setting.version = version;
	}
	/**
	 * 設定の保存先
	 */
	private transient File file;
	
	/**
	 * 設定のマップ
	 */
	private final Map<String, String> map;
	

	/**
	 * 設定ファイルを読み込みます。
	 * ファイル読み込みで入出力エラーが生じた場合は、設定ファイルは初期化されます。
	 * 設定ファイルが存在しない場合には、
	 * @param file 設定ファイル
	 * @param ver バージョン
	 */
	public Setting(File file) {
		this.file = file;
		this.map = new TreeMap<String, String>();
		try {
			Pattern pattern = Pattern.compile("([^\\s=#/]+)\\s*=\\s*([^\\s=#/]+)\\s*");
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(new FileInputStream(file), CODE));
				String line;
				while ((line = in.readLine()) != null) {
					Matcher match = pattern.matcher(line);
					if (match.matches()) {
						String key = match.group(1);
						String value = match.group(2);
						this.map.put(key, value);
					}
				}
			} finally {
				if (in != null) {
					in.close();
				}
			}
			String initVer = this.map.get(KEY_VERSION);
			if (initVer == null || !initVer.equals(Setting.version)) {
				// バージョン間の互換性がないキーを削除
				this.map.remove(KEY_TERMS);
				this.map.put(KEY_VERSION, Setting.version);
				this.write();
			}
		} catch (IOException e) {
			this.map.clear();
			this.map.put(KEY_VERSION, Setting.version);
			this.write();
		}
	}
	
	/**
	 * 設定ファイルを読み込みます。
	 * ファイル読み込みで入出力エラーが生じた場合は、設定ファイルは初期化されます。
	 * 設定ファイルが存在しない場合には、
	 * @param file 設定ファイル
	 * @param ver バージョン
	 */
	public Setting(String file) {
		this.map = new TreeMap<String, String>();
		this.file = new File(file);
		if (!this.file.getParentFile().isDirectory()) {
			this.file.getParentFile().mkdirs();
		}
		try {
			Pattern pattern = Pattern.compile("([^\\s=#/]+)\\s*=\\s*([^\\s=#/]+)\\s*");
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(new FileInputStream(this.file), CODE));
				String line;
				while ((line = in.readLine()) != null) {
					Matcher match = pattern.matcher(line);
					if (match.matches()) {
						String key = match.group(1);
						String value = match.group(2);
						this.map.put(key, value);
					}
				}
			} finally {
				if (in != null) {
					in.close();
				}
			}
			String initVer = this.map.get(KEY_VERSION);
			if (initVer == null || !initVer.equals(Setting.version)) {
				// バージョン間の互換性がないキーを削除
				this.map.remove(KEY_TERMS);
				this.map.put(KEY_VERSION, Setting.version);
				this.write();
			}
		} catch (IOException e) {
			this.map.clear();
			this.map.put(KEY_VERSION, Setting.version);
			this.write();
		}
	}

	
	/**
	 * 設定を取得します。
	 * @param key 設定のキー
	 * @return キーに対する値
	 */
	public String get(String key) {
		return this.map.get(key);
	}

	/**
	 * 更新情報の確認するかどうか設定します。
	 * @param key Key of setting
	 * @param value Value of setting
	 */
	public void set(String key, String value) {
		this.map.put(key, value);
		this.write();
	}
	/**
	 * 設定を保存します。
	 * @return 保存に成功すればtrue
	 */
	public boolean write() {
		try {
			PrintWriter out = null;
			try {
				out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.file), CODE));
				for (Map.Entry<String, String> entry : this.map.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					if (key != null && value != null) {
						out.printf("%s = %s\n", key, value);
						out.flush();
					}
				}
			} finally {
				if (out != null) {
					out.close();
				}
			}
		} catch (IOException e) {
			if (this.file.exists()) {
				this.file.delete();
			}
			Log.err(this, e);
			return false;
		}
		return true;
	}
}
