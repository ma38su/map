package jp.sourceforge.ma38su.util;

import java.io.File;

/**
 * ダウンロードするファイル
 * @author ma38su
 */
public class DLFile extends File {
	public static final int STATE_FINISH = 0;
	public static final int STATE_DOWNLOAD = 1;
	public static final int STATE_READ = 2;

	private final String label;
	private int state;
	private int length;

	public DLFile(String label, String name) {
		super(name);
		this.label = label;
	}
	
	/**
	 * ファイルサイズを設定します。
	 * @param length
	 */
	public void setContentLength(int length) {
		this.length = length;
	}

	/**
	 * 状態を変更します。
	 * @param state
	 */
	public void setState(int state) {
		this.state = state;
	}

	/**
	 * ファイルの状態を返します。
	 * @return ファイルの状態
	 */
	public int getState() {
		return this.state;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		switch (this.state) {
		case STATE_DOWNLOAD :
			sb.append("DOWNLOAD ");
			sb.append(this.label);
			sb.append(": ");
			sb.append(this.length() * 100 / this.length);
			sb.append('%');
			break;
		case STATE_READ :
			sb.append("READ　");
			sb.append(this.label);
			break;
		}
		return sb.toString();
	}
}
