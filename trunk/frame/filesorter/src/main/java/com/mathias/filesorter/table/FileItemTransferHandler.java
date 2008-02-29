package com.mathias.filesorter.table;

import java.awt.datatransfer.Transferable;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

import com.mathias.drawutils.FileListTransferable;

public class FileItemTransferHandler extends TransferHandler {
	
	public FileItemTransferHandler(){
		super();
	}

	@Override
	public int getSourceActions(JComponent c) {
		return COPY;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		if(!(c instanceof JTable)){
			return null;
		}else{
			JTable table = (JTable)c;
			FileItemTableModel model = (FileItemTableModel)table.getModel();
			int[] selection = table.getSelectedRows();
			for (int i = 0; i < selection.length; i++) {
				selection[i] = table.convertRowIndexToModel(selection[i]);
			}
			List<String> files = model.getFilePaths(selection);
			return new FileListTransferable(files);
		}
	}

}
