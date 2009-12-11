package com.mathias.android.boardgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class BoardView extends SurfaceView implements SurfaceHolder.Callback {

	private final static String TAG = BoardView.class.getSimpleName();

	private final Paint mPaint = new Paint();
	
	private Bitmap mBitmap;

	private int mX = 0;

	private int mY = 0;

	private boolean mEnabled = false;

	public BoardView(Context context) {
		super(context);
		initialize();
	}

	public BoardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	public BoardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	private void initialize() {
		// mRedrawHandler.sleep(1000);
//		mBitmap = BitmapFactory.decodeResource(getResources(),
//				R.drawable.hexagon2);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawBoard(canvas);
	}

	private void drawBoard(Canvas canvas) {
		if (!mEnabled) {
			Log.w(TAG, "not enabled");
		} else {
			canvas.drawRect(0, 0, 500, 500, mPaint);
			canvas.drawBitmap(mBitmap, mX, mY, mPaint);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		Log.v(TAG, "onSizeChanged w=" + w);
		super.onSizeChanged(w, h, oldw, oldh);
	}

	public void setPosition(int x, int y) {
		mX = x - 126;
		mY = y - 126;
	}
	
	public void drawBoard(SurfaceHolder holder) {
		Canvas canvas = holder.lockCanvas();
		drawBoard(canvas);
		holder.unlockCanvasAndPost(canvas);
//		invalidate();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mEnabled = true;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mEnabled = false;
	};

}
