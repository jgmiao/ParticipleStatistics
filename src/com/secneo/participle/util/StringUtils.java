package com.secneo.participle.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

	public static String stringFilter(String str) {
		String dest = "";
		if (null != str && str != "") {
			Pattern p = Pattern.compile("\\s*|\t|\r|\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
			dest = dest.replaceAll("\\(", "");
			dest = dest.replaceAll("\\)", "");
			dest = dest.replaceAll("\\（", "");
			dest = dest.replaceAll("\\）", "");
			dest = dest.replaceAll("\\.", "");
			dest = dest.replaceAll("\\。", "");
			dest = dest.replaceAll("\\*", "");
		}
		return dest;
	}
	
	public static boolean isContainChinese(String s){
		char[] apkChars = s.toCharArray();
		for (int iName = 0; iName < s.length(); iName++) {
			char apkChar = apkChars[iName];
			if (StringUtils.isChinese(apkChar)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	public static boolean isNotEmpty(String s) {
		return null != s && !s.equals("") && !s.equals(" ") ? true : false;
	}
}