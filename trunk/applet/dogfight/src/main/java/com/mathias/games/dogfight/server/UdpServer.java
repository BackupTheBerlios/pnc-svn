package com.mathias.games.dogfight.server;

import com.mathias.games.dogfight.common.WorldEngine;

public class UdpServer {

	private WorldEngine logic;

	public UdpServer() {
		logic = new WorldEngine();

		new NetworkThread(logic.players);
	}

	public static void main(String[] args) {
		new UdpServer();
	}

}
