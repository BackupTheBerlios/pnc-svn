package com.mathias.clocks;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JTextField;

import com.mathias.drawutils.Audio;
import com.mathias.drawutils.FormDialog;


@SuppressWarnings("serial")
public class TimerDialog extends FormDialog {

	private JTextField timer = new JTextField(20);

	public TimerDialog() {
		super("Timer", false);
		initUI();
	}

	@Override
	protected void setupForm() {
		addItem("Timer: ", timer);
	}

	@Override
	protected boolean validateDialog() {
		long counter;
		try{
			counter = Integer.parseInt(timer.getText());
		}catch(NumberFormatException e){
			return false;
		}
		new Timer().schedule(new TimerTask(){
			@Override
			public void run() {
				Audio.play(TimerDialog.class.getResource("resources/alarm.au"));
			}
		}, counter*1000);
		return true;
	}

}
