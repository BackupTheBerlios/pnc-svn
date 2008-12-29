package com.mathias.android.acast.podcast;

public class Settings {

	public static final long AUTODOWNLOAD = 1;

	public static final long AUTOREFRESH = 2;
	
	public static final long ONLYWIFIDOWNLOAD = 4;

	public static final long ONLYWIFISTREAM = 8;

	public static final long AUTODELETE = 16;

	public static final long RESUMEPARTLYDOWNLOADED = 32;

	public static final long AUTODELETECOMPLETED = 64;

	public static final long AUTOPLAYNEXT = 128;

	private Integer volume;
	
	private Long lastFeedItemId;
	
	private Long flags;
	
	public Settings(Integer volume, Long lastFeedItemId, Long flags){
		this.volume = volume;
		this.lastFeedItemId = lastFeedItemId;
		this.flags = flags;
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

	public boolean getFlag(long id) {
		return (flags & id) == id;
	}

	public void setFlag(long id, boolean value) {
		if(value){
			flags |= id;
		}else{
			flags &= ~id;
		}
	}

	public boolean isAutodownload(){
		return getFlag(AUTODOWNLOAD);
	}
	
	public void setAutodownload(boolean value){
		setFlag(AUTODOWNLOAD, value);
	}

	public boolean isAutorefresh(){
		return getFlag(AUTOREFRESH);
	}
	
	public void setAutorefresh(boolean value){
		setFlag(AUTOREFRESH, value);
	}

	public boolean isOnlyWifiDownload(){
		return getFlag(ONLYWIFIDOWNLOAD);
	}
	
	public void setOnlyWifiDownload(boolean value){
		setFlag(ONLYWIFIDOWNLOAD, value);
	}

	public boolean isOnlyWifiStream(){
		return getFlag(ONLYWIFISTREAM);
	}
	
	public void setOnlyWifiStream(boolean value){
		setFlag(ONLYWIFISTREAM, value);
	}

	public boolean isAutoDelete(){
		return getFlag(AUTODELETE);
	}
	
	public void setAutoDelete(boolean value){
		setFlag(AUTODELETE, value);
	}

	public boolean isResumePartly(){
		return getFlag(RESUMEPARTLYDOWNLOADED);
	}
	
	public void setResumePartly(boolean value){
		setFlag(RESUMEPARTLYDOWNLOADED, value);
	}

	public boolean isAutoDeleteCompleted(){
		return getFlag(AUTODELETECOMPLETED);
	}
	
	public void setAutoDeleteCompleted(boolean value){
		setFlag(AUTODELETECOMPLETED, value);
	}

	public boolean isAutoPLayNext(){
		return getFlag(AUTOPLAYNEXT);
	}
	
	public void setAutoPlayNext(boolean value){
		setFlag(AUTOPLAYNEXT, value);
	}

}
