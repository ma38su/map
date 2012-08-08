package database;

import java.awt.Polygon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jp.sourceforge.ma38su.util.DLFile;
import jp.sourceforge.ma38su.util.GeneralFileFilter;
import jp.sourceforge.ma38su.util.Log;

/**
 * 数値地図のデータ管理
 * @author ma38su
 */
public class FileDatabase extends Observable {

	/**
	 * 国土数値情報のベースとなるURL
	 */
	private static final String KSJ_URL = "http://nlftp.mlit.go.jp/ksj/dls/data/";

	/**
	 * 都道府県データの相対位置リスト
	 */
	private static final String prefData = "/.data/prefecture.csv";

	/**
	 * 都道府県界のデータ
	 */
	private static final String prefPolygon = "/.data/prefecture.dat";

	public void clearCache() {
		File file = new File(this.SERIALIZE_DIR);
		if (file.delete()) {
			Log.err(this, "failure: delete "+ this.SERIALIZE_DIR);
		}
	}

	/**
	 * FreeGISの世界地図データ
	 * [0] 日本以外
	 * [1] 日本のみ
	 */
	private static final String worldPolygon = "/.data/freegis.dat";

	/**
	 * 保存フォルダ
	 */
	private final String CACHE_DIR;

	private final NumberFormat cityFormat;

	/**
	 * 都道府県のURL補助
	 */
	private final String[] prefecture;

	private final NumberFormat prefFormat;
	
	/**
	 * 直列化したファイルの保存先ディレクトリ
	 */
	private final String SERIALIZE_DIR;
	
	/**
	 * コンストラクタ
	 * @param cacheDir データ格納ディレクトリ
	 * @param status ステータスバー
	 * @throws IOException 入出力エラー
	 */
	public FileDatabase(String cacheDir) throws IOException {
		this.CACHE_DIR = cacheDir;

		this.cityFormat = new DecimalFormat("00000");
		this.prefFormat = new DecimalFormat("00");
		
		File dirSdf25k = new File(cacheDir + "sdf25k");
		File dirSdf2500 = new File(cacheDir + "sdf2500");
		File dirKsj = new File(cacheDir + "ksj");
		File dirSerializable = new File(cacheDir + "serialize");
		this.SERIALIZE_DIR = dirSerializable.getCanonicalPath() + File.separatorChar;
		
		Log.out(this, "init Cache Directory "+ dirSdf25k.getCanonicalPath());
		if(!dirSdf25k.isDirectory()) {
			dirSdf25k.mkdirs();
		}
		Log.out(this, "init Cache Directory "+ dirSdf2500.getCanonicalPath());
		if(!dirSdf2500.isDirectory()) {
			dirSdf2500.mkdirs();
		}
		Log.out(this, "init Cache Directory "+ dirKsj.getCanonicalPath());
		if(!dirKsj.isDirectory()) {
			dirKsj.mkdirs();
		}
		Log.out(this, "init Cache Serialize Directory "+ dirSerializable.getCanonicalPath());
		if(!dirSerializable.isDirectory()) {
			dirSerializable.mkdirs();
		}

		// 都道府県番号（都道府県数47だが、北海道は1のため）
		this.prefecture = new String[48];
		BufferedReader out = null;
		try {
			out = new BufferedReader(new InputStreamReader(FileDatabase.class.getResourceAsStream(FileDatabase.prefData), "SJIS"));
			int i = 1;
			Pattern csv = Pattern.compile(",");
			while(out.ready()) {
				String line = out.readLine();
				this.prefecture[i++] = csv.split(line)[1];
			}
		} finally {
			if(out != null) {
				out.close();
			}
		}
		this.setChanged();
	}
	
	private String cityFormat(int code) {
		return this.cityFormat.format(code);
	}
	
	@Override
	protected synchronized void clearChanged() {
	}
	

	/**
	 * ファイルのコピーを行います。
	 * 入出力のストリームは閉じないので注意が必要です。
	 * 
	 * @param in 入力ストリーム
	 * @param out 出力ストリーム
	 * @throws IOException 入出力エラー
	 */
	private void copy(InputStream in, OutputStream out) throws IOException {
		final byte buf[] = new byte[1024];
		int size;
		while ((size = in.read(buf)) != -1) {
			out.write(buf, 0, size);
			out.flush();
		}
	}

