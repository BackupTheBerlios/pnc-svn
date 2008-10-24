package com.mathias.batchlaunch;

import javax.swing.JLabel;

public class BatchItem extends JLabel {
	
	private String name;
	
	private String command;

	public BatchItem(String name, String command) {
		this.name = name;
		this.command = command;
		setText(name);
	}

	public String getName() {
		return name;
	}

	public String getCommand() {
		return command;
	}

}
