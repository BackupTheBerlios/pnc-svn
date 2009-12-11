package com.mathias.android.boardgame.tetris;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.KeyEvent;

import com.mathias.android.boardgame.R;
import com.mathias.android.boardgame.Util;
import com.mathias.android.boardgame.tetris.Shape.Direction;

public class Tetris {

	private static final long serialVersionUID = 1L;

	public static final int COLS = 10;
	public static final int ROWS = 20;
	public static final int SIZE = 30;
	public static final int CSTART = COLS / 2;
	public static final int RSTART = 1;

	private static final int WIDTH = COLS * SIZE + 300;
	private static final int HEIGHT = ROWS * SIZE;

	private final List<Shape> shapes = new ArrayList<Shape>();

	private long score = 0;
	private boolean gameOver = true;
	private Shape curr = null;
	private Shape next = null;
	private List<Block> grid = new ArrayList<Block>();
	private List<String> highScore = null;

	private final Paint mPaint = new Paint();

	private Map<Integer, Bitmap> images;

	public void init(Context cxt) {

		images = new HashMap<Integer, Bitmap>();
		images.put(R.drawable.a, BitmapFactory.decodeResource(cxt
				.getResources(), R.drawable.a));
		images.put(R.drawable.b, BitmapFactory.decodeResource(cxt
				.getResources(), R.drawable.b));
		images.put(R.drawable.e, BitmapFactory.decodeResource(cxt
				.getResources(), R.drawable.e));
		images.put(R.drawable.floor, BitmapFactory.decodeResource(cxt
				.getResources(), R.drawable.floor));
		images.put(R.drawable.green, BitmapFactory.decodeResource(cxt
				.getResources(), R.drawable.green));
		images.put(R.drawable.l, BitmapFactory.decodeResource(cxt
				.getResources(), R.drawable.l));
		images.put(R.drawable.l2, BitmapFactory.decodeResource(cxt
				.getResources(), R.drawable.l2));
		images.put(R.drawable.leaf, BitmapFactory.decodeResource(cxt
				.getResources(), R.drawable.leaf));
		images.put(R.drawable.pastell, BitmapFactory.decodeResource(cxt
				.getResources(), R.drawable.pastell));
		images.put(R.drawable.stone, BitmapFactory.decodeResource(cxt
				.getResources(), R.drawable.stone));
		images.put(R.drawable.tiger, BitmapFactory.decodeResource(cxt
				.getResources(), R.drawable.tiger));
		images.put(R.drawable.wall, BitmapFactory.decodeResource(cxt
				.getResources(), R.drawable.wall));
		images.put(R.drawable.water, BitmapFactory.decodeResource(cxt
				.getResources(), R.drawable.water));

		// T
		shapes.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[] {
				new Point(-1, 0), new Point(0, 0), new Point(1, 0),
				new Point(0, 1) }, images.get(R.drawable.b)));
		// |
		shapes.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[] {
				new Point(1, 0), new Point(0, 0), new Point(-1, 0),
				new Point(-2, 0) }, images.get(R.drawable.e)));
		// #
		shapes.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[] {
				new Point(1, 0), new Point(0, 0), new Point(0, 1),
				new Point(1, 1) }, images.get(R.drawable.l)));
		// s
		shapes.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[] {
				new Point(0, -1), new Point(0, 0), new Point(1, 0),
				new Point(1, 1) }, images.get(R.drawable.l2)));
		// z
		shapes.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[] {
				new Point(0, -1), new Point(0, 0), new Point(-1, 0),
				new Point(-1, 1) }, images.get(R.drawable.a)));
		// L
		shapes.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[] {
				new Point(0, -1), new Point(0, 0), new Point(0, 1),
				new Point(1, 1) }, images.get(R.drawable.b)));
		// _|
		shapes.add(new Shape(CSTART, RSTART, SIZE, SIZE, new Point[] {
				new Point(0, -1), new Point(0, 0), new Point(0, 1),
				new Point(-1, 1) }, images.get(R.drawable.e)));

		gameOver = false;
		curr = newStructure();

		addWalls();
	}

	private void addWalls() {
		Bitmap wall = images.get(R.drawable.wall);
		for (int i = 0; i < ROWS; i++) {
			// first col
			grid.add(new Block(0, i, SIZE, SIZE, wall));
			// last col
			grid.add(new Block(COLS - 1, i, SIZE, SIZE, wall));
		}
		for (int i = 0; i < COLS; i++) {
			// bottom row
			grid.add(new Block(i, ROWS - 1, SIZE, SIZE, wall));
		}
	}

	protected void paintAnimation(Canvas g) {
		g.drawBitmap(images.get(R.drawable.b), 0, 0, mPaint);
		// background
		g.drawColor(android.R.color.primary_text_light); // red
		g.drawLine(0, 0, 0, ROWS * SIZE, mPaint);
		g.drawLine(0, ROWS * SIZE, COLS * SIZE, ROWS * SIZE, mPaint);
		g.drawLine(COLS * SIZE, ROWS * SIZE, COLS * SIZE, 0, mPaint);

		// score
		g.drawText("Score: " + score, SIZE * COLS + 20, 50, mPaint);

		// next
		next.paint(g, 15, 3);

		// blocks
		for (Block s : grid) {
			s.paint(g, 0, 0);
		}

		// current block
		curr.paint(g);

		// game over
		if (gameOver) {
			g.drawText("GAME OVER", SIZE * COLS + 20, HEIGHT / 2 - 115, mPaint);
			for (int i = 0; highScore != null && i < highScore.size(); i++) {
				g.drawText(highScore.get(i), SIZE * COLS + 20, HEIGHT / 2 - 100
						+ (40 * (i + 1)), mPaint);
			}
		}
	}

	public void keyPressed(KeyEvent e) {
		// if(KeyEvent.VK_LEFT == e.getKeyCode()){
		// left();
		// }else if(KeyEvent.VK_RIGHT == e.getKeyCode()){
		// right();
		// }else if(KeyEvent.VK_UP == e.getKeyCode()){
		// up();
		// }else if(KeyEvent.VK_DOWN == e.getKeyCode()){
		// down();
		// }
		// repaint();
		// e.consume();
	}

	public synchronized void left() {
		if (gameOver) {
			return;
		}
		if (curr.x > 0) {
			curr.x--;
			if (curr.inside(grid)) {
				curr.x++;
			}
		}
	}

	public synchronized void right() {
		if (gameOver) {
			return;
		}
		if (curr.x < COLS) {
			curr.x++;
			if (curr.inside(grid)) {
				curr.x--;
			}
		}
	}

	public synchronized void down() {
		if (gameOver) {
			return;
		}
		if (curr.y < ROWS) {
			curr.y++;
			if (curr.inside(grid)) {
				curr.y--;
				for (Block p : curr.getPoints()) {
					grid.add(p.clone(p.x + curr.x, p.y + curr.y));
				}
				int counter = 0;
				for (int j = 0; j < ROWS - 1; j++) {
					boolean completeRow = true;
					for (int i = 1; i < COLS - 1; i++) {
						if (getGrid(i, j) == null) {
							completeRow = false;
							break;
						}
					}
					if (completeRow) {
						for (Iterator<Block> it = grid.iterator(); it.hasNext();) {
							Block b = it.next();
							if (b.x > 0 && b.x < COLS - 1) {
								if (b.y == j) {
									it.remove();
								} else if (b.y < j) {
									b.y++;
								}
							}
						}
						counter++;
					}
				}
				score += Math.pow(counter * 10, 1.5);
				curr = newStructure();
				if (curr.inside(grid)) {
					gameOver = true;
					// StoreHighscore hs = new StoreHighscore(hsHost, hsPort);
					// try {
					// hs.sendHighScore("bella", score);
					// highScore = hs.fetchHighScore();
					// } catch (IOException e) {
					// highScore = new ArrayList<String>();
					// highScore.add("No connection!");
					// System.err.println(e.getMessage());
					// }
				}
			}
		}
	}

	private Block getGrid(int x, int y) {
		for (Block b : grid) {
			if (b.x == x && b.y == y) {
				return b;
			}
		}
		return null;
	}

	public synchronized void up() {
		if (gameOver) {
			return;
		}
		curr.transpose(Direction.CLOCKWISE);
		if (curr.inside(grid)) {
			curr.transpose(Direction.COUNTERCLOCKWISE);
		}
	}

	private Shape newStructure() {
		Shape t = next;
		if (t == null) {
			t = shapes.get(getRand()).clone();
		}
		next = shapes.get(getRand()).clone();
		return t;
	}

	private int getRand() {
		return new Random().nextInt(shapes.size());
	}

	protected void animate() {
		if (gameOver) {
			Util.sleep(Long.MAX_VALUE);
		} else {
			Util.sleep(1000);
		}
		down();
	}

	public long delay() {
		if (gameOver) {
			return Long.MAX_VALUE;
		}
		return 1000;
	}

}
