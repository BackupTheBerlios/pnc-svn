package com.mathias.drawutils.applet;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.mathias.drawutils.Util;

public abstract class MediaApplet extends Applet implements KeyListener {

	private static final Logger log = Logger.getLogger(MediaApplet.class.getName());

	private MediaTracker mediaTracker;

	private boolean initialized;

	// Vars for the offscreen image.
	private Image offImage;
	private Graphics2D offGraphics;
	
	private Map<Integer, Image> images = new HashMap<Integer, Image>();
	private Map<Integer, AudioClip> sounds = new HashMap<Integer, AudioClip>();

	@Override
	public void init() {
		Util.addConsoleHandler(MediaApplet.class.getPackage().getName());

		setSize(getDimension());
		setBackground(Color.black);

		offImage = createImage(getWidth(), getHeight());
		offGraphics = (Graphics2D)offImage.getGraphics();

		Animation ani = new Animation();
		ani.setDaemon(true);

		addKeyListener(this);
		ani.start();
	}
	
	public abstract Dimension getDimension();

	public abstract void keyPressed(KeyEvent e);

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void paint(Graphics g) {
		update(g);
	}

	@Override
	public void update(Graphics g) {
		offGraphics.setColor(Color.black);
		offGraphics.fillRect(0, 0, getWidth(), getHeight());
		if(initialized){
			paintAnimation(offGraphics);
		}else{
			//Show slash screen
			//offGraphics.drawString(images[Images.LOADING.ordinal()], 0, 0, this);
		}
		g.drawImage(offImage, 0, 0, this);
	}
	
	protected abstract void paintAnimation(Graphics2D g);

	protected abstract void animate();

	class Animation extends Thread {
		@Override
		public void run() {
			log.fine("Starting Animation!");
			try {
				if(mediaTracker != null) {
					mediaTracker.waitForAll();
					if(MediaTracker.COMPLETE != mediaTracker.statusAll(true)){
						log.severe("Could not load all images!"+getImageProblem());
					}else{
						log.fine("All images loaded!");
						initialized = true;
					}
				}else{
					initialized = true;
				}
			} catch (InterruptedException e) {
				log.warning("waitForAll exception: "+e);
			}
            while (true) {
        		animate();
        		repaint();
            }
		}
	}

	protected void addImage(int id, String filename, boolean wait){
		if(mediaTracker == null){
			mediaTracker = new MediaTracker(this);
		}

		if(images.get(id) != null){
			return;
		}
		Image image = getImage(getCodeBase(), filename);
		images.put(id, image);
		mediaTracker.addImage(image, id);
		
		if(wait){
			try {
				mediaTracker.waitForID(id);
			} catch (InterruptedException e) {
				log.warning("waitForID exception: "+e);
			}
			if(MediaTracker.COMPLETE != mediaTracker.statusID(id, true)){
				log.severe("Could not load loading image!");
			}else{
				log.fine("Loaded loading image!");
			}
		}
	}
	
	protected Image getImage(int id){
		return images.get(id);
	}

	protected void addAudio(int id, String filename){
		sounds.put(id, getAudioClip(getCodeBase(), filename));
	}

	protected AudioClip getAudio(int id){
		return sounds.get(id);
	}

	private String getImageProblem(){
		StringBuffer sb = new StringBuffer();
		sb.append("Image problem: ");
		for (Entry<Integer, Image> image : images.entrySet()) {
			switch(mediaTracker.statusID(image.getKey(), false)){
			case MediaTracker.ABORTED:
				sb.append(image.getValue() +" was aborted! ");
				break;
			case MediaTracker.COMPLETE:
				break;
			case MediaTracker.ERRORED:
				sb.append(image.getValue()+" was errored! ");
				break;
			case MediaTracker.LOADING:
				sb.append(image.getValue()+" is loading! ");
				break;
			default:
				sb.append(image.getValue()+" has unknown status! ");
			}
		}
		return sb.toString();
	}

	protected boolean isInitialized(){
		return initialized;
	}
	
}
