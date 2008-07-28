package com.mathias.batchlaunch;

public class BatchItem {
	
	private String name;
	
	private String command;

	public BatchItem(String name, String command) {
		this.name = name;
		this.command = command;
	}

	public String getName() {
		return name;
	}

	public String getCommand() {
		return command;
	}

}
