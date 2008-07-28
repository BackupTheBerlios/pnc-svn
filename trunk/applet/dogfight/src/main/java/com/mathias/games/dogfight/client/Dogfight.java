package com.mathias.games.dogfight.client;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import com.mathias.drawutils.MultiImage;
import com.mathias.drawutils.RotateImage;
import com.mathias.drawutils.applet.MediaApplet;
import com.mathias.games.dogfight.common.Constants;
import com.mathias.games.dogfight.common.WorldEngine;
import com.mathias.games.dogfight.common.items.AbstractItem;
import com.mathias.games.dogfight.common.items.Bullet;
import com.mathias.games.dogfight.common.items.Explosion;
import com.mathias.games.dogfight.common.items.Plane;
import com.mathias.games.dogfight.common.items.TtlItem;
import com.mathias.games.dogfight.common.items.AbstractItem.Action;

@SuppressWarnings("serial")
public class Dogfight extends MediaApplet implements MouseListener, KeyListener {
	
	private static final Logger log = LoggerFactory.getLogger(Dogfight.class);
	
	private enum Images{
		Login,
		Background,
		Plane,
		PlaneMask,
		RedPlane,
		BluePlane,
		G5FighterPlane,
		H8Bomber,
		Gripen,
		Yak1,
		RedExplosion,
		GifExplosion,
		Explosion,
		ExplosionMask;
	}
	
	private enum Audio{
		Explosion,
		Fire;
	}

	private static final double angleadd = 0.1;
	
	private Plane player;
	
	private WorldEngine engine = new WorldEngine();

	private Map<Integer, RotateImage> planes = new HashMap<Integer, RotateImage>();
	
	private UdpClient client;
	
	private boolean connected = false;
	
	private Random rand;
	
	private Map<Integer, Boolean> keys = new HashMap<Integer, Boolean>();
	
	private boolean canFire = true;
	
	private String password = "";
	
	private MultiImage explosion;

	@Override
	public void init() {
		
		rand = new Random();
		
		keys.put(KeyEvent.VK_LEFT, false);
		keys.put(KeyEvent.VK_RIGHT, false);
		keys.put(KeyEvent.VK_DOWN, false);
		keys.put(KeyEvent.VK_UP, false);
		keys.put(KeyEvent.VK_SPACE, false);

		addMouseListener(this);
		addKeyListener(this);
		
		addImage(Images.Login, "images/login.jpg", true);
		addImage(Images.Background, "images/clouds.jpg", true);
		addImage(Images.RedPlane, "images/plane.gif", true);
		addImage(Images.BluePlane, "images/plane_blue.gif", true);
		addImage(Images.H8Bomber, "images/h8bomber.gif", true);
		addImage(Images.G5FighterPlane, "images/g5fighter.gif", true);
		addImage(Images.Gripen, "images/gripen.gif", true);
		addImage(Images.Yak1, "images/yak1.gif", true);
		addImage(Images.GifExplosion, "images/explosion.gif", true);
		addImage(Images.Explosion, "images/explosion.jpg", true);
		addImage(Images.ExplosionMask, "images/explosion_mask.jpg", true);
		addImage(Images.RedExplosion, "images/redexplosion.gif", true);
		addImage(Images.Plane, "images/plane.jpg", true);
		addImage(Images.PlaneMask, "images/plane_mask.jpg", true);
		
		addAudio(Audio.Explosion, "audio/explosion.au");
		addAudio(Audio.Fire, "audio/fire.au");

		planes.put(Images.BluePlane.ordinal(), new RotateImage(this, DrawUtil
				.composite(this, getImage(Images.Plane),
						getImage(Images.PlaneMask))));
		planes.put(Images.RedPlane.ordinal(), new RotateImage(this,
				getImage(Images.RedPlane)));
		planes.put(Images.G5FighterPlane.ordinal(), new RotateImage(this,
				getImage(Images.G5FighterPlane)));
		planes.put(Images.H8Bomber.ordinal(), new RotateImage(this,
				getImage(Images.H8Bomber)));
		planes.put(Images.Yak1.ordinal(), new RotateImage(this,
				getImage(Images.Yak1)));
		planes.put(Images.Gripen.ordinal(), new RotateImage(this,
				getImage(Images.Gripen)));
		
		
		Image e = DrawUtil.composite(this, getImage(Images.Explosion), getImage(Images.ExplosionMask));
		explosion = new MultiImage(this, e, 8, 1, 100, 100);

		// networking
		client = new UdpClient(engine);

		new Timer(true).schedule(new TimerTask(){
			@Override
			public void run() {
				updatePlayer();
			}
		}, 0, 1000);

		super.init();

		log.debug("DogFight intialized!");
	}
	
