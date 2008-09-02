package com.mathias.clocks.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.mathias.clocks.ShutdownTimerDialog;

public class ShutdownAction implements ActionListener {
	
	private TimerListener listener;
	
	public ShutdownAction(TimerListener listener) {
		this.listener = listener;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		ShutdownTimerDialog shutdownTimerDialog = new ShutdownTimerDialog("Shutdown");
		listener.handleTimer(shutdownTimerDialog.getTimerTime());
	}

}
