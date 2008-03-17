package com.mathias.filesorter.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import com.mathias.filesorter.Icons;
import com.mathias.filesorter.table.FileItemTableModel;

@SuppressWarnings("serial")
public class AddDirectoryAction extends AbstractAction {

	private FileItemTableModel model;
	
	private List<File> dirs = new ArrayList<File>();

	public AddDirectoryAction(FileItemTableModel model) {
		super("Add dir...");

		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control K"));
		putValue(Action.SMALL_ICON, Icons.REFRESH.getIcon());
		putValue(Action.SHORT_DESCRIPTION, "Add dir...");

		this.model = model;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int ret = fc.showOpenDialog(null);
		File selDir = fc.getSelectedFile();
		if(JFileChooser.APPROVE_OPTION == ret && selDir != null){
			dirs.add(selDir);
			recurseDirectory(selDir);
		}
	}
	
	public void refresh(){
		model.clear();
		for (File dir : dirs) {
			recurseDirectory(dir);
		}
	}
	
	private void recurseDirectory(File directory){
		File[] files = directory.listFiles(new DirFileFilter());
		for (File file : files) {
			recurseDirectory(file);
		}
		files = directory.listFiles(new FileFileFilter());
		model.addFile(files);
	}
	
	class FileFileFilter implements FileFilter {
		@Override
		public boolean accept(File file) {
			return file.isFile();
		}
	}

	class DirFileFilter implements FileFilter {
		@Override
		public boolean accept(File file) {
			return file.isDirectory();
		}
	}

}
