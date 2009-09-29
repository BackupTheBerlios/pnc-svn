package com.mathias.android.searchwidget;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class SearchWidget extends Activity {

	private final static String TAG = SearchWidget.class.getSimpleName();
	
	private final static String SEARCH_WIKIPEDIA_EN = "http://en.wikipedia.org/wiki/Special:Search?go=Go&search=";

//	private final static String SEARCH_WIKIPEDIA_SV = "http://sv.wikipedia.org/wiki/Special:Search?go=Go&search=";

	private final static String SEARCH_GOOGLE = "http://www.google.com/search?q=";

	private final static String SEARCH_IMDB = "http://imdb.com/find?s=all&q=";

	private final static String[] engine_strings = new String[] {
			SEARCH_WIKIPEDIA_EN, SEARCH_GOOGLE, SEARCH_IMDB };

	private final static int[] engine_images = new int[] {
			R.drawable.wikipedia_en, R.drawable.google, R.drawable.imdb };

	private int selected_engine = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		final ImageButton engine = (ImageButton) findViewById(R.id.engine);
		engine.setImageResource(engine_images[selected_engine]);
		engine.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG, "onClick engine");
				selected_engine++;
				if(selected_engine >= engine_strings.length){
					selected_engine = 0;
				}
				engine.setImageResource(engine_images[selected_engine]);
			}
		});

		final EditText input = (EditText) findViewById(R.id.input);

		Button go = (Button) findViewById(R.id.go);
		go.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG, "onClick go");
				String search = engine_strings[selected_engine]
					+ input.getText().toString();
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri
						.parse(search));
				startActivity(intent);
			}
		});

	}

}
