package com.mathias.drawutils.applet;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

public abstract class MediaApplet extends Applet {

	private MediaTracker mediaTracker;

	private boolean initialized;

	// Vars for the offscreen image.
	private Image offImage;
	private Graphics2D offGraphics;
	
	private Map<Integer, Image> images = new HashMap<Integer, Image>();
	private Map<Integer, AudioClip> sounds = new HashMap<Integer, AudioClip>();

	@Override
	public void init() {
		setSize(getDimension());
		setBackground(Color.black);

		offImage = createImage(getWidth(), getHeight());
		offGraphics = (Graphics2D)offImage.getGraphics();

		new Thread(){
			public void run() {
				try {
					if(mediaTracker != null) {
						mediaTracker.waitForAll();
						if(MediaTracker.COMPLETE != mediaTracker.statusAll(true)){
							LOG("Could not load all images!"+getImageProblem());
						}else{
//							LOG("All images loaded!");
							initialized = true;
						}
					}else{
						initialized = true;
					}
				} catch (InterruptedException e) {
					LOG("waitForAll exception: "+e);
				}
			}
		}.start();
//		Animation ani = new Animation();
//		ani.setDaemon(true);
//		ani.start();

		new Timer(true).schedule(new TimerTask(){
			@Override
			public void run() {
        		repaint();
        		animate();
			}
		}, 0, delay());

	}

	public abstract long delay();

	public abstract Dimension getDimension();

	@Override
	public void paint(Graphics g) {
		update(g);
	}

	@Override
	public void update(Graphics g) {
//		offGraphics.setColor(Color.black);
//		offGraphics.fillRect(0, 0, getWidth(), getHeight());
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

	protected void addImage(Enum<?> e, String filename, boolean wait){
		addImage(e.ordinal(), filename, wait);
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
				LOG("waitForID exception: "+e);
			}
			if(MediaTracker.COMPLETE != mediaTracker.statusID(id, true)){
				LOG("Could not load loading image!");
			}
		}
	}
	
	protected Image getImage(int id){
		return images.get(id);
	}

	protected Image getImage(Enum<?> e){
		return images.get(e.ordinal());
	}

	protected void addAudio(Enum<?> e, String filename){
		this.addAudio(e.ordinal(), filename);
	}

	protected void addAudio(int id, String filename){
		sounds.put(id, getAudioClip(getCodeBase(), filename));
	}

	protected AudioClip getAudio(Enum<?> e){
		return this.getAudio(e.ordinal());
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

	public static void sleep(long millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}

	public static void LOG(String msg){
		System.out.println(msg);
	}

}
