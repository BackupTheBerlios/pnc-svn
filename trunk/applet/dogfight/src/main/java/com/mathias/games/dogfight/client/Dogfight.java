package com.mathias.games.dogfight.client;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mathias.drawutils.DrawUtil;
import com.mathias.drawutils.GenericDialog;
import com.mathias.drawutils.MathUtil;
import com.mathias.drawutils.RotateImage;
import com.mathias.drawutils.applet.MediaApplet;
import com.mathias.games.dogfight.AbstractItem;
import com.mathias.games.dogfight.Bullet;
import com.mathias.games.dogfight.Explosion;
import com.mathias.games.dogfight.Plane;
import com.mathias.games.dogfight.AbstractItem.Action;
import com.mathias.games.dogfight.common.Constants;
import com.mathias.games.dogfight.common.WorldEngine;

@SuppressWarnings("serial")
public class Dogfight extends MediaApplet implements MouseListener {
	
	private static final Logger log = LoggerFactory.getLogger(Dogfight.class);
	
	private enum Images{
		Background,
		Plane,
		PlaneMask,
		RedPlane,
		BluePlane,
		RedExplosion,
		Explosion;
	}

	private static final double angleadd = 0.1;
	
	private Plane player;
	
	private WorldEngine engine = new WorldEngine();

	private Map<Integer, RotateImage> planes = new HashMap<Integer, RotateImage>();
	
	private UdpClient client;
	
	private boolean connected = false;
	
	private Random rand;

	@Override
	public void init() {
		
		rand = new Random();
		
		addMouseListener(this);
		
		addImage(Images.Background, "images/clouds.jpg", true);
		addImage(Images.RedPlane, "images/plane.gif", true);
		addImage(Images.BluePlane, "images/plane_blue.gif", true);
		addImage(Images.Explosion, "images/explosion.gif", true);
		addImage(Images.RedExplosion, "images/redexplosion.gif", true);
		addImage(Images.Plane, "images/plane.jpg", true);
		addImage(Images.PlaneMask, "images/plane_mask.jpg", true);

		planes.put(Images.BluePlane.ordinal(), new RotateImage(this, DrawUtil
				.composite(this, getImage(Images.Plane),
						getImage(Images.PlaneMask))));
		planes.put(Images.RedPlane.ordinal(), new RotateImage(this,
				getImage(Images.RedPlane)));

		// networking
		client = new UdpClient(engine);

		LoginDialog dlg = new LoginDialog();
		if (!dlg.isCancelled()) {
			try {
				player = createPlane(dlg.getUsername());
				engine.add(player);

				if(dlg.getUsername().equals("x")){
					player.action = Action.ONGOING;
				}else{
					client.login(dlg.getUsername(), dlg.getPassword());
					connected = true;
				}
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

	private Plane createPlane(String username){
		int planeindex = Images.BluePlane.ordinal();
		int w = getImage(planeindex).getWidth(this);
		int h = getImage(planeindex).getHeight(this);
		return new Plane(username, rand.nextInt(628) / 100,
				rand.nextInt(Constants.WIDTH), rand
						.nextInt(Constants.HEIGHT), w, h, 5, planeindex);
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
				+ (int) (MathUtil.cos(player.angle) * player.w), player.y
				+ (int) (MathUtil.sin(player.angle) * player.h), 10);
		try {
			client.update(bullet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		engine.add(bullet);
	}

	@Override
	protected void paintAnimation(Graphics2D g) {
		g.drawImage(getImage(Images.Background.ordinal()), 0, 0, null);

		for (AbstractItem item : engine.getItems()) {
			if(item.action == Action.REMOVED){
				continue;
			}else if(item instanceof Plane){
				Image img = planes.get(((Plane)item).planeindex).getImage(item.angle);
				g.drawImage(img, item.x-(img.getWidth(null)/2),
						item.y-(img.getHeight(null)/2), null);
			}else if(item instanceof Bullet){
				g.drawString("x", item.x, item.y);
			}else if(item instanceof Explosion){
				Image img = getImage(Images.Explosion.ordinal());
				g.drawImage(img, item.x-(img.getWidth(null)/2), item.y-(img.getHeight(null)/2),
						null);
			}else{
				LOG("ERROR not a valid object: "+item);
			}
//			if(item instanceof SolidItem){
//				g.setColor(Color.black);
//				g.drawPolygon(item.getPolygon());
//			}
		}
		
		if(player != null && player.action == Action.REMOVED){
			g.drawImage(getImage(Images.RedExplosion), 300, 300, null);
		}
	}

	@Override
	public long delay() {
		return 50;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (player.action == Action.REMOVED && e.getX() >= 300
				&& e.getX() <= 400 && e.getY() >= 300 && e.getY() <= 400) {
			log.debug("restart button pressed!");
			player = createPlane(player.key);
			player.action = Action.ONGOING;
			engine.add(player);
			if(connected){
				try {
					client.update(player);
				} catch (IOException e1) {
					log.warn("Could not update player: "+player);
				}
			}
		}
	}

	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
	@Override public void mousePressed(MouseEvent e) {}
	@Override public void mouseReleased(MouseEvent e) {}

}
