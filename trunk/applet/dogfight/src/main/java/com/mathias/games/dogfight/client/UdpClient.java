package com.mathias.games.dogfight.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mathias.drawutils.GenericDialog;
import com.mathias.drawutils.Util;
import com.mathias.games.dogfight.AbstractItem;
import com.mathias.games.dogfight.AbstractItem.Action;
import com.mathias.games.dogfight.common.Constants;
import com.mathias.games.dogfight.common.TimeoutMap;
import com.mathias.games.dogfight.common.TimeoutMapListener;
import com.mathias.games.dogfight.common.command.AbstractCommand;
import com.mathias.games.dogfight.common.command.LoginCommand;
import com.mathias.games.dogfight.common.command.MessageCommand;
import com.mathias.games.dogfight.common.command.StateCommand;
import com.mathias.games.dogfight.common.command.UpdateCommand;

public class UdpClient extends Thread implements TimeoutMapListener<Integer, AbstractCommand> {

	private static final Logger log = LoggerFactory.getLogger(UdpClient.class);
	
	private DatagramSocket socket;

	private InetAddress address;

	private Map<String, AbstractItem> objects;

	private TimeoutMap<Integer, AbstractCommand> notifications;
	
	private boolean initialized = false;
	
	public UdpClient(Map<String, AbstractItem> objects) {
		this.objects = objects;

		notifications = new TimeoutMap<Integer, AbstractCommand>(500, this);

		start();
	}
	
	/**
	 * Login
	 * State change/notification
	 * @param player
	 * @throws IOException
	 */
	public void login(String username, String password) throws IOException {
		sendCommand(new LoginCommand(username, password));
	}
	
	public void update(AbstractItem item) throws IOException {
		UpdateCommand upd = new UpdateCommand(item);
		sendCommand(upd);
	}
	
	private void sendCommand(AbstractCommand cmd) throws IOException{
		if(cmd instanceof StateCommand){
			notifications.put(cmd.sequence, cmd);
		}
		byte[] buf = Util.serialize(cmd);
		DatagramPacket packet = new DatagramPacket(buf, buf.length,
				address, Constants.PORT);
		if(socket != null && initialized){
			socket.send(packet);
		}
	}

	private void receiveCommand(AbstractCommand cmd) {
		if(cmd instanceof StateCommand){
			notifications.remove(cmd.sequence);
		}
		if(cmd instanceof LoginCommand){
			LoginCommand lgn = (LoginCommand) cmd;
			if(!lgn.authenticated){
				GenericDialog.showErrorDialog("Login",
						"Could not login with user " + lgn.getUsername());
			}else{
//				engine.players.put(player.key, player);
			}
		}else if(cmd instanceof UpdateCommand){
			UpdateCommand upd = (UpdateCommand) cmd;
			for (AbstractItem item : upd.items) {
				if(item != null){
					if(item.action == Action.REMOVED){
						objects.remove(item.key);
					}else{
						AbstractItem dest = objects.get(item.key);
						if(dest != null){
							AbstractItem.update(item, dest);
						}else{
							objects.put(item.key, item);
						}
					}
				}
			}
		}else if(cmd instanceof MessageCommand){
			MessageCommand msg = (MessageCommand) cmd;
			GenericDialog.showInfoDialog("Message", msg.msg);
		}else{
			log.warn("Unknown command");
		}
	}

	@Override
	public void run() {
		try {
			byte[] buf = new byte[Constants.MAX_PACKET_SIZE];
			while(true){
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				Object obj = Util.deserialize(packet.getData());
				if(obj == null){
					log.error("ERROR for "+obj);
				}else if(obj instanceof AbstractCommand){
					receiveCommand((AbstractCommand)obj);
				}else{
					log.error("Unknown object: "+obj);
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void start() {
		try {
			address = InetAddress.getByName("localhost");
			socket = new DatagramSocket();
			initialized = true;
			super.start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void handleTimeout(Integer key, AbstractCommand cmd) {
		try {
			sendCommand(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
