package com.mathias.bella.lumines;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class BellaLumines extends Applet implements KeyListener, Constants, RepaintListener {

	private static final Logger log = Logger.getLogger(BellaLumines.class.getName());

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
	private Graphics offGraphics;

	public BellaLumines() {
		super();
	}
	
	@Override
	public void init() {
		startup = System.currentTimeMillis()+1000;

		Util.addConsoleHandler(BellaLumines.class.getPackage().getName());
		log.setLevel(Level.INFO);

		log.fine("initializing Bella Lumnines...");

		initialized = false;

		setBackground(Color.black);
		setSize(width, height);
		
		offImage = createImage(width, height);
		offGraphics = offImage.getGraphics();

		loadImages();

		matrix = new Matrix(this, COLS, ROWS);
		matrix.addBlockGroup();

		Animation ani = new Animation();
		ani.setDaemon(true);

		addKeyListener(this);
		ani.start();
	}
	
	@Override
	public void update(Graphics g) {
		offGraphics.setColor(Color.black);
		offGraphics.fillRect(0, 0, width, height);
		if(initialized && startup < System.currentTimeMillis()){
			paintMatrix(offGraphics);
		}else{
			offGraphics.drawImage(images[IMAGE_LOADING], 0, 0, this);
		}
		g.drawImage(offImage, 0, 0, this);
	}
	
	@Override
	public void paint(Graphics g) {
		update(g);
	}

	private void paintMatrix(Graphics g){
		//background
		paintBackground(g);

		//blocks
		for (Block b : matrix.getGrid()) {
			if(b != null){
				g.drawImage(images[b.image], b.x*WIDTH, b.y*HEIGHT, this);
			}
		}

		//game over?
		if(matrix.isGameOver()){
			g.drawImage(images[IMAGE_GAMEOVER], width/2, height/2, this);
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

	private void paintBackground(Graphics g){
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
						sleep(delay--);
						matrix.setDelay(delay);
					}
        		} catch (InterruptedException e) {
        			log.fine("InterruptedException: "+e.getMessage());
        		}
        		//gravity
            	if(!matrix.down()){
            		matrix.delayedGravity();
            		matrix.addBlockGroup();
            	}
        		repaint();
            }
		}
	}

	private void loadImages() {
		log.fine("loading images...");

		URL codeBase = getCodeBase();
		images = new Image[Constants.IMAGES];
		mediaTracker = new MediaTracker(this);

		//loading image
		images[IMAGE_LOADING] = getImage(codeBase, "images/loading.gif");
		mediaTracker.addImage(images[IMAGE_LOADING], IMAGE_LOADING);
		try {
			mediaTracker.waitForID(IMAGE_LOADING);
			if(MediaTracker.COMPLETE != mediaTracker.statusID(IMAGE_LOADING, true)){
				log.severe("Could not load loading image!");
			}else{
				log.fine("Loaded loading image!");
			}
		} catch (InterruptedException e) {
			log.warning("waitForID exception: "+e);
		}

		//images
		images[IMAGE_1TL] =  getImage(codeBase, "images/b_tl.gif");
		images[IMAGE_1TR] =  getImage(codeBase, "images/b_tr.gif");
		images[IMAGE_1BL] =  getImage(codeBase, "images/b_bl.gif");
		images[IMAGE_1BR] =  getImage(codeBase, "images/b_br.gif");
		images[IMAGE_2TL] =  getImage(codeBase, "images/e_tl.gif");
		images[IMAGE_2TR] =  getImage(codeBase, "images/e_tr.gif");
		images[IMAGE_2BL] =  getImage(codeBase, "images/e_bl.gif");
		images[IMAGE_2BR] =  getImage(codeBase, "images/e_br.gif");
		images[IMAGE_3TL] =  getImage(codeBase, "images/l1_tl.gif");
		images[IMAGE_3TR] =  getImage(codeBase, "images/l1_tr.gif");
		images[IMAGE_3BL] = getImage(codeBase, "images/l1_bl.gif");
		images[IMAGE_3BR] = getImage(codeBase, "images/l1_br.gif");
		images[IMAGE_4TL] = getImage(codeBase, "images/l2_tl.gif");
		images[IMAGE_4TR] = getImage(codeBase, "images/l2_tr.gif");
		images[IMAGE_4BL] = getImage(codeBase, "images/l2_bl.gif");
		images[IMAGE_4BR] = getImage(codeBase, "images/l2_br.gif");
		images[IMAGE_5TL] = getImage(codeBase, "images/a_tl.gif");
		images[IMAGE_5TR] = getImage(codeBase, "images/a_tr.gif");
		images[IMAGE_5BL] = getImage(codeBase, "images/a_bl.gif");
		images[IMAGE_5BR] = getImage(codeBase, "images/a_br.gif");
		images[IMAGE_WALL] = getImage(codeBase, "images/wall.gif");
		images[IMAGE_GAMEOVER] = getImage(codeBase, "images/gameover.gif");

		for (int i = 0; i < images.length; i++) {
			mediaTracker.addImage(images[i], i);
		}
	}

	private String getImageProblem(){
		StringBuffer sb = new StringBuffer();
		sb.append("Image problem: ");
		for(int i = 0; i < IMAGES; i++){
			switch(mediaTracker.statusID(i, false)){
			case MediaTracker.ABORTED:
				sb.append(getImageName(i)+" was aborted! ");
				break;
			case MediaTracker.COMPLETE:
				break;
			case MediaTracker.ERRORED:
				sb.append(getImageName(i)+" was errored! ");
				break;
			case MediaTracker.LOADING:
				sb.append(getImageName(i)+" is loading! ");
				break;
			default:
				sb.append(getImageName(i)+" has unknown status! ");
			}
		}
		return sb.toString();
	}
	
	public static String getImageName(int index){
		switch(index){
		case IMAGE_1TL:
			return "IMAGE_1TL";
		case IMAGE_1TR:
			return "IMAGE_1TR";
		case IMAGE_1BL:
			return "IMAGE_1BL";
		case IMAGE_1BR:
			return "IMAGE_1BR";
		case IMAGE_2TL:
			return "IMAGE_2TL";
		case IMAGE_2TR:
			return "IMAGE_2TR";
		case IMAGE_2BL:
			return "IMAGE_2BL";
		case IMAGE_2BR:
			return "IMAGE_2BR";
		case IMAGE_3TL:
			return "IMAGE_3TL";
		case IMAGE_3TR:
			return "IMAGE_3TR";
		case IMAGE_3BL:
			return "IMAGE_3BL";
		case IMAGE_3BR:
			return "IMAGE_3BR";
		case IMAGE_4TL:
			return "IMAGE_4TL";
		case IMAGE_4TR:
			return "IMAGE_4TR";
		case IMAGE_4BL:
			return "IMAGE_4BL";
		case IMAGE_4BR:
			return "IMAGE_4BR";
		case IMAGE_5TL:
			return "IMAGE_5TL";
		case IMAGE_5TR:
			return "IMAGE_5TR";
		case IMAGE_5BL:
			return "IMAGE_5BL";
		case IMAGE_5BR:
			return "IMAGE_5BR";
		case IMAGE_LOADING:
			return "IMAGE_LOADING";
		case IMAGE_WALL:
			return "IMAGE_WALL";
		case IMAGE_GAMEOVER:
			return "IMAGE_GAMEOVER";
		default:
			return "UNKNOWN";
		}
	}

}
