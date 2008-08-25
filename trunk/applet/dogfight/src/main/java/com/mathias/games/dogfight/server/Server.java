package com.mathias.games.dogfight.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mathias.games.dogfight.common.UdpNetworkListener;
import com.mathias.games.dogfight.common.WorldEngine;
import com.mathias.games.dogfight.common.command.AbstractCommand;
import com.mathias.games.dogfight.common.command.LoginCommand;
import com.mathias.games.dogfight.common.command.LogoutCommand;
import com.mathias.games.dogfight.common.command.UpdateCommand;
import com.mathias.games.dogfight.common.items.AbstractItem;
import com.mathias.games.dogfight.server.dao.UserDao;

public class Server implements UdpNetworkListener {

	private static final Logger log = LoggerFactory.getLogger(Server.class);
	
	private WorldEngine engine;
	
	private Map<String, SocketAddress> connections = new HashMap<String, SocketAddress>();
	
	private UdpNetworkServer networkThread;
	
	public Server() {
		engine = new WorldEngine();

		networkThread = new UdpNetworkServer(this);
		
		log.debug("Dogfight UDP server started!");
	}

	private void sendAllCommand(AbstractCommand cmd, SocketAddress exclude) throws IOException{
		for (SocketAddress addr : connections.values()) {
			if(exclude == null || !exclude.equals(addr)){
				networkThread.sendCommand(cmd, addr, false);
			}
		}
	}

	public void receiveCommand(AbstractCommand cmd, SocketAddress addr) throws IOException{
		log.debug("Received command: "+cmd+" from "+addr);
		if(cmd instanceof LoginCommand){
			LoginCommand lgn = (LoginCommand) cmd;
			lgn.authenticated = connections.get(lgn.getUsername()) == null
					&& UserDao.authenticated(lgn.getUsername(), lgn
							.getPassword());
//			lgn.authenticated = UserDao.authenticated(lgn.getUsername(), lgn.getPassword());
			if(lgn.authenticated){
				connections.put(lgn.getUsername(), addr);
			}
			networkThread.sendCommand(lgn, addr, false);
		}else if(cmd instanceof LogoutCommand){
			LogoutCommand lgo = (LogoutCommand) cmd;
			if (UserDao.authenticated(lgo.getUsername(), lgo.getPassword())) {
				engine.remove(lgo.getUsername());
				networkThread.sendCommand(lgo, addr, false);
				connections.remove(lgo.getUsername());
			}
		}else if(cmd instanceof UpdateCommand){
			UpdateCommand upd = (UpdateCommand) cmd;
			log.debug("Update command received. Items: "+upd.items.length);
			for (AbstractItem item : upd.items) {
				item.dirty = true;
				engine.update(item);
			}
//			List<AbstractItem> items = new ArrayList<AbstractItem>();
//			for (AbstractItem p : objects.getItems()) {
//				if(p.dirty){
//					p.dirty = false;
//					items.add(p);
//				}
//			}
			upd = new UpdateCommand(engine.getItems().toArray(new AbstractItem[0]));//items.toArray(new AbstractItem[0]));
			sendAllCommand(upd, null /*addr*/);
		}else{
			log.warn("Unknown command");
		}
	}

	public static void main(String[] args) {
		new Server();
	}

}
