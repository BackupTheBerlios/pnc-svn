package com.mathias.android.acast.podcast;

public class Settings {

	public enum SettingEnum {
		AUTODOWNLOAD(TypeEnum.BOOLEAN, false),
		AUTOREFRESH(TypeEnum.BOOLEAN, false),
		ONLYWIFIDOWNLOAD(TypeEnum.BOOLEAN, false),
		ONLYWIFISTREAM(TypeEnum.BOOLEAN, false),
		AUTODELETE(TypeEnum.BOOLEAN, false),
		RESUMEPARTLYDOWNLOADED(TypeEnum.BOOLEAN, false),
		AUTODELETECOMPLETED(TypeEnum.BOOLEAN, false),
		OPENINFORMATION(TypeEnum.BOOLEAN, false),
		AUTOPLAYNEXT(TypeEnum.BOOLEAN, false),
		AUTOPLAYNEXT_DOWNLOADED(TypeEnum.BOOLEAN, false),
		AUTOPLAYNEXT_COMPLETED(TypeEnum.BOOLEAN, false),
		PLAYERINLANDSCAPE(TypeEnum.BOOLEAN, false),
		DEFAULTMEDIAPLAYER(TypeEnum.BOOLEAN, false),
		LASTFEEDITEMID(TypeEnum.INTEGER),
		LASTFULLUPDATE(TypeEnum.DATE),
		SORTBY(TypeEnum.SortbyEnum);
		
		TypeEnum type;
		
		String defaultVal;
		
		private SettingEnum(TypeEnum type){
			this.type = type;
		}
		
		private SettingEnum(TypeEnum type, boolean def){
			this.type = type;
			this.defaultVal = ""+def;
		}
		
		private SettingEnum(TypeEnum type, String def){
			this.type = type;
			this.defaultVal = def;
		}
		
		public TypeEnum getType(){
			return type;
		}

	}

	public enum SortbyEnum {
		UNSORTED,
		PUBDATE,
		TITLE,
		PUBLISHER;
	}

	public enum TypeEnum {
		BOOLEAN,
		STRING,
		INTEGER,
		DATE,
		SortbyEnum;
	}

}
