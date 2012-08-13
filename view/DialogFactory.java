package view;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import util.Setting;
import util.Version;
import controller.MapController;

/**
 * ダイアログを生成するクラス
 * @author ma38su
 */
public class DialogFactory {
	
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
	
	/**
	 * 致命的なエラーが発生した場合に表示するダイアログ
	 * エラーレポートの協力をお願いします。
	 * このメソッドを呼び出すと、自動的に終了します。
	 * @param comp 
	 * @param e 致命的なエラー
	 * @param setting 設定ファイル
	 */
	public static void errorDialog(JComponent comp, Throwable e) {
		JPanel text = new JPanel(new GridLayout(0, 1, 0, 0));
		text.add(new JLabel("申し訳ございません。致命的な問題が発生しました。", SwingConstants.LEFT));
		text.add(new JLabel(e.getClass().getName() +": "+ e.getLocalizedMessage(), SwingConstants.LEFT));
		JOptionPane.showMessageDialog(comp, text, "KSJ Map - 致命的なエラー", JOptionPane.ERROR_MESSAGE);
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
		JOptionPane.showMessageDialog(comp, text, "KSJ Map - メモリ不足", JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * DigitalMapの情報を表示するダイアログ
	 */
	public static void aboutDialog() {
		String title = "KSJ Map ver."+ Version.get("/history.txt");
		JPanel text = new JPanel(new GridLayout(0, 1, 0, 0));
		text.add(new JLabel(title, SwingConstants.LEFT));
		text.add(new JLabel("Copyright 2005-2006 ma38su", SwingConstants.LEFT));
		JOptionPane.showMessageDialog(null, text, title, JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * 地図データの利用についてを読んでもらうために表示する。
	 * @param setting 
	 */
	public static void termsDialog(Setting setting) {
		String title = "KSJ Map ver."+ Version.get("/history.txt");
		JPanel text = new JPanel(new GridLayout(0, 1, 0, 0));
		text.add(new JLabel("本ソフトウェア利用の前に必ずお読みください。"));
		text.add(new JLabel("同意いただいた方のみご利用いただけます。"));
		int ret = JOptionPane.showOptionDialog(null, text, title, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"同意する", "同意しない"}, null);
		if (ret != JOptionPane.YES_OPTION) {
			System.exit(0);
		}
		setting.set(Setting.KEY_TERMS, "true");
	}
}
