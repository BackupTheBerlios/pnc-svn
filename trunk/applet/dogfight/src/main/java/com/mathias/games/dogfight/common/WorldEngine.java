package com.mathias.games.dogfight.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mathias.drawutils.MathUtil;
import com.mathias.games.dogfight.common.items.AbstractItem;
import com.mathias.games.dogfight.common.items.Explosion;
import com.mathias.games.dogfight.common.items.SolidItem;
import com.mathias.games.dogfight.common.items.TtlItem;
import com.mathias.games.dogfight.common.items.AbstractItem.Action;

public class WorldEngine extends TimerTask {

	private static final Logger log = LoggerFactory.getLogger(WorldEngine.class);
	
	private Map<String, AbstractItem> items = new HashMap<String, AbstractItem>();

	public WorldEngine() {
		new Timer(false).schedule(this, 0, Constants.DELAY);
		log.debug("WorldEngine started!");
	}

	@Override
	public void run() {
		// new location
		synchronized (items) {
			for (Iterator<AbstractItem> it = items.values().iterator(); it.hasNext();) {
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
		synchronized (items) {
			Map<String, Explosion> exp = new HashMap<String, Explosion>();
			for(Iterator<AbstractItem> it = items.values().iterator(); it.hasNext(); ){
				AbstractItem ply = it.next();
				if(ply instanceof SolidItem){
					AbstractItem sitem = ply.intersects(items.values());
					if(sitem != null){
						Explosion explosion = new Explosion(ply.x, ply.y, 20);
						exp.put(explosion.key, explosion);
						ply.action = Action.REMOVED;
						sitem.action = Action.REMOVED;
					}
				}
			}
			items.putAll(exp);
		}
		
		// clean up
//		synchronized (items) {
//			for(Iterator<AbstractItem> it = items.values().iterator(); it.hasNext(); ){
//				AbstractItem item = it.next();
//				if(!(item instanceof TtlItem)){
//				}
//			}
//		}
	}

	public void add(AbstractItem item){
		synchronized (items) {
			items.put(item.key, item);
		}
	}

	public void remove(String item){
		synchronized (items) {
			items.remove(item);
		}
	}

	public void updateAction(String item, Action action){
		synchronized (items) {
			items.get(item).action = action;
		}
	}

	public void update(AbstractItem item){
		synchronized (items) {
			AbstractItem dest = items.get(item.key);
			if(dest != null){
				if(dest.action != Action.REMOVED){
					AbstractItem.update(item, dest);
				}
			}else{
				items.put(item.key, item);
			}
		}
	}

	public List<AbstractItem> getItems(){
		return new ArrayList<AbstractItem>(items.values());
	}

}
