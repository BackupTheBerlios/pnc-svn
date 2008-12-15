package com.mathias.android.acast.podcast;

public class Settings {

	private Integer volume;
	
	private Long lastFeedItemId;

	public Settings(Integer volume, Long lastFeedItemId){
		this.volume = volume;
		this.lastFeedItemId = lastFeedItemId;
	}

	public Integer getVolume() {
		return volume;
	}

	public void setVolume(Integer volume) {
		this.volume = volume;
	}

	public Long getLastFeedItemId() {
		return lastFeedItemId;
	}

	public void setLastFeedItemId(Long lastFeedId) {
		this.lastFeedItemId = lastFeedId;
	}

}
