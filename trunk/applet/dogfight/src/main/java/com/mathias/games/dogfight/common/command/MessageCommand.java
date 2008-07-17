package com.mathias.games.dogfight.common.command;

public class MessageCommand extends AbstractCommand implements StateCommand {

	private static final long serialVersionUID = 8657511875461389472L;

	public String msg;

	public MessageCommand(String msg) {
		super();
		this.msg = msg;
	}

}
