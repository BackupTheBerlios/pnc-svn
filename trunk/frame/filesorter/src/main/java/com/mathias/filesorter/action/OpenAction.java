package com.mathias.filesorter.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;

@SuppressWarnings("serial")
public class OpenAction extends AbstractAction {
	
	private List<String> files;

	public OpenAction(List<String> files) {
		super("Open");
		
		this.files = files;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		for (String file : files) {
			try {
				Runtime.getRuntime().exec("cmd /c \""+file+"\"");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

}
