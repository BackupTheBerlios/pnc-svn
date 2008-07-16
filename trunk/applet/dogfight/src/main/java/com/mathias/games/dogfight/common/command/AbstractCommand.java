package com.mathias.games.dogfight.common.command;

import com.mathias.games.dogfight.common.NetworkItem;
import com.mathias.games.dogfight.common.Sequence;

public abstract class AbstractCommand implements NetworkItem {

	public int sequence;
	
	public long timestamp;
	
	public AbstractCommand() {
		sequence = Sequence.next();
		timestamp = System.currentTimeMillis();
	}

}
