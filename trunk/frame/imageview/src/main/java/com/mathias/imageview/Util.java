package com.mathias.imageview;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

public abstract class Util {

	private Util() {
	}

	// Converts all strings in 'strings' to lowercase
	// and returns an array containing the unique values.
	// All returned values are lowercase.
	public static String[] unique(String[] strings) {
		Set<String> set = new HashSet<String>();
		for (int i = 0; i < strings.length; i++) {
			String name = strings[i].toLowerCase();
			set.add(name);
		}
		return set.toArray(new String[0]);
	}

	public static BufferedImage rotate90DX(BufferedImage bi) {
		int width = bi.getWidth();
		int height = bi.getHeight();

		BufferedImage biFlip = new BufferedImage(height, width, bi.getType());

		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				biFlip.setRGB(height - 1 - j, width - 1 - i, bi.getRGB(i, j));

		return biFlip;
	}

	public static BufferedImage rotate90SX(BufferedImage bi) {
		int width = bi.getWidth();
		int height = bi.getHeight();

		BufferedImage biFlip = new BufferedImage(height, width, bi.getType());

		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				biFlip.setRGB(j, i, bi.getRGB(i, j));

		return biFlip;
	}

}
