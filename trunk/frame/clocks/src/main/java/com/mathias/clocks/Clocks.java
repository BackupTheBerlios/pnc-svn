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
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

import com.mathias.clocks.action.ExitAction;

@SuppressWarnings("serial")
public class Clocks extends JFrame implements MouseListener {

	private List<Clock> clocks;
	private Image[] imgs;
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

	public Clocks(){
		setUndecorated(Configuration.getBoolean("undecorated", true));
		setAlwaysOnTop(Configuration.getBoolean("ontop", true));
		setVisible(true);

		imgs = new Image[1];
		imgs[0] = getToolkit().getImage(getClass().getResource("images/0.gif"));
//		imgs[1] = getToolkit().getImage(getClass().getResource("images/1.gif"));
//		imgs[2] = getToolkit().getImage(getClass().getResource("images/2.gif"));
//		imgs[3] = getToolkit().getImage(getClass().getResource("images/3.gif"));
//		imgs[4] = getToolkit().getImage(getClass().getResource("images/4.gif"));
//		imgs[5] = getToolkit().getImage(getClass().getResource("images/5.gif"));
//		imgs[6] = getToolkit().getImage(getClass().getResource("images/6.gif"));
//		imgs[7] = getToolkit().getImage(getClass().getResource("images/7.gif"));
//		imgs[8] = getToolkit().getImage(getClass().getResource("images/8.gif"));
//		imgs[9] = getToolkit().getImage(getClass().getResource("images/9.gif"));
//		imgs[10] = getToolkit().getImage(getClass().getResource("images/k.gif"));
//		imgs[11] = getToolkit().getImage(getClass().getResource("images/corner.gif"));

		MediaTracker mt = new MediaTracker(this);
		for (int i = 0; i < imgs.length; i++) {
			mt.addImage(imgs[i], i);
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

			trayIcon = new TrayIcon(imgs[0], "Clocks", createPopupMenu());
		    trayIcon.setImageAutoSize(true);

		    try {
		        tray.add(trayIcon);
		    } catch (AWTException e) {
		        System.err.println("TrayIcon could not be added.");
		    }
		}
		
		clocks = Configuration.getClocks();
		location = Configuration.get("location");

		height = clocks.size()*DHEIGHT+20;
		img = createImage(WIDTH, height);
		g = (Graphics2D)img.getGraphics();

		setLocation(false);
		setSize(WIDTH, height);

		addMouseListener(this);
	}
	
	private PopupMenu createPopupMenu(){
		PopupMenu popup = new PopupMenu();
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
		paintClocks(arg0);
	}

	private void paintClocks(Graphics arg0){
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
			layout = new TextLayout(c.name, font, frc);
			layout.draw(g, (float) (WIDTH/2-layout.getBounds().getCenterX()), (float)y);
			//draw clock time
			layout = new TextLayout(time, font, frc);
			layout.draw(g, (float) (WIDTH/2-layout.getBounds().getCenterX()), (float) y+25);
			//title and tooltip
			sb.append("\n"+c.name+" "+time);
		}
		arg0.drawImage(img, 0, 0, null);
	    trayIcon.setToolTip(sb.toString());
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
			int hidden = Configuration.getInt("hidden", 5);
			if("left".equals(location)) {
				setLocation(hidden-WIDTH, ss.height/2-height/2);
			} else if("right".equals(location)) {
				setLocation(ss.width-hidden, ss.height/2-height/2);
			} else if("top".equals(location)) {
				setLocation(ss.width/2-WIDTH/2, hidden-height);
			} else if("bottom".equals(location)) {
				setLocation(ss.width/2-WIDTH/2, ss.height-hidden);
			}else{
				setLocation(ss.width, ss.height);
			}
		}
	}

	public static void main(String[] args) {
		new Clocks();
	}

}
