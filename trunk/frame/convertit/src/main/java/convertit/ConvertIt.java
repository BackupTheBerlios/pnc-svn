package convertit;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

public class ConvertIt extends JFrame {

	public ConvertIt() {
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);

		initUI();
		JUtil.centerFrame(this);
		setVisible(true);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				initAction();
			}
		});
	}

	public void exitAction() {
		System.exit(0);
	}

	private void initAction() {
		GenericDialog.setDefaultOwner(this);
	}

	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			exitAction();
		}
	}

	private void initUI() {
		setTitle("WEEEE");
		setIconImage(JUtil.APPLICATION_ICON.getImage());
		setSize(500, 500);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		JMenuBar menubar = new JMenuBar();
		JMenu menu;

		// File menu
		menu = menubar.add(new JMenu("File"));
//		menu.add(connectAction);
//		menu.add(refreshAction);
//		menu.addSeparator();
//		menu.add(new SaveStateAction(this));
//		menu.addSeparator();
//		menu.add(new ShowSpecificationAction(this));
//		menu.add(new FetchConfigurationAction(this));
//		menu.add(new UpdateConfigurationAction(this));
//		menu.addSeparator();
//		menu.add(new ReinitializeAction(this));
//		menu.addSeparator();
//		menu.add(new PreferencesAction(this));
//		menu.addSeparator();
		menu.add(new AbstractAction("Exit"){
			public void actionPerformed(ActionEvent arg0) {
				exitAction();
			}
		});

		// Edit menu
		menu = menubar.add(new JMenu("Edit"));
//		menu.add(new ExpandActiveAction(this));
//		menu.add(new ToggleExpandedAction(this));
//		menu.add(new ToggleNamesAction(this));
//		menu.addSeparator();
//		menu.add(mRestoreStateMenu);
//		menu.add(mRemoveStateMenu);

		// Help menu
		menu = menubar.add(new JMenu("Help"));
//		menu.add(new AboutAction(this));

		JToolBar toolbar = new JToolBar();
		JTextField searchField = new JTextField(20);
		toolbar.setLayout(new GridBagLayout());
		toolbar.setFloatable(false);
//		toolbar.add(createButton(connectAction), Util.getGBC(0, 0, false, null));
//		toolbar.add(createButton(refreshAction), Util.getGBC(1, 0, false, null));
		toolbar.add(new JLabel(""), JUtil.getGBC(2, 0, true, null));
		toolbar.add(new JLabel("Find:"), JUtil.getGBC(3, 0, false, null));
		toolbar.add(searchField, JUtil.getGBC(4, 0, false, new Insets(1, 2, 1, 2)));
//		toolbar.add(createButton(new FindAction(this, searchField)), Util.getGBC(5, 0, false, null));

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(2, 2, 2, 2)));
		mainPanel.add(new CenterPanel(), BorderLayout.CENTER);

		setJMenuBar(menubar);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		getContentPane().add(mainPanel, BorderLayout.CENTER);
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// Do nothing...
		}

		new ConvertIt();
	}

}
