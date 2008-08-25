package com.mathias.games.dogfight.common.command;


public class LoginCommand extends AbstractCommand {

	private static final long serialVersionUID = 8076291243790587909L;

	private String username;

	private String password;
	
	public boolean authenticated = false;

	public LoginCommand(String username, String password) {
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
