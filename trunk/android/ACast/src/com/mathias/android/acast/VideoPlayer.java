package com.mathias.android.acast;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoPlayer extends Activity {

	private static final String TAG = VideoPlayer.class.getSimpleName();

	public static final String VIDEOURI = "VIDEOURI";

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.videoplayer);

		String path = (icicle != null ? icicle.getString(VIDEOURI) : null);

		if (path == null) {
			Bundle extras = getIntent().getExtras();
			path = (extras != null ? extras.getString(VideoPlayer.VIDEOURI)
					: null);
		}

		Log.d(TAG, "Trying to play video: " + path);

		if (path != null) {
			VideoView video = (VideoView) findViewById(R.id.surface);
			if (URLUtil.isNetworkUrl(path)) {
				video.setVideoURI(Uri.parse(path));
			} else {
				video.setVideoPath(path);
			}
			video.setMediaController(new MediaController(this));
			video.requestFocus();
		}
	}

}
