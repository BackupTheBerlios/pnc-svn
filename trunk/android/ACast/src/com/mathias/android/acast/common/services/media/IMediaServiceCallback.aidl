package com.mathias.android.acast.common.services.media;

oneway interface IMediaServiceCallback {
    void onPlaylistCompleted();
    void onTrackCompleted();
}
