package com.mathias.filesorter.table;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.mathias.filesorter.action.CopyToAction;
import com.mathias.filesorter.action.MoveToAction;
import com.mathias.filesorter.action.OpenAction;

@SuppressWarnings("serial")
public class FileItemTable extends JTable {

	public FileItemTable(){
		final FileItemTable _this = this;
		final FileItemTableModel model = new FileItemTableModel();
		setModel(model);
		model.addTableModelListener(new FileItemTableModelListener(this));
		setPreferredScrollableViewportSize(new Dimension(600, 600));

		TableRowSorter<FileItemTableModel> sorter = new TableRowSorter<FileItemTableModel>(model);
		sorter.setComparator(FileItem.SIZE, new FileItem.SizeComparator());
		setRowSorter(sorter);
		setTransferHandler(new FileItemTransferHandler());
		setDragEnabled(true);
		
		addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON3){
					JPopupMenu popup = createPopup(getSelectedFiles());
					popup.show(_this, e.getX(), e.getY());
				}
			}
		});

	}

	private JPopupMenu createPopup(List<String> files){
		//open, copy to..., move to...
		JPopupMenu popup = new JPopupMenu("label");
		JMenuItem openItem = new JMenuItem(new OpenAction(files));
		popup.add(openItem);
		JMenuItem copyToItem = new JMenuItem(new CopyToAction(files));
		popup.add(copyToItem);
		JMenuItem moveToItem = new JMenuItem(new MoveToAction(files));
		popup.add(moveToItem);
		return popup;
	}

	private List<String> getSelectedFiles(){
		int[] selection = getSelectedRows();
		for (int i = 0; i < selection.length; i++) {
			selection[i] = convertRowIndexToModel(selection[i]);
		}
		return getModel().getFilenames(selection);
	}

	@Override
	public FileItemTableModel getModel() {
		TableModel model = super.getModel();
		if(model instanceof FileItemTableModel){
			return (FileItemTableModel)model;
		}
		return null;
	}

	@Override
	public TableRowSorter<FileItemTableModel> getRowSorter() {
		RowSorter<? extends TableModel> sorter = super.getRowSorter();
		if(sorter instanceof TableRowSorter){
			return (TableRowSorter<FileItemTableModel>)sorter;
		}
		return null;
	}

}
