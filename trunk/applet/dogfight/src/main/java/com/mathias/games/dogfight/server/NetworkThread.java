package com.mathias.games.dogfight.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.mathias.drawutils.Util;
import com.mathias.games.dogfight.AbstractItem;
import com.mathias.games.dogfight.common.Constants;

public class NetworkThread extends TimerTask {

	private DatagramSocket socket;
	
	public Map<String, AbstractItem> objects = new HashMap<String, AbstractItem>();
	
	private List<SocketAddress> connections = new ArrayList<SocketAddress>();
	
	public NetworkThread(Map<String, AbstractItem> objects){
		this.objects = objects;

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
				if(!connections.contains(addr)){
					connections.add(addr);
				}

//				Util.LOG("incoming to server: "+res);
				Object obj = Util.deserialize(packet.getData());
				if(obj instanceof AbstractItem){
					AbstractItem item = (AbstractItem)obj;
					if(item == null){
						Util.LOG("ERROR for "+new String(packet.getData()));
						break;
					}
					item.dirty = true;
					AbstractItem dest = objects.get(item.key);
					if(dest != null){
						AbstractItem.update(item, dest);
					}else{
						objects.put(item.key, item);
					}
					List<AbstractItem> items = new ArrayList<AbstractItem>();
					for (AbstractItem p : objects.values()) {
						if(p.dirty){
							p.dirty = false;
							items.add(p);
						}
					}
					sendRawAll(Util.serialize(items));
				}else{
					Util.LOG("Unknown object: "+obj);
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

	private void sendRawAll(byte[] buf) throws IOException{
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		for (SocketAddress address : connections) {
			packet.setSocketAddress(address);
			socket.send(packet);
		}
	}

	private void sendRaw(SocketAddress address, byte[] buf) throws IOException{
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		packet.setSocketAddress(address);
		socket.send(packet);
	}

}
