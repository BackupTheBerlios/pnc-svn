package com.mathias.games.dogfight.server;

import java.net.DatagramSocket;
import java.net.SocketException;

import com.mathias.games.dogfight.common.AbstractUdpNetwork;
import com.mathias.games.dogfight.common.Constants;
import com.mathias.games.dogfight.common.UdpNetworkListener;

public class UdpNetworkServer extends AbstractUdpNetwork {

//	private static final Logger log = LoggerFactory.getLogger(UdpNetworkServer.class);

	public UdpNetworkServer(UdpNetworkListener listener){
		super(listener);
	}

	@Override
	protected DatagramSocket createSocket() {
		try {
			return new DatagramSocket(Constants.PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}

}
