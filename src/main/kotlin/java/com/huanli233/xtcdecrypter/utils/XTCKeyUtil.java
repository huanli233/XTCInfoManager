package com.huanli233.xtcdecrypter.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XTCKeyUtil {
	public static boolean checkKey(String key) {
		String regex = "^[a-zA-Z0-9]{32}:[a-zA-Z0-9=+/]{128}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(key);
        return matcher.matches();
	}
}
