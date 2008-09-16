package com.mathias.imageview;

import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;

public class ImageFileFilter extends FileFilter {

	@Override
	public boolean accept(File pathname) {
		String[] formatNames = ImageIO.getReaderFormatNames();
		formatNames = Util.unique(formatNames);
		for (String fn : formatNames) {
			if(pathname.getName().toLowerCase().endsWith("."+fn)){
//				System.out.println("Found: "+filename+" "+"   ."+fn);
				return true;
			}
		}
		return false;
	}

	@Override
	public String getDescription() {
		return "Image files";
	}
}
