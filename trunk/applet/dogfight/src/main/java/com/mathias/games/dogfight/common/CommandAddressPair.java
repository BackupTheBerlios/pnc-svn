package com.mathias.games.dogfight.common;

import java.net.SocketAddress;

import com.mathias.games.dogfight.common.command.AbstractCommand;

public class CommandAddressPair {

	AbstractCommand cmd;
	SocketAddress addr;

	public CommandAddressPair(AbstractCommand cmd, SocketAddress addr) {
		this.cmd = cmd;
		this.addr = addr;
	}

}
