package convertit;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class JUtil {

	public static final ImageIcon APPLICATION_ICON = loadIcon("application.gif");

	public static final ImageIcon CONNECT_ICON = loadIcon("connect.gif");

	public static final ImageIcon REFRESH_ICON = loadIcon("refresh.gif");

	public static final ImageIcon FIND_ICON = loadIcon("find.gif");

	public static final ImageIcon LEAF_ICON = loadIcon("inactive.gif");

	public static final ImageIcon ACTIVE_LEAF_ICON = loadIcon("active.gif");

	public static final ImageIcon FOLDER_ICON = loadIcon("folder.gif");

	public static final ImageIcon ACTIVE_FOLDER_ICON = loadIcon("activefolder.gif");

	public static final ImageIcon LOGO_ICON = loadIcon("stlogo.gif");

	public static void centerFrame(JFrame frame) {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (screen.width - frame.getWidth()) / 2;
		int y = (screen.height - frame.getHeight()) / 2;
		frame.setLocation(x, y);
	}

	public static GridBagConstraints getGBC(int x, int y, boolean fill, Insets insets) {
		return getGBC(x, y, 1, 1, fill, insets);
	}

	public static GridBagConstraints getGBC(int x, int y, int w, int h, boolean fill, Insets insets) {
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = w;
		gbc.gridheight = h;
		gbc.anchor = GridBagConstraints.WEST;

		if (fill) {
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
		}

		if (insets != null) {
			gbc.insets = insets;
		}

		return gbc;
	}

	private static ImageIcon loadIcon(String name) {
		return new ImageIcon(JUtil.class.getResource("icons/" + name));
	}

}
