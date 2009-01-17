package com.mathias.android.acast.common.services.wifi;

oneway interface IWifiServiceCallback {
    void onWifiStateChanged(boolean connected);
}
