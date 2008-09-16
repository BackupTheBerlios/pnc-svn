package com.mathias.drawutils.frame;

import java.awt.Insets;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;

import com.mathias.drawutils.Util;

public abstract class BasicFrame extends JFrame {

	public BasicFrame(String title){

//		setLayout(new FlowLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle(title);

		init();

		Util.centerFrame(this);
		pack();

		setVisible(true);
	}

	public BasicFrame(String title, int width, int height){
		setSize(width, height);
//		setLayout(new FlowLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle(title);

		init();

		Util.centerFrame(this);

		setVisible(true);
	}

	public BasicFrame(String title, int left, int top, int width, int height){
		setSize(width, height);
//		setLayout(new FlowLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle(title);

		setLocation(left, top);
		
		init();

		setVisible(true);
	}

	protected abstract void init();

	protected JButton createButton(Action action) {
		JButton button = new JButton(action);
		if(action.getValue(Action.SMALL_ICON) == null){
			button.setText((String)action.getValue(Action.NAME));
		}
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setFocusable(false);
		return button;
	}

}
