package com.mathias.games.dogfight.common.command;

import com.mathias.games.dogfight.common.items.AbstractItem;

public class UpdateCommand extends AbstractCommand {

	private static final long serialVersionUID = 1103379364385077650L;

	public AbstractItem[] items;

	public UpdateCommand(AbstractItem ... items) {
		super();
		this.items = items;
	}

}
