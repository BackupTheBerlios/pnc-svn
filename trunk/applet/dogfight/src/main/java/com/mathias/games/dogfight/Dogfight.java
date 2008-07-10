package com.mathias.games.dogfight;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.List;

import com.mathias.drawutils.applet.MediaApplet;
import com.mathias.games.dogfight.client.Client;
import com.mathias.games.dogfight.client.ClientImpl;
import com.mathias.games.dogfight.server.Server;

@SuppressWarnings("serial")
public class Dogfight extends MediaApplet {

	private Player player = new Player("player1", 0, 10, 10);
	private int speed = 10;
	private static final double angleadd = 0.1;
	
	private Client client;
	private List<Player> opponents = null;

	int px;
	int py;

	@Override
	public void init() {
		client = new ClientImpl();
		try {
			client.connect("localhost", Server.PORT);
		} catch (IOException e) {
			throw new RuntimeException("Could not connect! "+e.getMessage());
		}
		
		addImage(0, "images/plane.gif", true);
		addImage(1, "images/clouds.jpg", true);

		Image p = getImage(0);
		px = p.getWidth(this)/2;
		py = p.getHeight(this)/2;

		super.init();
	}
	
	@Override
	protected void animate() {

		player.x += Math.cos(player.angle)*speed;
		player.y += Math.sin(player.angle)*speed;
		
		if(player.x > getWidth()){
			player.x = 0;
		}
		if(player.x < 0){
			player.x = getWidth();
		}
		if(player.y > getHeight()){
			player.y = 0;
		}
		if(player.y < 0){
			player.y = getHeight();
		}
		
		try {
			opponents = client.update(player);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Dimension getDimension() {
		return new Dimension(500, 500);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(KeyEvent.VK_LEFT == e.getKeyCode()){
			player.angle-=angleadd;
		}else if(KeyEvent.VK_RIGHT == e.getKeyCode()){
			player.angle+=angleadd;
		}else if(KeyEvent.VK_UP == e.getKeyCode()){
		}else if(KeyEvent.VK_DOWN == e.getKeyCode()){
		}
		e.consume();
	}

	@Override
	protected void paintAnimation(Graphics2D g) {
		g.drawImage(getImage(1), 0, 0, null);

		AffineTransform af = new AffineTransform();
		af.setToTranslation(player.x, player.y);
		af.rotate(player.angle, px, py);
		g.drawImage(getImage(0), af, null);

		if(opponents != null){
			System.out.println("opponents: "+opponents.size());
			for (Player ply : opponents) {
				af.setToTranslation(ply.x, ply.y);
				af.rotate(ply.angle, px, py);
				g.drawImage(getImage(0), af, null);
			}
		}
	}

	@Override
	public long delay() {
		return 200;
	}

}
