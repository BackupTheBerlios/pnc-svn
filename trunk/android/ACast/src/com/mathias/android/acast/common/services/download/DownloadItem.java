package com.mathias.android.acast.common.services.download;

import java.io.Serializable;

import com.mathias.android.acast.common.Util;

@SuppressWarnings("serial")
public class DownloadItem implements Serializable {

	public long externalId;
	
	public String srcuri;

	public String destfile;

	public long progress;
	
	public long size;
	
	public DownloadItem(long externalId, String srcuri, String destfile, long progress, long size){
		this.externalId = externalId;
		this.srcuri = srcuri;
		this.destfile = destfile;
		this.progress = progress;
		this.size = size;
	}
	
	@Override
	public String toString() {
		return Util.buildString("[externalId=", externalId, "][srcuri=",
				srcuri, "][destfile=", destfile, "][progress=", progress, "][size=", size, "]");
	}

}

