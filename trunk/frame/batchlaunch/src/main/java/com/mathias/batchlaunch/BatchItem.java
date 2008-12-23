package com.mathias.batchlaunch;

import javax.swing.JLabel;

public class BatchItem extends JLabel {
	
	private String name;
	
	private String command;
	
	private String cwd;

	public BatchItem(String name, String command, String cwd) {
		this.name = name;
		this.command = command;
		this.cwd = cwd;
		setText(name);
	}

	public String getName() {
		return name;
	}

	public String getCommand() {
		return command;
	}

	public String getCwd() {
		return cwd;
	}

}
