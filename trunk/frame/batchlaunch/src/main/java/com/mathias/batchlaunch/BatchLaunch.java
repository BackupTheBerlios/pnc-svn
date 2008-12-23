package com.mathias.batchlaunch;

import java.awt.FlowLayout;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.UIManager;

import com.mathias.batchlaunch.actions.ExecuteAction;
import com.mathias.batchlaunch.actions.ExitAction;
import com.mathias.drawutils.frame.BasicFrame;

@SuppressWarnings("serial")
public class BatchLaunch extends BasicFrame {
	
	public BatchLaunch(){
		super("BatchLaunch", 100, 100, 100, 300);
	}

	@Override
	protected void init() {
		try {
			setLayout(new FlowLayout());
			BatchList list = new BatchList();
			Properties prop = new Properties();
			prop.load(new FileInputStream("batchlaunch.properties"));

			for (int i = 0; i < 100; i++) {
				String val = prop.getProperty("command"+i);
				if(val != null && val.length() > 0){
					String[] split = val.split(",");
					if(split.length > 2){
						list.add(new BatchItem(split[0], split[1], split[2]));
					}else if(split.length > 1){
						list.add(new BatchItem(split[0], split[1], null));
					}else{
						list.add(new BatchItem(val, val, null));
					}
//					System.out.println("added: "+val);
				}
			}
			getContentPane().add(list);
			JButton button = createButton(new ExecuteAction("Launch", list));
			getContentPane().add(button);
			getContentPane().add(createButton(new ExitAction("Exit")));
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException "+e.getMessage());
			throw new RuntimeException(e);
		} catch (IOException e) {
			System.out.println("IOException "+e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// Do nothing...
		}
		new BatchLaunch();
	}

}
