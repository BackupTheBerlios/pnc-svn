package com.mathias.bellatetris;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Shape {

	//private static final Logger log = Logger.getLogger(Tetris.class.getName());

	public enum Direction {
		CLOCKWISE,
		COUNTERCLOCKWISE
	}

	protected List<Block> blocks;
	public int x;
	public int y;

	public Shape(int x, int y, Block[] points) {
		this.x = x;
		this.y = y;
		this.blocks = Arrays.asList(points);
	}

	public Shape(int x, int y, int width, int height, Point[] points, Image image) {
		this(x, y);
		blocks = new ArrayList<Block>();
		for (Point p : points) {
			blocks.add(new Block(p.x, p.y, width, height, image));
		}
	}

	public Shape(int x, int y, int width, int height, Point[] points, Color color) {
		this(x, y);
		blocks = new ArrayList<Block>();
		for (Point p : points) {
			blocks.add(new Block(p.x, p.y, width, height, color));
		}
	}
	
	private Shape(int x, int y){
		this.x = x;
		this.y = y;
	}

	public void paint(Graphics2D g, int nx, int ny){
		for (Block p : blocks) {
			p.paint(g, nx, ny);
		}
	}

	public void paint(Graphics2D g){
		paint(g, x, y);
	}

	protected void transpose(Direction dir){
		for (Block p : blocks) {
			if(Direction.CLOCKWISE.equals(dir)){
				int t = p.y;
				p.y = p.x;
				p.x = -1*t;
			}else{ //COUNTERCLOCKWISE
				int t = p.x;
				p.x = p.y;
				p.y = -1*t;
			}
		}
	}

	public List<Block> getPoints() {
		return Collections.unmodifiableList(blocks);
	}

	public Shape clone(){
		Block[] p = new Block[blocks.size()];
		for (int i = 0; i < blocks.size(); i++) {
			p[i] = blocks.get(i).clone();
		}
		return new Shape(x, y, p);
	}

	public boolean inside(List<Block> s){
		for (Block sp : blocks) {
			for (Block p : s) {
				if((p.x == sp.x+x) && (p.y == sp.y+y)){
					return true;
				}
			}
		}
		return false;
	}

}
