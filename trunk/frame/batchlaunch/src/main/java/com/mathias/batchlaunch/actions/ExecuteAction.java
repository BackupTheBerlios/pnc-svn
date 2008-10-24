package com.mathias.batchlaunch.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;

import com.mathias.batchlaunch.BatchItem;
import com.mathias.batchlaunch.BatchList;

@SuppressWarnings("serial")
public class ExecuteAction extends AbstractAction {
	
	private BatchList list;
	
	public ExecuteAction(String name, BatchList list){
		super(name);
		this.list = list;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		for (Object s : list.getSelectedValues()) {
			BatchItem item = list.get(s);
			try {
				Process child = Runtime.getRuntime().exec(item.getCommand());
//				System.out.println("Executed "+item.getName()+" command: "+item.getCommand());
			} catch (IOException e) {
				System.out.println("Could not execute "+item.getName()+" command: "+item.getCommand());
			}
		}
//		System.exit(0);
	}

}
