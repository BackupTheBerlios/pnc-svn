package com.mathias.android.acast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class SettingsEdit extends ListActivity {

	private static final List<SettingsItem> settings = new ArrayList<SettingsItem>();

	static {
		settings.add(new SettingsItem(0, "Only Wifi download", "Only automatic download through Wifi", true));
		settings.add(new SettingsItem(0, "Only Wifi stream", "Only stream audio through Wifi", true));
		settings.add(new SettingsItem(0, "Auto delete", "Downloaded items are automatically deleted during feed refresh", true));
		settings.add(new SettingsItem(0, "Auto refresh", "Auto refresh all feeds at spec time (hourly, daily, week, month)", true));
		settings.add(new SettingsItem(0, "Auto download", "Download all feeds during auto refresh", true));
		settings.add(new SettingsItem(0, "Resume partly downloaded", "Resume partly downloaded files", true));
		Collections.sort(settings);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_edit);
		setListAdapter(new SettingsAdapter(this));
	}

	private static class SettingsAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public SettingsAdapter(Context cxt){
			mInflater = LayoutInflater.from(cxt);
		}
		@Override
		public int getCount() {
			return settings.size();
		}
		@Override
		public Object getItem(int position) {
			return settings.get(position);
		}
		@Override
		public long getItemId(int position) {
			return settings.get(position).id;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
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

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder.
            
            holder.title.setText(settings.get(position).title);
            holder.description.setText(settings.get(position).description);
            holder.selected.setChecked(settings.get(position).selected);

            return convertView;
		}
		
	    private static class ViewHolder {
	        TextView title;
	        TextView description;
	        CheckBox selected;
	    }

	}

	private static class SettingsItem implements Comparable<SettingsItem> {
		private int id;
		private String title;
		private String description;
		private boolean selected;

		public SettingsItem(int id, String title, String description, boolean selected){
			this.id = id;
			this.title = title;
			this.description = description;
			this.selected = selected;
		}

		@Override
		public int compareTo(SettingsItem arg0) {
			return arg0.title.compareTo(title);
		}
	}

}
