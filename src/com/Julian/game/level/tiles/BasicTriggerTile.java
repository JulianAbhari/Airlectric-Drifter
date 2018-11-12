package com.Julian.game.level.tiles;

public abstract class BasicTriggerTile extends BasicTile {

	public BasicTriggerTile(int id, int x, int y, int tileColor, int levelImageColor) {
		super(id, x, y, tileColor, levelImageColor);
		this.trigger = true;
	}

	public abstract void doAction();

}
