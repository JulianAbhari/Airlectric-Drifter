package com.Julian.game.entities;

import java.util.ArrayList;

import com.Julian.game.Game;
import com.Julian.game.InputHandler;
import com.Julian.game.JMath;
import com.Julian.game.gfx.Colors;
import com.Julian.game.gfx.Font;
import com.Julian.game.gfx.Screen;
import com.Julian.game.level.Level;
import com.Julian.game.level.tiles.BasicInteractiveTile;
import com.Julian.game.level.tiles.BasicTriggerTile;
import com.Julian.game.net.packets.Packet02Move;
import com.Julian.game.net.packets.Packet03LaserFired;

public class Player extends Mob {

	private InputHandler input;
	private int color = Colors.get(-1, 054, 555, -1);
	private int scale = 1;
	protected boolean isSwimming = false;
	protected boolean didJump = false;
	protected boolean nearInteractive = true;
	private int tickCount = 0;
	private int lastTickCount = 0;
	private String username;
	private ArrayList<Laser> firedLasers;

	public int xFace = 0;
	public int yFace = 0;

	public Player(Level level, int x, int y, InputHandler input, String username) {
		super(level, "Player", x, y, 1);
		this.input = input;
		this.username = username;
		firedLasers = new ArrayList<Laser>();
	}

	// This updates the game, it updates the internal variables and the logic of the
	// game
	public void tick() {
		int xDir = 0;
		int yDir = 0;

		if (didJump) {
			lastTickCount = tickCount;
		}
		if (lastTickCount >= (tickCount - 30)) {
			this.speed = 2;
		} else {
			this.speed = 1;
		}

		if (didCollideWithLaser()) {
			this.x = (int) JMath.map((float) Math.random(), 0, 1, 0, Game.game.level.width * 8);
			this.y = (int) JMath.map((float) Math.random(), 0, 1, 0, Game.game.level.height * 8);
			Packet02Move packet = new Packet02Move(this.getUsername(), this.x, this.y, this.numSteps, this.isMoving,
					this.movingDir);
			packet.writeData(Game.game.socketClient);
		}
		
		if (input != null) {
			if (input.up.isPressed()) {
				yDir -= 1;
				yFace = -1;
				xFace = 0;
			}
			if (input.down.isPressed()) {
				yDir += 1;
				yFace = 1;
				xFace = 0;
			}
			if (input.left.isPressed()) {
				xDir -= 1;
				xFace = -1;
				yFace = 0;
			}
			if (input.right.isPressed()) {
				xDir += 1;
				xFace = 1;
				yFace = 0;
			}
			// Check if the player is trying to interact with an interactive tile
			if (input.D.isPressed() && level.getTile((this.x >> 3) + xFace, (this.y >> 3) + yFace).isInteractive()) {
				((BasicInteractiveTile) level.getTile((this.x >> 3) + xFace, (this.y >> 3) + yFace)).doAction();
			}
			// Check if the player is trying to fire the laser
			if (input.space.isPressed()) {
				if (firedLasers.size() <= 1) {
					firedLasers.add(new Laser(level, this.username, this.x, this.y, xFace, yFace));
					Packet03LaserFired packet = new Packet03LaserFired(this.username, this.x, this.y, xFace, yFace);
					packet.writeData(Game.game.socketClient);
				}
			}
		}

		if (xDir != 0 || yDir != 0) {
			move(xDir, yDir);
			isMoving = true;

			Packet02Move packet = new Packet02Move(this.getUsername(), this.x, this.y, this.numSteps, this.isMoving,
					this.movingDir);
			packet.writeData(Game.game.socketClient);

		} else {
			isMoving = false;
		}

		// Check if the player is near an interactive tile, if so, then pull up
		// interacting prompt
		if (level.getTile((this.x >> 3) + xFace, (this.y >> 3) + yFace).isInteractive()) {
			nearInteractive = true;
		}
		// Set nearInteractive to false if the player isn't near an interactive tile
		// anymore
		if (!(level.getTile((this.x >> 3) + xFace, (this.y >> 3) + yFace).isInteractive())) {
			nearInteractive = false;
		}

		// Check if the player is on a trigger tile
		if (level.getTile(this.x >> 3, this.y >> 3).isTrigger()) {
			// The tile will be triggered if the player is on it and the tile can be
			// triggered
			((BasicTriggerTile) level.getTile(this.x >> 3, this.y >> 3)).doAction();
		}

		// Check if the player is trying to get in the water
		if (level.getTile((this.x >> 3), (this.y >> 3)).getId() == 3) {
			isSwimming = true;
		}
		// Check if the player is trying to get out of the water
		if (isSwimming && level.getTile((this.x >> 3), (this.y >> 3)).getId() != 3) {
			isSwimming = false;
		}

		if (!firedLasers.isEmpty()) {
			for (int i = 0; i < firedLasers.size(); i += 1) {
				firedLasers.get(i).tick();
				if (firedLasers.get(i).hasCollided) {
					firedLasers.remove(firedLasers.get(i));
				}
			}
		}

		tickCount += 1;
	}

