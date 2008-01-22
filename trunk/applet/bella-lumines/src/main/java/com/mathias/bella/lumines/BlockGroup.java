package com.mathias.bella.lumines;

public class BlockGroup {
	
	public Block tl;
	public Block tr;
	public Block bl;
	public Block br;
	private boolean active = false;

	public BlockGroup(int cstart, int rstart){
		tl = new Block(cstart, rstart);
		tr = new Block(cstart+1, rstart);
		bl = new Block(cstart, rstart+1);
		br = new Block(cstart+1, rstart+1);
		active = true;
	}
	
	public void rotate(){
		Block temp = new Block(0,0,0);
		swapImage(temp, tl);
		swapImage(tl, bl);
		swapImage(bl, br);
		swapImage(br, tr);
		swapImage(tr, temp);
	}
	
	private static void swapImage(Block b1, Block b2){
		int image = b1.image;
		b1.image = b2.image;
		b2.image = image;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void left(){
		tl.x--;
		tr.x--;
		bl.x--;
		br.x--;
	}

	public void right(){
		tl.x++;
		tr.x++;
		bl.x++;
		br.x++;
	}

	public void up(){
		tl.y--;
		tr.y--;
		bl.y--;
		br.y--;
	}

	public void down(){
		tl.y++;
		tr.y++;
		bl.y++;
		br.y++;
	}

}
