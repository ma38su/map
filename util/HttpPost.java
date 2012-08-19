package util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;


/**
 * URLへPostで情報を送信するためのクラス
 * 現在httpにのみ対応
 * @author ma38su
 */
public class HttpPost {

	/**
	 * USER-AGENT
	 * アクセス解析時に通常のブラウザと区別できるようにしておくことをオススメします。
	 */
	private String agent;

	/**
	 * コメントを投稿する記事のID
	 */
	private int id;

	/**
	 * referer
	 */
	private String referer;

	/**
	 * 送信先のURL
	 */
	private URL url;

	/**
	 * コンストラクタ
	 * @param url 投稿するURL
	 * @param referer 投稿する記事のURL
	 * @param agent USER-AGENT
	 * @param id 投稿する記事のID
	 * @throws MalformedURLException URLが不正な場合
	 * @throws IllegalArgumentException URLがhttp://で始まらない場合
	 */
	public HttpPost(String url, String referer, String agent, int id) throws MalformedURLException, IllegalArgumentException {
		if (url.startsWith("http://")) {
			this.url = new URL(url);
		} else {
			throw new IllegalArgumentException("URLはhttp://で始まらなければなりません。");
		}
		this.id = id;
		this.referer = referer;
		this.agent = agent;
	}

	/**
	 * 投稿します。
	 * @param map 投稿内容のMap
	 * @return 投稿の成否によらず、Javaの処理中にエラーが生じなければtrue
	 */
	public boolean post(Map<String, String> map) {
		map.put("submit", "Post");
		map.put("comment_post_ID", Integer.toString(this.id));
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (isFirst) {
				isFirst = false;
			} else {
				sb.append('&');
			}
			sb.append(entry.getKey());
			sb.append('=');
			sb.append(entry.getValue());
		}
		return this.post(sb.toString());
	}
	
	/**
	 * 文字列を投稿します。
	 * @param str Postの形式に変換した文字列
	 * @return 送信に成功すればtrue
	 */
	private boolean post(String str) {
		URLConnection connect = null;
		try {
			connect = this.url.openConnection();
			connect.setRequestProperty("User-Agent", this.agent);
			connect.setRequestProperty("Accept-Language", "ja");
			if (this.referer != null) {
				connect.setRequestProperty("referer", this.referer);
			}
			connect.setDoOutput(true);
			Writer out = null;
			try {
				out = new OutputStreamWriter(connect.getOutputStream(), "UTF-8");
				out.write(str);
				out.flush();
			} finally {
				if (out != null) {
					out.close();
				}
			}
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(connect.getInputStream(), "UTF-8"));
				String line;
				if ((line = in.readLine()) != null) {
					System.out.println(line);;
				}
			} finally {
				if (in != null) {
					in.close();
					in = null;
				}
			}
		} catch (IOException e) {
			Log.err(this, e);
			return false;
		}
		return true;
	}
	
	/**
	 * WordPressに投稿します。
	 * @param author 投稿者名
	 * @param email 電子メール
	 * @param url URL
	 * @param comment コメント
	 * @return Javaの処理に例外が発生しなければtrue
	 */
	public boolean postWP(String author, String email, String url, String comment) {
		StringBuilder sb = new StringBuilder();
		sb.append("submit=Post&comment_post_ID="+ Integer.toString(this.id));
		sb.append("&author=");
		if (author != null) {
			sb.append(author);
		}
		sb.append("&email=");
		if (email != null) {
			sb.append(email);
		}
		sb.append("&url=");
		if (url != null) {
			sb.append(url);
		}
		sb.append("&comment=");
		if (comment != null) {
			sb.append(comment);
		}
		return this.post(sb.toString());
	}
	
	/**
	 * コメントを投稿する記事のIDを設定します。
	 * @param id
	 */
	public void setPostID(int id) {
		this.id = id;
	}

	/**
	 * refererを設定します。
	 * 適切に設定しなければ、投稿に失敗します。
	 * @param referer
	 */
	public void setReferer(String referer) {
		this.referer = referer;
	}
}
