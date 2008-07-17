package com.mathias.games.dogfight;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mathias.drawutils.GenericDialog;
import com.mathias.drawutils.MathUtil;
import com.mathias.drawutils.applet.MediaApplet;
import com.mathias.games.dogfight.client.UdpClient;
import com.mathias.games.dogfight.common.Constants;
import com.mathias.games.dogfight.common.WorldEngine;

@SuppressWarnings("serial")
public class Dogfight extends MediaApplet {
	
	private static final Logger log = LoggerFactory.getLogger(Dogfight.class);
	
	private enum Images{
		Background,
		RedPlane,
		BluePlane,
		Explosion;
	}

	private static final double angleadd = 0.1;
	
	private Plane player;
	
	private WorldEngine engine = new WorldEngine();

	private Map<Integer, RotateImage> planes = new HashMap<Integer, RotateImage>();
	
	private UdpClient client;
	
	private boolean connected = false;

	@Override
	public void init() {
		
		addImage(Images.Background.ordinal(), "images/clouds.jpg", true);
		addImage(Images.RedPlane.ordinal(), "images/plane.gif", true);
		addImage(Images.BluePlane.ordinal(), "images/plane_blue.gif", true);
		addImage(Images.Explosion.ordinal(), "images/explosion.jpg", true);

		planes.put(Images.BluePlane.ordinal(), new RotateImage(getImage(Images.BluePlane.ordinal())));
		planes.put(Images.RedPlane.ordinal(), new RotateImage(getImage(Images.RedPlane.ordinal())));

		player = new Plane("player2", 0, 10, 10, 5, Images.BluePlane.ordinal());
		player.w = getImage(player.planeindex).getWidth(this);
		player.h = getImage(player.planeindex).getHeight(this);

		engine.players.put(player.key, player);

		// networking
		client = new UdpClient(engine.players);

		LoginDialog dlg = new LoginDialog();
		if (!dlg.isCancelled()) {
			try {
				client.login(dlg.getUsername(), dlg.getPassword());
				player.key = dlg.getUsername();
				connected = true;
			} catch (IOException e) {
				GenericDialog.showErrorDialog("Login", "Could connect to server: "+e.getMessage());
			}
		}

		new Timer(true).schedule(new TimerTask(){
			@Override
			public void run() {
				updatePlayer();
			}
		}, 0, 1000);

		super.init();

		log.debug("DogFight intialized!");
	}
	
	private void updatePlayer(){
		try {
			if(connected){
				client.update(player);
			}
		} catch (IOException e) {
			LOG(e.getMessage());
		}
	}
	
	@Override
	protected void animate() {
	}

	@Override
	public Dimension getDimension() {
		return new Dimension(Constants.WIDTH, Constants.HEIGHT);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(KeyEvent.VK_LEFT == e.getKeyCode()){
			player.angle-=angleadd;
		}else if(KeyEvent.VK_RIGHT == e.getKeyCode()){
			player.angle+=angleadd;
		}else if(KeyEvent.VK_UP == e.getKeyCode()){
		}else if(KeyEvent.VK_DOWN == e.getKeyCode()){
		}else if(KeyEvent.VK_SPACE == e.getKeyCode()){
			fire();
		}
		e.consume();

		updatePlayer();
	}
	
	private void fire(){
		Bullet bullet = new Bullet(player.angle, player.x
				+ (int) MathUtil.cos(player.angle) * 40, player.y
				+ (int) MathUtil.sin(player.angle) * 40, 10);
		try {
			client.update(bullet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		engine.players.put(bullet.key, bullet);
	}

	@Override
	protected void paintAnimation(Graphics2D g) {
		g.drawImage(getImage(Images.Background.ordinal()), 0, 0, null);

		synchronized (engine.players) {
			for (Iterator<AbstractItem> iterator = engine.players.values().iterator(); iterator.hasNext();) {
				AbstractItem ply = (AbstractItem) iterator.next();
				if(ply instanceof Plane){
					g.drawImage(planes.get(((Plane)ply).planeindex).getImage(ply.angle), ply.x,
							ply.y, null);
				}else if(ply instanceof Bullet){
					g.drawString("x", ply.x, ply.y);
				}else if(ply instanceof Explosion){
					g.drawImage(getImage(Images.Explosion.ordinal()), ply.x, ply.y,
							null);
				}else{
					LOG("ERROR not a valid object: "+ply);
				}
				if(ply instanceof SolidItem){
					g.setColor(Color.black);
					g.drawPolygon(ply.getPolygon());
				}
			}
		}
	}

	@Override
	public long delay() {
		return 50;
	}

}
