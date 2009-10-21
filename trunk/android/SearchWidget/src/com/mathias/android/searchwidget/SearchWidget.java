package com.mathias.android.searchwidget;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class SearchWidget extends Activity {

	private final static String TAG = SearchWidget.class.getSimpleName();
	
	private final static int BUTTONS = 4;
	
	private DbAdapter dbAdapter;

	private EditText input;
	private ImageButton[] btn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.main);
		
		dbAdapter = new DbAdapter(TAG);
		dbAdapter.open(this, false);

		input = (EditText) findViewById(R.id.input);
		btn = new ImageButton[BUTTONS];
		btn[0] = (ImageButton)findViewById(R.id.b0);
		btn[1] = (ImageButton)findViewById(R.id.b1);
		btn[2] = (ImageButton)findViewById(R.id.b2);
		btn[3] = (ImageButton)findViewById(R.id.b3);
	}
	
	@Override
	protected void onResume() {
		populate();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		dbAdapter.close();
		dbAdapter = null;
		super.onDestroy();
	}

	private void populate(){
		List<Engine> engines = dbAdapter.fetchAllEngines();
		for (Iterator<Engine> it = engines.iterator(); it.hasNext();) {
			Engine engine = it.next();
			if(engine.button == 0){
				input.setOnEditorActionListener(createOnEditorActionListener(engine));
			}
			if(0 <= engine.button && engine.button < BUTTONS) {
				Log.v(TAG, "setImage: "+engine.button);
				setImage(btn[engine.button], engine.icon);
				btn[engine.button].setOnClickListener(createOnClickListener(engine));
				btn[engine.button].setOnLongClickListener(createOnLongClickListener(engine.button));
			}
		}
	}

	private OnEditorActionListener createOnEditorActionListener(
			final Engine engine) {
		return new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				sendIntent(engine, input);
				return true;
			}
		};
	}

	private OnClickListener createOnClickListener(final Engine engine){
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendIntent(engine, input);
			}
		};
	}

	private OnLongClickListener createOnLongClickListener(final int button){
		return new OnLongClickListener(){
			@Override
			public boolean onLongClick(View v) {
				final List<Engine> engines = dbAdapter.fetchAllEngines();
				final EngineAdapter adapter = new EngineAdapter(getLayoutInflater(), engines);
		        AlertDialog.Builder builder = new AlertDialog.Builder(SearchWidget.this);
		        builder.setTitle("Engines");
		        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
//						Log.v(TAG, "single choice on click: "+which);
						String title = adapter.getItem(which);
						for (Iterator<Engine> it2 = engines.iterator(); it2
								.hasNext();) {
							Engine engine = it2.next();
							if(engine.title.equals(title)){
								Engine oldEngine = dbAdapter.fetchEngineByButton(button);
								if(oldEngine != null){
									dbAdapter.updateEngine(
													oldEngine.id,
													DbAdapter.ENGINE_BUTTON,
													Constants.INVALID_POS);
								}
								dbAdapter.updateEngine(engine.id,
												DbAdapter.ENGINE_BUTTON, button);
								populate();
								break;
							}
						}
					}
				});
		        builder.create().show();
				return true;
			}
		};
	}

	private void sendIntent(Engine engine, EditText input){
		if(engine == null){
			Log.d(TAG, "Engine is null");
		}else{
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri
					.parse(engine.uri+input.getText().toString()));
			startActivity(intent);
		}
	}
	
	private void setImage(ImageButton btn, String icon){
		Log.v(TAG, "setImage: "+icon);
		if("google".equals(icon)){
			btn.setImageResource(R.drawable.google);
		}else if("googlelucky".equals(icon)){
			btn.setImageResource(R.drawable.googlelucky);
		}else if("bing".equals(icon)){
			btn.setImageResource(R.drawable.bing);
		}else if("imdb".equals(icon)){
			btn.setImageResource(R.drawable.imdb);
		}else if("market".equals(icon)){
			btn.setImageResource(R.drawable.market);
		}else if("wikipedia".equals(icon)){
			btn.setImageResource(R.drawable.wikipedia_en);
		}else if("youtube".equals(icon)){
			btn.setImageResource(R.drawable.youtube);
		}else{
			btn.setImageResource(R.drawable.icon);
		}
	}

	private static class EngineAdapter extends BaseAdapter {

		private LayoutInflater inflater;

		private List<Engine> engines;

		public EngineAdapter(LayoutInflater inflater, List<Engine> engines2){
			engines = engines2;
			if(engines != null){
				Collections.sort(engines, new Comparator<Engine>(){
					@Override
					public int compare(Engine object1, Engine object2) {
						return object1.title.compareToIgnoreCase(object2.title);
					}
				});
			}
			this.inflater = inflater;
		}

		@Override
		public int getCount() {
			return engines.size();
		}

		@Override
		public String getItem(int position) {
			return engines.get(position).title;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			String title = engines.get(position).title;

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.simple_list_item, null);
				holder = new ViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.text1);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.title.setText(title);
			return convertView;
		}

		private static class ViewHolder {
	        TextView title;
		}

	}

}
