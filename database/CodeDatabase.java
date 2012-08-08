package database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.sourceforge.ma38su.util.Log;

/**
 * 都道府県番号、市区町村名番号データベース
 * 番号から名前、利用できるデータの種類などを検索するためのクラス
 * @author ma38su
 */
public class CodeDatabase {

	public static final int MATCH_CONTAINS = 1;

	private final String file;

	private HashMap<SerializableInteger, SerializableInteger> map;

	/**
	 * キャッシュ
	 */
	private final SerializableInteger p;

	/**
	 * 固定長ディスクアクセス
	 */
	private RandomAccessFile disc;
	
	/**
	 * データベース
	 * @param from
	 * @param db
	 * @param index
	 * @throws IOException
	 */
	public CodeDatabase(String from, String db, String index) throws IOException {
		this.file = from;
		this.p = new SerializableInteger();
		File disc = new File(db);
		BufferedReader bi = null;
		ObjectOutputStream out = null;
		if (disc.exists()) {
			this.map = this.readIndexMap(new File(index));
		} else {
			if (!disc.getParentFile().isDirectory()) {
				disc.getParentFile().mkdirs();
			}
		}
		if (this.map != null) {
			this.disc = new RandomAccessFile(disc, "r");
		} else {
			this.map = new HashMap<SerializableInteger, SerializableInteger>();
			try {
				bi = new BufferedReader(new InputStreamReader(CodeDatabase.class.getResourceAsStream(from), "SJIS"));
				this.disc = new RandomAccessFile(disc, "rw");
				this.disc.seek(0);
				this.disc.setLength(0);
				String line;
				Pattern csv = Pattern.compile(",");
				while ((line = bi.readLine()) != null) {
					String[] param = csv.split(line);
					SerializableInteger entry = new SerializableInteger(this.disc.getFilePointer());
					this.map.put(new SerializableInteger(param[1]), entry);
					int flag = (Integer.parseInt(param[2]) << 1) | Integer.parseInt(param[3]);
					this.disc.writeByte(flag);
					this.disc.writeUTF(param[0]);
				}
				File file = new File(index);
				if (file.getParentFile().isDirectory()) {
					file.getParentFile().mkdirs();
				}
				out = new ObjectOutputStream(new FileOutputStream(file));
				out.writeObject(this.map);
				out.flush();
			} finally {
				if (bi != null) {
					bi.close();
				}
				if (out != null) {
					out.close();
				}
			}
		}
	}
	
	public synchronized boolean hasSdf25k(int code) {
		this.p.setValue(code);
		SerializableInteger entry = this.map.get(this.p);
		if (entry == null) {
			return false;
		}
		int name = 0;
		try {
			this.disc.seek(entry.getValue() + 1);
			name = this.disc.readByte();
		} catch (IOException e) {
			Log.err(this, e);
			name = 0;
		}
		return name != 0;
	}

	public synchronized boolean hasSdf2500(int code) {
		this.p.setValue(code);
		SerializableInteger entry = this.map.get(this.p);
		if (entry == null) {
			return false;
		}
		int flag = 0;
		try {
			this.disc.seek(entry.getValue());
			flag = this.disc.readByte();
		} catch (IOException e) {
			Log.err(this, e);
			flag = 0;
		}
		return (flag & 1) != 0;
	}
	
	/**
	 * @param code 市区町村番号
	 * @return 文字列
	 */
	public synchronized String get(int code) {
		this.p.setValue(code);
		SerializableInteger entry = this.map.get(this.p);
		if (entry == null) {
			return null;
		}
		String name = null;
		try {
			this.disc.seek(entry.getValue() + 1);
			name = this.disc.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("error: "+ code);
			System.err.println("entry: "+ entry.getValue());
			name = null;
		}
		return name;
	}

