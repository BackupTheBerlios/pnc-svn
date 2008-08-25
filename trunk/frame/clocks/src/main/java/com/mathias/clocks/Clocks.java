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
import java.awt.Robot;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import javax.swing.JWindow;

import com.mathias.clocks.action.ExitAction;
import com.mathias.clocks.action.SettingsAction;
import com.mathias.clocks.action.TimerAction;
import com.mathias.drawutils.GenericDialog;

@SuppressWarnings("serial")
public class Clocks extends JWindow implements MouseListener {
	
//	private final static Logger log = Logger.getLogger(Clocks.class.getName());

	private List<Clock> clocks;
	private Map<Integer, Image> imgs;
	private Image img;
	private Graphics2D g;
	private String location;
	private TrayIcon trayIcon = null;
	private Font font;
	private int height;
	private Robot robot;
	private TimerAction ta = null;
	private Configuration conf;

	private final static int WIDTH = 130;
	private final static int DHEIGHT = 55;
	private final static Color TEXTCOLOR = new Color(75, 75, 255);
	
//	private final static int IMG_0 = 0;
	private final static int IMG_ICO = 11;

	public Clocks(){
//		System.out.println("cons");

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

		setVisible(true);

		init();

		addMouseListener(this);

		try {
			robot = new Robot();
		} catch (AWTException e1) {
			e1.printStackTrace();
		}

		final JWindow win = this;
		new Timer().schedule(new TimerTask(){
			@Override
			public void run() {
				paintClocks();
				win.setAlwaysOnTop(conf.getAlwaysOnTop());
			}
		}, 1000, 1000);
	}
	
	public void init(){
//		System.out.println("init");
		
		conf = new Configuration("clocks.properties");
		
		setAlwaysOnTop(conf.getAlwaysOnTop());

		int fontSize = conf.getFontSize();
		String fn = conf.getFont();
		Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		for (Font f : fonts) {
			if(fn.equals(f.getName())){
				font = f.deriveFont(Font.PLAIN, fontSize);
				break;
			}
		}
		if(font == null){
			GenericDialog.showErrorDialog("Clocks", "Could not find font: "+fn);
			font = fonts[0];
		}

		if(SystemTray.isSupported()){
		    if(conf.getSystray()){
				SystemTray tray = SystemTray.getSystemTray();
				
				if(tray.getTrayIcons().length == 0){
					trayIcon = new TrayIcon(imgs.get(IMG_ICO), "Clocks", createPopupMenu());
				    trayIcon.setImageAutoSize(true);

				    try {
				        tray.add(trayIcon);
				    } catch (AWTException e) {
				        System.err.println("TrayIcon could not be added.");
				    }
				}
			}else{
				for (TrayIcon icon : SystemTray.getSystemTray().getTrayIcons()) {
					SystemTray.getSystemTray().remove(icon);
				}
			}
		}
		
		clocks = conf.getClocks();
		location = conf.getLocation();

		height = clocks.size()*DHEIGHT+40;
		img = createImage(WIDTH, height);
		g = (Graphics2D)img.getGraphics();

		setLocation(false);
		setSize(WIDTH, height);
	}
	
	private PopupMenu createPopupMenu(){
		PopupMenu popup = new PopupMenu();
		MenuItem timerItem = new MenuItem("Timer...");
		ta = new TimerAction();
		timerItem.addActionListener(ta);
		popup.add(timerItem);
		popup.addSeparator();
		MenuItem settingsItem = new MenuItem("Settings...");
		settingsItem.addActionListener(new SettingsAction(this));
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
	public void update(Graphics g) {
//		System.out.println("update");

		super.update(g);

		paintClocks();
	}
	
	@Override
	public void paint(Graphics arg0) {
//		System.out.println("paint");

		super.paint(arg0);
		paintClocks();
	}

	private void paintClocks(){
//		System.out.println("alsdkjajsdkajs");

		BufferedImage cap = robot.createScreenCapture(getBounds());
		g.drawImage(cap, 0, 0, null);

		g.setColor(TEXTCOLOR);
		g.fillRoundRect(0, 0, WIDTH, height, 20, 20);
		g.setColor(Color.white);
		g.fillRoundRect(10, 10, WIDTH-20, height-20, 20, 20);

		StringBuilder sb = new StringBuilder();
		sb.append("Clocks");
		Iterator<Clock> it = clocks.iterator();
		g.setColor(TEXTCOLOR);
		FontRenderContext frc = g.getFontRenderContext();
		boolean seconds = conf.getSeconds();
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

		if(ta != null && ta.td != null){
			long time = ta.td.getTimerTime();
			long millis = time - System.currentTimeMillis();
			if(millis > 999){
				new TextLayout(getTime(millis),
						font.deriveFont(Font.PLAIN, 10), frc).draw(g,
						(float) 15, height - 20);
			}
		}

		getContentPane().getGraphics().drawImage(img, 0, 0, null);
		if(trayIcon != null){
		    trayIcon.setToolTip(sb.toString());
		}
	}
	
	private String getTime(long millis){
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(new Date(millis));
		int h = gc.get(Calendar.HOUR_OF_DAY)-19;
		int m = gc.get(Calendar.MINUTE);
		int s = gc.get(Calendar.SECOND);
		return String.format("%02d:%02d:%02d", h, m, s);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON3){
			PopupMenu pm = createPopupMenu();
			add(pm);
			pm.show(e.getComponent(), e.getX(), e.getY());
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
		if(visible || !conf.getAutohide()){
			//Show clocks
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
			if(conf.getHidden()){
				setVisible(true);
			}
		}else{
			//Auto hide clocks
			int overlap = conf.getOverlap();
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
			if(conf.getHidden()){
				setVisible(false);
			}
		}
	}

	public Configuration getConf() {
		return conf;
	}

	public static void main(String[] args) {
		new Clocks();
	}

}
