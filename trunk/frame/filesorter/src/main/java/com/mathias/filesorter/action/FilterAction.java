package com.mathias.filesorter.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.table.TableRowSorter;

import com.mathias.filesorter.table.FileItemTableModel;

@SuppressWarnings("serial")
public class FilterAction extends AbstractAction implements CaretListener {

	TableRowSorter<FileItemTableModel> sorter;
	private JTextField filter;
	private JCheckBox ci;

	public FilterAction(TableRowSorter<FileItemTableModel> sorter, JTextField filter,
			JCheckBox ci) {
		this.sorter = sorter;
		this.filter = filter;
		this.ci = ci;
	}

	@Override
	public void caretUpdate(CaretEvent arg0) {
		action();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		action();
	}
	
	private void action(){
		sorter.setRowFilter(RowFilter.regexFilter((ci.isSelected() ? "(?i)" : "")+filter.getText()));
	}

}
