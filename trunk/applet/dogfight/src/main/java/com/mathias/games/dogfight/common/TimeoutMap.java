package com.mathias.games.dogfight.common;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.mathias.drawutils.Util;

public class TimeoutMap<K, V> {

	private HashMap<K, TimeoutEntry> mMap;
	private Timer mTimer;
	private long mTimeout;
	private TimeoutMapListener<K, V> mListener;

	public TimeoutMap(long timeout, TimeoutMapListener<K, V> listener) {
		mTimeout = timeout;
		mListener = listener;

		mMap = new HashMap<K, TimeoutEntry>();
		mTimer = new Timer(false);
	}

	public synchronized void put(K key, V value) {
		// Remove entry if already assigned to key
		remove(key);
		// Add new entry
		TimeoutEntry entry = new TimeoutEntry(key, value);
		mMap.put(key, entry);
		mTimer.schedule(entry, mTimeout);
	}

	public synchronized V get(K key) {
		TimeoutEntry entry = mMap.get(key);
		return (entry != null ? entry.mValue : null);
	}

	public synchronized V remove(K key) {
		TimeoutEntry entry = mMap.remove(key);
		if (entry != null) {
			entry.cancel();
			// Since cancelled TimerTask are left in the Timer queue until timed
			// out
			// even if they are cancelled, we remove the data reference from the
			// TimeoutEntry to allow for the garbage collector to do it's job...
			V value = entry.mValue;
			entry.mValue = null;
			return value;
		} else {
			return null;
		}
	}

	public synchronized boolean exists(K key) {
		return mMap.containsKey(key);

	}

	public synchronized int size() {
		return mMap.size();
	}

	private class TimeoutEntry extends TimerTask {

		protected K mKey;
		protected V mValue;

		public TimeoutEntry(K key, V value) {
			mKey = key;
			mValue = value;
		}

		public void run() {
			try {
				if (mKey != null) {
					V value = TimeoutMap.this.remove(mKey);
					if (value != null) {
						mListener.handleTimeout(mKey, value);
					}
				}
			} catch (Throwable e) {
				Util.LOG("Trapped exception in run()" + e);
			}
		}

	}

}