	private void login(){
		LoginDialog dlg = new LoginDialog();
		if (!dlg.isCancelled()) {
			try {
				int planeindex = -1;
				if("Blue".equals(dlg.getPlane())){
					planeindex = Images.BluePlane.ordinal();
				}else if("Red".equals(dlg.getPlane())){
					planeindex = Images.RedPlane.ordinal();
				}else if("G5Fighter".equals(dlg.getPlane())){
					planeindex = Images.G5FighterPlane.ordinal();
				}else if("H8Bomber".equals(dlg.getPlane())){
					planeindex = Images.H8Bomber.ordinal();
				}else if("Yak1".equals(dlg.getPlane())){
					planeindex = Images.Yak1.ordinal();
				}else if("Gripen".equals(dlg.getPlane())){
					planeindex = Images.Gripen.ordinal();
				}else{
					planeindex = Images.RedPlane.ordinal();
				}
				player = createPlane(dlg.getUsername(), planeindex);
				engine.add(player);

				if(dlg.getUsername().equals("x")){
					player.action = Action.ONGOING;
				}else{
					password = dlg.getPassword();
					client.login(dlg.getUsername(), password);
					connected = true;
				}
			} catch (IOException e) {
				GenericDialog.showErrorDialog("Login", "Could connect to server: "+e.getMessage());
			}
		}
	}

	private Plane createPlane(String username, int planeindex){
		int w = getImage(planeindex).getWidth(this);
		int h = getImage(planeindex).getHeight(this);
		return new Plane(username, rand.nextInt(628) / 100,
				rand.nextInt(Constants.WIDTH), rand
						.nextInt(Constants.HEIGHT), w, h, 5, planeindex);
	}
	
	private void updatePlayer(){
		try {
			if (connected && player != null && player.action == Action.ONGOING) {
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

	private void fire(){
		Bullet bullet = new Bullet(player.angle, player.x
				+ (int) (MathUtil.cos(player.angle) * player.w), player.y
				+ (int) (MathUtil.sin(player.angle) * player.h), 10);
		try {
			client.update(bullet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		getAudio(Audio.Fire).play();
		engine.add(bullet);
	}

	@Override
	protected void paintAnimation(Graphics2D g) {
		handleKeys();

		g.drawImage(getImage(Images.Background), 0, 0, null);

		for (AbstractItem item : engine.getItems()) {
			if(item.action != Action.ONGOING){
				continue;
			}else if(item instanceof Plane){
				Image img = planes.get(((Plane)item).planeindex).getImage(item.angle);
				g.drawImage(img, item.x-(img.getWidth(null)/2),
						item.y-(img.getHeight(null)/2), null);
			}else if(item instanceof Bullet){
				g.drawString("x", item.x, item.y);
			}else if(item instanceof Explosion){
				int counter = 20-((TtlItem)item).getTtl();
				if(counter == 0){
					getAudio(Audio.Explosion).play();
				}
				if(counter < 8){
					Image img = explosion.get(counter, 0) ;//getImage(Images.Explosion);
					g.drawImage(img, item.x-(img.getWidth(null)/2), item.y-(img.getHeight(null)/2),
							null);
				}
			}else{
				LOG("ERROR not a valid object: "+item);
			}
//			if(item instanceof SolidItem){
//				g.setColor(Color.black);
//				g.drawPolygon(item.getPolygon());
//			}
		}
		
		if(player == null || player.action == Action.REMOVED){
			g.drawImage(getImage(Images.Login), 300, 300, null);
		}
		if(player != null && player.action == Action.REMOVED){
//			try {
//				client.logout(player.key, password);
				engine.remove(player.key);
//			} catch (IOException e) {
//				log.warn("Could not connect to server: "+e.getMessage());
//			}
		}
	}

	@Override
	public long delay() {
		return 50;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (player == null || player.action == Action.REMOVED) {
			if (e.getX() >= 300
					&& e.getX() <= 300 + getImage(Images.Login).getWidth(this)
					&& e.getY() >= 300
					&& e.getY() <= 300 + getImage(Images.Login).getHeight(this)) {
				log.debug("login button pressed!");
				login();
			}
		}
	}

	public void handleKeys(){
		if(keys.get(KeyEvent.VK_LEFT)){
			player.angle-=angleadd;
		}else if(keys.get(KeyEvent.VK_RIGHT)){
			player.angle+=angleadd;
//		}else if(keys.get(KeyEvent.VK_UP)){
//		}else if(keys.get(KeyEvent.VK_DOWN)){
		}else if(keys.get(KeyEvent.VK_SPACE)){
			if(canFire){
				fire();
				canFire = false;
			}
		}
		updatePlayer();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		keys.put(e.getKeyCode(), true);
		e.consume();
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		keys.put(e.getKeyCode(), false);
		canFire = true;
	}

	@Override public void keyTyped(KeyEvent arg0) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
	@Override public void mousePressed(MouseEvent e) {}
	@Override public void mouseReleased(MouseEvent e) {}

}
