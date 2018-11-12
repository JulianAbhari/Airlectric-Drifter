package com.Julian.game.entities;

import com.Julian.game.gfx.Colors;
import com.Julian.game.gfx.Screen;
import com.Julian.game.level.Level;

public class Laser extends Mob {
	int xDir;
	int yDir;
	String sender;
	
	public Laser(Level level, String name, int x, int y, int xDir, int yDir) {
		super(level, name, x, y, 3);
		this.xDir = xDir;
		this.yDir = yDir;
		String[] nameArray = name.split(",");
		sender = nameArray[0];
	}
	
	@Override
	public boolean hasCollided(int xDir, int yDir) {
		int xMin = 0;
		int xMax = 7;
		int yMin = 0;
		int yMax = 7;
		for (int x = xMin; x < xMax; x += 1) {
			if (isSolidTile(xDir, yDir, x, yMin)) {
				return true;
			}
		}
		for (int x = xMin; x < xMax; x += 1) {
			if (isSolidTile(xDir, yDir, x, yMax)) {
				return true;
			}
		}
		for (int y = yMin; y < yMax; y += 1) {
			if (isSolidTile(xDir, yDir, xMin, y)) {
				return true;
			}
		}
		for (int y = yMin; y < yMax; y += 1) {
			if (isSolidTile(xDir, yDir, xMax, y)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void tick() {
		if (xDir == 0 && yDir == 0) {
			xDir = 1;
		}
		move(xDir, yDir);
	}

	@Override
	public void render(Screen screen) {
		int flipX = (movingDir - 1) % 2;
		
		int laserColor = Colors.get(-1, -1, 142, 253);
		if (movingDir == 0) {
			screen.render(x, y - 3, 3 + 27 * 32, laserColor, false, false, 1);
		} else if (movingDir == 1) {
			screen.render(x, y - 3, 3 + 27 * 32, laserColor, false, true, 1);
		} else {
			screen.render(x, y - 3, 2 + 27 * 32, laserColor, flipX == 1, false, 1);
		}
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public String getSender() {
		return sender;
	}

}
