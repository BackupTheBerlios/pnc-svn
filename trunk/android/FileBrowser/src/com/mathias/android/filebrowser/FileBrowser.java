package com.mathias.android.filebrowser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FileBrowser extends ListActivity {

	private List<String> items = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.directory_list);
		fillWithRoot();
	}

	private void fill(File[] files) {
		items = new ArrayList<String>();
		items.add(getString(R.string.to_top));
		for (File file : files){
			items.add(file.getPath());
		}
		ArrayAdapter<String> fileList = new ArrayAdapter<String>(this,
				R.layout.file_row, items);
		setListAdapter(fileList);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (id == 0) {
			fillWithRoot();
		} else {
			File file = new File(items.get((int)id));
			if (file.isDirectory()){
				fill(file.listFiles());
			} else if (file.getName().endsWith("ogg")
					|| file.getName().endsWith("mp3")) {
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

	private void fillWithRoot() {
		fill(new File("/").listFiles());
	}

}
