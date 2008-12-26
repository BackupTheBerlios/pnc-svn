package com.mathias.android.acast.common.services.download;

oneway interface IDownloadServiceCallback {
    void onProgress(long externalid, long diff);
    void onCompleted(long externalid);
    void onException(long externalid, in String exception);
}
