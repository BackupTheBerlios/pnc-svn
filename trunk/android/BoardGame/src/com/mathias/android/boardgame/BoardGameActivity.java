package com.mathias.android.boardgame;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Handler.Callback;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;

public class BoardGameActivity extends Activity {

	private final static String TAG = BoardGameActivity.class.getSimpleName();

	private Handler handler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		final BoardView board = (BoardView) findViewById(R.id.board);
		board.setFocusable(true);
		board.setClickable(true);
		final SurfaceHolder holder = board.getHolder();
		holder.addCallback(board);
		HandlerThread ht = new HandlerThread(TAG);
		ht.start();
		handler = new Handler(ht.getLooper(), new Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				Point event = (Point) msg.obj;
				board.setPosition(event.x, event.y);
				board.drawBoard(holder);
				return false;
			}
		});
		board.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
//				Log.v(TAG, "event: " + event.toString());
//				board.setPosition((int)event.getX(), (int)event.getY());
//				board.drawBoard(holder);
				handler.removeMessages(0);
				handler.sendMessage(handler.obtainMessage(0, new Point(
						(int) event.getX(), (int) event.getY())));
				return false;
			}
		});

	}

}
