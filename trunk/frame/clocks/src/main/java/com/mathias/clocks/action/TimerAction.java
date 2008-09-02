package com.mathias.clocks.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.mathias.clocks.TimerDialog;

public class TimerAction implements ActionListener {
	
	private TimerListener listener;
	
	public TimerAction(TimerListener listener) {
		this.listener = listener;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		TimerDialog timerDialog = new TimerDialog("Timer");
		listener.handleTimer(timerDialog.getTimerTime());
	}

}
