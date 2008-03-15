package com.mathias.clocks;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.mathias.drawutils.FormDialog;
import com.mathias.drawutils.Util;


@SuppressWarnings("serial")
public class SettingsDialog extends FormDialog {
	
	/*
##clock1-clock10, Name, TimeZone
clock1=Stockholm, CET
clock2=Atlanta, US/Eastern
clock3=Dehli, IST
#clock4=Yahoo, YahooTimeZone
##none, right, left, top, bottom
location=left
#autohide=false
#overlap=5
#hidden=false
#ontop=true
#seconds=true
#font=Arial
#fontsize
#systray=false
	 */

	private List<ClockComponent> clockFields = new ArrayList<ClockComponent>();
	private JComboBox location = new JComboBox();
	private JCheckBox autohide = new JCheckBox();
	private JComboBox overlap = new JComboBox();
	private JCheckBox hidden = new JCheckBox();
	private JCheckBox ontop = new JCheckBox();
	private JCheckBox seconds = new JCheckBox();
	private JComboBox font = new JComboBox();
	private JComboBox fontsize = new JComboBox();
	private JCheckBox systray = new JCheckBox();
	
	private Clocks clocks;

	public SettingsDialog(Clocks clocks) {
		super("Settings", false);
		
		this.clocks = clocks;

		initUI();
	}

	@Override
	protected void setupForm() {
		List<Clock> clocks = Configuration.getClocks();
		for (Clock clock : clocks) {
			ClockComponent nameCombo = new ClockComponent(clock.getIndex(),
					clock.getName(), clock.getTimeZone().getID());
			clockFields.add(nameCombo);
			addItem(nameCombo);
		}
		ClockComponent nameCombo = new ClockComponent(clocks.size()+1, "", "");
		clockFields.add(nameCombo);
		addItem(nameCombo);

		location.addItem("none");
		location.addItem("left");
		location.addItem("right");
		location.addItem("top");
		location.addItem("bottom");
		location.setSelectedItem(Configuration.get("location"));
		addItem("Location", location);
		autohide.setSelected(Configuration.getBoolean("autohide", true));
		addItem("Autohide", autohide);
		for (int i = 0; i < 10; i++) {
			overlap.addItem(i);
		}
		overlap.setSelectedItem(Configuration.getInt("overlap", 5));
		addItem("Overlap", overlap);
		hidden.setSelected(Configuration.getBoolean("hidden", true));
		addItem("Hidden", hidden);
		ontop.setSelected(Configuration.getBoolean("ontop", true));
		addItem("On top", ontop);
		seconds.setSelected(Configuration.getBoolean("seconds", false));
		addItem("Seconds", seconds);
		Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		for (Font f : fonts) {
			font.addItem(f.getName());
		}
		font.setSelectedItem(Configuration.get("font"));
		addItem("Font", font);
		for (int i = 10; i <= 60; i+=10) {
			fontsize.addItem(i);
		}
		fontsize.setSelectedItem(Configuration.getInt("fontsize", 20));
		addItem("Font size", fontsize);
		systray.setSelected(Configuration.getBoolean("systray", false));
		addItem("Systray", systray);
	}

	@Override
	protected boolean validateDialog() {
		for (ClockComponent nc : clockFields) {
			if(nc.isComplete()){
				Configuration.set(nc.getKey(), nc.getValue());
			}else{
				Configuration.remove(nc.getKey());
			}
		}
		Configuration.set("location", ""+location.getSelectedItem());
		Configuration.set("autohide", ""+autohide.isSelected());
		Configuration.set("overlap", ""+overlap.getSelectedItem());
		Configuration.set("hidden", ""+hidden.isSelected());
		Configuration.set("ontop", ""+ontop.isSelected());
		Configuration.set("seconds", ""+seconds.isSelected());
		Configuration.set("font", ""+font.getSelectedItem());
		Configuration.set("fontsize", ""+fontsize.getSelectedItem());
		Configuration.set("systray", ""+systray.isSelected());
		Configuration.store();
		clocks.init();
		return true;
	}

	private class ClockComponent extends JPanel {
		private int index;
		private JTextField name = new JTextField(20);
		private JComboBox timeZone = new JComboBox();
		
		private ClockComponent(int index, String clockName, String timeZoneName){
			setLayout(new GridLayout(3, 1));

			this.index = index;
			name.setText(clockName);
			for (String tz : Configuration.TIMEZONES) {
				timeZone.addItem(tz);
			}
			timeZone.setSelectedItem(timeZoneName);

			add(new JLabel("Clock"+index));
			add(name);
			add(timeZone);
		}
		
		private boolean isComplete(){
			return !Util.isEmpty(name.getText()) && !Util.isEmpty(timeZone.getSelectedItem());
		}
		
		private String getKey(){
			return "clock"+index;
		}
		
		private String getValue(){
			return name.getText()+", "+timeZone.getSelectedItem();
		}
	}

}
