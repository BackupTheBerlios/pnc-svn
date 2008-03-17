package com.mathias.filesorter.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

public class MoveToAction extends AbstractAction {

	private List<String> files;

	public MoveToAction(List<String> files) {
		super("Move to...");
		
		this.files = files;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int ret = fc.showOpenDialog(null);
		File selDir = fc.getSelectedFile();
		if(JFileChooser.APPROVE_OPTION == ret && selDir != null){
			for (String file : files) {
				File f = new File(file);
				f.renameTo(new File(selDir.getAbsolutePath()
						+ File.pathSeparator + f.getName()));
			}
		}
	}

}
