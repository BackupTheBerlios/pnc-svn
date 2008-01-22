package com.mathias.bellatetris;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mathias.bellatetris.Shape.Direction;


public class Tetris extends Applet implements KeyListener {

	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger(Tetris.class.getName());

	public static final int COLS = 10;
	public static final int ROWS = 20;
	public static final int SIZE = 10;
	public static final int CSTART = COLS/2;
	public static final int RSTART = 1;

	private static final int WIDTH = 500;
	private static final int HEIGHT = 500;

	private static final List<Shape> SHAPES = new ArrayList<Shape>();
	static {
		//T
		SHAPES.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[]{new Point(-1,0), new Point(0,0), new Point(1,0), new Point(0,1)}, Color.red));
		//|
		SHAPES.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[]{new Point(1,0), new Point(0,0), new Point(-1,0), new Point(-2,0)}, Color.green));
		//#
		SHAPES.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[]{new Point(1,0), new Point(0,0), new Point(0,1), new Point(1,1)}, Color.magenta));
		//s
		SHAPES.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[]{new Point(0,-1), new Point(0,0), new Point(1,0), new Point(1,1)}, Color.yellow));
		//z
		SHAPES.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[]{new Point(0,-1), new Point(0,0), new Point(-1,0), new Point(-1,1)}, Color.blue));
		//L
		SHAPES.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[]{new Point(0,-1), new Point(0,0), new Point(0,1), new Point(1,1)}, Color.cyan));
		//_|
		SHAPES.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[]{new Point(0,-1), new Point(0,0), new Point(0,1), new Point(-1,1)}, Color.orange));
	}

	// Vars for the offscreen image.
	private Image offImage;
	private Graphics2D offGraphics;
	private long score = 0;
	private Animation ani;
	private boolean gameOver = false;
	private Shape curr = null;
	private List<Block> grid = new ArrayList<Block>();

	public Tetris() {
		super();
	}

	public void init() {
		Util.addConsoleHandler(Tetris.class.getPackage().getName());
		log.setLevel(Level.FINE);

		log.fine("initializing Bella Lumnines...");

		this.setSize(WIDTH, HEIGHT);
		setBackground(Color.black);
		addKeyListener(this);

		offImage = createImage(WIDTH, HEIGHT);
		offGraphics = (Graphics2D)offImage.getGraphics();

		gameOver = false;
		curr = newStructure();
		
		addWalls();

		ani = new Animation();
		ani.setDaemon(true);
		ani.start();
	}
	
	private void addWalls(){
		for(int i = 0; i < ROWS; i++){
			//first col
			grid.add(new Block(0, i, SIZE, SIZE, Color.gray));
			//last col
			grid.add(new Block(COLS-1, i, SIZE, SIZE, Color.gray));
		}
		for(int i = 0; i < COLS; i++){
			//bottom row
			grid.add(new Block(i, ROWS-1, SIZE, SIZE, Color.gray));
		}
	}
	
	@Override
	public void paint(Graphics g) {
		update(g);
	}

	@Override
	public void update(Graphics g) {
		offGraphics.setColor(Color.black);
		offGraphics.fillRect(0, 0, WIDTH, HEIGHT);
		paintIt(offGraphics);
		g.drawImage(offImage, 0, 0, this);
	}
	
	private void paintIt(Graphics2D g){
		paintBackground(g);

		for (Block s : grid) {
			s.paint(g, 0, 0);
		}

		curr.paint(g);

		if(gameOver){
			g.setColor(Color.yellow);
			g.drawString("GAME OVER", WIDTH/2, HEIGHT/2);
		}
	}

	public void paintBackground(Graphics2D g){
		g.setColor(Color.red);
		g.drawLine(0, 0, 0, ROWS*SIZE);
		g.drawLine(0, ROWS*SIZE, COLS*SIZE, ROWS*SIZE);
		g.drawLine(COLS*SIZE, ROWS*SIZE, COLS*SIZE, 0);
		
		g.drawString("Score: "+score, 200, 50);
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
					boolean complete = true;
					for(int i = 1; i < COLS-1; i++){
						if(getGrid(i, j) == null){
							complete = false;
							break;
						}
					}
					if(complete){
						List<Block> remove = new ArrayList<Block>();
						for (Block b : grid) {
							if(b.x > 0 && b.x < COLS-1){
								if(b.y == j){
									remove.add(b);
								}else if(b.y < j){
									b.y++;
								}
							}
						}
						grid.removeAll(remove);
						counter++;
					}
				}
				score += Math.pow(counter*10, 1.5);
				curr = newStructure();
				if(curr.inside(grid)){
					gameOver = true;
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

	public Shape newStructure(){
		int i = new Random().nextInt(SHAPES.size());
		Shape s = SHAPES.get(i);
		return s.clone(s.x, s.y);
	}

	class Animation extends Thread {
		@Override
		public void run() {
            while (true) {
        		try {
	            	if (gameOver) {
						sleep(Long.MAX_VALUE);
					} else {
						sleep(1000);
					}
        		} catch (InterruptedException e) {
        		}
            	down();
            	repaint();
            }
		}
	}
	
	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

}
