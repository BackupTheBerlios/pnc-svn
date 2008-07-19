package com.mathias.games.dogfight;

import java.awt.Polygon;
import java.util.Collection;
import java.util.Iterator;

import com.mathias.games.dogfight.common.NetworkItem;

public abstract class AbstractItem implements NetworkItem {

	public enum Action {
		INITIALIZED,
		ONGOING,
		REMOVED;
	}

	public boolean dirty = false;
	public String key;
	public double angle;
	public int x;
	public int y;
	public int h;
	public int w;
	public int speed;
	public Action action = Action.INITIALIZED;

	public AbstractItem() {
	}

	public AbstractItem(String key, double angle, int x, int y, int w, int h,
			int speed) {
		this.key = key;
		this.angle = angle;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.speed = speed;
	}

	public static void update(AbstractItem src, AbstractItem dest) {
		dest.key = src.key;
		dest.angle = src.angle;
		dest.speed = src.speed;
		dest.x = src.x;
		dest.y = src.y;
		dest.w = src.w;
		dest.h = src.h;
		dest.action = src.action;
		dest.dirty = src.dirty;
	}

	public Polygon getPolygon(){
		Polygon p = new Polygon();
		p.addPoint(x-(w/2), y-(h/2));
		p.addPoint(x+(w/2), y-(h/2));
		p.addPoint(x+(w/2), y+(h/2));
		p.addPoint(x-(w/2), y+(h/2));
		return p;
	}

	public AbstractItem intersects(Collection<AbstractItem> items) {
		if(action == Action.REMOVED){
			return null;
		}else if(this instanceof SolidItem){
			for(Iterator<AbstractItem> it = items.iterator(); it.hasNext(); ){
				AbstractItem item = it.next();
				if(item.action != Action.REMOVED && this != item && item instanceof SolidItem){
					if(getPolygon().intersects(item.x, item.y, item.w, item.h)){
						return item;
					}
				}
			}
		}
		return null;
	}

}
