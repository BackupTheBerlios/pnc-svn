package com.mathias.games.dogfight.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mathias.drawutils.Util;
import com.mathias.games.dogfight.AbstractItem;
import com.mathias.games.dogfight.common.Constants;
import com.mathias.games.dogfight.common.WorldEngine;
import com.mathias.games.dogfight.common.command.AbstractCommand;
import com.mathias.games.dogfight.common.command.LoginCommand;
import com.mathias.games.dogfight.common.command.UpdateCommand;

public class NetworkThread extends TimerTask {

	private static final Logger log = LoggerFactory.getLogger(NetworkThread.class);
	
	private DatagramSocket socket;
	
	public WorldEngine engine;

	private Map<String, SocketAddress> connections = new HashMap<String, SocketAddress>();
	
	public NetworkThread(WorldEngine objects){
		this.engine = objects;

		try {
			socket = new DatagramSocket(Constants.PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		new Timer(true).schedule(this, Constants.DELAY);
	}

	@Override
	public void run() {
		try {
			while(true){
				byte[] buf = new byte[Constants.MAX_PACKET_SIZE];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				
				SocketAddress addr = packet.getSocketAddress();

//				Util.LOG("incoming to server: "+res);
				Object obj = Util.deserialize(packet.getData());
				if(obj == null){
					log.error("ERROR for "+obj);
				}else if(obj instanceof AbstractCommand){
					receiveCommand((AbstractCommand)obj, addr);
				}else{
					log.error("Unknown object: "+obj);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void sendAllCommand(AbstractCommand cmd, SocketAddress exclude) throws IOException{
		for (SocketAddress addr : connections.values()) {
			if(exclude == null || !exclude.equals(addr)){
				sendCommand(cmd, addr);
			}
		}
	}

	private void sendCommand(AbstractCommand cmd, SocketAddress addr) throws IOException {
		log.debug("Sending command: "+cmd);
		byte[] buf = Util.serialize(cmd);
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		packet.setSocketAddress(addr);
		socket.send(packet);
	}

	private void receiveCommand(AbstractCommand cmd, SocketAddress addr) throws IOException{
		log.debug("Received command: "+cmd+" from "+addr);
		if(cmd instanceof LoginCommand){
			LoginCommand lgn = (LoginCommand) cmd;
			lgn.authenticated = connections.get(lgn.getUsername()) == null;// && UserDao.exists(lgn.getUsername());
//			lgn.authenticated = UserDao.authenticated(lgn.getUsername(), lgn.getPassword());
			if(lgn.authenticated){
				connections.put(lgn.getUsername(), addr);
			}
			sendCommand(lgn, addr);
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

}
