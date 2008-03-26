package com.mathias.clocks;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JTextField;

import com.mathias.drawutils.Audio;
import com.mathias.drawutils.FormDialog;
import com.mathias.drawutils.Util;


@SuppressWarnings("serial")
public class TimerDialog extends FormDialog {

	private JTextField timer = new JTextField(20);
	
	private TimerTask task = null;

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
		long seconds = getTime(timer.getText());
		task = new TimerTask(){
			@Override
			public void run() {
				Audio.play(TimerDialog.class.getResource("resources/alarm.au"));
			}
		};
		new Timer().schedule(task, seconds*1000);
		return true;
	}
	
	public long getTimerTime(){
		return (task != null ? task.scheduledExecutionTime() : 0);
	}

	private long getTime(String timeStr){
		long ret = 0;
		try{
			String[] t = Util.split(timeStr, ':');
			ret += Integer.parseInt(t[0]);
			if(t.length >= 2){
				ret += Integer.parseInt(t[1])*60;
			}
			if(t.length >= 3){
				ret += Integer.parseInt(t[2])*60*60;
			}
		}catch(NumberFormatException e){
			ret = 0;
		}
		return ret;
	}

}
