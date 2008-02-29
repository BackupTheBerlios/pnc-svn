package com.mathias.filesorter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableRowSorter;

import com.mathias.drawutils.Util;
import com.mathias.filesorter.action.AddDirectoryAction;
import com.mathias.filesorter.action.ExitAction;
import com.mathias.filesorter.table.FileItemTableModel;
import com.mathias.filesorter.table.FileItemTransferHandler;

public class FileSorter extends JFrame {

	public FileSorter() {
		initUI();
		pack();
		Util.centerFrame(this);
		setVisible(true);
	}

	private void initUI() {
		setLayout(new BorderLayout());
		setTitle("FileSorter");

		FileItemTableModel model = new FileItemTableModel();
		JTable table = new JTable(model);
		table.setPreferredScrollableViewportSize(new Dimension(600, 600));
		//model.addTableModelListener(new FileItemTableModelListener(table));

		table.setRowSorter(new TableRowSorter<FileItemTableModel>(model));
		table.setTransferHandler(new FileItemTransferHandler());
		table.setDragEnabled(true);
		
		JMenuBar menubar = new JMenuBar();
		JMenu menu;
		// File menu
		menu = menubar.add(new JMenu("File"));
		menu.add(new AddDirectoryAction(model));
		menu.add(new ExitAction());

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new EtchedBorder());
		mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
		setJMenuBar(menubar);
		getContentPane().add(mainPanel, BorderLayout.CENTER);
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			System.exit(0);
		}
	}
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// Do nothing...
		}
		new FileSorter();
	}

}
