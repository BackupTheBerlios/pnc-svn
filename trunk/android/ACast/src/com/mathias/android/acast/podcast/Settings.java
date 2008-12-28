package com.mathias.android.acast.podcast;

public class Settings {

	private static final long AUTODOWNLOAD = 1;

	private static final long AUTOREFRESH = 2;
	
	private static final long ONLYWIFIDOWNLOAD = 4;

	private static final long ONLYWIFISTREAM = 8;

	private static final long AUTODELETE = 16;

	private static final long RESUMEPARTLYDOWNLOADED = 32;

	private Integer volume;
	
	private Long lastFeedItemId;
	
	private Long flags;
	
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

	public Long getFlags() {
		return flags;
	}

	public void setFlags(Long flags) {
		this.flags = flags;
	}

	public boolean isAutodownload(){
		return (flags & AUTODOWNLOAD) == AUTODOWNLOAD;
	}
	
	public void setAutodownload(boolean value){
		if(value){
			flags |= AUTODOWNLOAD;
		}else{
			flags &= ~AUTODOWNLOAD;
		}
	}

	public boolean isAutorefresh(){
		return (flags & AUTOREFRESH) == AUTOREFRESH;
	}
	
	public void setAutorefresh(boolean value){
		if(value){
			flags |= AUTOREFRESH;
		}else{
			flags &= ~AUTOREFRESH;
		}
	}

	public boolean isOnlyWifiDownload(){
		return (flags & ONLYWIFIDOWNLOAD) == ONLYWIFIDOWNLOAD;
	}
	
	public void setOnlyWifiDownload(boolean value){
		if(value){
			flags |= ONLYWIFIDOWNLOAD;
		}else{
			flags &= ~ONLYWIFIDOWNLOAD;
		}
	}

	public boolean isOnlyWifiStream(){
		return (flags & ONLYWIFISTREAM) == ONLYWIFISTREAM;
	}
	
	public void setOnlyWifiStream(boolean value){
		if(value){
			flags |= ONLYWIFISTREAM;
		}else{
			flags &= ~ONLYWIFISTREAM;
		}
	}

	public boolean isAutoDelete(){
		return (flags & AUTODELETE) == AUTODELETE;
	}
	
	public void setAutoDelete(boolean value){
		if(value){
			flags |= AUTODELETE;
		}else{
			flags &= ~AUTODELETE;
		}
	}

	public boolean isResumePartly(){
		return (flags & RESUMEPARTLYDOWNLOADED) == RESUMEPARTLYDOWNLOADED;
	}
	
	public void setResumePartly(boolean value){
		if(value){
			flags |= RESUMEPARTLYDOWNLOADED;
		}else{
			flags &= ~RESUMEPARTLYDOWNLOADED;
		}
	}

}
