package com.mathias.drawutils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RotateImage {
	
	private double aadd = 0.1;

	private Map<Double, Image> images = new HashMap<Double, Image>();

	public RotateImage(Component comp, Image image){
		int width = image.getWidth(null);
		int height = image.getHeight(null);

		for (double i = -6.28; i < 6.28; i += aadd) {
			AffineTransform af = new AffineTransform();
			af.rotate(i, width/2, height/2);

			Image img = comp.createImage(width, height);
			((Graphics2D)img.getGraphics()).drawImage(image, af, null);
			img = DrawUtil.makeColorTransparent(img, Color.white);
			images.put(i, img);
		}
	}

	public Collection<Image> getImages(){
		 return images.values();
	}

	public Image getImage(double angle){
		if(angle < -6.28){
			angle = angle % -6.28;
		}
		if(angle > 6.28){
			angle = angle % 6.28;
		}

		for (double i = -6.28; i < 6.28; i += aadd) {
			if(angle >= i && angle <= i+aadd){
				Image image = images.get(i);
				if(image == null){
					System.out.println("image null: "+i);
				}
				return image;
			}
		}
		System.out.println("using default image");
		return images.get(-6.28);
	}

}
