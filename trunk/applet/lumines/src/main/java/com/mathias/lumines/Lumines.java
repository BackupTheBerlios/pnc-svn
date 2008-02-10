package com.mathias.lumines;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mathias.drawutils.Util;
import com.mathias.drawutils.applet.MediaApplet;

@SuppressWarnings("serial")
public class Lumines extends MediaApplet {

	private static final Logger log = Logger.getLogger(Lumines.class.getName());

	private long startup;

	public static final int COLS = 20;
	public static final int ROWS = 20;
	public static final int WIDTH = 20;
	public static final int HEIGHT = 23;

	private Matrix matrix;

	@Override
	public void init() {

		startup = System.currentTimeMillis()+1000;

		Util.addConsoleHandler(Lumines.class.getPackage().getName());
		log.setLevel(Level.INFO);

		log.fine("initializing Lumnines...");

		addImage(Images.LOADING.ordinal(), Images.LOADING.getFilename(), true);

		for (Images image : Images.values()) {
			addImage(image.ordinal(), image.getFilename(), false);
		}

		for (Sounds sound : Sounds.values()) {
			addAudio(sound.ordinal(), sound.getFilename());
		}

		matrix = new Matrix(COLS, ROWS);

		super.init();
	}

	protected void paintAnimation(Graphics2D g){
		if(isInitialized() && startup < System.currentTimeMillis()){
			paintMatrix(g);		
		}else{
			g.drawImage(getImage(Images.LOADING.ordinal()), 0, 0, this);			
		}
	}
	
	private void paintMatrix(Graphics2D g){
		//background
		paintBackground(g);

		//blocks
		for (Block b : matrix.getGrid()) {
			if(b != null){
				int smooth = matrix.getSmooth();
				if(b.falling && smooth != 0){
					g.drawImage(getImage(b.image), b.x*WIDTH, b.y*HEIGHT-HEIGHT/smooth, this);
				}else{
					if(b.remove){
						g.drawImage(getImage(b.image+2), b.x*WIDTH, b.y*HEIGHT, this);
					}else{
						g.drawImage(getImage(b.image), b.x*WIDTH, b.y*HEIGHT, this);
					}
				}
			}
		}

		//game over?
		if(matrix.isGameOver()){
			g.drawImage(getImage(Images.GAMEOVER.ordinal()), getWidth()/2, getHeight()/2, this);
		}

		//next blocks
		int counter = 0;
		List<BlockGroup> q = matrix.getQueue();
		for (BlockGroup bg : q) {
			if(bg != null && bg.isActive()){
				int left = bg.tl.x-COLS-5;
				int top = bg.tl.y-5-(counter*q.size());
				g.drawImage(getImage(bg.tl.image), (bg.tl.x-left)*WIDTH, (bg.tl.y-top)*HEIGHT, this);
				g.drawImage(getImage(bg.tr.image), (bg.tr.x-left)*WIDTH, (bg.tr.y-top)*HEIGHT, this);
				g.drawImage(getImage(bg.bl.image), (bg.bl.x-left)*WIDTH, (bg.bl.y-top)*HEIGHT, this);
				g.drawImage(getImage(bg.br.image), (bg.br.x-left)*WIDTH, (bg.br.y-top)*HEIGHT, this);

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
		g.drawImage(getImage(Images.MOON.ordinal()), 0, 0, COLS*WIDTH, ROWS*HEIGHT, this);
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
	
	@Override
	protected void animate() {
    	if (matrix.isGameOver()) {
			Util.sleep(Long.MAX_VALUE);
		} else {
			long delay = matrix.getDelay();
			if(delay > 50){
				delay--;
			}
			Util.sleep(delay);
			matrix.setDelay(delay);
		}
		//gravity
		matrix.gravity();
	}

	@Override
	public Dimension getDimension() {
		return new Dimension(800, 800);
	}

}
