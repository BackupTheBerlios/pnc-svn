package com.mathias.games.dogfight.common;

import java.io.IOException;
import java.net.SocketAddress;

import com.mathias.games.dogfight.common.command.AbstractCommand;

public interface UdpNetworkListener {

	void receiveCommand(AbstractCommand cmd, SocketAddress addr) throws IOException;
}
