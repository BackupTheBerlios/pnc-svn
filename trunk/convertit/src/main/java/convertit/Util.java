package convertit;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class Util {

	public static final ImageIcon APPLICATION_ICON = loadIcon("application.gif");

	public static final ImageIcon CONNECT_ICON = loadIcon("connect.gif");

	public static final ImageIcon REFRESH_ICON = loadIcon("refresh.gif");

	public static final ImageIcon FIND_ICON = loadIcon("find.gif");

	public static final ImageIcon LEAF_ICON = loadIcon("inactive.gif");

	public static final ImageIcon ACTIVE_LEAF_ICON = loadIcon("active.gif");

	public static final ImageIcon FOLDER_ICON = loadIcon("folder.gif");

	public static final ImageIcon ACTIVE_FOLDER_ICON = loadIcon("activefolder.gif");

	public static final ImageIcon LOGO_ICON = loadIcon("stlogo.gif");

	public static boolean isEmpty(String s) {
		return (s == null || s.length() == 0);
	}

	public static boolean isNotEmpty(String s) {
		return !isEmpty(s);
	}

	public static boolean isBlank(String s) {
		return (s == null || s.trim().length() == 0);
	}

	public static String join(String[] sa, char delimiter) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sa.length; i++) {
			if (i != 0) {
				sb.append(delimiter);
			}
			if (sa[i] != null) {
				sb.append(sa[i]);
			}
		}
		return sb.toString();
	}

	public static String join(List<String> sa, char delimiter) {
		return join(sa.toArray(new String[sa.size()]), delimiter);
	}

	public static String[] split(String s, char delimiter) {
		List<String> list = splitToList(s, delimiter);
		return list.toArray(new String[list.size()]);
	}

	public static List<String> splitToList(String s, char delimiter) {
		ArrayList<String> tokens = new ArrayList<String>();

		if (isEmpty(s)) {
			return tokens;
		}

		int start = 0;
		int end = s.indexOf(delimiter);

		while (true) {
			if (end == -1) {
				tokens.add(s.substring(start));
				break;
			}
			tokens.add(s.substring(start, end));
			start = end + 1;
			end = s.indexOf(delimiter, start);
		}

		return tokens;
	}

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
		return new ImageIcon(Util.class.getResource("icons/" + name));
	}

}