	/**
	 * 市区町村名から市区町村名を検索します。
	 * @param prefecture 都道府県名
	 * @param city 市区町村名
	 * @param type 検索するタイプ
	 * @return 市区町村番号
	 * @throws IOException 入出力エラー
	 */
	public Map<Integer, String> get(String prefecture, String city, int type) throws IOException {
		StringBuilder sb = new StringBuilder();
		switch (type) {
			case MATCH_CONTAINS :
				sb.append(".*");
				sb.append(city);
				sb.append(".*");
		}
		Pattern pattern = Pattern.compile(sb.toString());
		Pattern csv = Pattern.compile(",");
		Map<Integer, String> map = new TreeMap<Integer, String>();
		Map<Integer, String> pref = new HashMap<Integer, String>();
		Set<Integer> set = new HashSet<Integer>();
		BufferedReader bi = null;
		try {
			bi = new BufferedReader(new InputStreamReader(CodeDatabase.class.getResourceAsStream(this.file), "SJIS"));
			String line;
			while ((line = bi.readLine()) != null) {
				String[] param = csv.split(line);
				int code = Integer.parseInt(param[1]);
				int prefCode = code / 1000;
				Matcher matcher = pattern.matcher(param[0]);
				boolean flag = "".equals(city) || city == null;
				boolean pflag = "".equals(prefecture) || prefecture == null;
				if (prefCode == 0) {
					pref.put(code, param[0]);
					if (pflag || param[0].endsWith(prefecture)) {
						set.add(code);
					}
				} else {
					if (flag && set.contains(prefCode)) {
						StringBuilder s = new StringBuilder();
						s.append(pref.get(prefCode));
						s.append(param[0]);
						map.put(code, s.toString());
					} else if (matcher.matches()) {
						StringBuilder s = new StringBuilder();
						s.append(pref.get(prefCode));
						s.append(param[0]);
						map.put(code, s.toString());
					}
				}
			}
		} finally {
			if (bi != null) {
				bi.close();
			}
		}
		return map;
	}
	
	/**
	 * 市区町村名から市区町村名を検索します。
	 * @param name 市区町村名
	 * @param type 検索するタイプ
	 * @return 市区町村番号
	 * @throws IOException 入出力エラー
	 */
	public Map<Integer, String> get(String name, int type) throws IOException {
		StringBuilder sb = new StringBuilder();
		switch (type) {
			case MATCH_CONTAINS :
				sb.append(".*");
				sb.append(name);
				sb.append(".*");
		}
		BufferedReader bi = null;
		Pattern pattern = Pattern.compile(sb.toString());
		Pattern csv = Pattern.compile(",");
		Map<Integer, String> map = new TreeMap<Integer, String>();
		Map<Integer, String> pref = new HashMap<Integer, String>();
		Set<Integer> set = new HashSet<Integer>();
		try {
			bi = new BufferedReader(new InputStreamReader(CodeDatabase.class.getResourceAsStream(this.file), "SJIS"));
			String line;
			while ((line = bi.readLine()) != null) {
				String[] param = csv.split(line);
				int code = Integer.parseInt(param[1]);
				int prefCode = code / 1000;
				Matcher matcher = pattern.matcher(param[0]);
				if (set.contains(prefCode) || matcher.matches()) {
					if (prefCode == 0) {
						set.add(code);
					} else {
						StringBuilder s = new StringBuilder();
						s.append(pref.get(prefCode));
						s.append(param[0]);
						map.put(code, s.toString());
					}
				}
				if (prefCode == 0) {
					pref.put(code, param[0]);
				}
			}
		} finally {
			if (bi != null) {
				bi.close();
			}
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	private HashMap<SerializableInteger, SerializableInteger> readIndexMap(File index) {
		ObjectInputStream in = null;
		HashMap<SerializableInteger, SerializableInteger> map = null;
		if (index.isFile()) {
			try {
				try {
					in = new ObjectInputStream(new FileInputStream(index));
					map = (HashMap<SerializableInteger, SerializableInteger>) in.readObject();
				} catch (Exception e) {
					this.map = null;
					index.delete();
				} finally {
					if (in != null) {
						in.close();
					}
				}
			} catch (Exception e) {
				map = null;
			}
		}
		return map;
	}

	@Override
	protected void finalize() throws Throwable {
		if (this.disc != null) {
			this.disc.close();
			this.disc = null;
		}
		super.finalize();
	}
}
