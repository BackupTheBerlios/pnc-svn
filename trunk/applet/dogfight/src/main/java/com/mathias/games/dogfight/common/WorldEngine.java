package com.mathias.games.dogfight.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.mathias.drawutils.MathUtil;
import com.mathias.games.dogfight.AbstractItem;
import com.mathias.games.dogfight.Explosion;
import com.mathias.games.dogfight.SolidItem;
import com.mathias.games.dogfight.TtlItem;
import com.mathias.games.dogfight.AbstractItem.Action;

public class WorldEngine extends TimerTask {

	public Map<String, AbstractItem> players = new HashMap<String, AbstractItem>();

	public WorldEngine() {
		new Timer(false).schedule(this, 0, Constants.DELAY);
	}

	@Override
	public void run() {
		// new location
		synchronized (players) {
			for (Iterator<AbstractItem> it = players.values().iterator(); it.hasNext();) {
				AbstractItem item = it.next();
				if(item.action != Action.ONGOING){
					continue;
				}
				item.x += MathUtil.cos(item.angle) * item.speed;
				item.y += MathUtil.sin(item.angle) * item.speed;
	
				if (item.x > Constants.WIDTH) {
					item.x = 0;
				}
				if (item.x < 0) {
					item.x = Constants.WIDTH;
				}
				if (item.y > Constants.HEIGHT) {
					item.y = 0;
				}
				if (item.y < 0) {
					item.y = Constants.HEIGHT;
				}
				if(item instanceof TtlItem){
					TtlItem ttlItem = (TtlItem)item;
					if(ttlItem.decreaseTtl()){
						it.remove();
					}
				}
			}
		}

		// collision check
		synchronized (players) {
			List<String> rem = new ArrayList<String>();
			Map<String, Explosion> exp = new HashMap<String, Explosion>();
			for(Iterator<AbstractItem> it = players.values().iterator(); it.hasNext(); ){
				AbstractItem ply = it.next();
				if(ply instanceof SolidItem){
					AbstractItem sitem = ply.intersects(players.values());
					if(sitem != null){
						Explosion explosion = new Explosion(ply.x, ply.y, 20);
						exp.put(explosion.key, explosion);
						ply.action = Action.REMOVED;
//						it.remove();
						rem.add(sitem.key);
					}else if(rem.contains(ply.key)){
						ply.action = Action.REMOVED;
//						it.remove();
					}
				}
			}
			players.putAll(exp);
		}
	}

}
