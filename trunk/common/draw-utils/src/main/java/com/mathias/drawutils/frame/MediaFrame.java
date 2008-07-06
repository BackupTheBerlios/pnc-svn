package com.mathias.drawutils.frame;

import java.awt.FlowLayout;

import javax.swing.JFrame;

public abstract class MediaFrame extends JFrame {

	public MediaFrame(int left, int top, int width, int height){
		init();
		
		setLayout(new FlowLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocation(left, top);
		setSize(width, height);
		setVisible(true);
	}

	protected abstract void init();

//	@Override
//	protected void processWindowEvent(WindowEvent e) {
//		super.processWindowEvent(e);
//		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
//			System.exit(0);
//		}
//	}

}
