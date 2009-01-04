package com.mathias.android.acast.podcast;

public class Settings {

	public enum SettingEnum {
		AUTODOWNLOAD(TypeEnum.BOOLEAN),
		AUTOREFRESH(TypeEnum.BOOLEAN),
		ONLYWIFIDOWNLOAD(TypeEnum.BOOLEAN),
		ONLYWIFISTREAM(TypeEnum.BOOLEAN),
		AUTODELETE(TypeEnum.BOOLEAN),
		RESUMEPARTLYDOWNLOADED(TypeEnum.BOOLEAN),
		AUTODELETECOMPLETED(TypeEnum.BOOLEAN),
		OPENINFORMATION(TypeEnum.BOOLEAN),
		AUTOPLAYNEXT(TypeEnum.BOOLEAN),
		AUTOPLAYNEXT_DOWNLOADED(TypeEnum.BOOLEAN),
		PLAYERINLANDSCAPE(TypeEnum.BOOLEAN),
		LASTFEEDITEMID(TypeEnum.INTEGER),
		SORTBY(TypeEnum.SortbyEnum);
		
		TypeEnum type;
		
		private SettingEnum(TypeEnum type){
			this.type = type;
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
		SortbyEnum;
	}

}
