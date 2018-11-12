package com.Julian.game.net.packets;

import com.Julian.game.net.GameClient;
import com.Julian.game.net.GameServer;

public abstract class Packet {

	public static enum PacketTypes {
		INVALID(-1), LOGIN(00), DISCONNECT(01), MOVE(02), LASERFIRED(03);

		private int packetId;

		private PacketTypes(int packetId) {
			this.packetId = packetId;
		}

		public int getId() {
			return packetId;
		}
	}

	public byte packetId;

	public Packet(int packetId) {
		this.packetId = (byte) packetId;
	}

	public abstract void writeData(GameClient client);

	public abstract void writeData(GameServer server);
	
	public String readData(byte[] data) {
		String message = new String(data).trim();
		return message.substring(2);
	}
	
	public abstract byte[] getData();
	
	public static PacketTypes lookupPacket(String packetId) {
		try {
			return lookupPacket(Integer.parseInt(packetId));
		} catch (NumberFormatException e) {
			return PacketTypes.INVALID;
		}
	}
	
	public static PacketTypes lookupPacket(int id) {
		for (PacketTypes packetType : PacketTypes.values()) {
			if (packetType.getId() == id) {
				return packetType;
			}
		}
		return PacketTypes.INVALID;
	}
}
