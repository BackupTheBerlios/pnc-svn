package com.mathias.filesorter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.table.TableRowSorter;

import com.mathias.drawutils.Util;
import com.mathias.filesorter.action.AddDirectoryAction;
import com.mathias.filesorter.action.ExitAction;
import com.mathias.filesorter.table.FileItemTableModel;
import com.mathias.filesorter.table.FileItemTableModelListener;
import com.mathias.filesorter.table.FileItemTransferHandler;

@SuppressWarnings("serial")
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

		final FileItemTableModel model = new FileItemTableModel();
		JTable table = new JTable(model);
		table.setPreferredScrollableViewportSize(new Dimension(600, 600));
		model.addTableModelListener(new FileItemTableModelListener(table));

		table.setRowSorter(new TableRowSorter<FileItemTableModel>(model));
		table.setTransferHandler(new FileItemTransferHandler());
		table.setDragEnabled(true);
		
		AddDirectoryAction addDir = new AddDirectoryAction(model);
		
		JMenuBar menubar = new JMenuBar();
		JMenu menu;
		// File menu
		menu = menubar.add(new JMenu("File"));
		menu.add(addDir);
		menu.addSeparator();
		menu.add(new ExitAction());

		JToolBar toolbar = new JToolBar();
		final JTextField filter = new JTextField(20);
		filter.addCaretListener(new CaretListener(){
			@Override
			public void caretUpdate(CaretEvent arg0) {
				model.filter(filter.getText(), false);
			}
		});
		toolbar.setLayout(new GridBagLayout());
		toolbar.setFloatable(false);
		toolbar.add(createButton(addDir), Util.getGBC(0, 0, false, null));
		toolbar.add(new JLabel("Filter:"), Util.getGBC(1, 0, false, null));
		toolbar.add(filter, Util.getGBC(2, 0, false, new Insets(1, 2, 1, 2)));

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new EtchedBorder());
		mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
		setJMenuBar(menubar);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		getContentPane().add(mainPanel, BorderLayout.CENTER);
	}

	private JButton createButton(Action action) {
		JButton button = new JButton(action);
		if(action.getValue(Action.SMALL_ICON) == null){
			button.setText((String)action.getValue(Action.NAME));
		}
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setFocusable(false);
		return button;
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
