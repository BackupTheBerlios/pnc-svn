package com.mathias.games.dogfight.common.command;

import com.mathias.games.dogfight.AbstractItem;

public class UpdateCommand extends AbstractCommand {

	public AbstractItem[] items;

	public UpdateCommand(AbstractItem ... items) {
		this.items = items;
	}

}
