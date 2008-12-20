package com.mathias.android.acast.common.services.media;

import com.mathias.android.acast.common.services.media.IMediaServiceCallback;

interface IMediaService {
  void playFeedItem(long externalid, String locator, boolean stream);
  void play();
  void pause();
  void stop();
  void seek(int msec);
  int getCurrentPosition();
  void setCurrentPosition(int position);
  int getDuration();
  long getExternalId();
  String getLocator();
  boolean isPlaying();
  boolean isStreming();
  void registerCallback(IMediaServiceCallback cb);
  void unregisterCallback(IMediaServiceCallback cb);
}
