package view;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.biojava.bio.program.das.client.BrowserLauncher;

import util.HttpPost;
import util.Setting;
import util.Version;
import controller.MapController;

/**
 * ダイアログを生成するクラス
 * @author ma38su
 */
public class DialogFactory {
	
	/**
	 * 最新バージョンが取得できるページのURL
	 */
	private static final String DOWNLOAD_URL = "http://ma38su.sourceforge.jp/map/download/";

	/**
	 * 最新バージョンが取得できるページのURL
	 */
	private static final String HELP_URL = "http://ma38su.sourceforge.jp/map/help/";
	
	/**
	 * 更新情報を確認します。
	 * @param version 現在バージョン
	 * @param comp コンポーネント
	 * @param controller コントローラ
	 */
	public static void versionDialog(String version, Component comp, MapController controller) {
		if (version != null) {
			JPanel panel = new JPanel(new GridLayout(0, 1, 0, 0));
			panel.add(new JLabel("最新版 ver."+ version + " が見つかりました。"));
			boolean isUpdate = !"false".equalsIgnoreCase(controller.getSetting().get(Setting.KEY_UPDATE));
			JCheckBox check = new JCheckBox("起動時に更新情報を確認する", isUpdate);
			check.setActionCommand("check");
			check.addActionListener(controller);
			panel.add(check);
			String title = "Digital Map ver."+ Version.get("/history.txt");
			int ret = JOptionPane.showOptionDialog(comp, panel, title, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"最新版を確認", "無視"}, null);
			if (ret == JOptionPane.YES_OPTION) {
				try {
					BrowserLauncher.openURL(DOWNLOAD_URL);
				} catch (IOException ex) {
					JPanel text = new JPanel(new GridLayout(0, 1, 0, 0));
					text.add(new JLabel("ブラウザを開けませんでした。"));
					text.add(new JLabel("最新バージョンは以下のページで公開しています。"));
					text.add(new JTextField(DOWNLOAD_URL, SwingConstants.CENTER));
					JOptionPane.showMessageDialog(comp, text, "IO Exception", JOptionPane.ERROR_MESSAGE);
				}
			}
		} else {
			JOptionPane.showMessageDialog(comp, "新しい更新はみつかりませんでした。", "更新情報", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	/**
	 * 緯度経度を指定して表示位置を移動させるためのダイアログ
	 * @param panel 地図パネル
	 * @param controller コントローラ
	 */
	public static void locationDialog(final MapPanel panel, MapController controller) {
		final JDialog dialog = new JDialog();
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setTitle("移動");
		dialog.setLayout(new GridLayout(0, 1, 0, 0));
		dialog.add(new JLabel("経度緯度を指定して移動します。", SwingConstants.CENTER));
		LayoutManager fieldLayout = new FlowLayout(FlowLayout.CENTER, 5, 3);
		JPanel xFieldPanel = new JPanel(fieldLayout);
		final JTextField xField = new JTextField(5);
		xField.setText(Float.toString((int) (controller.getLocationMouseX() * 100) / 100f));
		xFieldPanel.add(new JLabel("経度 :"));
		xFieldPanel.add(xField);

		JPanel yFieldPanel = new JPanel(fieldLayout);
		final JTextField yField = new JTextField(5);
		yField.setText(Float.toString((int) (controller.getLocationMouseY() * 100) / 100f));
		yFieldPanel.add(new JLabel("緯度 :"));
		yFieldPanel.add(yField);

		JPanel fieldPanel = new JPanel(fieldLayout);
		fieldPanel.add(xFieldPanel);
		fieldPanel.add(yFieldPanel);

		JPanel buttonPanel = new JPanel(fieldLayout);
		JButton export = new JButton("移動");
		export.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					double x = Double.parseDouble(xField.getText());
					double y = Double.parseDouble(yField.getText());
					panel.setMapLocation(x, y);
					panel.repaint();
					dialog.dispose();
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(dialog, "数値を入力してください。", "警告", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
		});

		JButton cancel = new JButton("取消");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});

		buttonPanel.add(export);
		buttonPanel.add(cancel);

