package com.mathias.android.acast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.mathias.android.acast.common.ACastUtil;
import com.mathias.android.acast.podcast.Settings;

public class SettingsEdit extends ListActivity {

	private static final String TAG = SettingsEdit.class.getSimpleName();

	private static final List<SettingsItem> items = new ArrayList<SettingsItem>();
	
	static {
		items.add(new SettingsItem(Settings.SettingEnum.ONLYWIFIDOWNLOAD, "Only Wifi download", "Only automatic download through Wifi"));
		items.add(new SettingsItem(Settings.SettingEnum.ONLYWIFISTREAM, "Only Wifi stream", "Only stream audio through Wifi"));
		items.add(new SettingsItem(Settings.SettingEnum.AUTODELETE, "Auto delete", "Downloaded items are automatically deleted during feed refresh"));
		items.add(new SettingsItem(Settings.SettingEnum.AUTOREFRESH, "Auto refresh", "Auto refresh all feeds at spec time (hourly, daily, week, month)"));
		items.add(new SettingsItem(Settings.SettingEnum.AUTODOWNLOAD, "Auto download", "Download all feeds during auto refresh"));
		items.add(new SettingsItem(Settings.SettingEnum.RESUMEPARTLYDOWNLOADED, "Resume partly downloaded", "Resume partly downloaded files"));
		items.add(new SettingsItem(Settings.SettingEnum.AUTODELETECOMPLETED, "Auto delete after played", "Auto delete on completion played"));
		items.add(new SettingsItem(Settings.SettingEnum.OPENINFORMATION, "Open information", "Open information as default instead of media player."));
		items.add(new SettingsItem(Settings.SettingEnum.AUTOPLAYNEXT, "Auto queue next", "Auto queue next feed items after a feed item is completed."));
		items.add(new SettingsItem(Settings.SettingEnum.AUTOPLAYNEXT_DOWNLOADED, "Only auto queue downloaded", "Only auto queue downloaded feed items."));
		items.add(new SettingsItem(Settings.SettingEnum.AUTOPLAYNEXT_COMPLETED, "Only auto queue completed", "Only auto queue downloaded feed items."));
		items.add(new SettingsItem(Settings.SettingEnum.PLAYERINLANDSCAPE, "Player in landscape mode", "Always show player in landscape mode."));
		Collections.sort(items);
	}

	private ACastDbAdapter mDbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ACastUtil.customTitle(this, "Settings", R.layout.settings_edit);

		setListAdapter(new SettingsAdapter(this));
		
		mDbHelper = new ACastDbAdapter(this);
		mDbHelper.open();
	}
	
	@Override
	protected void onDestroy() {
		mDbHelper.close();
		mDbHelper = null;
		super.onDestroy();
	}

	private class SettingsAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public SettingsAdapter(Context cxt){
			mInflater = LayoutInflater.from(cxt);
		}
		@Override
		public int getCount() {
			return items.size();
		}
		@Override
		public Object getItem(int position) {
			return items.get(position);
		}
		@Override
		public long getItemId(int position) {
			return items.get(position).type.ordinal();
		}
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            ViewHolder holder;

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.settings_row, null);

                // Creates a ViewHolder and store references to the children views
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.description = (TextView) convertView.findViewById(R.id.description);
                holder.selected = (CheckBox) convertView.findViewById(R.id.selected);
                holder.selected.setOnCheckedChangeListener(new OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						Log.d(TAG, "onCheckedChanged: "+position);
						mDbHelper.setSetting(items.get(position).type, isChecked);
					}
                });

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder.
            
            holder.title.setText(items.get(position).title);
            holder.description.setText(items.get(position).description);
            holder.selected.setChecked(Boolean.parseBoolean(mDbHelper
					.getSetting(items.get(position).type)));

            return convertView;
		}
		
	}

    private static class ViewHolder {
        TextView title;
        TextView description;
        CheckBox selected;
    }

	private static class SettingsItem implements Comparable<SettingsItem> {
		Settings.SettingEnum type;
		private String title;
		private String description;

		public SettingsItem(Settings.SettingEnum type, String title, String description){
			this.type = type;
			this.title = title;
			this.description = description;
		}

		@Override
		public int compareTo(SettingsItem arg0) {
			return arg0.title.compareTo(title);
		}
	}

}