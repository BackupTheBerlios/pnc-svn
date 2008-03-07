package com.mathias.filesorter.table;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.mathias.drawutils.Mp3Util;
import com.mathias.drawutils.Util;

public class FileItem {
	
	public final static int NAME = 0;
	public final static int PATH = 1;
	public final static int ABSOLUTENAME = 2;
	public final static int SIZE = 3;
	public final static int MODIFIED = 4;
	public final static int ID3_TITLE = 5;
	public final static int ID3_ARTIST = 6;
	public final static int ID3_ALBUM = 7;
	public final static int ID3_YEAR = 8;
	public final static int NOTES = 9;
	public final static int COUNTER = 10;
	
	private static long counter = 0;

	public static final Map<Integer, FileItemProperties> KEYS = new HashMap<Integer, FileItemProperties>();
	static{
		KEYS.put(NAME, new FileItemProperties("Name", false));
		KEYS.put(PATH, new FileItemProperties("Path", false));
		KEYS.put(ABSOLUTENAME, new FileItemProperties("AbsoluteName", false));
		KEYS.put(NOTES, new FileItemProperties("Notes", true));
		KEYS.put(SIZE, new FileItemProperties("Size", false));
		KEYS.put(MODIFIED, new FileItemProperties("Modified", false));
		KEYS.put(ID3_TITLE, new FileItemProperties("ID3 Title", false));
		KEYS.put(ID3_ARTIST, new FileItemProperties("ID3 Artist", false));
		KEYS.put(ID3_ALBUM, new FileItemProperties("ID3 Album", false));
		KEYS.put(ID3_YEAR, new FileItemProperties("ID3 Year", false));
		KEYS.put(COUNTER, new FileItemProperties("Counter", true));
	}

	private Map<Integer, String> map = new HashMap<Integer, String>();	

	public FileItem(File file){
		map.put(NAME, file.getName());
		map.put(ABSOLUTENAME, file.getAbsolutePath());
		map.put(PATH, Util.dirName(file.getPath()));
		map.put(NOTES, "");
		map.put(SIZE, ""+file.length()/1024+"K");
		map.put(MODIFIED, new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date(file.lastModified())));
		Map<String, String> id3 = Mp3Util.readID3(file);
		map.put(ID3_TITLE, id3.get(Mp3Util.TITLE));
		map.put(ID3_ARTIST, id3.get(Mp3Util.ARTIST));
		map.put(ID3_ALBUM, id3.get(Mp3Util.ALBUM));
		map.put(ID3_YEAR, id3.get(Mp3Util.YEAR));
		map.put(COUNTER, String.format("%03d", counter++));
	}

	public String get(Integer key){
		return map.get(key);
	}

	public void set(Integer key, String value){
		if(KEYS.get(key).editable){
			map.put(key, value);
		}
	}

}
