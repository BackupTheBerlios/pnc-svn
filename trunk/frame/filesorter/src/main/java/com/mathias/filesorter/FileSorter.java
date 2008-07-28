package com.mathias.filesorter;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;

import com.mathias.drawutils.Util;
import com.mathias.drawutils.frame.BasicFrame;
import com.mathias.filesorter.action.AddDirectoryAction;
import com.mathias.filesorter.action.ExitAction;
import com.mathias.filesorter.action.FilterAction;
import com.mathias.filesorter.table.FileItemTable;

@SuppressWarnings("serial")
public class FileSorter extends BasicFrame {

	public FileSorter() {
		super("FileSorter");
	}

	protected void init() {
		setLayout(new BorderLayout());

		FileItemTable table = new FileItemTable();

		AddDirectoryAction addDir = new AddDirectoryAction(table.getModel());
		
		JMenuBar menubar = new JMenuBar();
		JMenu menu;
		// File menu
		menu = menubar.add(new JMenu("File"));
		menu.add(addDir);
		menu.addSeparator();
		menu.add(new ExitAction());

		JToolBar toolbar = new JToolBar();
		final JTextField filter = new JTextField(20);
		final JCheckBox ci = new JCheckBox("Case insensitive", true);
		
		FilterAction filterAction = new FilterAction(table.getRowSorter(), filter, ci);
		ci.addActionListener(filterAction);
		filter.addCaretListener(filterAction);

		toolbar.setLayout(new GridBagLayout());
		toolbar.setFloatable(false);
		toolbar.add(createButton(addDir), Util.getGBC(0, 0, false, null));
		toolbar.add(new JLabel(""), Util.getGBC(1, 0, true, null));
		toolbar.add(new JLabel("Filter:"), Util.getGBC(2, 0, false, null));
		toolbar.add(filter, Util.getGBC(3, 0, false, new Insets(1, 2, 1, 2)));
		toolbar.add(ci, Util.getGBC(4, 0, false, null));

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new EtchedBorder());
		mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
		setJMenuBar(menubar);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		getContentPane().add(mainPanel, BorderLayout.CENTER);
	}
	
//	@Override
//	protected void processWindowEvent(WindowEvent e) {
//		super.processWindowEvent(e);
//		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
//			System.exit(0);
//		}
//	}
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// Do nothing...
		}
		new FileSorter();
	}

}
