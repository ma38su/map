package jp.sourceforge.ma38su.util;

/**
 * ログ出力のためのクラス
 * @author ma38su
 */
public class Log {

	/**
	 * デバッグモードの時はtrue
	 */
	public static boolean isDebug = true;

	public static boolean showError = true;
	/**
	 * 標準出力
	 * @param c 呼び出し元のクラス
	 * @param message メッセージ
	 */
	@SuppressWarnings("rawtypes")
	public static void out(Class c, String message) {
		if (Log.isDebug) {
			System.out.println(c.getName() + ": "+ message);
		}
	}

	/**
	 * 標準出力
	 * @param o 呼び出し元のインスタンス
	 * @param message メッセージ
	 */
	public static void out(Object o, String message) {
		if (Log.isDebug) {
			System.out.println(o.getClass().getName() + ": "+ message);
		}
	}

	/**
	 * エラー出力
	 * @param o 呼び出し元のインスタンス
	 * @param e 投げられたクラス
	 */
	public static void err(Object o, Throwable e) {
		Log.err(o.getClass(), e);
	}

	/**
	 * エラー出力
	 * @param c 呼び出し元のクラス
	 * @param message メッセージ
	 */
	@SuppressWarnings("rawtypes")
	public static void err(Class c, String message) {
		System.out.println(c.getName() + ": "+ message);
	}

	/**
	 * エラー出力
	 * @param o 呼び出し元のインスタンス
	 * @param message
	 */
	public static void err(Object o, String message) {
		Log.err(o.getClass(), message);
	}

	/**
	 * エラー
	 * @param c 呼び出し元のクラス
	 * @param e 投げられたクラス
	 */
	@SuppressWarnings("rawtypes")
	public static void err(Class c, Throwable e) {
		if (Log.showError) {
			e.printStackTrace();
		} else if (Log.isDebug) {
			for (StackTraceElement stack : e.getStackTrace()) {
				if (c.equals(stack.getClass())) {
					Log.err(c, stack.getLineNumber() + " - "+ e.getMessage());
					break;
				}
			}
		}
	}

	private Log() {
	}
}
