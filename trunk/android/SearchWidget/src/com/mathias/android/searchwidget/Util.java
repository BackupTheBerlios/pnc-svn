package com.mathias.android.searchwidget;

import android.database.Cursor;

public class Util {

	public static void closeCursor(Cursor c){
		if(c != null){
			c.close();
			c = null;
		}
	}

	public static String buildString(Object ... objs){
		StringBuilder sb = new StringBuilder();
		for (Object o : objs) {
			sb.append(o);
		}
		return sb.toString();
	}

}
