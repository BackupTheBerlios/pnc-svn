package com.mathias.games.dogfight;

public class Player {

	public String name;
	public double angle;
	public int x;
	public int y;

	public Player(String name, double angle, int x, int y) {
		this.name = name;
		this.angle = angle;
		this.x = x;
		this.y = y;
	}

	public String serialize() {
		return name+","+angle+","+x+","+y;
	}

	public static Player deserialize(String data){
		String[] split = data.split(",");
		String name = split[0];
		double angle = Double.parseDouble(split[1]);
		int x = Integer.parseInt(split[2]);
		int y = Integer.parseInt(split[3]);
		return new Player(name, angle, x, y);
	}
}
