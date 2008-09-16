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
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JWindow;

import com.mathias.drawutils.FormDialog;

@SuppressWarnings("serial")
public class ImageView extends JFrame implements KeyListener {
	
	private boolean fullscreen = false;
	
	private boolean aspectRatio = true;
	
	private JPanel panel;
	
	private JWindow fsWin;
	
	private BufferedImage image;
	
	private File[] imageFiles;
	
	private int imagePtr = 0;

	public ImageView(String[] args) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        if(args.length == 1){
        	imageFiles = getSiblingImageFiles(new File(args[0]));
		}
        
        if(imageFiles == null){
        	imageFiles = fileDialog(null);
        	if(imageFiles == null){
    			System.exit(0);
        	}
        }
        
        for (int i = 0; i < imageFiles.length; i++) {
        	if(args != null && args.length > 0 && imageFiles[i].getAbsolutePath().equals(args[0])){
        		imagePtr = i;
        		break;
        	}
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

   		openImage(imageFiles[imagePtr]);

	    setVisible(true);
	}
	
	private static File[] fileDialog(File cwd){
    	JFileChooser fc = new JFileChooser(cwd);
//    	fc.setFileFilter(new ImageFileFilter());
		int ret = fc.showOpenDialog(null);
		File sel = fc.getSelectedFile();
		if(JFileChooser.APPROVE_OPTION == ret && sel != null){
			return getSiblingImageFiles(sel);
		}else{
			return null;
		}
	}
	
	private static File[] getSiblingImageFiles(File file){
    	File cwd = file.getParentFile();
    	return cwd.listFiles(new ImageFilenameFilter());
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
	
	private void openImage(File file){
	    try {
	        image = ImageIO.read(file);
	        panel.setPreferredSize(new Dimension(image.getWidth(this), image.getHeight(this)));
//			setTitle(imageNames[0]);
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
		int sw = g.getClipBounds().width;
		int sh = g.getClipBounds().height;
		int nw = sw;
		int nh = sh;
		if(aspectRatio){
//			do{
				nh = sw*ih/iw;
				if(nh > sh){
					nh = sh;
					nw = sh*iw/ih;
				}
//				nw = nh*ih/iw;
//			} while(iw > w || ih > h);
		}
		g.drawImage(image.getScaledInstance(nw, nh, Image.SCALE_FAST), 0, 0, this);
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
			File[] files = fileDialog(imageFiles[imagePtr]);
			if(files != null){
				imageFiles = files;
			}
		}else if(k.getKeyChar() == 'i'){
			//TODO show information
			new ImageViewOptions();
		}else if(k.getKeyChar() == 'n'){
			if(++imagePtr >= imageFiles.length){
				imagePtr = 0;
			}
			openImage(imageFiles[imagePtr]);
		}else if(k.getKeyChar() == 'p'){
			if(--imagePtr < 0){
				imagePtr = imageFiles.length - 1;
			}
			openImage(imageFiles[imagePtr]);
		}else if(k.getKeyChar() == 'r'){
			rotateRight();
			repaint();
		}else if(k.getKeyChar() == 'l'){
			rotateLeft();
			repaint();
		}
	}
	
	private void rotateRight(){
		image = Util.rotate90DX(image);
	}

	private void rotateLeft(){
		image = Util.rotate90SX(image);
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

}
