package com.mathias.android.acast.common.services.wifi;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

public class WifiService extends Service {

	private static final String TAG = WifiService.class.getSimpleName();
	
	private static final long UPDATE_DELAY = 10000;

    private final RemoteCallbackList<IWifiServiceCallback> mCallbacks = new RemoteCallbackList<IWifiServiceCallback>();

	private WifiManager wifiManager;
	
	private boolean wifiAvailable = false;
	
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		super.onCreate();

		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		handler.sendEmptyMessageDelayed(0, UPDATE_DELAY);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mCallbacks.kill();
	}

    private final IWifiService.Stub binder = new IWifiService.Stub() {
		@Override
		public boolean isWifiAvailable() throws RemoteException {
			WifiInfo info = wifiManager.getConnectionInfo();
			wifiAvailable = info != null && info.getSSID() != null;
			return wifiAvailable;
		}
		@Override
		public void registerCallback(IWifiServiceCallback cb)
				throws RemoteException {
			mCallbacks.register(cb);
		}
		@Override
		public void unregisterCallback(IWifiServiceCallback cb)
				throws RemoteException {
			mCallbacks.unregister(cb);
		}
    };

    private Handler handler = new Handler(){
    	@Override
    	public void handleMessage(Message msg) {
    		Log.d(TAG, "handleMessage");

    		WifiInfo info = wifiManager.getConnectionInfo();
    		boolean t = wifiAvailable;
			wifiAvailable = info != null && info.getSSID() != null;
			if(wifiAvailable != t){
				Log.d(TAG, "broadcastWifiStateChanged; wifi="+wifiAvailable);
				broadcastWifiStateChanged();
			}
    		
    		handler.sendEmptyMessageDelayed(0, UPDATE_DELAY);
    	}
    };

	private void broadcastWifiStateChanged(){
        final int N = mCallbacks.beginBroadcast();
		for (int i = 0; i < N; i++) {
			try {
				mCallbacks.getBroadcastItem(i).onWifiStateChanged(wifiAvailable);
			} catch (RemoteException e) {
				// The RemoteCallbackList will take care of removing
				// the dead object for us.
			}
		}
		mCallbacks.finishBroadcast();
	}

}
