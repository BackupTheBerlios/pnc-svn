package com.mathias.android.acast.common.services.download;

import com.mathias.android.acast.common.services.download.IDownloadServiceCallback;

interface IDownloadService {
  void download(long externalid, String srcuri, String destfile);
  void cancelAndRemove();
  void registerCallback(IDownloadServiceCallback cb);
  void unregisterCallback(IDownloadServiceCallback cb);
}
