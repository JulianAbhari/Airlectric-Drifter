package com.Julian.game.level.tiles;

import com.Julian.game.Game;
import com.Julian.game.entities.Player;
import com.Julian.game.level.Level;

public class OpenedDoorTile extends BasicTriggerTile {

	public OpenedDoorTile(int id, int x, int y, int tileColor, int levelImageColor) {
		super(id, x, y, tileColor, levelImageColor);
	}
	
	public void doAction() {
		Game.game.level = new Level("/Levels/cage_test_2.png");
		Game.game.player = new Player(Game.game.level, (Game.game.level.width/2)*8, (Game.game.level.height/2)*8, Game.input, Game.game.player.getUsername());
		Game.game.level.addEntity(Game.game.player);
	}

}
