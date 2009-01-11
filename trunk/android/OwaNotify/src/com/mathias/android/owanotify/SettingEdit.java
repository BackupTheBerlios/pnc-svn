package com.mathias.android.owanotify;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class SettingEdit extends Activity {
	
	private static final String[] freqstrings = new String[]{"15 min", "30 min", "1 hour", "8 hour", "24 hour"};
	private static final long[] freqvalues = new long[]{15*60000, 30*60000, 60*60000, 180*60000, 480*60000, 1440*60000};

	private ViewHolder holder = new ViewHolder();
	
	private DbAdapter dbHelper;

    private PendingIntent mAlarmSender;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.settings);
		
        mAlarmSender = PendingIntent.getService(SettingEdit.this,
                0, new Intent(SettingEdit.this, OwaService.class), 0);

        dbHelper = new DbAdapter(this);
        dbHelper.open();

		final ArrayAdapter<String> freqAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, freqstrings);
		freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// Find views
		holder.service = (CheckBox) findViewById(R.id.service);
		holder.frequency = (Spinner) findViewById(R.id.frequency);
		holder.frequency.setAdapter(freqAdapter);
		holder.internalviewer = (CheckBox) findViewById(R.id.internalviewer);
		holder.checkmail = (CheckBox) findViewById(R.id.checkmail);
		holder.checkcalendar = (CheckBox) findViewById(R.id.checkcalendar);
		holder.alwaysshowmailcount = (CheckBox) findViewById(R.id.alwaysshowmailcount);
		holder.url = (EditText) findViewById(R.id.url);
		holder.name = (EditText) findViewById(R.id.name);
		holder.inbox = (EditText) findViewById(R.id.inbox);
		holder.calendar = (EditText) findViewById(R.id.calendar);
		holder.username = (EditText) findViewById(R.id.username);
		holder.password = (EditText) findViewById(R.id.password);
		holder.save = (Button) findViewById(R.id.save);
		holder.save.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// Store settings
				dbHelper.setSetting(Setting.URL, holder.url.getText());
				dbHelper.setSetting(Setting.NAME, holder.name.getText());
				dbHelper.setSetting(Setting.INBOX, holder.inbox.getText());
				dbHelper.setSetting(Setting.CALENDAR, holder.calendar.getText());
				dbHelper.setSetting(Setting.USERNAME, holder.username.getText());
				dbHelper.setSetting(Setting.PASSWORD, holder.password.getText().toString());
				dbHelper.setSetting(Setting.FULLINBOXURL, getInboxUrl(holder));
				dbHelper.setSetting(Setting.FULLCALENDARURL, getCalendarUrl(holder));
				long interval = freqvalues[holder.frequency.getSelectedItemPosition()];
				dbHelper.setSetting(Setting.FREQUENCY, interval);
				dbHelper.setSettingBool(Setting.INTERNALVIEWER, holder.internalviewer.isChecked());
				dbHelper.setSettingBool(Setting.SERVICE, holder.service.isChecked());
				dbHelper.setSettingBool(Setting.CHECKMAIL, holder.checkmail.isChecked());
				dbHelper.setSettingBool(Setting.CHECKCALENDAR, holder.checkcalendar.isChecked());
				dbHelper.setSettingBool(Setting.ALWAYSSHOWMAILCOUNT, holder.alwaysshowmailcount.isChecked());
				
				//set alarm
				if(holder.service.isChecked()){
		            long firstTime = SystemClock.elapsedRealtime();
		            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		            am.setRepeating(AlarmManager.ELAPSED_REALTIME,
		                            firstTime, interval, mAlarmSender);
				}else{
					//Cancel alarm
		            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		            am.cancel(mAlarmSender);

					Intent i = new Intent(SettingEdit.this, OwaMailView.class);
					startActivity(i);
				}
				finish();
			}
		});

		// Load settings
		String url = dbHelper.getSetting(Setting.URL);
		if(url != null){
			holder.url.setText(url);
		}
		String name = dbHelper.getSetting(Setting.NAME);
		if(name != null){
			holder.name.setText(name);
		}
		String inbox = dbHelper.getSetting(Setting.INBOX);
		if(inbox != null){
			holder.inbox.setText(inbox);
		}
		String calendar = dbHelper.getSetting(Setting.CALENDAR);
		if(calendar != null){
			holder.calendar.setText(calendar);
		}
		String username = dbHelper.getSetting(Setting.USERNAME);
		if(username != null){
			holder.username.setText(username);
		}
		String password = dbHelper.getSetting(Setting.PASSWORD);
		if(password != null){
			holder.password.setText(password);
		}
		holder.service.setChecked(dbHelper.getSettingBool(Setting.SERVICE));
		holder.internalviewer.setChecked(dbHelper.getSettingBool(Setting.INTERNALVIEWER));
		holder.checkmail.setChecked(dbHelper.getSettingBool(Setting.CHECKMAIL));
		holder.checkcalendar.setChecked(dbHelper.getSettingBool(Setting.CHECKCALENDAR));
		holder.alwaysshowmailcount.setChecked(dbHelper.getSettingBool(Setting.ALWAYSSHOWMAILCOUNT));
		
		String freq = dbHelper.getSetting(Setting.FREQUENCY);
		int freqval = (freq != null ? Integer.parseInt(freq) : 0);
		int count = freqvalues.length;
		for (int i = 0; i < count; i++) {
			if(freqval == freqvalues[i]){
				holder.frequency.setSelection(i);
				break;
			}
		}
    }
	
	@Override
	protected void onDestroy() {
		dbHelper.close();
		dbHelper = null;
		super.onDestroy();
	}

	class ViewHolder {
		CheckBox service;
		Spinner frequency;
		CheckBox internalviewer;
		CheckBox checkmail;
		CheckBox checkcalendar;
		CheckBox alwaysshowmailcount;
		EditText url;
		EditText name;
		EditText inbox;
		EditText calendar;
		EditText username;
		EditText password;
		Button save;
	}

	private static String getInboxUrl(ViewHolder holder){
		StringBuilder sb = new StringBuilder();
		sb.append(holder.url.getText().toString());
		sb.append(holder.name.getText().toString());
		sb.append(holder.inbox.getText().toString());
		return sb.toString();
	}

	private static String getCalendarUrl(ViewHolder holder){
		StringBuilder sb = new StringBuilder();
		sb.append(holder.url.getText().toString());
		sb.append(holder.name.getText().toString());
		sb.append(holder.calendar.getText().toString());
		return sb.toString();
	}

}
