package com.mathias.clocks.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.mathias.clocks.Clocks;
import com.mathias.clocks.SettingsDialog;

@SuppressWarnings("serial")
public class SettingsAction implements ActionListener {
	
	private Clocks clocks;
	
	public SettingsAction(Clocks clocks){
		this.clocks = clocks;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		new SettingsDialog(clocks);
	}

}
