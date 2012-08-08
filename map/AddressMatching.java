package map;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.sourceforge.ma38su.util.AbstractPatternEntry;
import jp.sourceforge.ma38su.util.GeneralPatternEntry;
import jp.sourceforge.ma38su.util.PatternEntry;

/**
 * アドレスマッチングのためのクラス
 * @author ma38su
 */
public class AddressMatching {

	/**
	 * Singletonのためのインスタンス保存用の変数
	 */
	public static AddressMatching matching;

	/**
	 * 必要なくなったときにインスタンスを破棄できるよう、
	 * 参照を取り消します。
	 */
	public static void deploy() {
		AddressMatching.matching = null;
	}
	
	/**
	 * アドレスマッチングのインスタンスを生成して返す。
	 * Singletonパターン
	 * 
	 * @return アドレスマッチングクラスのインスタンス
	 */
	public static AddressMatching getInstance() {
		if (AddressMatching.matching == null) {
			AddressMatching.matching = new AddressMatching();
		}
		return AddressMatching.matching;
	}

	public static void main(String[] args) {
		AddressMatching matching = new AddressMatching();
		List<String> list = new ArrayList<String>();
		list.add("兵庫県西宮市高須町１－６－１１２");
		list.add("埼玉県朝霞市本町２－２５－３２スペースクラフト２１ ２１０号");
		list.add("兵庫県神戸市灘区高羽町５－４－３六高荘２０６");
		list.add("兵庫県神戸市北区有馬町字峠堂１７３－１");
		list.add("兵庫県西宮市上ヶ原二番町２－１５");
		list.add("北海道札幌市中央区南四条西三丁目 第3グリ-ンビル");
		list.add("札幌市中央区南四条西三丁目 第3グリ-ンビル");
		list.add("札幌市中央区南4条西三丁目第3グリ-ンビル");
		list.add("兵庫県神戸市東灘区御影中町1-6-6 シティーホームズ御影");
		list.add("兵庫県神戸市灘区灘北通１０－２－３");
		list.add("兵庫県神戸市灘区高羽町５４六高荘２０６");
		list.add("兵庫県西脇市高田井町７６４番地の１");
		list.add("神戸市西区北別府4-2-2");
		list.add("京都");
		for (String str : list) {
			System.out.println(str);
			String address = matching.format(str);
			System.out.println(address);			
			for (String value : matching.parseAddress(address)) {
				System.out.println("\t" + value);
			}
		}
	}
	
	/**
	 * アドレスマッチングのための置換List
	 */
	private final List<PatternEntry> REPLACE_LIST;

	/**
	 * 住所解析のためのPattern
	 */
	private final Pattern ADDR_PARSER;

	/**
	 * 番地切り落としのためのPattern
	 */
	private final Pattern CHOP_NUMBER;

	private AddressMatching() {
		this.CHOP_NUMBER = Pattern.compile("([^-]+)-[\\d]+$");
//		this.ADDR_PARSER = Pattern.compile("^([^都道府県]+[都道府県])?([^市].+市?市[^区市]+区|[^市区町村]+市?[市区町村])([^町].*)");
		this.ADDR_PARSER = Pattern.compile("^(東京都|[^道府県]{1,3}[道府県])?([^市].+市?市[^区市]+区|[^市区町村]+市?[市区町村])?([^町].*)?");
		this.REPLACE_LIST = new ArrayList<PatternEntry>();
		this.REPLACE_LIST.add(new GeneralPatternEntry("", "[\"\\s　]"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("-", "[ー－―]"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("1", "１"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("2", "２"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("3", "３"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("4", "４"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("5", "５"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("6", "６"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("7", "７"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("8", "８"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("9", "９"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("0", "０"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("一条", "1条"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("二条", "2条"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("三条", "3条"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("四条", "4条"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("五条", "5条"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("六条", "6条"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("七条", "7条"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("八条", "8条"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("九条", "9条"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("-", "番地の?"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("-", "番の?"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("1-", "[一1]丁目の?"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("2-", "[二2]丁目の?"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("3-", "[三3]丁目の?"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("4-", "[四4]丁目の?"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("5-", "[五5]丁目の?"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("6-", "[六6]丁目の?"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("7-", "[七7]丁目の?"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("8-", "[八8]丁目の?"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("9-", "[九9]丁目の?"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("ノ", "の"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("ヶ", "[がケ]"));
		this.REPLACE_LIST.add(new GeneralPatternEntry("", "-$"));
		this.REPLACE_LIST.add(new AbstractPatternEntry("^(.+\\d)-(\\d*)[^\\d]*.*$") {
			@Override
			public String replace(String input) {
				Matcher matcher = this.pattern.matcher(input);
				if(matcher.find()) {
					String part2 = matcher.group(2);
					if (part2 != null && !part2.equals("")) {
						input = matcher.group(1) + '-' + part2;
					} else {
						input = matcher.group(1);
					}
				}
				return input;
			}
		});
		this.REPLACE_LIST.add(new AbstractPatternEntry("([^\\d]+[1-9][\\d]*-[1-9][\\d]*)") {
			@Override
			public String replace(String input) {
				Matcher matcher = this.pattern.matcher(input);
				if(matcher.find()) {
					input = matcher.group(1);
				}
				return input;
			}
		});
	}

	public String chopNumber(String input) {
		Matcher matcher = this.CHOP_NUMBER.matcher(input);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	/**
	 * 大字・町丁目、街区符号・地番を[1-9][0-9]*-[1-9][0-9]に変換
	 * （以降切捨て）
	 * @param input
	 * @return 変換した文字列
	 */
	public String format(String input) {
		for (PatternEntry entry : this.REPLACE_LIST) {
			input = entry.replace(input);
		}
		return input;
	}
	
	/**
	 * 町域を解析する
	 * @param input 解析する住所
	 * @return 解析結果の町域
	 */
	public String parse(String input) {
		input = this.format(input);
		return this.parseCityarea(input);
	}
	
	/**
	 * 住所を解析する
	 * @param input 解析する住所
	 * @return 解析結果の住所
	 */
	public String[] parseAddress(String input) {
		String[] address = new String[3];
		Matcher matcher = this.ADDR_PARSER.matcher(input);
		if (matcher.find()) {
			address[0] = matcher.group(1);
			address[1] = matcher.group(2);
			address[2] = matcher.group(3);
		}
		return address;
	}

	/**
	 * 市区町村を解析する
	 * @param input 解析する住所
	 * @return 解析結果の市区町村
	 */
	public String parseCity(String input) {
		Matcher matcher = this.ADDR_PARSER.matcher(input);
		if (matcher.find()) {
			return matcher.group(2);
		}
		return null;
	}

	/**
	 * 町域を解析する
	 * @param input 解析する住所
	 * @return 解析結果の町域
	 */
	public String parseCityarea(String input) {
		Matcher matcher = this.ADDR_PARSER.matcher(input);
		if (matcher.find()) {
			return matcher.group(3);
		}
		return input;
	}
	
	/**
	 * 都道府県を解析する
	 * @param input 解析する住所
	 * @return 解析結果の都道府県
	 */
	public String parsePrefecture(String input) {
		Matcher matcher = this.ADDR_PARSER.matcher(input);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}
}
