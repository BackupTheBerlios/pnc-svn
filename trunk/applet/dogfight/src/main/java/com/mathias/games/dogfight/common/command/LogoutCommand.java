package com.mathias.games.dogfight.common.command;


public class LogoutCommand extends AbstractCommand implements StateCommand {

	private static final long serialVersionUID = 4875233616220054693L;

	private String username;

	private String password;
	
	public LogoutCommand(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

}
