package com.mathias.bellatetris;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mathias.bellatetris.Shape.Direction;
import com.mathias.drawutils.Util;
import com.mathias.drawutils.applet.MediaApplet;


public class Tetris extends MediaApplet {

	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger(Tetris.class.getName());

	public static final int COLS = 10;
	public static final int ROWS = 20;
	public static final int SIZE = 30;
	public static final int CSTART = COLS/2;
	public static final int RSTART = 1;

	private static final int WIDTH = COLS*SIZE;
	private static final int HEIGHT = ROWS*SIZE;

	private final List<Shape> shapes = new ArrayList<Shape>();

	private long score = 0;
	private boolean gameOver = false;
	private Shape curr = null;
	private List<Block> grid = new ArrayList<Block>();

	public void init() {

		Util.addConsoleHandler(Tetris.class.getPackage().getName());
		log.setLevel(Level.FINE);

		log.fine("initializing Bella Lumnines...");

		for (Images image : Images.values()) {
			addImage(image.ordinal(), image.getFile(), true);
		}

		// T
		shapes.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[] {
				new Point(-1, 0), new Point(0, 0), new Point(1, 0),
				new Point(0, 1) }, getImage(Images.FOOD.ordinal())));
		// |
		shapes.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[] {
				new Point(1, 0), new Point(0, 0), new Point(-1, 0),
				new Point(-2, 0) }, getImage(Images.GREEN.ordinal())));
		// #
		shapes.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[] {
				new Point(1, 0), new Point(0, 0), new Point(0, 1),
				new Point(1, 1) }, getImage(Images.LEAF.ordinal())));
		// s
		shapes.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[] {
				new Point(0, -1), new Point(0, 0), new Point(1, 0),
				new Point(1, 1) }, getImage(Images.PASTELL.ordinal())));
		// z
		shapes.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[] {
				new Point(0, -1), new Point(0, 0), new Point(-1, 0),
				new Point(-1, 1) }, getImage(Images.STONE.ordinal())));
		// L
		shapes.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[] {
				new Point(0, -1), new Point(0, 0), new Point(0, 1),
				new Point(1, 1) }, getImage(Images.TIGER.ordinal())));
		// _|
		shapes.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[] {
				new Point(0, -1), new Point(0, 0), new Point(0, 1),
				new Point(-1, 1) }, getImage(Images.WATER.ordinal())));

		gameOver = false;
		curr = newStructure();
		
		addWalls();

		super.init();
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
	protected void paintAnimation(Graphics2D g){
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
		int i = new Random().nextInt(shapes.size());
		Shape s = shapes.get(i);
		return s.clone(s.x, s.y);
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

}
