package com.mathias.bellatetris.server;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class HighscoreItem implements Comparable<HighscoreItem>{
	
	@SuppressWarnings("unused")
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	public String name;
	public long score;
	public String ipaddr;
	
	@SuppressWarnings("unused")
	private HighscoreItem(){
	}

	public HighscoreItem(String name, long score, String ipaddr){
		this.name = name;
		this.score = score;
		this.ipaddr = ipaddr;
	}
	
	@Override
	public String toString() {
		return name+","+score+","+ipaddr;
	}

	public int compareTo(HighscoreItem item) {
		if(score == item.score){
			return 0;
		}else if(score > item.score){
			return -1;
		}else{
			return 1;
		}
	}

}
