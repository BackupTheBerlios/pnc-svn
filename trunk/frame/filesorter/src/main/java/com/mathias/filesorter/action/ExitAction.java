package com.mathias.filesorter.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

@SuppressWarnings("serial")
public class ExitAction extends AbstractAction {
	
	public ExitAction(){
		super("Exit");
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		System.exit(0);
	}

}
