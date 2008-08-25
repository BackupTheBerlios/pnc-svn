package com.mathias.games.dogfight.client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mathias.games.dogfight.common.AbstractUdpNetwork;
import com.mathias.games.dogfight.common.UdpNetworkListener;
import com.mathias.games.dogfight.common.command.LoginCommand;
import com.mathias.games.dogfight.common.command.LogoutCommand;
import com.mathias.games.dogfight.common.command.UpdateCommand;
import com.mathias.games.dogfight.common.items.AbstractItem;

public class UdpNetworkClient extends AbstractUdpNetwork {

	private static final Logger log = LoggerFactory.getLogger(UdpNetworkClient.class);

	public UdpNetworkClient(String address, int port, UdpNetworkListener listener) {
		super(address, port, listener);
	}

	public UdpNetworkClient(UdpNetworkListener listener) {
		super(listener);
	}
	
	/**
	 * Login
	 * State change/notification
	 * @param player
	 * @throws IOException
	 */
	public void login(String username, String password) throws IOException {
		log.debug("login: username="+username);
		sendCommand(new LoginCommand(username, password), true);
	}
	
	public void logout(String username, String password) throws IOException {
		log.debug("logout: username="+username);
		sendCommand(new LogoutCommand(username, password), true);
	}
	
	public void update(AbstractItem item) throws IOException {
		log.debug("update");
		UpdateCommand upd = new UpdateCommand(item);
		sendCommand(upd, false);
	}

	@Override
	protected DatagramSocket createSocket() {
		try {
			return new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