	private boolean didCollideWithLaser() {
		for (int i = 0; i < Game.game.level.getEntities().size(); i += 1) {
			if (Game.game.level.getEntities().get(i) instanceof Laser) {
				Laser laser = (Laser) Game.game.level.getEntities().get(i);
				if (!(laser.getSender().trim().equals(username.trim()))) {
					if ((laser.x >= this.x - 7 && laser.x <= this.x + 7) && (laser.y >= this.y - 7 && laser.y <= this.y + 7)) {
						System.out.println(this.username + " was blasted by " + laser.getSender() + "'s laser");
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void move(int xDir, int yDir) {
		// This checks if they're not 0
		// They should only move in 1 direction at a time because if they move
		// diagonally they'll move 2 blocks at a time
		if (xDir != 0 && yDir != 0) {
			move(xDir, 0);
			move(0, yDir);
			// The reason why I'm removing one from the 'numSteps' is because it's counting
			// these two move functions diagonally as 2 steps which is not truer
			numSteps -= 1;
			return;
		}
		numSteps += 1;
		if (!hasCollided(xDir, yDir)) {
			didJump = false;
		} else {
			didJump = true;
		}

		if (!hasHitVoid(xDir, yDir)) {
			// When the player is going up the movingDir is set to 0
			if (yDir < 0)
				movingDir = 0;
			// When the player is going down the movingDir is set to 1
			if (yDir > 0)
				movingDir = 1;
			// When the player is moving to the left the movingDir is set to 2
			if (xDir < 0)
				movingDir = 2;
			// When the player is moving to the right the movingDir is set to 3
			if (xDir > 0)
				movingDir = 3;
			// This is moving the players position by whatever the direction is (a value
			// between one and negative one) multiplied by the speed.
			x += xDir * speed;
			y += yDir * speed;
		}
	}

	public void render(Screen screen) {
		int xTile = 0;
		int yTile = 28;
		int walkingSpeed = 4;
		int flipTop = (numSteps >> walkingSpeed) & 1;
		int flipBottom = (numSteps >> walkingSpeed) & 1;

		// When the player is facing towards the camera the x place for getting the Tile
		// pixels increases by 2 (because the player is 2 tiles wide)
		if (movingDir == 1) {
			xTile += 2;
			flipTop = (movingDir - 1) % 2;
		} else if (movingDir > 1) {
			xTile += 4 + ((numSteps >> walkingSpeed) & 1) * 2;
			flipTop = (movingDir - 1) % 2;
			flipBottom = (movingDir - 1) % 2;
		}

		int modifier = 8 * scale;
		int xOffset = x - modifier / 2;
		int yOffset = y - modifier / 2 - 4;

		yOffset = glide(yOffset);

		if (isSwimming) {
			int waterColor = 0;
			if (tickCount % 60 < 15) {
				waterColor = Colors.get(-1, -1, 225, -1);
			} else if (tickCount % 60 >= 15 && tickCount % 60 < 30) {
				waterColor = Colors.get(-1, 225, 115, -1);
			} else if (tickCount % 60 >= 30 && tickCount % 60 < 45) {
				waterColor = Colors.get(-1, 115, -1, 225);
			} else {
				waterColor = Colors.get(-1, 225, 115, -1);
			}
			screen.render(xOffset, yOffset + 5, 0 + 27 * 32, waterColor, 1);
			screen.render(xOffset + 8, yOffset + 5, 0 + 27 * 32, waterColor, true, false, 1);
		}

		screen.render(xOffset + (modifier * flipTop), yOffset, xTile + yTile * 32, color, flipTop == 1, false, scale);
		screen.render(xOffset + modifier - (modifier * flipTop), yOffset, (xTile + 1) + yTile * 32, color, flipTop == 1,
				false, scale);
		screen.render(xOffset + (modifier * flipBottom), yOffset + modifier, xTile + (yTile + 1) * 32, color,
				flipBottom == 1, false, scale);
		screen.render(xOffset + modifier - (modifier * flipBottom), yOffset + modifier, (xTile + 1) + (yTile + 1) * 32,
				color, flipBottom == 1, false, scale);

		if (nearInteractive) {
			screen.render(xOffset - 8, yOffset, 1 + 27 * 32, Colors.get(-1, 323, 452, 555), 1);
		}

		if (username != null) {
			Font.render(username, screen, xOffset - (((username.length() - 1) / 2) * 8), yOffset - 10,
					Colors.get(-1, -1, -1, 555), 1);
		}

		if (!firedLasers.isEmpty()) {
			for (int i = 0; i < firedLasers.size(); i += 1) {
				firedLasers.get(i).render(screen);
			}
		}
	}

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

	public boolean hasHitVoid(int xDir, int yDir) {
		int xMin = 0;
		int xMax = 7;
		int yMin = 0;
		int yMax = 7;
		for (int x = xMin; x < xMax; x += 1) {
			if (isVoidTile(xDir, yDir, x, yMin)) {
				return true;
			}
		}
		for (int x = xMin; x < xMax; x += 1) {
			if (isVoidTile(xDir, yDir, x, yMax)) {
				return true;
			}
		}
		for (int y = yMin; y < yMax; y += 1) {
			if (isVoidTile(xDir, yDir, xMin, y)) {
				return true;
			}
		}
		for (int y = yMin; y < yMax; y += 1) {
			if (isVoidTile(xDir, yDir, xMax, y)) {
				return true;
			}
		}
		return false;
	}

	public int glide(int yOffset) {
		if (didJump) {
			return yOffset -= 2;
		}
		if (tickCount % 60 < 15) {
			return yOffset += 1;
		} else if (tickCount % 60 >= 15 && tickCount % 60 < 30) {
			return yOffset;
		} else if (tickCount % 60 >= 30 && tickCount % 60 < 45) {
			return yOffset += 1;
		}
		return yOffset;
	}

	public String getUsername() {
		return this.username;
	}

}
