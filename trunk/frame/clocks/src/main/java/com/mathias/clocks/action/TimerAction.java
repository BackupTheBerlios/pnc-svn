package com.mathias.clocks.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.mathias.clocks.TimerDialog;

@SuppressWarnings("serial")
public class TimerAction implements ActionListener {
	
	public TimerDialog td = null;

	@Override
	public void actionPerformed(ActionEvent arg0) {
		td = new TimerDialog();
	}

}
