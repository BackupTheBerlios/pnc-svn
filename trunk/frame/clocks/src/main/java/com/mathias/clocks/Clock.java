package com.mathias.clocks;

import java.util.TimeZone;

public class Clock {
	public TimeZone timeZone;
	public String name;

	public Clock(String name, TimeZone timeZone) {
		this.timeZone = timeZone;
		this.name = name;
	}
}

