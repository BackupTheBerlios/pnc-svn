package com.mathias.batchlaunch.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

@SuppressWarnings("serial")
public class ExitAction extends AbstractAction {

	public ExitAction(String name){
		super(name);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		System.exit(0);
	}

}
