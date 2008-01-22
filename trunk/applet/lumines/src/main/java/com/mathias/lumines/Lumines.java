package com.mathias.lumines;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class Lumines extends Applet implements KeyListener {

	private static final Logger log = Logger.getLogger(Lumines.class.getName());

	private MediaTracker mediaTracker;
	private boolean initialized;
	private long startup;
	private Image[] images;

	public static final int COLS = 20;
	public static final int ROWS = 20;
	public static final int WIDTH = 20;
	public static final int HEIGHT = 23;

	private int width = 800;
	private int height = 800;
	private Matrix matrix;

	// Vars for the offscreen image.
	private Image offImage;
	private Graphics2D offGraphics;
	
	AudioClip lock;

	public Lumines() {
		super();
	}
	
	@Override
	public void init() {
		startup = System.currentTimeMillis()+1000;

		Util.addConsoleHandler(Lumines.class.getPackage().getName());
		log.setLevel(Level.INFO);

		log.fine("initializing Bella Lumnines...");

		initialized = false;

		setBackground(Color.black);
		setSize(width, height);
		
		offImage = createImage(width, height);
		offGraphics = (Graphics2D)offImage.getGraphics();

		loadAudio();
		loadImages();

		matrix = new Matrix(COLS, ROWS);

		Animation ani = new Animation();
		ani.setDaemon(true);

		addKeyListener(this);
		ani.start();
	}
	
	@Override
	public void paint(Graphics g) {
		update(g);
	}

	@Override
	public void update(Graphics g) {
		offGraphics.setColor(Color.black);
		offGraphics.fillRect(0, 0, width, height);
		if(initialized && startup < System.currentTimeMillis()){
			paintMatrix(offGraphics);
		}else{
			offGraphics.drawImage(images[Images.LOADING.ordinal()], 0, 0, this);
		}
		g.drawImage(offImage, 0, 0, this);
	}
	
	private void paintMatrix(Graphics2D g){
		//background
		paintBackground(g);

		//blocks
		for (Block b : matrix.getGrid()) {
			if(b != null){
				int smooth = matrix.getSmooth();
				if(b.falling && smooth != 0){
					g.drawImage(images[b.image], b.x*WIDTH, b.y*HEIGHT-HEIGHT/smooth, this);
				}else{
					if(b.remove){
						g.drawImage(images[b.image+2], b.x*WIDTH, b.y*HEIGHT, this);
					}else{
						g.drawImage(images[b.image], b.x*WIDTH, b.y*HEIGHT, this);
					}
				}
			}
		}

		//game over?
		if(matrix.isGameOver()){
			g.drawImage(images[Images.GAMEOVER.ordinal()], width/2, height/2, this);
		}

		//next blocks
		int counter = 0;
		List<BlockGroup> q = matrix.getQueue();
		for (BlockGroup bg : q) {
			if(bg != null && bg.isActive()){
				int left = bg.tl.x-COLS-5;
				int top = bg.tl.y-5-(counter*q.size());
				g.drawImage(images[bg.tl.image], (bg.tl.x-left)*WIDTH, (bg.tl.y-top)*HEIGHT, this);
				g.drawImage(images[bg.tr.image], (bg.tr.x-left)*WIDTH, (bg.tr.y-top)*HEIGHT, this);
				g.drawImage(images[bg.bl.image], (bg.bl.x-left)*WIDTH, (bg.bl.y-top)*HEIGHT, this);
				g.drawImage(images[bg.br.image], (bg.br.x-left)*WIDTH, (bg.br.y-top)*HEIGHT, this);

				g.setColor(Color.white);
				g.drawString(""+(counter+1), (COLS+3)*WIDTH, (5+(counter*q.size()))*HEIGHT);
				counter ++;
			}
		}
		
		//score
		g.setColor(Color.red);
		g.drawString("Score: "+matrix.getScore(), (COLS+3)*WIDTH, 4*HEIGHT);
	}

	private void paintBackground(Graphics2D g){
		g.setColor(Color.red);
		g.drawLine(0, 0, 0, ROWS*HEIGHT);
		g.drawLine(0, ROWS*HEIGHT, COLS*WIDTH, ROWS*HEIGHT);
		g.drawLine(COLS*WIDTH, ROWS*HEIGHT, COLS*WIDTH, 0);
		g.setColor(Color.gray);
		for(int y = 1; y < ROWS-1; y++){
			g.drawLine(0, y*HEIGHT, COLS*WIDTH, y*HEIGHT);
		}
		for(int x = 1; x < COLS-1; x++){
			g.drawLine(x*WIDTH, 0, x*WIDTH, ROWS*HEIGHT);
		}
		g.drawImage(images[Images.MOON.ordinal()], 0, 0, COLS*WIDTH, ROWS*HEIGHT, this);
	}

	public void keyPressed(KeyEvent e) {
		log.fine("keyPressed!");
		if(KeyEvent.VK_LEFT == e.getKeyCode()){
			matrix.left();
		}else if(KeyEvent.VK_RIGHT == e.getKeyCode()){
			matrix.right();
		}else if(KeyEvent.VK_UP == e.getKeyCode()){
			matrix.rotate();
		}else if(KeyEvent.VK_DOWN == e.getKeyCode()){
			matrix.down();
		}
		repaint();
		e.consume();
	}
	
	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	class Animation extends Thread {
		@Override
		public void run() {
			log.fine("Starting Animation!");
			try {
				mediaTracker.waitForAll();
				if(MediaTracker.COMPLETE != mediaTracker.statusAll(true)){
					log.severe("Could not load all images!"+getImageProblem());
				}else{
					log.fine("All images loaded!");
					initialized = true;
				}
			} catch (InterruptedException e) {
				log.warning("waitForAll exception: "+e);
			}
            while (true) {
        		try {
	            	if (matrix.isGameOver()) {
						sleep(Long.MAX_VALUE);
					} else {
						long delay = matrix.getDelay();
						if(delay > 50){
							delay--;
						}
						sleep(delay);
						matrix.setDelay(delay);
					}
        		} catch (InterruptedException e) {
        			log.fine("InterruptedException: "+e.getMessage());
        		}
        		//gravity
        		matrix.gravity();
        		repaint();
            }
		}
	}

	private void loadImages() {
		log.fine("loading images...");

		images = new Image[Images.count()];
		mediaTracker = new MediaTracker(this);

		//loading image
		addImage(Images.LOADING);
		try {
			mediaTracker.waitForID(Images.LOADING.ordinal());
			if(MediaTracker.COMPLETE != mediaTracker.statusID(Images.LOADING.ordinal(), true)){
				log.severe("Could not load loading image!");
			}else{
				log.fine("Loaded loading image!");
			}
		} catch (InterruptedException e) {
			log.warning("waitForID exception: "+e);
		}

		//images
		for (Images image : Images.values()){
			if(!image.equals(Images.LOADING)){
				addImage(image);
			}
		}
	}
	
	private void addImage(Images image){
		images[image.ordinal()] = getImage(getCodeBase(), image.getFile());
		mediaTracker.addImage(images[image.ordinal()], image.ordinal());
	}

	private String getImageProblem(){
		StringBuffer sb = new StringBuffer();
		sb.append("Image problem: ");
		for (Images image : Images.values()) {
			switch(mediaTracker.statusID(image.ordinal(), false)){
			case MediaTracker.ABORTED:
				sb.append(image.name() +" was aborted! ");
				break;
			case MediaTracker.COMPLETE:
				break;
			case MediaTracker.ERRORED:
				sb.append(image.name()+" was errored! ");
				break;
			case MediaTracker.LOADING:
				sb.append(image.name()+" is loading! ");
				break;
			default:
				sb.append(image.name()+" has unknown status! ");
			}
		}
		return sb.toString();
	}

	private void loadAudio(){
		lock = getAudioClip(getCodeBase(), "audio/lock.au");
	}

}
