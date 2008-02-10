package com.mathias.clocks.action;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class ExitAction implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent arg0) {
	    if(SystemTray.isSupported()){
			SystemTray tray = SystemTray.getSystemTray();
			for (TrayIcon icon : tray.getTrayIcons()) {
				tray.remove(icon);
			}
	    }
		System.exit(0);
	}

}
