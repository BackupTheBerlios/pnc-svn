package com.mathias.android.acast.common.services.media;

import com.mathias.android.acast.common.services.media.IMediaServiceCallback;

interface IMediaService {
  void initItem(long id);
  void queue(long id);
  void clearQueue();
  void play();
  void pause();
  void stop();
  void seek(int msec);
  int getCurrentPosition();
  void setCurrentPosition(int position);
  int getDuration();
  long getId();
  List getQueue();
  boolean isPlaying();
  void registerCallback(in IMediaServiceCallback cb);
  void unregisterCallback(in IMediaServiceCallback cb);
}
