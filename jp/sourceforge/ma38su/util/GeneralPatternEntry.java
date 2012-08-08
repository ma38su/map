package jp.sourceforge.ma38su.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 簡易に一般化した正規化のためのクラス
 * @author ma38su
 */
public class GeneralPatternEntry implements PatternEntry {
	private Pattern pattern;
	private String word;
	public GeneralPatternEntry(String word, String regex) {
		this.word = word;
		this.pattern = Pattern.compile(regex);
	}
	public String replace(String input) {
		Matcher match = this.pattern.matcher(input);
		return match.replaceAll(this.word);
	}
}
