package com.mathias.imageview;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JWindow;

import com.mathias.drawutils.FormDialog;

public class ImageView extends JFrame implements KeyListener {
	
	private boolean fullscreen = false;
	
	private boolean aspectRatio = true;
	
	private JPanel panel;
	
	private JWindow fsWin;
	
	private Image image;
	
	private String[] imageNames;
	
	private int imagePtr = 0;

	public ImageView(String[] args) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        if(args.length == 0){
			File cwd = new File(System.getProperty("user.dir"));
			imageNames = cwd.list(new ImageFileFilter());
			if(imageNames.length == 0){
				imageNames = new String[]{"clouds.jpg"};
			}
		}else{
			imageNames = args;
		}

		// Determine if full-screen mode is supported directly
	    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    GraphicsDevice gs = ge.getDefaultScreenDevice();
	    if (gs.isFullScreenSupported()) {
	        // Full-screen mode is supported
	    } else {
	        // Full-screen mode will be simulated
	    }

	    panel = new JPanel(){
	    	@Override
	    	public void paint(Graphics g) {
	    		super.paint(g);
	    		paintScreen((Graphics2D)g);
	    	}
	    	@Override
	    	public void update(Graphics g) {
	    		super.update(g);
	    		paintScreen((Graphics2D)g);
	    	}
	    };
	    getContentPane().add(panel);

        fsWin = new JWindow(this){
	    	@Override
	    	public void paint(Graphics g) {
	    		super.paint(g);
	    		paintScreen((Graphics2D)g);
	    	}
	    	@Override
	    	public void update(Graphics g) {
	    		super.update(g);
	    		paintScreen((Graphics2D)g);
	    	}
        };

	    addKeyListener(this);

		setTitle(imageNames[0]);

   		openImage(imageNames[0]);

	    setVisible(true);
	}
	
	public static BufferedImage shrink(BufferedImage source, double factor) {
        int w = (int) (source.getWidth() * factor);
        int h = (int) (source.getHeight() * factor);
        Image image = source.getScaledInstance(w, h, Image.SCALE_AREA_AVERAGING);
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return result;
    }
	
	private void openImage(String filename){
	    try {
	        File file = new File(filename);
	        image = ImageIO.read(file);
	        panel.setPreferredSize(new Dimension(image.getWidth(this), image.getHeight(this)));
			pack();
	        fsWin.repaint();
	    } catch (IOException e) {
			System.out.println("IOException: "+e.getMessage());
	    }
	}
	
	private void closeFullscreen(){
		System.out.println("closeFs");
		// Return to normal windowed mode
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gs = ge.getDefaultScreenDevice();

        gs.setFullScreenWindow(null);
        
        fullscreen = false;
	}
	
	private void openFullscreen(){
		System.out.println("openFs");
		if(fullscreen){
			return;
		}

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gs = ge.getDefaultScreenDevice();

        // Enter full-screen mode
        gs.setFullScreenWindow(fsWin);
        fsWin.validate();
        
        fsWin.repaint();

		fullscreen = true;
	}

//	@Override
//	public void paint(Graphics g) {
//		System.out.println("paint");
//		super.paint(g);
//		paintScreen((Graphics2D)g);
//	}
//	
//	@Override
//	public void update(Graphics g) {
//		super.update(g);
//		paintScreen((Graphics2D)g);
//	}
	
	private void paintScreen(Graphics2D g){
		int iw = image.getWidth(this);
		int ih = image.getHeight(this);
		int w = g.getClipBounds().width;
		int h = g.getClipBounds().height;
		if(aspectRatio){
//			do{
				h = w*ih/iw;
//			} while(iw > w || ih > h);
		}
		g.drawImage(image.getScaledInstance(w, h, Image.SCALE_FAST), 0, 0, this);
	}

	@Override
	public void keyPressed(KeyEvent k) {
//		System.out.println("pressed: "+k.getKeyChar());
	}

	@Override
	public void keyReleased(KeyEvent k) {
//		System.out.println("released: "+k.getKeyChar());
	}

	@Override
	public void keyTyped(KeyEvent k) {
//		System.out.println("typed: "+k.getKeyChar());
		if(k.getKeyChar() == 'f'){
			if(fullscreen){
				closeFullscreen();
			}else{
				openFullscreen();
			}
		}else if(k.getKeyChar() == 'q'){
			System.exit(0);
		}else if(k.getKeyChar() == 'o'){
			new ImageViewOptions();
		}else if(k.getKeyChar() == 'i'){
			//TODO show information
		}else if(k.getKeyChar() == 'n'){
			if(++imagePtr >= imageNames.length){
				imagePtr = 0;
			}
			openImage(imageNames[imagePtr]);
		}else if(k.getKeyChar() == 'p'){
			if(--imagePtr < 0){
				imagePtr = imageNames.length - 1;
			}
			openImage(imageNames[imagePtr]);
		}
	}

	private class ImageViewOptions extends FormDialog {

		private JCheckBox aspectRatio = new JCheckBox();

		public ImageViewOptions() {
			super("ImageViewOptions", true);
		}

		@Override
		protected void setupForm() {
			addItem(aspectRatio);
		}

		@Override
		protected boolean validateDialog() {
			return true;
		}

	}

	public static void main(String[] args) {
		new ImageView(args);
	}

	public class ImageFileFilter implements FilenameFilter {

		@Override
		public boolean accept(File file, String filename) {
			String[] formatNames = ImageIO.getReaderFormatNames();
			formatNames = unique(formatNames);
			for (String fn : formatNames) {
				if(filename.endsWith("."+fn)){
//					System.out.println("Found: "+filename+" "+"   ."+fn);
					return true;
				}
			}
			return false;
		}
	}
	
	// Converts all strings in 'strings' to lowercase
    // and returns an array containing the unique values.
    // All returned values are lowercase.
	public static String[] unique(String[] strings) {
		Set<String> set = new HashSet<String>();
		for (int i = 0; i < strings.length; i++) {
			String name = strings[i].toLowerCase();
			set.add(name);
		}
		return set.toArray(new String[0]);
	}

}
