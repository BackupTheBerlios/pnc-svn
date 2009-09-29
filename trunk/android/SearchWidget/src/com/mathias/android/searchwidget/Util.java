package com.mathias.android.searchwidget;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

public class Util {

	private static final String TAG = Util.class.getSimpleName();

	public static void downloadFile(String uri, String file){
		FileOutputStream os = null;
		try {
			URL url = new URL(uri);
			InputStream is = url.openStream();
			os = new FileOutputStream(file);

			byte[] buffer = new byte[8192];
			while (true) {
				int c = is.read(buffer);
				if (c == -1) {
					break;
				}
				os.write(buffer, 0, c);
			}
			is.close();
		} catch (MalformedURLException e) {
			Log.e(TAG, e.getMessage(), e);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
				}
			}
		}
	}

}