	/**
	 * ファイルをダウンロードします。
	 * @param url URL
	 * @param file ダウンロード先のファイル
	 * @return ダウンロードできればtrue
	 * @throws IOException 入出力エラー
	 */
	private boolean download(URL url, DLFile file) throws IOException {
		boolean ret = true;
		InputStream in = null;
		OutputStream out = null;
		try {
			URLConnection connect = url.openConnection();
			// ファイルのチェック（ファイルサイズの確認）
			int contentLength = connect.getContentLength();
			if (contentLength != file.length()) {
				if (!file.getParentFile().isDirectory()) {
					file.getParentFile().mkdirs();
				}

				file.setContentLength(contentLength);
				file.setState(DLFile.STATE_DOWNLOAD);
				this.notifyObservers(file);

				final long start = System.currentTimeMillis();
				// ダウンロード
				in = connect.getInputStream();
				out = new FileOutputStream(file);
				this.copy(in, out);
				Log.out(this, "download "+ url + " / " + (System.currentTimeMillis() - start) + "ms");
				
				file.setState(DLFile.STATE_FINISH);
				this.notifyObservers(file);
			}
		} catch (Exception e) {
			ret = false;
		} finally {
			if(in != null) {
				in.close();
			}
			if(out != null) {
				out.close();
			}
		}
		return ret;
	}

