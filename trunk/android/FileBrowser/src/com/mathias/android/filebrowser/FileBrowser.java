package com.mathias.android.filebrowser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class FileBrowser extends ListActivity {

	private List<String> items = null;
	
	private EditText at;

	private Button go;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.directory_list);
		at = (EditText) findViewById(R.id.at);
		go = (Button) findViewById(R.id.go);
		go.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				list(at.getText().toString());
			}
		});
		fillWithRoot();
	}

	private void fill(String[] files) {
		items = new ArrayList<String>();
		items.add(getString(R.string.to_top));
		items.add(getString(R.string.to_priv));
		items.add(getString(R.string.to_cache));
		for (String file : files){
			items.add(file);
		}
		ArrayAdapter<String> fileList = new ArrayAdapter<String>(this,
				R.layout.file_row, items);
		setListAdapter(fileList);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (id == 0) {
			fillWithRoot();
		} else if(id == 1) {
			fillWithPrivate();
		} else if(id == 2) {
			fillWithCache();
		} else {
			File file = new File(items.get((int)id));
			if (file.isDirectory()){
				at.setText(file.getAbsolutePath());
				fill(file.list());
			} else if (file.getName().endsWith(".ogg")
					|| file.getName().endsWith(".mp3")) {
				try {
					MediaPlayer mp = new MediaPlayer();
					mp.setDataSource(file.getAbsolutePath());
					mp.prepare();
					mp.start();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void list(String path){
		list(new File(path));
	}

	private void list(File path){
		at.setText(path.getAbsolutePath());
		fill(path.list());
	}

	private void fillWithRoot() {
		list("/");
	}

	private void fillWithCache() {
		list(getCacheDir());
	}

	private void fillWithPrivate() {
		at.setText(R.string.to_priv);
		fill(fileList());
	}

}
