package com.mathias.filesorter.table;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class FileItemTableModelListener implements TableModelListener {

//	private JTable table;

	public FileItemTableModelListener(JTable table) {
//		this.table = table;
	}

	@Override
	public void tableChanged(TableModelEvent evt) {
		int col = evt.getColumn();
		int row = evt.getFirstRow();
		System.out.println("row: " + row + " col: " + col + " source: "
				+ evt.getSource() + " type: " + evt.getType());
		if (evt.getType() == TableModelEvent.UPDATE) {
			//TODO
		}
	}

}
