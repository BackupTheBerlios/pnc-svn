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
	
	private Configuration conf;

	public SettingsDialog(Clocks clocks) {
		super("Settings", false);
		
		this.clocks = clocks;

		conf = clocks.getConf();

		initUI();
	}

	@Override
	protected void setupForm() {
		List<Clock> clocks = conf.getClocks();
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
		location.setSelectedItem(conf.getLocation());
		addItem("Location", location);
		autohide.setSelected(conf.getAutohide());
		addItem("Autohide", autohide);
		for (int i = 0; i < 10; i++) {
			overlap.addItem(i);
		}
		overlap.setSelectedItem(conf.getOverlap());
		addItem("Overlap", overlap);
		hidden.setSelected(conf.getHidden());
		addItem("Hidden", hidden);
		ontop.setSelected(conf.getAlwaysOnTop());
		addItem("On top", ontop);
		seconds.setSelected(conf.getSeconds());
		addItem("Seconds", seconds);
		Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		for (Font f : fonts) {
			font.addItem(f.getName());
		}
		font.setSelectedItem(conf.getFont());
		addItem("Font", font);
		for (int i = 10; i <= 60; i+=10) {
			fontsize.addItem(i);
		}
		fontsize.setSelectedItem(conf.getFontSize());
		addItem("Font size", fontsize);
		systray.setSelected(conf.getSystray());
		addItem("Systray", systray);
	}

	@Override
	protected boolean validateDialog() {
		for (ClockComponent nc : clockFields) {
			if(nc.isComplete()){
				conf.set(nc.getKey(), nc.getValue());
			}else{
				conf.remove(nc.getKey());
			}
		}
		conf.setLocation(location.getSelectedItem());
		conf.setAutohide(autohide.isSelected());
		conf.setOverlap(overlap.getSelectedItem());
		conf.setHidden(hidden.isSelected());
		conf.setAlwaysOnTop(ontop.isSelected());
		conf.setSeconds(seconds.isSelected());
		conf.setFont(font.getSelectedItem());
		conf.setFontSize(fontsize.getSelectedItem());
		conf.setSystray(systray.isSelected());
		conf.store();
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
