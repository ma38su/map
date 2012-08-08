package jp.sourceforge.ma38su.util;


import java.util.regex.Pattern;

public abstract class AbstractPatternEntry implements PatternEntry {
	protected final Pattern pattern;
	public AbstractPatternEntry(String regex) {
		this.pattern = Pattern.compile(regex);
	}
	public abstract String replace(String input);
}
