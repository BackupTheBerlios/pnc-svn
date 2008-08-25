package com.mathias.games.dogfight.common;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mathias.drawutils.Util;
import com.mathias.games.dogfight.client.UdpNetworkClient;
import com.mathias.games.dogfight.common.command.AbstractCommand;

public abstract class AbstractUdpNetwork {
	

	private static final Logger log = LoggerFactory.getLogger(UdpNetworkClient.class);
	
	private DatagramSocket socket;

	private TimeoutMap<Integer, CommandAddressPair> notifications;
	
	private boolean initialized = false;
	
	private InetAddress address;
	
	private int port;

	public AbstractUdpNetwork(String address, int port, UdpNetworkListener listener){
		this(listener);
		
		try {
			this.address = InetAddress.getByName(address);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}

		this.port = port;
	}

	public AbstractUdpNetwork(final UdpNetworkListener listener){

		TimeoutMapListener<Integer, CommandAddressPair> notlistener = new TimeoutMapListener<Integer, CommandAddressPair>(){
			@Override
			public void handleTimeout(Integer key, CommandAddressPair cmdaddr) {
				try {
					if(cmdaddr.addr == null){
						sendCommand(cmdaddr.cmd, true);
					}else{
						sendCommand(cmdaddr.cmd, cmdaddr.addr, true);						
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		notifications = new TimeoutMap<Integer, CommandAddressPair>(500, notlistener);

		socket = createSocket();

		new Thread(){
			@Override
			public void run() {
				try {
					while(true){
						byte[] buf = new byte[Constants.MAX_PACKET_SIZE];
						DatagramPacket packet = new DatagramPacket(buf, buf.length);
						socket.receive(packet);
						Object obj = Util.deserialize(packet.getData());
						if(obj == null){
							log.error("ERROR for "+obj);
						}else if(obj instanceof AbstractCommand){
							AbstractCommand cmd = (AbstractCommand)obj;
							notifications.remove(cmd.sequence);
							listener.receiveCommand(cmd, packet.getSocketAddress());
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
		}.start();

		initialized = true;
	}

	public void sendCommand(AbstractCommand cmd, boolean stateful) throws IOException{
		log.debug("Send command: "+cmd);
		if(stateful){
			notifications.put(cmd.sequence, new CommandAddressPair(cmd, null));
		}
		byte[] buf = Util.serialize(cmd);
		DatagramPacket packet = new DatagramPacket(buf, buf.length,
				address, port);
		if(socket != null && initialized){
			socket.send(packet);
		}
	}

	public void sendCommand(AbstractCommand cmd, SocketAddress addr, boolean stateful) throws IOException {
		log.debug("Send command: "+cmd);
		if(stateful){
			notifications.put(cmd.sequence, new CommandAddressPair(cmd, addr));
		}
		byte[] buf = Util.serialize(cmd);
		DatagramPacket packet = new DatagramPacket(buf, buf.length, addr);
		socket.send(packet);
	}

	protected abstract DatagramSocket createSocket();

}
