package controller;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import map.AddressMatching;

import view.DialogFactory;
import database.CodeDatabase;

/**
 * 検索のためのコントローラ
 * @author ma38su
 */
public class SearchController implements ActionListener, Runnable {
	
	private final DefaultListModel list;
	private final CodeDatabase db;
	private final JTextField field;
	private final JCheckBox geoCheck;
	
	public SearchController(CodeDatabase db, DefaultListModel list, JTextField field, JCheckBox geoCheck) {
		this.db = db;
		this.field = field;
		this.list = list;
		this.geoCheck = geoCheck;
	}
	
	public void actionPerformed(ActionEvent e) {
		new Thread(this).start();
	}
	
	public void run() {
		try {
			this.list.clear();

			if (this.geoCheck.isSelected()) {
				this.geocoding();
			}

			AddressMatching adm = AddressMatching.getInstance();
			String[] address = adm.parseAddress(this.field.getText());

			if (address[0] != null || address[1] != null) {
				Map<Integer, String> result = this.db.get(address[0], address[1], CodeDatabase.MATCH_CONTAINS);
				for (Map.Entry<Integer, String> entry : result.entrySet()) {
					this.list.addElement(new SearchEntry(entry.getKey(), entry.getValue()));
				}
			}
		} catch (IOException ex) {
			DialogFactory.errorDialog(null, ex);
		}
	}

	/**
	 * Geocoding APIから座標を取得
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private void geocoding() throws MalformedURLException, UnsupportedEncodingException, IOException {
		Pattern tagPattern = Pattern.compile("<([a-zA-Z]+)>([^<>]+)</[a-zA-Z]+>");
		URL url = new URL("http://www.geocoding.jp/api/?q="+ this.field.getText());
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
			String line;
			int lat = 0;
			int lng = 0;
			String address = null;
			while ((line = in.readLine()) != null) {
				Matcher match = tagPattern.matcher(line);
				if (match.find()) {
					String label = match.group(1);
					if ("address".equals(label)) {
						address = match.group(2);
					} else if ("lat".equals(label)){
						lat = (int) (Double.parseDouble(match.group(2)) * 3600000);
					} else if ("lng".equals(label)) {
						lng = (int) (Double.parseDouble(match.group(2)) * 3600000);
						SearchEntry entry = new SearchEntry("Geocooding: "+ address , new Point(lng, lat));
						this.list.addElement(entry);
					} else if ("error".equals(label)) {
						break;
					}
				}
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}
}
