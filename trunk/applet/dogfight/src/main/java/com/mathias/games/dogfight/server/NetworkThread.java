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

import com.mathias.games.dogfight.AbstractItem;
import com.mathias.games.dogfight.AbstractItemFactory;
import com.mathias.games.dogfight.common.Constants;
import com.mathias.games.dogfight.common.Util;

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
				packet.setLength(buf.length);
				socket.receive(packet);
				
				SocketAddress addr = packet.getSocketAddress();
				if(!connections.contains(addr)){
					connections.add(addr);
				}
				
				String res = new String(packet.getData(), 0, packet.getLength());
//				Util.LOG("incoming to server: "+res);
				if(res != null){
					AbstractItem item = AbstractItemFactory.deserialize(res);
					if(item == null){
						Util.LOG("ERROR for "+res);
						break;
					}
					item.dirty = true;
					AbstractItem dest = objects.get(item.key);
					if(dest != null){
						AbstractItem.update(item, dest);
					}else{
						objects.put(item.key, item);
					}
				}
				StringBuffer sb = new StringBuffer();
				for (AbstractItem p : objects.values()) {
					if(p.dirty){
						sb.append(p.serialize()+"\n");
						p.dirty = false;
					}
				}
				buf = sb.toString().getBytes();
				for (SocketAddress addr2 : connections) {
//					Util.LOG("kasjd: "+sb.toString()+" "+addr2);
					packet = new DatagramPacket(buf, buf.length, addr2);
					packet.setLength(buf.length);
					socket.send(packet);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}

}
