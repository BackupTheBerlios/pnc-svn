package com.mathias.games.dogfight.server.dao;

import java.util.HashMap;
import java.util.Map;

public class UserDao {

	private static Map<String, String> users = new HashMap<String, String>();
	
	static{
		users.put("p1", "p1");
		users.put("p2", "p2");
		users.put("p3", "p3");
		users.put("p4", "p4");
		users.put("p5", "p5");
		users.put("p6", "p6");
		users.put("p7", "p7");
		users.put("p8", "p8");
		users.put("p9", "p9");
	}

	public static boolean authenticated(String username, String password){
		String pwd = users.get(username);
		return pwd != null && pwd.equals(password);
	}

	public static boolean exists(String username){
		return users.containsKey(username);
	}

}
