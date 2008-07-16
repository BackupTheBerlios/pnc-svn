package com.mathias.games.dogfight.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import com.mathias.drawutils.Util;
import com.mathias.games.dogfight.AbstractItem;
import com.mathias.games.dogfight.Plane;
import com.mathias.games.dogfight.AbstractItem.Action;
import com.mathias.games.dogfight.common.Constants;
import com.mathias.games.dogfight.common.TimeoutMap;
import com.mathias.games.dogfight.common.TimeoutMapListener;
import com.mathias.games.dogfight.common.command.AbstractCommand;
import com.mathias.games.dogfight.common.command.LoginCommand;

public class UdpClient extends Thread implements TimeoutMapListener<Integer, AbstractCommand> {

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
	public void login(Plane player) throws IOException {
		//username, password
		AbstractCommand cmd = new LoginCommand();
		notifications.put(cmd.sequence, cmd);
		sendRaw(Util.serialize(cmd));
	}
	
	public void update(AbstractItem item) throws IOException {
		sendRaw(Util.serialize(item));
	}

	private void sendRaw(byte[] buf) throws IOException {
		DatagramPacket packet = new DatagramPacket(buf, buf.length,
				address, Constants.PORT);
		if(socket != null && initialized){
			socket.send(packet);
		}
	}
	
	private void gotUpdate(List<AbstractItem> items){
		for (AbstractItem item : items) {
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
	}
	
	private void receiveRaw() throws ClassNotFoundException{
		try{
			byte[] buf = new byte[Constants.MAX_PACKET_SIZE];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);
			buf = packet.getData();
			Object obj = Util.deserialize(buf);
			if(obj instanceof List){
				List<AbstractItem> items = (List<AbstractItem>)obj;
				gotUpdate(items);
			}else{
				Util.LOG("Unknown object received: "+new String(buf));
			}
		}catch(IOException e){
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

	@Override
	public void run() {
		try {
			while(true){
				receiveRaw();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void handleTimeout(Integer key, AbstractCommand value) {
		//resend
		// key = cmd.id, value = cmd
	}

}
