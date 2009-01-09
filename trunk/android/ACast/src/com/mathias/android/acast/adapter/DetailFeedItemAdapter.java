package com.mathias.android.acast.adapter;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mathias.android.acast.ACastDbAdapter;
import com.mathias.android.acast.R;
import com.mathias.android.acast.common.ACastUtil;
import com.mathias.android.acast.common.BitmapCache;
import com.mathias.android.acast.podcast.FeedItem;

public class DetailFeedItemAdapter extends BaseAdapter {

	private LayoutInflater mInflater;

	private List<FeedItem> items;
	
	private ACastDbAdapter dbHelper;

	public DetailFeedItemAdapter(Context cxt, ACastDbAdapter dbHelper, List<FeedItem> items){
		Collections.sort(items, ACastUtil.FEEDITEM_BYDATE);
		this.items = items;
		this.dbHelper = dbHelper;
		mInflater = LayoutInflater.from(cxt);
	}
	public FeedItem getByExternalId(long externalId){
		for (FeedItem item : items) {
			if(externalId == item.id){
				return item;
			}
		}
		return null;
	}
	@Override
	public int getCount() {
		return items.size();
	}
	@Override
	public FeedItem getItem(int position) {
		return items.get(position);
	}
	@Override
	public long getItemId(int position) {
		return getItem(position).id;
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
            //Log.d(TAG, "getView; convertView=null position="+position+" items="+items.size());

            convertView = mInflater.inflate(R.layout.detailfeeditem_row, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.feedicon = (ImageView) convertView.findViewById(R.id.feedicon);
            holder.statusicon = (ImageView) convertView.findViewById(R.id.statusicon);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.author = (TextView) convertView.findViewById(R.id.author);
            holder.pubdate = (TextView) convertView.findViewById(R.id.pubdate);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        // Bind the data efficiently with the holder.
        FeedItem item = items.get(position);
        if(item == null) {
        	return null;
        }
        Bitmap bm = BitmapCache.instance().get(item.feedId, dbHelper);
        if(bm != null){
    		holder.feedicon.setImageBitmap(bm);
		}else{
			holder.feedicon.setImageResource(R.drawable.question);
        }
		holder.statusicon.setImageResource(ACastUtil.getStatusIcon(item));
        holder.title.setText(item.title);
        holder.author.setText(item.author);
        holder.pubdate.setText(new Date(item.pubdate).toString());

        return convertView;
	}

	private static class ViewHolder {
        ImageView feedicon;
        ImageView statusicon;
        TextView title;
        TextView author;
        TextView pubdate;
    }

}
