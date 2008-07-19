package com.mathias.drawutils;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.util.HashMap;
import java.util.Map;

public class MultiImage {
	
	Map<String, Image> images = new HashMap<String, Image>();

	public MultiImage(Component cmp, Image img, int x, int y, int w, int h){
		for (int j = 0; j < y; j++) {
			for (int i = 0; i < x; i++) {
				images.put(getKey(i, j), cmp
						.createImage(new FilteredImageSource(img.getSource(),
								new CropImageFilter(i*w, j*h, w, h))));
			}
		}
	}

	public Image get(int x, int y){
		return images.get(getKey(x, y));
	}

	private String getKey(int x, int y){
		String key = x + ";" + y;
		return key;
	}

}
