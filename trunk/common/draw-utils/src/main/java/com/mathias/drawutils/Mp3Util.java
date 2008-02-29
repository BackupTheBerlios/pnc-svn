package com.mathias.drawutils;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

public abstract class Mp3Util {
	
	public static final String TAG = "TAG";
	public static final String TITLE = "Title";
	public static final String ARTIST = "Artist";
	public static final String ALBUM = "Album";
	public static final String YEAR = "Year";
	
	private Mp3Util(){
	}

	public static Map<String, String> readID3(File song){
		Map<String, String> ret = new HashMap<String, String>();
        try { 
            FileInputStream file = new FileInputStream(song); 
            file.skip(song.length() - 128); 
            byte[] last128 = new byte[128]; 
            file.read(last128); 
            String id3 = new String(last128); 
            String tag = id3.substring(0, 3); 
            if (TAG.equals(tag)) {
            	ret.put(TITLE, id3.substring(3, 32));
                ret.put(ARTIST, id3.substring(33, 62)); 
                ret.put(ALBUM, id3.substring(63, 91)); 
                ret.put(YEAR, id3.substring(93, 97)); 
            }
            file.close(); 
        } catch (Exception e) { 
            System.out.println("Error — " + e.toString()); 
        }
        return ret;
	}

}
