package com.mathias.android.acast;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.rss.RssUtil;

public class FeedEdit extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed_edit);

		final ACastDbAdapter dbHelper = new ACastDbAdapter(this);
		dbHelper.open();
		
		final EditText urlText = (EditText) findViewById(R.id.url);
		Button confirm = (Button) findViewById(R.id.add);

		urlText.setText("http://");

		confirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String uri = urlText.getText().toString();
				Feed feed = new RssUtil().parse(uri);
				dbHelper.createFeed(feed);
				setResult(RESULT_OK);
				finish();
			}
		});
	}

}
