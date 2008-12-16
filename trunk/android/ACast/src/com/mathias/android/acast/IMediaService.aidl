package com.mathias.android.acast;

interface IMediaService {
  void playFeedItem(int id);
  void play();
  void pause();
  void stop();
  void reset();
  void forward();
  void rewind();
  int getCurrentPosition();
  void setCurrentPosition(int position);
  int getDuration();
  int getPid();
}
