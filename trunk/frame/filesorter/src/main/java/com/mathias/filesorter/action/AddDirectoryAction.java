package com.mathias.filesorter.action;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import com.mathias.filesorter.table.FileItemTableModel;

public class AddDirectoryAction extends AbstractAction {
	
	private FileItemTableModel model;

	public AddDirectoryAction(FileItemTableModel model) {
		super("Add dir...");
		this.model = model;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int ret = fc.showOpenDialog(null);
		File selDir = fc.getSelectedFile();
		if(JFileChooser.APPROVE_OPTION == ret && selDir != null){
			recurseDirectory(selDir);
		}
	}
	
	private void recurseDirectory(File directory){
		File[] files = directory.listFiles();
		for (File file : files) {
			if(file.isFile()){
				model.addFile(file);
			}else if(file.isDirectory()){
				recurseDirectory(file);
			}else{
				System.out.println("Not dir or reg file: "+file.getName());
			}
		}
	}

}
