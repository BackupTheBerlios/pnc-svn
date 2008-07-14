package com.mathias.games.dogfight.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;

import com.mathias.games.dogfight.AbstractItem;
import com.mathias.games.dogfight.AbstractItemFactory;
import com.mathias.games.dogfight.AbstractItem.Action;
import com.mathias.games.dogfight.common.Constants;

public class UdpClient extends Thread {

	private DatagramSocket socket;

	private InetAddress address;

	private Map<String, AbstractItem> objects;
	
	private boolean initialized = false;

	public UdpClient(Map<String, AbstractItem> objects) {
		this.objects = objects;

		start();
	}

	public void update(AbstractItem player) throws IOException {
		byte[] buf = player.serialize().getBytes();
		DatagramPacket packet = new DatagramPacket(buf, buf.length,
				address, Constants.PORT);
		if(socket != null && initialized){
			socket.send(packet);
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
		while(true){
			try{
				byte[] buf = new byte[Constants.MAX_PACKET_SIZE];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);

				String received = new String(packet.getData());
				String[] split = received.split("\n");
				for (String plystr : split) {
					if(plystr != null && plystr.length() > 0){
						AbstractItem item = AbstractItemFactory.deserialize(plystr);
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
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}

}
