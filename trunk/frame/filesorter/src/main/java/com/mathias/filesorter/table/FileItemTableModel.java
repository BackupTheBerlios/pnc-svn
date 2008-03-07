package com.mathias.filesorter.table;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.mathias.drawutils.Util;

@SuppressWarnings("serial")
public class FileItemTableModel extends AbstractTableModel {

	private List<FileItem> visibleFileItems = new ArrayList<FileItem>();
	private List<FileItem> allFileItems = new ArrayList<FileItem>();

	@Override
	public Object getValueAt(int row, int col) {
		FileItem fi = visibleFileItems.get(row);
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
		return visibleFileItems.size();
	}

	public void addFile(File...file){
		for (File f : file) {
			FileItem fi = new FileItem(f);
			visibleFileItems.add(fi);
			allFileItems.add(fi);
		}
		fireTableDataChanged();
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return FileItem.KEYS.get(col).editable;
	}

	public List<String> getFilenames(int[] rows){
		List<String> ret = new ArrayList<String>();
		if(rows != null){
			for (int i : rows) {
				try{
					ret.add(visibleFileItems.get(i).get(FileItem.ABSOLUTENAME));
				}catch(IndexOutOfBoundsException e){
					System.out.println(e.getMessage());
				}
			}
		}
		return ret;
	}
	
	public void filter(String filter, boolean casesensitive){
		visibleFileItems.clear();
		visibleFileItems.addAll(allFileItems);
		if(!Util.isEmpty(filter)){
			for (Iterator<FileItem> it = visibleFileItems.iterator(); it.hasNext(); ) {
				FileItem fi = it.next();
				int cols = getColumnCount();
				boolean found = false;
				for(int i = 0; i < cols; i++){
					String str = fi.get(i);
					if(casesensitive){
						if(str != null && str.indexOf(filter) != -1){
							found = true;
							break;
						}
					}else{
						//TODO
						if(str != null && str.matches(".*"+filter+".*")){
							found = true;
							break;
						}
					}
				}
				if(!found){
					it.remove();
				}
			}
		}
		fireTableDataChanged();
	}

}
