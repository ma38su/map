package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.sourceforge.ma38su.util.Log;

/**
 * バージョン情報
 * @author ma38su
 * @since 0.80L
 */
public class Version {
	
	private Version() {
	}

	/**
	 * 現在のバージョンが最新であるかか確認します。
	 * @return 最新バージョン、現在バージョンが最新のバージョンであれば、nullを返します。
	 */
	public static String getVersion() {
		try {
			String ver = Version.get("/history.txt");
			String latest = Version.getLatest("ma38su", "KSJ Map");
			if (ver != null && latest != null && !ver.equals(latest)) {
				return latest;
			}
		} catch (IOException e) {
			Log.err(Version.class, e);
		}
		return null;
	}

	/**
	 * 現在のバージョンを確認します。
	 * 
	 * @param file 更新履歴ファイル
	 * @return fileから読み取れる最新のバージョン
	 */
	public static String get(String file) {
		Pattern pattern = Pattern.compile("^- ver.([^\\s　]*)");
		String ver = null;
		try {
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(Version.class.getResourceAsStream(file), "UTF-8"));
				while (in.ready()) {
					String line = in.readLine();
					Matcher match = pattern.matcher(line);
					if (match.find()) {
						ver = match.group(1);
					}
				}
			} finally {
				if (in != null) {
					in.close();
				}
			}
		} catch (IOException e) {
			ver = null;
		}
		return ver;
	}

	public static URL getURL(String project, String title, String version) throws IOException {
		Pattern titlePattern = Pattern.compile("<TD colspan=\"5\"><strong>" + title + "</strong></TD>");
		Pattern versionPattern = Pattern.compile("<strong><a name=\"[0-9]+\" class=\"frs_name\">"+ version +"</a></strong>");
		Pattern urlPattern = Pattern.compile("<TD bgcolor=\"#EEEEEE\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"(http://prdownloads.sourceforge.jp/[^<>/]+/[^<>/]+/[^<>/]+)\">[^<>/]+</a></TD>");
		int flag = 0;
		URL url = new URL("https://sourceforge.jp/projects/"+ project +"/files/");
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(url.openStream(), "EUC-JP"));
			String line;
			while ((line = in.readLine()) != null) {
				Matcher match = null;
				switch (flag) {
					case 0: 
						match = titlePattern.matcher(line);
					break;
					case 1: 
						match = versionPattern.matcher(line);
					break;
					case 2: 
						match = urlPattern.matcher(line);
					break;
				}
				if (match.find()) {
					if (flag == 2) {
						String str = match.group(1);
						return new URL(str);
					}
					flag++;
				}
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return null;
	}
	
	/**
	 * sourceforge.jpからソフトウェアの最新のバージョンを取得します。
	 * 
	 * @param project プロジェクト名
	 * @param title パッケージ名
	 * @return 最新のバージョンであれば、trueを返します。 
	 * @throws IOException
	 */
	public static String getLatest(String project, String title) throws IOException {
		Pattern titlePattern = Pattern.compile("<TD colspan=\"5\"><strong>" + title + "</strong></TD>");
		Pattern versionPattern = Pattern.compile("<strong><a name=\"[0-9]+\" class=\"frs_name\">([^<>]+)</a></strong>");
		int flag = 0;
		URL url = new URL("https://sourceforge.jp/projects/"+ project +"/files/");
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(url.openStream(), "EUC-JP"));
			String line;
			while ((line = in.readLine()) != null) {
				if (flag == 0) {
					Matcher match = titlePattern.matcher(line);
					if (match.find()) {
						flag = 1;
					}
				} else if (flag == 1) {
					Matcher match = versionPattern.matcher(line);
					if (match.find()) {
						return match.group(1);
					}
				}
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return null;
	}
}
