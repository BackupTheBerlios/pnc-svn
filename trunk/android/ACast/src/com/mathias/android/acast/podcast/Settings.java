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
	public static final long OPENINFORMATION = 256;
	//512
	//1024
	//2048
	//4096
	//8192
	//16384
	//32768
	//65536
	//131072
	//262144
	//524288
	//1048576

	private Integer volume;
	
	private Long lastFeedItemId;
	
	private long flags;
	
	public Settings(Integer volume, Long lastFeedItemId, long flags){
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

	public long getFlags() {
		return flags;
	}

	public void setFlags(long flags) {
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

	public boolean isAutoPlayNext(){
		return getFlag(AUTOPLAYNEXT);
	}
	
	public void setAutoPlayNext(boolean value){
		setFlag(AUTOPLAYNEXT, value);
	}

	public boolean isOpenInformtion(){
		return getFlag(OPENINFORMATION);
	}
	
	public void setOpenInformtion(boolean value){
		setFlag(OPENINFORMATION, value);
	}

}
