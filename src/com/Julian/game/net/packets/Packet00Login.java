package com.Julian.game.net.packets;

import com.Julian.game.net.GameClient;
import com.Julian.game.net.GameServer;

public class Packet00Login extends Packet {

	private String username;
	private int x, y;
	private int numSteps = 0;
	private boolean isMoving;
	private int movingDir = 1;

	public Packet00Login(byte[] data) {
		super(00);
		String[] dataArray = readData(data).split(",");
		this.username = dataArray[0];
		this.x = Integer.parseInt(dataArray[1]);
		this.y = Integer.parseInt(dataArray[2]);
		this.numSteps = Integer.parseInt(dataArray[3]);
		this.isMoving = Integer.parseInt(dataArray[4]) == 1;
		this.movingDir = Integer.parseInt(dataArray[5]);
	}

	public Packet00Login(String username, int x, int y, int numSteps, boolean isMoving, int movingDir) {
		super(00);
		this.username = username;
		this.x = x;
		this.y = y;
		this.numSteps = numSteps;
		this.isMoving = isMoving;
		this.movingDir = movingDir;
	}

	@Override
	public void writeData(GameClient client) {
		client.sendData(getData());
	}

	@Override
	public void writeData(GameServer server) {
		server.sendDataToAllClients(getData());
	}

	@Override
	public byte[] getData() {
		return ("00" + this.username + "," + getX() + "," + getY() + "," + this.numSteps + "," + (isMoving ? 1 : 0)
				+ "," + this.movingDir).getBytes();
	}

	public String getUsername() {
		return username;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return y;
	}
	
	public int getNumSteps() {
		return this.numSteps;
	}

	public boolean isMoving() {
		return this.isMoving;
	}

	public int getMovingDir() {
		return this.movingDir;
	}

}
