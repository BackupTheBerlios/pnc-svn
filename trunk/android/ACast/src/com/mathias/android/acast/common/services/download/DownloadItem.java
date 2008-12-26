package com.mathias.android.acast.common.services.download;

import java.io.Serializable;

import com.mathias.android.acast.common.Util;

@SuppressWarnings("serial")
public class DownloadItem implements Serializable {

	private long externalId;
	
	private String srcuri;

	private String destfile;

	private long progress;
	
	public DownloadItem(long externalId, String srcuri, String destfile, long progress){
		this.externalId = externalId;
		this.srcuri = srcuri;
		this.destfile = destfile;
		this.progress = progress;
	}
	
	@Override
	public String toString() {
		return Util.buildString("[externalId=", externalId, "][srcuri=",
				srcuri, "][destfile=", destfile, "][progress=", progress, "]");
	}

	public long getExternalId() {
		return externalId;
	}

	public void setExternalId(long externalId) {
		this.externalId = externalId;
	}

	public String getSrcuri() {
		return srcuri;
	}

	public void setSrcuri(String srcuri) {
		this.srcuri = srcuri;
	}

	public String getDestfile() {
		return destfile;
	}

	public void setDestfile(String destfile) {
		this.destfile = destfile;
	}

	public long getProgress() {
		return progress;
	}

	public void setProgress(long progress) {
		this.progress = progress;
	}

}

