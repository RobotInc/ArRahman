package com.bss.arrahmanlyrics.utils;

import android.util.Log;

/**
 * Created by mohan on 6/17/17.
 */

public class FirstLetterUpperCase {

	public static String convert(String source) {
		source = source.toLowerCase();
		StringBuffer res = new StringBuffer();

		String[] strArr = source.split(" ");
		Log.e("string array", source);

		for (String str : strArr) {
			char[] stringArray = str.trim().toCharArray();

				if (stringArray.length > 0) {
					if((int)stringArray[0] != 32) {
						stringArray[0] = Character.toUpperCase(stringArray[0]);
						str = new String(stringArray);
					}
				}



			res.append(str).append(" ");
		}

		return res.toString().trim();
	}
}
