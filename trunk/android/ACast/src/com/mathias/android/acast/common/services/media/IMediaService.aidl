package com.mathias.android.acast.common.services.media;

import com.mathias.android.acast.common.services.media.IMediaServiceCallback;

interface IMediaService {
  void initItem(long externalid, in String locator, boolean stream);
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
  void registerCallback(in IMediaServiceCallback cb);
  void unregisterCallback(in IMediaServiceCallback cb);
}
