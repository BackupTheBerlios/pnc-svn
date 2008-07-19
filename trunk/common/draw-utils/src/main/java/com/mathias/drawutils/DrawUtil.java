package com.mathias.drawutils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.awt.image.RGBImageFilter;

public abstract class DrawUtil {

	public static Image makeColorTransparent(Image im, final Color color) {
		ImageFilter filter = new RGBImageFilter() {
			// the color we are looking for... Alpha bits are set to opaque
			public int markerRGB = color.getRGB() | 0xFF000000;

			public final int filterRGB(int x, int y, int rgb) {
				if ((rgb | 0xFF000000) == markerRGB) {
					// Mark the alpha bits as zero - transparent
					return 0x00FFFFFF & rgb;
				} else {
					// nothing to do
					return rgb;
				}
			}
		};

		ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
	}

	// composite two images
	public static Image composite(Component who, Image im, Image mask) {
		int w = im.getWidth(who);
		int h = im.getHeight(who);
		int ipix[] = new int[w * h];
		int mpix[] = new int[w * h];
		boolean gotfg = false;
		boolean gotma = false;
		try {
			// note that there are problems with pixelgrabber from images
			// created with
			// createimage, but apparently not with images from loadimage
			gotfg = new PixelGrabber(im, 0, 0, w, h, ipix, 0, w).grabPixels();
			gotma = new PixelGrabber(mask, 0, 0, w, h, mpix, 0, w).grabPixels();
		} catch (InterruptedException e) {
		}
		if (gotfg && gotma) {
			for (int i = 0; i < w * h; i++) {
				int ma = 0xff - (mpix[i] & 0xff); // center pixel
				int fg = ipix[i];
				ipix[i] = (fg & 0xffffff) | (ma << 24);
			}
			Image fin = who.createImage(new MemoryImageSource(w, h, ipix, 0, w));
			return (fin);
		}
		System.out.println("Composite failed");

		return (im);
	}

}
