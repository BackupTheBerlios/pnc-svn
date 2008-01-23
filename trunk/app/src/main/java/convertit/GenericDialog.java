package convertit;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

public abstract class GenericDialog extends JDialog {

	protected static Frame sOwner;

	public GenericDialog(String title, boolean resizable) {
		this(title, true, resizable);
	}

	public GenericDialog(String title, boolean modal, boolean resizable) {
		super(sOwner, title, modal);
		setResizable(resizable);
	}

	public static void setDefaultOwner(Frame owner) {
		sOwner = owner;
	}

	public static void showErrorDialog(String title, String message) {
		JOptionPane.showMessageDialog(sOwner, message, title, JOptionPane.ERROR_MESSAGE);
	}

	public static void showInfoDialog(String title, String message) {
		JOptionPane.showMessageDialog(sOwner, message, title, JOptionPane.INFORMATION_MESSAGE);
	}

	public static boolean showConfirmDialog(String title, String message) {
		return (JOptionPane.showConfirmDialog(sOwner, message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
	}

	protected abstract void setupUI();

	protected void setComponent(Component component, String position) {
		getContentPane().add(component, position);
	}

	protected void initUI() {
		initUI(0, 0, true);
	}

	protected void initUI(int minWidth, int minHeight) {
		initUI(minWidth, minHeight, true);
	}
	
	protected void initUI(int minWidth, int minHeight, boolean show) {
		setupUI();
		pack();

		if (minWidth > 0 && minHeight > 0) {
			setMimimumSize(minWidth, minHeight);
		}

		setLocationRelativeTo(sOwner);
		setVisible(show);
	}

	private void handleClosing(boolean cancelled) {
		setVisible(false);
	}

	protected JRootPane createRootPane() {
		JRootPane rootPane = new JRootPane();
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		rootPane.registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleClosing(true);
			}
		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		return rootPane;
	}

	protected void setMimimumSize(int minWidth, int minHeight) {
		Dimension d = getSize();
		d.width = Math.max(minWidth, d.width);
		d.height = Math.max(minHeight, d.height);
		setSize(d);
	}
}
