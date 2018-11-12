package com.Julian.game.net.packets;

import com.Julian.game.net.GameClient;
import com.Julian.game.net.GameServer;

public class Packet03LaserFired extends Packet {
	
	private String sender;
	private int x, y;
	private int xDir, yDir;
	public String username;
	
	public Packet03LaserFired(byte[] data) {
		super(03);
		String[] dataArray = readData(data).split(",");
		this.sender = dataArray[0];
		this.x = Integer.parseInt(dataArray[1]);
		this.y = Integer.parseInt(dataArray[2]);
		this.xDir = Integer.parseInt(dataArray[3]);
		this.yDir = Integer.parseInt(dataArray[4]);
	}
	
	public Packet03LaserFired(String sender, int x, int y, int xDir, int yDir) {
		super(03);
		this.sender = sender;
		this.x = x;
		this.y = y;
		this.xDir = xDir;
		this.yDir = yDir;
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
		return ("03" + getSender() + "," + getX() + "," + getY() + "," + getXDir() + "," + getYDir()).getBytes();
	}
	
	public String getSender() {
		return this.sender;
	}
	
	public int getX() {
		return this.x;
	}

	public int getY() {
		return y;
	}
	
	public int getXDir() {
		return xDir;
	}
	
	public int getYDir() {
		return yDir;
	}

}
