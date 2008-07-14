package com.mathias.games.dogfight;


public class AbstractItemFactory {

	public static AbstractItem deserialize(String plystr){
		String[] split2 = plystr.split(",");
		if(plystr.charAt(0) == Plane.TYPE){
			return new Plane().deserialize(split2);
		}else if(plystr.charAt(0) == Explosion.TYPE){
			return new Explosion().deserialize(split2);
		}else if(plystr.charAt(0) == Bullet.TYPE){
			return new Bullet().deserialize(split2);
		}else{
//			Util.LOG("Unknown object: "+plystr.charAt(0));
		}
		return null;
	}

}
