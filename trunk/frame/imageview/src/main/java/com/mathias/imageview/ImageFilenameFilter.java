package com.mathias.imageview;

import java.io.File;
import java.io.FilenameFilter;

import javax.imageio.ImageIO;

public class ImageFilenameFilter implements FilenameFilter {

	@Override
	public boolean accept(File file, String filename) {
		String[] formatNames = ImageIO.getReaderFormatNames();
		formatNames = Util.unique(formatNames);
		for (String fn : formatNames) {
			if(filename.toLowerCase().endsWith("."+fn)){
//				System.out.println("Found: "+filename+" "+"   ."+fn);
				return true;
			}
		}
		return false;
	}
}

