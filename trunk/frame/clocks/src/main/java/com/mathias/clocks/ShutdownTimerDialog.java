package com.mathias.clocks;

import java.io.IOException;

public class ShutdownTimerDialog extends TimerDialog {

	public ShutdownTimerDialog(String name) {
		super(name);
	}

	@Override
	protected void action() {
		try {
			Runtime.getRuntime().exec("shutdown /h");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
