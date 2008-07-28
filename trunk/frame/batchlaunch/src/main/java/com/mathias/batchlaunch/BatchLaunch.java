package com.mathias.batchlaunch;

import javax.swing.JButton;
import javax.swing.UIManager;

import com.mathias.batchlaunch.actions.ExecuteAction;
import com.mathias.drawutils.frame.BasicFrame;

@SuppressWarnings("serial")
public class BatchLaunch extends BasicFrame {
	
	public BatchLaunch(){
		super("BatchLaunch");
	}

	@Override
	protected void init() {
		BatchList list = new BatchList();
		list.add(new BatchItem("Outlook", "\"C:\\Program Files\\Microsoft Office\\Office12\\OUTLOOK.EXE\"  /recycle"));
		list.add(new BatchItem("Outlook1", "\"C:\\Program Files\\Microsoft Office\\Office12\\OUTLOOK.EXE\"  /recycle"));
		list.add(new BatchItem("Outlook2", "\"C:\\Program Files\\Microsoft Office\\Office12\\OUTLOOK.EXE\"  /recycle"));
		list.add(new BatchItem("Outlook3", "\"C:\\Program Files\\Microsoft Office\\Office12\\OUTLOOK.EXE\"  /recycle"));
		list.add(new BatchItem("Outlook4", "\"C:\\Program Files\\Microsoft Office\\Office12\\OUTLOOK.EXE\"  /recycle"));
		list.add(new BatchItem("Outlook5", "\"C:\\Program Files\\Microsoft Office\\Office12\\OUTLOOK.EXE\"  /recycle"));
		list.add(new BatchItem("Outlook6", "\"C:\\Program Files\\Microsoft Office\\Office12\\OUTLOOK.EXE\"  /recycle"));
		list.add(new BatchItem("Outlook7", "\"C:\\Program Files\\Microsoft Office\\Office12\\OUTLOOK.EXE\"  /recycle"));
		list.add(new BatchItem("Outlook8", "\"C:\\Program Files\\Microsoft Office\\Office12\\OUTLOOK.EXE\"  /recycle"));
		add(list);
		JButton button = createButton(new ExecuteAction("Launch", list));
		add(button);
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
