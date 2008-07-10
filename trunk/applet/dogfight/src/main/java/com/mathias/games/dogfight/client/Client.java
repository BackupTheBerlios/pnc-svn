package com.mathias.games.dogfight.client;

import java.io.IOException;
import java.util.List;

import com.mathias.games.dogfight.Player;


public interface Client {

	List<Player> update(Player player) throws IOException;

	void connect(String host, int port) throws IOException;

	void disconnect();

}
