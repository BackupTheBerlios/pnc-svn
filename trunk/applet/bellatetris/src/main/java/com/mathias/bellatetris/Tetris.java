package com.mathias.bellatetris;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.mathias.bellatetris.Shape.Direction;
import com.mathias.drawutils.Util;
import com.mathias.drawutils.applet.MediaApplet;


public class Tetris extends MediaApplet {

	private static final long serialVersionUID = 1L;

	public static final int COLS = 10;
	public static final int ROWS = 20;
	public static final int SIZE = 30;
	public static final int CSTART = COLS/2;
	public static final int RSTART = 1;

	private static final int WIDTH = COLS*SIZE+300;
	private static final int HEIGHT = ROWS*SIZE;

	private final List<Shape> shapes = new ArrayList<Shape>();

	private long score = 0;
	// TODO private boolean gameOver = false;
	private boolean gameOver = true;
	private Shape curr = null;
	private Shape next = null;
	private List<Block> grid = new ArrayList<Block>();
	private Font font;
	private List<String> highScore = null;
	
	private String hsHost;
	private int hsPort = 7200;

	public void init() {

		for (Images image : Images.values()) {
			addImage(image.ordinal(), image.getFile(), true);
		}
		
		font = Util.getFallbackFont("arial", 30);

		hsHost = getParameter("host");
		if(Util.isEmpty(hsHost)){
			hsHost = "localhost";
		}
		String port = getParameter("port");
		if(!Util.isEmpty(port)){
			try{
				hsPort = Integer.parseInt(port);
			}catch(NumberFormatException e){
				System.err.println(e.getMessage());
			}
		}

		// T
		shapes.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[] {
				new Point(-1, 0), new Point(0, 0), new Point(1, 0),
				new Point(0, 1) }, getImage(Images.B.ordinal())));
		// |
		shapes.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[] {
				new Point(1, 0), new Point(0, 0), new Point(-1, 0),
				new Point(-2, 0) }, getImage(Images.E.ordinal())));
		// #
		shapes.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[] {
				new Point(1, 0), new Point(0, 0), new Point(0, 1),
				new Point(1, 1) }, getImage(Images.L.ordinal())));
		// s
		shapes.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[] {
				new Point(0, -1), new Point(0, 0), new Point(1, 0),
				new Point(1, 1) }, getImage(Images.L2.ordinal())));
		// z
		shapes.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[] {
				new Point(0, -1), new Point(0, 0), new Point(-1, 0),
				new Point(-1, 1) }, getImage(Images.A.ordinal())));
		// L
		shapes.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[] {
				new Point(0, -1), new Point(0, 0), new Point(0, 1),
				new Point(1, 1) }, getImage(Images.B.ordinal())));
		// _|
		shapes.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[] {
				new Point(0, -1), new Point(0, 0), new Point(0, 1),
				new Point(-1, 1) }, getImage(Images.E.ordinal())));

		gameOver = false;
		curr = newStructure();
		
		addWalls();

		super.init();
	}
	
	private void addWalls(){
		Image wall = getImage(Images.WALL.ordinal());
		for(int i = 0; i < ROWS; i++){
			//first col
			grid.add(new Block(0, i, SIZE, SIZE, wall));
			//last col
			grid.add(new Block(COLS-1, i, SIZE, SIZE, wall));
		}
		for(int i = 0; i < COLS; i++){
			//bottom row
			grid.add(new Block(i, ROWS-1, SIZE, SIZE, wall));
		}
	}
	
	@Override
	protected void paintAnimation(Graphics2D g){
		g.drawImage(getImage(Images.BELLATETRIS.ordinal()), 0, 0, null);
		//background
		g.setColor(Color.red);
		g.drawLine(0, 0, 0, ROWS*SIZE);
		g.drawLine(0, ROWS*SIZE, COLS*SIZE, ROWS*SIZE);
		g.drawLine(COLS*SIZE, ROWS*SIZE, COLS*SIZE, 0);

		//score
		Util.drawString(g, Color.red, font, "Score: "+score, SIZE*COLS+20, 50);

		//next
		next.paint(g, 15, 3);

		//blocks
		for (Block s : grid) {
			s.paint(g, 0, 0);
		}

		//current block
		curr.paint(g);

		// game over
		if(gameOver){
			Util.drawString(g, Color.yellow, font, "GAME OVER", SIZE*COLS+20, HEIGHT/2-115);
			
			for (int i = 0; highScore != null && i < highScore.size(); i++) {
				Util.drawString(g, Color.white, font, highScore.get(i), SIZE*COLS+20, HEIGHT/2-100+(40*(i+1)));
			}
		}
	}

	public void keyPressed(KeyEvent e) {
		if(KeyEvent.VK_LEFT == e.getKeyCode()){
			left();
		}else if(KeyEvent.VK_RIGHT == e.getKeyCode()){
			right();
		}else if(KeyEvent.VK_UP == e.getKeyCode()){
			up();
		}else if(KeyEvent.VK_DOWN == e.getKeyCode()){
			down();
		}
		repaint();
		e.consume();
	}
	
	public synchronized void left(){
		if(gameOver){
			return;
		}
		if(curr.x > 0){
			curr.x--;
			if(curr.inside(grid)){
				curr.x++;
			}
		}
	}

	public synchronized void right(){
		if(gameOver){
			return;
		}
		if(curr.x < COLS){
			curr.x++;
			if(curr.inside(grid)){
				curr.x--;
			}
		}
	}

	public synchronized void down(){
		if(gameOver){
			return;
		}
		if(curr.y < ROWS){
			curr.y++;
			if(curr.inside(grid)){
				curr.y--;
				for (Block p : curr.getPoints()) {
					grid.add(p.clone(p.x+curr.x, p.y+curr.y));
				}
				int counter = 0;
				for(int j = 0; j < ROWS-1; j++){
					boolean completeRow = true;
					for(int i = 1; i < COLS-1; i++){
						if(getGrid(i, j) == null){
							completeRow = false;
							break;
						}
					}
					if(completeRow){
						for (Iterator<Block> it = grid.iterator(); it.hasNext();) {
							Block b = it.next();
							if(b.x > 0 && b.x < COLS-1){
								if(b.y == j){
									it.remove();
								}else if(b.y < j){
									b.y++;
								}
							}
						}
						counter++;
					}
				}
				score += Math.pow(counter*10, 1.5);
				curr = newStructure();
				if(curr.inside(grid)){
					gameOver = true;
					StoreHighscore hs = new StoreHighscore(hsHost, hsPort);
					try {
						hs.sendHighScore("bella", score);
						highScore = hs.fetchHighScore();
					} catch (IOException e) {
						highScore = new ArrayList<String>();
						highScore.add("No connection!");
						System.err.println(e.getMessage());
					}
				}
			}
		}
	}
	
	private Block getGrid(int x, int y){
		for (Block b : grid) {
			if(b.x == x && b.y == y){
				return b;
			}
		}
		return null;
	}
	
	public synchronized void up(){
		if(gameOver){
			return;
		}
		curr.transpose(Direction.CLOCKWISE);
		if(curr.inside(grid)){
			curr.transpose(Direction.COUNTERCLOCKWISE);
		}
	}

	private Shape newStructure(){
		Shape t = next;
		if(t == null){
			t = shapes.get(getRand()).clone();
		}
		next = shapes.get(getRand()).clone();
		return t; 
	}
	
	private int getRand(){
		return new Random().nextInt(shapes.size());
	}

	@Override
	protected void animate() {
    	if (gameOver) {
			Util.sleep(Long.MAX_VALUE);
		} else {
			Util.sleep(1000);
		}
    	down();
	}

	@Override
	public Dimension getDimension() {
		return new Dimension(WIDTH, HEIGHT);
	}

	@Override
	public long delay() {
    	if (gameOver) {
			return Long.MAX_VALUE;
		}
		return 1000;
	}

}
