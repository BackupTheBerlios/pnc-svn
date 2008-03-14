package com.mathias.clocks;

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
		int counter;
		try{
			counter = Integer.parseInt(timer.getText());
		}catch(NumberFormatException e){
			return false;
		}
		new Timer(counter).start();
		return true;
	}

	class Timer extends Thread {
		private int counter;
		
		Timer(int counter){
			this.counter = counter;
		}
		@Override
		public void run() {
			while(true){
				doSleep(1000);
				counter--;
				if(counter <= 0){
					Audio.play("warp.au");
					System.out.println("Alarm!");
					break;
				}
			}
		}
	}

	private static void doSleep(long millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}

}