		dialog.add(fieldPanel);
		dialog.add(buttonPanel);
		dialog.pack();
		dialog.setLocationRelativeTo(panel);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
		dialog.setFocusable(true);
	}
	
	private static JCheckBox bugCheck;

	public static void setBugCheckBox(JCheckBox cbox) {
		bugCheck = cbox;
	}
	/**
	 * 致命的なエラーが発生した場合に表示するダイアログ
	 * エラーレポートの協力をお願いします。
	 * このメソッドを呼び出すと、自動的に終了します。
	 * @param comp 
	 * @param e 致命的なエラー
	 * @param setting 設定ファイル
	 */
	public static void errorDialog(JComponent comp, Throwable e) {
		if (bugCheck.isSelected()) {
			JPanel text = new JPanel(new GridLayout(0, 1, 0, 0));
			text.add(new JLabel("申し訳ございません。致命的な問題が発生しました。", SwingConstants.LEFT));
			text.add(new JLabel("品質向上のため、この問題を報告してください。", SwingConstants.LEFT));
			text.add(bugCheck);
			int ret = JOptionPane.showConfirmDialog(comp, text, "Digital Map - 致命的なエラー", JOptionPane.YES_NO_OPTION);
			if (ret == JOptionPane.YES_OPTION) {
				StringBuilder sb = new StringBuilder(e.toString());
				for (StackTraceElement element : e.getStackTrace()) {
					sb.append(' ');
					sb.append(element.toString());
				}
				String ver = Version.get("/history.txt");
				String author = ver != null ? "Digital Map ver."+ ver : "Digital Map";
				try {
					HttpPost post = new HttpPost("http://ma38su.sourceforge.jp/wordpress/wp-comments-post.php", "http://ma38su.sourceforge.jp/map/help/", "Digital Map Bug Reporter", 50);
					post.postWP(author, null, null, sb.toString());
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				try {
					BrowserLauncher.openURL("http://ma38su.sourceforge.jp/map/help/");
				} catch (IOException ex) {
					text = new JPanel(new GridLayout(0, 1, 0, 0));
					text.add(new JLabel("ブラウザを開けませんでした。"));
					text.add(new JLabel("ヘルプデスクは以下のページです。"));
					text.add(new JTextField(HELP_URL, SwingConstants.CENTER));
					JOptionPane.showMessageDialog(comp, text, "IO Exception", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	/**
	 * メモリが不足した場合に表示するダイアログ
	 * @param comp 
	 * @param e OutOfMemoryError
	 */
	public static void memoryDialog(JComponent comp, Throwable e) {
		JPanel text = new JPanel(new GridLayout(0, 1, 0, 0));
		text.add(new JLabel("メモリが不足しました。", SwingConstants.LEFT));
		text.add(new JLabel("詳しくはヘルプデスク（WEB）をご覧ください。", SwingConstants.LEFT));
		String str = "ヘルプデスクを開く";
		int ret = JOptionPane.showOptionDialog(comp, text, "Digital Map - メモリ不足", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{str, "閉じる"}, str);
		if (ret == JOptionPane.YES_OPTION) {
			try {
				BrowserLauncher.openURL("http://ma38su.sourceforge.jp/map/help/");
			} catch (IOException ex) {
				text = new JPanel(new GridLayout(0, 1, 0, 0));
				text.add(new JLabel("ブラウザを開けませんでした。"));
				text.add(new JLabel("ヘルプデスクは以下のページです。"));
				text.add(new JTextField(HELP_URL, SwingConstants.CENTER));
				JOptionPane.showMessageDialog(comp, text, "IO Exception", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * DigitalMapの情報を表示するダイアログ
	 */
	public static void aboutDialog() {
		String title = "Digital Map ver."+ Version.get("/history.txt");
		JPanel text = new JPanel(new GridLayout(0, 1, 0, 0));
		text.add(new JLabel(title, SwingConstants.LEFT));
		text.add(new JLabel("Copyright 2005-2006 ma38su", SwingConstants.LEFT));
		text.add(new JLabel(" "));
		text.add(new JLabel("Digital Mapの詳細はWEBをご覧ください。"));
		int ret = JOptionPane.showOptionDialog(null, text, title, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"詳細", "閉じる"}, null);
		if (ret == JOptionPane.YES_OPTION) {
			try {
				BrowserLauncher.openURL("http://ma38su.sourceforge.jp/map/");
			} catch (IOException e) {
				text = new JPanel(new GridLayout(0, 1, 0, 0));
				text.add(new JLabel("ブラウザを開けませんでした。"));
				text.add(new JLabel("Digital Mapの詳細は以下のURLをご覧ください。"));
				text.add(new JTextField("http://ma38su.sourceforge.jp/map/", SwingConstants.CENTER));
				JOptionPane.showMessageDialog(null, text, "IO Exception", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * 地図データの利用についてを読んでもらうために表示する。
	 * @param setting 
	 */
	public static void termsDialog(Setting setting) {
		String title = "Digital Map ver."+ Version.get("/history.txt");
		JPanel text = new JPanel(new GridLayout(0, 1, 0, 0));
		text.add(new JLabel("本ソフトウェア利用の前に必ずお読みください。"));
		JButton button = new JButton("Digital Map / 利用に際して");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					BrowserLauncher.openURL("http://ma38su.sourceforge.jp/map/licence/");
				} catch (IOException ex) {
					JPanel text = new JPanel(new GridLayout(0, 1, 0, 0));
					text.add(new JLabel("ブラウザを開けませんでした。"));
					text.add(new JLabel("国土数値情報利用約款は以下のURLをご覧ください。"));
					text.add(new JTextField("http://ma38su.sourceforge.jp/map/licence/", SwingConstants.CENTER));
					JOptionPane.showMessageDialog(null, text, "IO Exception", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		text.add(button);
		text.add(new JLabel("同意いただいた方のみご利用いただけます。"));
		int ret = JOptionPane.showOptionDialog(null, text, title, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"同意する", "同意しない"}, null);
		if (ret != JOptionPane.YES_OPTION) {
			System.exit(0);
		}
		setting.set(Setting.KEY_TERMS, "true");
	}
}
