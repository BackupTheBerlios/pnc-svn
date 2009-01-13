package com.mathias.android.acast.common.services.update;

oneway interface IUpdateServiceCallback {
    void onUpdateItemCompleted(String title);
    void onUpdateItemException(String title, String error);
    void onUpdateAllCompleted();
}
