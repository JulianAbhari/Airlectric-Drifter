package com.Julian.game.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.Julian.game.Game;
import com.Julian.game.entities.Laser;
import com.Julian.game.entities.PlayerMP;
import com.Julian.game.net.packets.Packet;
import com.Julian.game.net.packets.Packet.PacketTypes;
import com.Julian.game.net.packets.Packet00Login;
import com.Julian.game.net.packets.Packet01Disconnect;
import com.Julian.game.net.packets.Packet02Move;
import com.Julian.game.net.packets.Packet03LaserFired;

// The way all of this multiplayer support is being programmed is through UDP not TCP. 
// So there will be a lot of low level programming with packets, 
// unlike TCP where you just need to worry about sockets. 
// The reason why I'm using UDP instead of TCP is because if a packet is lost in TCP, 
// then the servers will wait until the packet is resent then everything else will 
// all happen at once. With UDP, if a packet is lost then it will just move on to 
// the next packet and not worry about the lost packets.
public class GameClient extends Thread {

	private InetAddress ipAddress;
	private DatagramSocket socket;
	private Game game;
	private int port;

	public GameClient(Game game, String ipAddress, int port) {
		this.game = game;
		try {
			this.socket = new DatagramSocket();
			this.ipAddress = InetAddress.getByName(ipAddress);
			this.port = port;
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (true) {
			// When the program is running it will listen for an array of bytes of data that
			// was received from the Server
			byte[] data = new byte[1024];
			// This is the actual packet that's going to be sent to and from the server.
			DatagramPacket packet = new DatagramPacket(data, data.length);
			// This line is accepting the data that it received
			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			parsePacket(packet.getData(), packet.getAddress(), packet.getPort());
		}
	}

	private void parsePacket(byte[] data, InetAddress address, int port) {
		String message = new String(data).trim();
		PacketTypes type = Packet.lookupPacket(message.substring(0, 2));
		Packet packet = null;

		switch (type) {
		default:
		case INVALID:
			break;
		case LOGIN:
			packet = new Packet00Login(data);
			handleLogin(((Packet00Login) packet), address, port);
			break;
		case DISCONNECT:
			packet = new Packet01Disconnect(data);
			System.out.println("CLIENT: [" + address.getHostAddress() + ": " + port + "] "
					+ ((Packet01Disconnect) packet).getUsername() + " has left the game...");
			game.level.removePlayerMP(((Packet01Disconnect) (packet)).getUsername());
			break;
		case MOVE:
			packet = new Packet02Move(data);
			handleMove(((Packet02Move) packet));
			break;
		case LASERFIRED:
			packet = new Packet03LaserFired(data);
			addLaser((Packet03LaserFired) packet);
			break;
		}
	}

	// This function allows us to send a byte[] of data
	public void sendData(byte[] data) {
		// This creates a new packet sent to the ipAddress with the port number
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleLogin(Packet00Login packet, InetAddress address, int port) {
		System.out.println("CLIENT: [" + address.getHostAddress() + ": " + port + "] " + packet.getUsername()
				+ " has joined the game...");
		PlayerMP player = new PlayerMP(game.level, packet.getX(), packet.getY(), packet.getUsername(), address, port);
		game.level.addEntity(player);
		game.level.movePlayer(packet.getUsername(), packet.getX(), packet.getY(), packet.getNumSteps(),
				packet.isMoving(), packet.getMovingDir());
	}

	private void handleMove(Packet02Move packet) {
		game.level.movePlayer(packet.getUsername(), packet.getX(), packet.getY(), packet.getNumSteps(),
				packet.isMoving(), packet.getMovingDir());
	}
	
	private void addLaser(Packet03LaserFired packet) {
		Laser laser = new Laser(game.level, packet.getSender(), packet.getX(), packet.getY(), packet.getXDir(), packet.getYDir());
		game.level.addEntity(laser);
	}
}
