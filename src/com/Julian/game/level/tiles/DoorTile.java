package com.Julian.game.level.tiles;

import com.Julian.game.Game;

public class DoorTile extends BasicInteractiveTile {

	public DoorTile(int id, int x, int y, int tileColor, int levelImageColor) {
		super(id, x, y, tileColor, levelImageColor);
		this.solid = true;
	}

	public void doAction() {
		Game.game.level.alterTile(this.xLevel, this.yLevel - 1, Tile.DOOR_TOP_OPENED);
		Tile.DOOR_TOP_OPENED.setXLevel(this.xLevel);
		Tile.DOOR_TOP_OPENED.setYLevel(this.yLevel - 1);
		Game.game.level.alterTile(this.xLevel, this.yLevel, Tile.DOOR_BOTTOM_OPENED);
		Tile.DOOR_BOTTOM_OPENED.setXLevel(this.xLevel);
		Tile.DOOR_BOTTOM_OPENED.setYLevel(this.yLevel);
	}
	
}
