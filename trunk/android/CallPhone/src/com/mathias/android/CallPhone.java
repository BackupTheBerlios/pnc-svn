package com.mathias.android;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class CallPhone extends ListActivity {
	private SimpleCursorAdapter mAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Cursor c = getContentResolver().query(People.CONTENT_URI, null, null,
				null, null);
		startManagingCursor(c);

		String[] columns = new String[] { People.NAME };
		int[] names = new int[] { R.id.row_entry };

		mAdapter = new SimpleCursorAdapter(this, R.layout.main, c, columns,
				names);
		setListAdapter(mAdapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(Intent.ACTION_CALL);
		
		Cursor c = (Cursor)mAdapter.getItem(position);
		long phoneid = c.getLong(c.getColumnIndex(People.PRIMARY_PHONE_ID));
		i.setData(Uri.parse("content://contacts/people/" + phoneid));
		
		startActivity(i);
	}

}
