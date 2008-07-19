package com.mathias.games.dogfight.server;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mathias.games.dogfight.common.WorldEngine;

public class UdpServer {

	private static final Logger log = LoggerFactory.getLogger(UdpServer.class);
	
	private List<WorldEngine> engines = new ArrayList<WorldEngine>();
	
	public UdpServer() {
		WorldEngine engine = new WorldEngine();

		engines.add(engine);
		
		new NetworkThread(engine);
		
		log.debug("Dogfight UDP server started!");
	}

	public static void main(String[] args) {
		new UdpServer();
	}

}
