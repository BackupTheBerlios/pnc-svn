package com.mathias.games.dogfight.server.dao;

import java.util.HashMap;
import java.util.Map;

public class UserDao {

	private static Map<String, String> users = new HashMap<String, String>();
	
	static{
		users.put("player1", "player1");
		users.put("player2", "player2");
		users.put("player3", "player3");
		users.put("player4", "player4");
		users.put("player5", "player5");
		users.put("player6", "player6");
	}

	public static boolean authenticated(String username, String password){
		String pwd = users.get(username);
		return pwd != null && pwd.equals(password);
	}

	public static boolean exists(String username){
		return users.containsKey(username);
	}

}
