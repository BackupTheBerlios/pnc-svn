package com.mathias.clocks.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.mathias.clocks.SettingsDialog;

@SuppressWarnings("serial")
public class SettingsAction implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent arg0) {
		new SettingsDialog();
	}

}
