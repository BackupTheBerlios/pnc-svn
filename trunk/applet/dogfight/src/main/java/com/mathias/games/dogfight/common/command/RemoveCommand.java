package com.mathias.games.dogfight.common.command;

import com.mathias.games.dogfight.common.items.AbstractItem;

public class RemoveCommand extends AbstractCommand {

	private static final long serialVersionUID = -2855791151549060571L;

	public AbstractItem item;

	public RemoveCommand(AbstractItem item){
		this.item = item;
	}

}
