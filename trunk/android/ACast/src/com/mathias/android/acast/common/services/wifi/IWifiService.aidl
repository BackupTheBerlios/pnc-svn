package com.mathias.android.acast.common.services.wifi;

import com.mathias.android.acast.common.services.wifi.IWifiServiceCallback;

interface IWifiService {
  boolean isWifiAvailable();
  void registerCallback(in IWifiServiceCallback cb);
  void unregisterCallback(in IWifiServiceCallback cb);
}
