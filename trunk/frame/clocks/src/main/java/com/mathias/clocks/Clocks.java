package com.mathias.clocks;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFrame;

import com.mathias.clocks.action.ExitAction;
import com.mathias.clocks.action.SettingsAction;

@SuppressWarnings("serial")
public class Clocks extends JFrame implements MouseListener {
	
//	private final static Logger log = Logger.getLogger(Clocks.class.getName());

	private List<Clock> clocks;
	private Map<Integer, Image> imgs;
	private Image img;
	private Graphics2D g;
	private String location;
	private TrayIcon trayIcon = null;
	private PopupMenu popup;
	private Font font;
	private int height;

	private final static int WIDTH = 130;
	private final static int DHEIGHT = 55;
	private final static Color TEXTCOLOR = new Color(75, 75, 255);
	
//	private final static int IMG_0 = 0;
	private final static int IMG_ICO = 11;

	public Clocks(){

//		GenericLogManager.init();
//		log.info("TESTING");
//		log.finest("TESTING2");

		setUndecorated(Configuration.getBoolean("undecorated", true));
		setAlwaysOnTop(Configuration.getBoolean("ontop", true));
		setVisible(true);

		imgs = new HashMap<Integer, Image>();
//		imgs.put(IMG_0, getToolkit().getImage(getClass().getResource("images/0.gif")));
//		imgs.put(1, getToolkit().getImage(getClass().getResource("images/1.gif")));
//		imgs.put(2, getToolkit().getImage(getClass().getResource("images/2.gif")));
//		imgs.put(3, getToolkit().getImage(getClass().getResource("images/3.gif")));
//		imgs.put(4, getToolkit().getImage(getClass().getResource("images/4.gif")));
//		imgs.put(5, getToolkit().getImage(getClass().getResource("images/5.gif")));
//		imgs.put(6, getToolkit().getImage(getClass().getResource("images/6.gif")));
//		imgs.put(7, getToolkit().getImage(getClass().getResource("images/7.gif")));
//		imgs.put(8, getToolkit().getImage(getClass().getResource("images/8.gif")));
//		imgs.put(9, getToolkit().getImage(getClass().getResource("images/9.gif")));
//		imgs.put(10, getToolkit().getImage(getClass().getResource("images/k.gif")));
		imgs.put(IMG_ICO, getToolkit().getImage(getClass().getResource("images/clocks_icon.gif")));

		MediaTracker mt = new MediaTracker(this);
		for (Entry<Integer, Image> e : imgs.entrySet()) {
			mt.addImage(e.getValue(), e.getKey());
		}
		try {
			mt.waitForAll();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		int fontSize = Configuration.getInt("font", 20);
		String fn = Configuration.get("font", "Arial");
		Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		for (Font f : fonts) {
			if(fn.equals(f.getName())){
				font = f.deriveFont(Font.PLAIN, fontSize);
				break;
			}
		}
		if(font == null){
			font = fonts[0];
		}

		popup = createPopupMenu();
		add(popup);

	    if(SystemTray.isSupported() && Configuration.getBoolean("systray", true)){
			SystemTray tray = SystemTray.getSystemTray();

			trayIcon = new TrayIcon(imgs.get(IMG_ICO), "Clocks", createPopupMenu());
		    trayIcon.setImageAutoSize(true);

		    try {
		        tray.add(trayIcon);
		    } catch (AWTException e) {
		        System.err.println("TrayIcon could not be added.");
		    }
		}
		
		clocks = Configuration.getClocks();
		location = Configuration.get("location", "left");

		height = clocks.size()*DHEIGHT+20;
		img = createImage(WIDTH, height);
		g = (Graphics2D)img.getGraphics();

		setLocation(false);
		setSize(WIDTH, height);

		addMouseListener(this);
	}
	
	private PopupMenu createPopupMenu(){
		PopupMenu popup = new PopupMenu();
		MenuItem settingsItem = new MenuItem("Settings...");
		settingsItem.addActionListener(new SettingsAction());
		popup.add(settingsItem);
		popup.addSeparator();
		MenuItem exitItem = new MenuItem("Exit");
	    exitItem.addActionListener(new ExitAction());
	    popup.add(exitItem);
	    return popup;
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			new ExitAction().actionPerformed(null);
		}
	}
	
	@Override
	public void paint(Graphics arg0) {
		super.paint(arg0);
		paintClocks();
	}

	private void paintClocks(){
		g.setColor(TEXTCOLOR);
		g.fillRoundRect(0, 0, WIDTH, height, 20, 20);
		g.setColor(Color.white);
		g.fillRoundRect(10, 10, WIDTH-20, height-20, 20, 20);

		StringBuilder sb = new StringBuilder();
		sb.append("Clocks");
		Iterator<Clock> it = clocks.iterator();
		g.setColor(TEXTCOLOR);
		FontRenderContext frc = g.getFontRenderContext();
		boolean seconds = Configuration.getBoolean("seconds", false);
		for (int i = 0; it.hasNext(); i++) {
			Clock c = it.next();
			int y = i*DHEIGHT+30;
			TextLayout layout;
			String time = c.getTime(seconds);
			//draw clock name
			layout = new TextLayout(c.getName(), font, frc);
			layout.draw(g, (float) (WIDTH/2-layout.getBounds().getCenterX()), (float)y);
			//draw clock time
			layout = new TextLayout(time, font, frc);
			layout.draw(g, (float) (WIDTH/2-layout.getBounds().getCenterX()), (float) y+25);
			//title and tooltip
			sb.append("\n"+c.getName()+" "+time);
		}
		getContentPane().getGraphics().drawImage(img, 0, 0, null);
		if(trayIcon != null){
		    trayIcon.setToolTip(sb.toString());
		}
	    setTitle(sb.toString());
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON3){
		    popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		setLocation(true);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		setLocation(false);
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	private void setLocation(boolean visible){
		Dimension ss = getToolkit().getScreenSize();
		if(visible || !Configuration.getBoolean("autohide", true)){
			if("left".equals(location)) {
				setLocation(0, ss.height/2-height/2);
			} else if("right".equals(location)) {
				setLocation(ss.width-WIDTH, ss.height/2-height/2);
			} else if("top".equals(location)) {
				setLocation(ss.width/2-WIDTH/2, 0);
			} else if("bottom".equals(location)) {
				setLocation(ss.width/2-WIDTH/2, ss.height-height);
			}else{
				setLocation(ss.width, ss.height);
			}
		}else{
			int overlap = Configuration.getInt("overlap", 1);
			if("left".equals(location)) {
				setLocation(overlap-WIDTH, ss.height/2-height/2);
			} else if("right".equals(location)) {
				setLocation(ss.width-overlap, ss.height/2-height/2);
			} else if("top".equals(location)) {
				setLocation(ss.width/2-WIDTH/2, overlap-height);
			} else if("bottom".equals(location)) {
				setLocation(ss.width/2-WIDTH/2, ss.height-overlap);
			}else{
				setLocation(ss.width, ss.height);
			}
		}
	}

	public static void main(String[] args) {
		new Clocks();
	}

}
