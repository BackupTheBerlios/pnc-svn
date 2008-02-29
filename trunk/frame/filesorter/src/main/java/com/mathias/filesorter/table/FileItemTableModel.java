package com.mathias.filesorter.table;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class FileItemTableModel extends AbstractTableModel {

	private List<FileItem> fileItems = new ArrayList<FileItem>();

	@Override
	public Object getValueAt(int row, int col) {
		FileItem fi = fileItems.get(row);
		return fi.get(col);
	}

	@Override
	public String getColumnName(int index) {
		String ret = FileItem.KEYS.get(index).name;
		if(ret == null){
			ret = "Error";
		}
		return ret;
	}

	@Override
	public int getColumnCount() {
		return FileItem.KEYS.size();
	}

	@Override
	public int getRowCount() {
		return fileItems.size();
	}

	public void addFile(File file){
		fileItems.add(new FileItem(file));
		fireTableDataChanged();
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return FileItem.KEYS.get(col).editable;
	}

	public List<String> getFilePaths(int[] rows){
		List<String> ret = new ArrayList<String>();
		if(rows != null){
			for (int i : rows) {
				try{
					ret.add(fileItems.get(i).get(FileItem.PATH));
				}catch(IndexOutOfBoundsException e){
					System.out.println(e.getMessage());
				}
			}
		}
		return ret;
	}

}
