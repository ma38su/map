package view;

import javax.swing.JLabel;

import jp.sourceforge.ma38su.util.Log;

/**
 * JLabelを継承したステータスバー
 * @author ma38su
 */
public class StatusBar extends JLabel implements Runnable {

	/**
	 * 数値地図2500の識別子
	 */
	public static final int SDF2500 = 1;

	/**
	 * 数値地図25000の識別子
	 */
	public static final int SDF25000 = 2;
	
	/**
	 * 数値地図25000の識別子
	 */
	public static final int ISJ = 3;

	/**
	 * 数値地図25000の識別子
	 */
	public static final int KSJ = 4;

	/**
	 * 使用メモリ量（MB）
	 */
	private float memory;

	/**
	 * 補助記憶装置（ハードディスク）を読み込んでいる市区町村番号
	 */
	private String content;

	/**
	 * ステータスバーの更新のためのスレッド
	 */
	private Thread thread;

	
	/**
	 * 読み込んでいる地図の種類
	 */
	private int type;

	/**
	 * コンストラクタ
	 * @param msg 初期メッセージ
	 * @param db 市区町村名データベース
	 */
	public StatusBar(String msg) {
		super(msg);
		this.thread = new Thread(this);
		this.thread.start();
	}

	/**
	 * 読み込みが終了したことを通知します。
	 */
	public void finishReading() {
		this.content = null;
		Log.out(this, "cleared reading.");
	}
	
	private void loop() {
		boolean flag = false;
		StringBuilder sb = new StringBuilder();
		sb.append("MEMORY : " + this.memory + "MB");
		if (this.content != null) {
			/* ダウンロードしているファイルと読み込んでいるファイルが異なる場合 */
			flag = true;
			sb.append(" / ");
			switch (this.type) {
				case SDF2500 : 
					sb.append("SDF2500 ");
					break;
				case SDF25000 :
					sb.append("SDF25K ");
					break;
				case ISJ :
					sb.append("ISJ ");
					break;
			}
			sb.append(this.content);
		}
		if (sb.length() == 0) {
			super.setText(" ");
		} else {
			super.setText(sb.toString());
		}
		try {
			if (!flag) {
				Thread.sleep(2500L);
			} else {
				Thread.sleep(500L);
			}
		} catch (InterruptedException e) {
			Log.err(this, e);
		}
	}
	
	public void run() {
		while (true) {
			this.memory = (float) (int) ((Runtime.getRuntime().totalMemory() - Runtime
					.getRuntime().freeMemory()) / 1024) / 1024;
			this.loop();
		}
	}

	/**
	 * ステータスバーの更新スレッドの優先度を設定します。
	 * @param priority 優先度（0から5）
	 */
	public void setThreadPriority(int priority) {
		this.thread.setPriority(priority);
	}

	/**
	 * 読み込みを設定します。
	 * ローカルの補助記憶装置（ハードディスク）からの読み込んでいる市区町村番号を設定します。
	 * @param content 読み込んでいるファイルの情報（市区町村名等）
	 * @param type 読み込む地図の種類
	 */
	public void startReading(String content, int type) {
		this.type = type;
		this.content = content;
	}

	/**
	 * 読み込みを設定します。
	 * @param arg 表示する文字列
	 */
	public void startReading(String arg) {
		this.content = arg;
	}
}
