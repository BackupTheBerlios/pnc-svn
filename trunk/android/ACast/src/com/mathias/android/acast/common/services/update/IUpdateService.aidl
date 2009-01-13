package com.mathias.android.acast.common.services.update;

import com.mathias.android.acast.common.services.update.IUpdateServiceCallback;

interface IUpdateService {
  void addFeed(in String url);
  void updateFeed(long id);
  void updateAll();
  void registerCallback(in IUpdateServiceCallback cb);
  void unregisterCallback(in IUpdateServiceCallback cb);
}
