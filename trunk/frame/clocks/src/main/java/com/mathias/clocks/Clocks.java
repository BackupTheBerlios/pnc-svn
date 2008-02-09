package com.mathias.clocks;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class Clocks extends JFrame implements MouseMotionListener {

	private List<Clock> clocks;
	private Image[] imgs;
	private Image img;
	private Graphics2D g;
	private GregorianCalendar gc;
	private String location;
	private TrayIcon trayIcon = null;

	public Clocks(){
		setUndecorated(true);
		setSize(180, 400);
		setAlwaysOnTop(true);
		setLocation(false);
		setVisible(true);

		imgs = new Image[11];
		imgs[0] = getToolkit().getImage(getClass().getResource("images/0.gif"));
		imgs[1] = getToolkit().getImage(getClass().getResource("images/1.gif"));
		imgs[2] = getToolkit().getImage(getClass().getResource("images/2.gif"));
		imgs[3] = getToolkit().getImage(getClass().getResource("images/3.gif"));
		imgs[4] = getToolkit().getImage(getClass().getResource("images/4.gif"));
		imgs[5] = getToolkit().getImage(getClass().getResource("images/5.gif"));
		imgs[6] = getToolkit().getImage(getClass().getResource("images/6.gif"));
		imgs[7] = getToolkit().getImage(getClass().getResource("images/7.gif"));
		imgs[8] = getToolkit().getImage(getClass().getResource("images/8.gif"));
		imgs[9] = getToolkit().getImage(getClass().getResource("images/9.gif"));
		imgs[10] = getToolkit().getImage(getClass().getResource("images/k.gif"));		

		MediaTracker mt = new MediaTracker(this);
		for (int i = 0; i < imgs.length; i++) {
			mt.addImage(imgs[i], i);			
		}
		try {
			mt.waitForAll();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if(SystemTray.isSupported()){
			SystemTray tray = SystemTray.getSystemTray();

		    ActionListener exitListener = new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		            System.exit(0);
		        }
		    };
		            
		    PopupMenu popup = new PopupMenu();
		    MenuItem defaultItem = new MenuItem("Exit");
		    defaultItem.addActionListener(exitListener);
		    popup.add(defaultItem);

		    trayIcon = new TrayIcon(imgs[0], "Clocks", popup);
		    trayIcon.setImageAutoSize(true);

		    try {
		        tray.add(trayIcon);
		    } catch (AWTException e) {
		        System.err.println("TrayIcon could not be added.");
		    }
		}
		
		clocks = Configuration.getClocks();
		location = Configuration.get("location");

		img = createImage(getWidth(), getHeight());
		g = (Graphics2D)img.getGraphics();
		gc = new GregorianCalendar();

		Animate ani = new Animate();
		ani.setDaemon(true);
		ani.start();
		
		addMouseMotionListener(this);		
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			System.exit(0);
		}
	}

	private class Animate extends Thread {
		@Override
		public void run() {
			long delay = 500;
			try{
				delay = Long.parseLong(Configuration.get("delay"));
			}catch(NumberFormatException e){
			}
			while (true) {
				paintClocks();
				if(getMousePosition() == null){
					setLocation(false);
				}
				Clocks.sleep(delay);
			}
		}
	}

	private void paintClocks(){
		StringBuilder sb = new StringBuilder();
		sb.append("Clocks\n");
		Iterator<Clock> it = clocks.iterator();
		for (int i = 0; it.hasNext(); i++) {
			Clock c = it.next();
			paintClock(c, i*70, false);			
			sb.append(c.name+" "+getTime()+"\n");
		}
	    trayIcon.setToolTip(sb.toString());
	}
	
	private String getTime(){
		int h = gc.get(Calendar.HOUR_OF_DAY);
		int m = gc.get(Calendar.MINUTE);
		return String.format("%02d:%02d", h, m);
	}

	private void paintClock(Clock clock, int y, boolean seconds){
		gc.setTimeZone(clock.timeZone);
		gc.setTime(new Date());
		int x = 0;
		final int w = 18;
		final int h = gc.get(Calendar.HOUR_OF_DAY);
		final int m = gc.get(Calendar.MINUTE);
		final int s = gc.get(Calendar.SECOND);
		
		g.clearRect(0, y, getWidth(), y+60);
		g.drawImage(imgs[h/10], (x++)*w, y, null);
		g.drawImage(imgs[h%10], (x++)*w, y, null);
		g.drawImage(imgs[10], (x++)*w, y, null);
		g.drawImage(imgs[m/10], (x++)*w, y, null);
		g.drawImage(imgs[m%10], (x++)*w, y, null);
		if(seconds){
			g.drawImage(imgs[10], (x++)*w, y, null);
			g.drawImage(imgs[s/10], (x++)*w, y, null);
			g.drawImage(imgs[s%10], (x++)*w, y, null);
		}
		
		g.drawString(clock.name, 10, y+60);

		getContentPane().getGraphics().drawImage(img, 0, 0, null);
	}

	private static void sleep(long millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void mouseDragged(MouseEvent ev) {
	}

	public void mouseMoved(MouseEvent ev) {
		setLocation(true);
		paintClocks();
	}

	private void setLocation(boolean visible){
		Dimension ss = getToolkit().getScreenSize();
		if(visible){
			if("left".equals(location)) {
				setLocation(0, ss.height/2-getHeight()/2);
			} else if("right".equals(location)) {
				setLocation(ss.width-getWidth(), ss.height/2-getHeight()/2);
			} else if("top".equals(location)) {
				setLocation(ss.width/2-getWidth()/2, 0);
			} else if("bottom".equals(location)) {
				setLocation(ss.width/2-getWidth()/2, ss.height-getHeight());
			}else{
				setLocation(ss.width, ss.height);
			}			
		}else{
			if("left".equals(location)) {
				setLocation(5-getWidth(), ss.height/2-getHeight()/2);
			} else if("right".equals(location)) {
				setLocation(ss.width-5, ss.height/2-getHeight()/2);
			} else if("top".equals(location)) {
				setLocation(ss.width/2-getWidth()/2, 5-getHeight());
			} else if("bottom".equals(location)) {
				setLocation(ss.width/2-getWidth()/2, ss.height-5);
			}else{
				setLocation(ss.width, ss.height);
			}
		}
	}

	public static void main(String[] args) {
		new Clocks();
	}

}
