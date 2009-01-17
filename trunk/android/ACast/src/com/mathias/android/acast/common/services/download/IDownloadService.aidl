package com.mathias.android.acast.common.services.download;

import com.mathias.android.acast.common.services.download.IDownloadServiceCallback;

interface IDownloadService {
  void download(long externalid, in String srcuri, in String destfile, long size);
  void cancelAndRemove(long externalid);
  void cancelAndRemoveCurrent();
  void cancelAndRemoveAll();
  List getDownloads();
  long getCurrentDownload();
  long getProgress();
  void registerCallback(in IDownloadServiceCallback cb);
  void unregisterCallback(in IDownloadServiceCallback cb);
}
