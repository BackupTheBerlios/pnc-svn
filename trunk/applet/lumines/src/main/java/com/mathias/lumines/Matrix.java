package com.mathias.lumines;

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
	private int smooth = 0;
	private int removeCountdown = 0;

	private List<Block> grid = Collections.synchronizedList(new ArrayList<Block>());
	private Queue<BlockGroup> queue = new ArrayBlockingQueue<BlockGroup>(3);

	private BlockGroup bg;

	public Matrix(int cols, int rows){
		this.cols = cols;
		this.rows = rows;

		addWalls();

		for(int i = 0; i < 3; i++){
			queue.add(new BlockGroup(cols/2, 1));
		}
		
		addBlockGroup();
	}
	
	private void addWalls(){
		for(int i = 0; i < rows; i++){
			//first col
			grid.add(new Block(0, i, Images.WALL.ordinal()));
			//last col
			grid.add(new Block(cols-1, i, Images.WALL.ordinal()));
		}
		for(int i = 0; i < cols; i++){
			//bottom row
			grid.add(new Block(i, rows-1, Images.WALL.ordinal()));
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

	private void addBlockGroup(){
		log.fine("addBlockGroup!");

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
	public void gravity(){
		if(false && smooth++ == 4){
			smooth = 0;
		}else{
			//return;
		}

		for (Block block : getGrid()) {
			if(get(block.x, block.y+1) == null && block.y < rows-1){
				for(int i = block.y-1; i >= 0; i--){
					Block b = get(block.x, i);
					if(b == null){
						break;
					}
					b.falling = true;
					b.y++;
				}
				block.falling = true;
				block.y++;
			}else if(!bg.inside(block)){
				block.falling = false;
			}
		}
		if(get(bg.bl.x, bg.bl.y+1) != null || get(bg.br.x, bg.br.y+1) != null){
			addBlockGroup();
		}
		for (Block block : getGrid()) {
			if(markRemove(block)){
				if(removeCountdown == 0){
					removeCountdown = 8;
				}
			}
		}
		if(removeCountdown == 0){
			double counter = 0;
			for (Block block : getGrid()) {
				if(block.remove){
					remove(block.x, block.y);
					counter++;
				}
			}
			if(counter > 0){
				score += Double.valueOf(counter*25*(counter/4)).intValue();
			}
		}else{
			removeCountdown--;
		}
	}

	private boolean markRemove(Block block){
		if(!block.falling && !bg.inside(block)){
			Block b = get(block.x+1, block.y);
			if(b != null && b.image == block.image && !b.falling && !bg.inside(b)){
				b = get(block.x, block.y+1);
				if(b != null && b.image == block.image && !b.falling && !bg.inside(b)){
					b = get(block.x+1, block.y+1);
					if(b != null && b.image == block.image && !b.falling && !bg.inside(b)){
						log.fine("removing block!");
						boolean alreadyRemove = true;
						for(int s = 0; s < 2; s++){
							for(int t = 0; t < 2; t++){
								b = get(block.x+s, block.y+t);
								if(!b.remove){
									alreadyRemove = false;
								}
								b.remove = true;
							}							
						}
						return !alreadyRemove;
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

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public long getScore() {
		return score;
	}

	public int getSmooth(){
		return smooth;
	}

}