	/**
	 * 圧縮ファイルを展開します。
	 * @param zip 展開するファイル
	 * @param dir 展開するディレクトリ
	 * @param filter ファイルフィルター
	 * @return 展開したファイル配列
	 * @throws IOException 入出力エラー
	 */
	private File[] extractZip(File zip, File dir, FileFilter filter) throws IOException {
		long start = System.currentTimeMillis();
		boolean isExtracted = false;
		ZipInputStream in = null;
		List<File> extracted = new ArrayList<File>();
		try {
			in = new ZipInputStream(new FileInputStream(zip));
			ZipEntry entry;
			while ((entry = in.getNextEntry()) != null) {
				String entryPath = entry.getName();
				/* 出力先ファイル */
				File outFile = new File(dir.getCanonicalPath() + File.separatorChar + entryPath);
				if (filter == null || filter.accept(outFile)) {
					if (!outFile.exists() || entry.getSize() != outFile.length()) {
						/* entryPathにディレクトリを含む場合があるので */
						File dirParent = outFile.getParentFile();
						if(!dirParent.isDirectory()) {
							if (!dirParent.mkdir()) {
								throw new IOException("extract mkdir failed");
							}
						}
						/* ディレクトリはmkdirで作成する必要がある */
						if (entryPath.endsWith(File.separator)) {
							if (!outFile.mkdirs()) {
								throw new IOException("extract mkdir failed");
							}
						} else {
							FileOutputStream out = null;
							try {
								out = new FileOutputStream(outFile);
								this.copy(in, out);
							} finally {
								if (out != null) {
									out.close();
								}
							}
						}
						isExtracted = true;
					}
					extracted.add(outFile);
				}
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
		long end = System.currentTimeMillis();
		if (isExtracted) {
			Log.out(this, "extract " + zip + " / " + (end - start) + "ms");
		}
		return extracted.toArray(new File[]{});
	}
	/**
	 * 頂点の外部への接続情報の取得
	 * @param code 市町村番号
	 * @return 市町村番号に対応する頂点の外部への接続情報
	 */
	public InputStream getBoundaryNode(int code) {
		String codeStr = this.cityFormat(code);
		return this.getClass().getResourceAsStream("/.data/" + codeStr.substring(0, 2) + "/" + codeStr +".nod");
	}
	
	/**
	 * 日本の都道府県データを取得します。
	 * @return 都道府県データ
	 */
	public Polygon[][] getPrefecturePolygon() {
		return (Polygon[][]) this.readSerializableArchive(FileDatabase.prefPolygon);
	}

	/**
	 * 国土数値情報の行政界を取得します。
	 * @param code 都道府県番号
	 * @return 国土数値情報の行政界のファイル
	 * @throws IOException 入出力エラー
	 */
	public File getKsjBoder(int code) throws IOException {
		String stringCode = this.prefectureFormat(code);
		File dir = new File(this.CACHE_DIR + "ksj" + File.separatorChar + stringCode);
		DLFile zip = new DLFile("KSJ " + code, dir.getParent() + File.separatorChar + stringCode + ".zip");
		if (!dir.isDirectory()) {
			dir.mkdirs();
		}
		if (zip.exists() || !dir.isDirectory() || dir.list().length == 0) {
			/* 圧縮ファイルが残っている
			 * or ディレクトリが存在しない
			 * or ディレクトリ内のファイルが存在しない
			 * or ディレクトリの内容が正確でない（チェックできてない）
			 */
			URL url = new URL(FileDatabase.KSJ_URL + "N03-11A-" + stringCode + "-01.0a.zip");
			
			if (!this.download(url, zip)) {
				throw new IOException("download failed: "+ code);
			}
		}
		File[] extracts = null;
		FileFilter filter = new GeneralFileFilter("txt");
		if(zip.exists()) {
			// ファイルの展開
			extracts = this.extractZip(zip, dir, filter);
			if(!zip.delete()){
				throw new IOException("delete failure: "+ zip.getCanonicalPath());
			}
		} else if(dir.isDirectory()) {
			extracts = dir.listFiles(filter);
		}
		if(extracts == null || extracts.length == 0) {
			throw new IOException("file not found: "+ code);
		} else if (extracts.length != 1) {
			throw new IOException("files found: "+ code);
		}
		return extracts[0];
	}

	/**
	 * 世界地図データの取得
	 * @return 世界地図データ
	 */
	public Polygon[][] getWorldPolygon() {
		return (Polygon[][]) this.readSerializableArchive(FileDatabase.worldPolygon);
	}
	
	private String getSerializablePath(String path) {
		return this.SERIALIZE_DIR + path;
	}
	
	private String prefectureFormat(int code) {
		return this.prefFormat.format(code);
	}
	
	/**
	 * 直列化されたメッシュ標高を読み込みます。
	 * synchronizedは経路探索処理と、地図データ表示のスレッドの衝突を防ぐため。
	 * @param path パス
	 * @return メッシュ標高
	 */
	public Object readSerializableArchive(String path) {
		Log.out(this, "read Serialize "+ path);
		Object obj = null;
		try {
			ObjectInputStream in = null;
			try {
				in = new ObjectInputStream(System.class.getResourceAsStream(path));
				obj = in.readObject();
			} finally {
				if (in != null) {
					in.close();
				}
			}
		} catch (Exception e) {
			obj = null;
			Log.err(this, e);
		}
		return obj;
	}

	/**
	 * 直列化されたメッシュ標高を読み込みます。
	 * synchronizedは経路探索処理と、地図データ表示のスレッドの衝突を防ぐため。
	 * @param path パス
	 * @return メッシュ標高
	 */
	public Object readSerializable(String path) {
		Object obj = null;
		File file = new File(this.getSerializablePath(path));
		if (file.isFile()) {
			try {
				Log.out(this, "read Serialize "+ file.getCanonicalPath());
				ObjectInputStream in = null;
				try {
					in = new ObjectInputStream(new FileInputStream(file));
					obj = in.readObject();
				} finally {
					if (in != null) {
						in.close();
					}
				}
			} catch (Throwable e) {
				Log.err(this, e);
				if (!file.delete()) {
					Log.err(this, "failure delete: "+ file);
				}
				obj = null;
			}
		}
		return obj;
	}

	/**
	 * オブジェクトを直列化して保存します。
	 * 衝突を避けるため一度.tmpファイルに保存して後にリネームします。
	 * @param path パス
	 * @param obj 直列化可能なオブジェクト
	 */
	public void writeSerializable(String path, Object obj) {
		String key = this.getSerializablePath(path);
		Log.out(this, "save "+ key);
		File file = new File(key + ".tmp");
		if (!file.getParentFile().isDirectory()) {
			if (!file.getParentFile().mkdirs()) {
				Log.err(this, "mkdir failure: "+ file.getParent());
			}
		}
		try {
			ObjectOutputStream out = null;
			try {
				out = new ObjectOutputStream(new FileOutputStream(file));
				out.writeObject(obj);
				out.flush();
			} finally {
				if (out != null) {
					out.close();
				}
			}
			if (!file.renameTo(new File(key))) {
				Log.err(this, "rename failure: "+ key);
				if (!file.delete()) {
					Log.err(this, "delete failure: "+ file);
				}
			}
		} catch (Exception e) {
			Log.err(this, e);
			if (file.exists()) {
				file.delete();
			}
		}
	}
}
