package com.mathias.bella.lumines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class Matrix {
	
	private static final Logger log = Logger.getLogger(Matrix.class.getName());

	private boolean gameOver = false;
	private int cols;
	private int rows;
	private long score = 0;
	private long delay = 600;

	private List<Block> grid = Collections.synchronizedList(new ArrayList<Block>());
	private Queue<BlockGroup> queue = new ArrayBlockingQueue<BlockGroup>(3);

	private BlockGroup bg;
	
	private RepaintListener listener;

	public Matrix(RepaintListener listener, int cols, int rows){
		this.cols = cols;
		this.rows = rows;
		this.listener = listener;

		addWalls();

		for(int i = 0; i < 3; i++){
			queue.add(new BlockGroup(cols/2, 1));
		}
	}
	
	private void addWalls(){
		for(int i = 0; i < rows; i++){
			//first col
			grid.add(new Block(0, i, Constants.IMAGE_WALL));
			//last col
			grid.add(new Block(cols-1, i, Constants.IMAGE_WALL));
		}
		for(int i = 0; i < rows; i++){
			//bottom row
			grid.add(new Block(i, rows-1, Constants.IMAGE_WALL));
		}
	}

	private Block get(int x, int y){
		for (Block block : getGrid()) {
			if(block != null){
				if(block.x == x && block.y == y){
					return block;
				}
			}
		}
		return null;
	}
	
	private Block remove(int x, int y){
		Block b = get(x, y);
		if(b != null){
			grid.remove(b);
		}
		return b;
	}

	public void addBlockGroup(){
		bg = queue.remove();
		if(get(bg.bl.x, bg.bl.y) != null || get(bg.bl.x, bg.bl.y) != null){
			gameOver = true;
			return;
		}
		queue.add(new BlockGroup(cols/2, 1));
		grid.add(bg.tl);
		grid.add(bg.tr);
		grid.add(bg.bl);
		grid.add(bg.br);
	}

	public boolean left(){
		if(isGameOver()){
			return false;
		}
		if(bg.isActive() && (get(bg.tl.x-1, bg.tl.y) == null) && (get(bg.bl.x-1, bg.bl.y) == null)){
			bg.left();
			return true;
		}
		return false;
	}

	public boolean right(){
		if(isGameOver()){
			return false;
		}
		if(bg.isActive() && (get(bg.tr.x+1, bg.tr.y) == null) && (get(bg.br.x+1, bg.br.y) == null)){
			bg.right();
			return true;
		}
		return false;
	}
	
	public boolean down(){
		if(isGameOver()){
			return false;
		}
		if(bg.isActive() && (get(bg.bl.x, bg.bl.y+1) == null) && (get(bg.br.x, bg.br.y+1) == null)){
			bg.down();
			return true;
		}
		return false;
	}
	
	/**
	 * @return true if block has moved
	 */
	public boolean gravity(){
		boolean moved = false;
		for (Block block : getGrid()) {
			if(get(block.x, block.y+1) == null && block.y < rows-1){
				for(int i = block.y-1; i >= 0; i--){
					Block b = get(block.x, i);
					if(b == null){
						break;
					}
					b.y++;
				}
				block.y++;
				moved = true;
			}
			if(get(bg.bl.x, bg.bl.y+1) != null || get(bg.br.x, bg.br.y+1) != null){
				bg.setActive(false);
			}
		}
		if(!moved){
			for (Block block : getGrid()) {
				for(int i = 0; i < 5; i++){
					if(checkImage(block, i*4)){
						log.fine("removing blocks 4*4!");
						score++;
						for(int s = -1; s < 3; s++){
							for(int t = -1; t < 3; t++){
								remove(block.x+s, block.y+t);
							}							
						}
						delayedGravity();
					}
				}
			}
		}
		return moved;
	}
	
	public void delayedGravity(){
		while(gravity()){
			listener.repaint();
			Util.sleep(delay/3);
		}
	}

	private boolean checkImage(Block block, int image){
		if(block.image == image){
			Block b = get(block.x+1, block.y);
			if(b != null && b.image == image+1){
				b = get(block.x, block.y+1);
				if(b != null && b.image == image+2){
					b = get(block.x+1, block.y+1);
					if(b != null && b.image == image+3){
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isGameOver(){
		return gameOver;
	}

	public void rotate(){
		bg.rotate();
	}

	public synchronized List<Block> getGrid(){
		List<Block> copy = new ArrayList<Block>();
		for (Block block : grid) {
			copy.add(block);
		}
		return copy;
	}
	
	public synchronized List<BlockGroup> getQueue(){
		List<BlockGroup> copy = new ArrayList<BlockGroup>();
		for (BlockGroup blockGroup : queue) {
			copy.add(blockGroup);
		}
		return copy;
	}

	public long getDelay() {
		return delay;
	}

	public long getScore() {
		return score;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}
	
}
