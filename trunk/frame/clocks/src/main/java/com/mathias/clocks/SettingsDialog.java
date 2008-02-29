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
#undecorated=true
#font=Arial
#fontsize
#systray=false
	 */

	private JComboBox nClocks = new JComboBox();
	private List<NameCombo> clockFields = new ArrayList<NameCombo>();
	private JComboBox location = new JComboBox();
	private JCheckBox autohide = new JCheckBox();
	private JComboBox overlap = new JComboBox();
	private JCheckBox hidden = new JCheckBox();
	private JCheckBox ontop = new JCheckBox();
	private JCheckBox seconds = new JCheckBox();
	private JCheckBox undecorated = new JCheckBox();
	private JComboBox font = new JComboBox();
	private JComboBox fontsize = new JComboBox();
	private JCheckBox systray = new JCheckBox();

	public SettingsDialog() {
		super("Settings", false);

		initUI();
	}

	@Override
	protected void setupForm() {
		List<Clock> clocks = Configuration.getClocks();
		int count = 0;
		for (Clock clock : clocks) {
//			if(c != null){
				NameCombo nameCombo = new NameCombo(count++, clock, null);
				clockFields.add(nameCombo);
				addItem(nameCombo);
//			}
		}
		NameCombo nameCombo = new NameCombo(count, null, null);
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
		undecorated.setSelected(Configuration.getBoolean("undecorated", false));
		addItem("Undecorated", undecorated);
		Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		for (Font f : fonts) {
			font.addItem(f.getName());
		}
		addItem("Font", font);
		for (int i = 10; i <= 60; i+=10) {
			fontsize.addItem(i);
		}
		fontsize.setSelectedItem(Configuration.getInt("fontsize", 40));
		addItem("Font size", fontsize);
		systray.setSelected(Configuration.getBoolean("systray", false));
		addItem("Systray", systray);
	}

	@Override
	protected boolean validateDialog() {
		int count = 0;
		for (NameCombo nc : clockFields) {
			if(!Util.isEmpty(nc.name.getText()) && !Util.isEmpty(nc.timeZone.getName())){
				Configuration.set("clock"+(count++), nc.name.getText()+", "+nc.timeZone.getName());
			}
		}
		Configuration.set("location", ""+location.getSelectedItem());
		Configuration.set("autohide", ""+autohide.isSelected());
		Configuration.set("overlap", ""+overlap.getSelectedItem());
		Configuration.set("hidden", ""+hidden.isSelected());
		Configuration.set("ontop", ""+ontop.isSelected());
		Configuration.set("seconds", ""+seconds.isSelected());
		Configuration.set("undecorated", ""+undecorated.isSelected());
		Configuration.set("fontsize", ""+fontsize.getSelectedItem());
		Configuration.set("systray", ""+systray.isSelected());
		Configuration.store();
		return true;
	}

	class NameCombo extends JPanel {
		int index;
		JTextField name = new JTextField(20);
		JComboBox timeZone = new JComboBox();
		
		private NameCombo(int index, Clock clock, JPanel panel){
			setLayout(new GridLayout(3, 1));

			for (String tz : Configuration.TIMEZONES) {
				timeZone.addItem(tz);
			}
			if(clock != null){
				name.setText(clock.getName());
				timeZone.setSelectedItem(clock.getTimeZone().getDisplayName());
			}
			add(new JLabel("Clock"+index));
			add(name);
			add(timeZone);
		}
	}

}
