package com.mathias.drawutils;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;

@SuppressWarnings("serial")
public class ImageDemo extends Applet {

	// load an array of images and wait for them to be completely loaded
	public Image[] load_images(String namelist[]) {
		int len = namelist.length;
		MediaTracker tr = new MediaTracker(this);
		int id = 1;
		Image ar[] = new Image[len];
		for (int i = 0; i < len; i++) {
			Image im = getImage(getCodeBase(), namelist[i] + ".jpg");
//			Image im = getImage(getDocumentBase(), namelist[i] + ".jpg");
			tr.addImage(im, id);
			id++;
			ar[i] = im;
		}
		try {
			tr.waitForAll();
		} catch (InterruptedException e) {
		}
		return (ar);
	}

	Image images[] = null;
	Image combined = null;

	String imageNames[] = { "images/demoimage", "images/demomask" };

	public void init() {
		images = load_images(imageNames);
		combined = DrawUtil.composite(this, images[0], images[1]);
	}

	public void paint(Graphics gc) {
		if (combined != null) {
			int w = getWidth();
			int h = getHeight();
//			int iw = combined.getWidth(this);
//			int ih = combined.getHeight(this);
//			double yscale = 0.7;
			int xcell = w / 3;
			int ycell = (int) (xcell * 0.7);
			int inset = xcell / 10;
			gc.setColor(new Color(0.7f, 0.7f, 0.9f));
			gc.fillRect(0, 0, w, h);
			gc.drawImage(images[0], inset, inset, xcell - 2 * inset, ycell - 2
					* inset, this);
			gc.drawImage(images[1], xcell + inset, inset, xcell - 2 * inset,
					ycell - 2 * inset, this);
			gc.drawImage(combined, 2 * xcell + inset, inset, xcell - 2 * inset,
					ycell - 2 * inset, this);
		}
//		gc.drawImage(images[0], 0, 0, null);
//		gc.drawImage(images[1], 100, 100, null);
	}

}
