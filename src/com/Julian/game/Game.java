package com.Julian.game;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.Julian.game.entities.Player;
import com.Julian.game.entities.PlayerMP;
import com.Julian.game.gfx.Screen;
import com.Julian.game.gfx.SpriteSheet;
import com.Julian.game.level.Level;
import com.Julian.game.net.GameClient;
import com.Julian.game.net.GameServer;
import com.Julian.game.net.packets.Packet00Login;

/**
 * Fixed Laser Collision 04/20/18
 * 
 * Working Multiplayer 04/12/18
 * 
 * Airlectric Drifter 03/20/18
 * 
 * @author Julian Abhari
 */

// A canvas is a blank Java workspace which includes all the JFrames,
// JComponents and whatnot
public class Game extends Canvas implements Runnable {

	private static final long serialVersionUID = 1L;

	public static final int WIDTH = 220;
	public static final int HEIGHT = (WIDTH / 12) * 9;
	public static final int SCALE = 4;
	public static final String NAME = "Airlectric Drifter";
	public static Game game;

	// The JFrame is a writable area that we can put stuff on
	// It's part of a window
	public JFrame frame;

	public boolean running = false;
	public int tickCount = 0;

	private BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	// This is representing how many pixels are inside the BufferedImage
	// We can update this pixels variable with whatever we want and it will update
	// that image
	private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
	// This will contain information of the 4 colors of pixels within the tiles
	public int[] colors = new int[6 * 6 * 6];

	private Screen screen;
	public static InputHandler input;
	public WindowHandler windowHandler;

	public Level level;
	public Player player;

	public GameClient socketClient;
	public GameServer socketServer;

	public Game() {
		// Calling the Canvas functions that will keep the size of the JFrame or canvas
		// at one size
		setMinimumSize(new Dimension(160 * SCALE, 120 * SCALE));
		setMaximumSize(new Dimension(160 * SCALE, 120 * SCALE));
		setPreferredSize(new Dimension(160 * SCALE, 120 * SCALE));

		frame = new JFrame(NAME);
		// This terminates the program when the user clicks the red exit button
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// This is a way of laying out the JFrame
		frame.setLayout(new BorderLayout());

		// This is centering and adding the canvas to the JFrame
		frame.add(this, BorderLayout.CENTER);
		// This sets the frame so the sizes are at the preferred size. It keeps
		// everything sized correctly.
		frame.pack();

		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public void init() {
		game = this;
		int index = 0;
		for (int r = 0; r < 6; r += 1) {
			for (int g = 0; g < 6; g += 1) {
				for (int b = 0; b < 6; b += 1) {
					// This is the red shade from 0-5
					int rr = (r * 255 / 5);
					// This is the green shade from 0-5
					int gg = (g * 255 / 5);
					// This is the blue shade from 0-5
					int bb = (b * 255 / 5);
					// This populates the color array with color values.
					colors[index] = rr << 16 | gg << 8 | bb;
					index += 1;
				}
			}
		}

		screen = new Screen(WIDTH, HEIGHT, new SpriteSheet("/SpriteSheet.png"));
		input = new InputHandler(this);
		windowHandler = new WindowHandler(this);
		level = new Level("/Levels/rocky_terrain.png");
		
		int playerX = (int) JMath.map((float) Math.random(), 0, 1, 0, Game.game.level.width * 8);
		int playerY = (int) JMath.map((float) Math.random(), 0, 1, 0, Game.game.level.height * 8);
		
		player = new PlayerMP(level, playerX, playerY, input,
				JOptionPane.showInputDialog(this, "Please enter a username"), null, -1);
		level.addEntity(player);

		Packet00Login loginPacket = new Packet00Login(player.getUsername(), player.x, player.y, player.getNumSteps(),
				player.isMoving(), player.getMovingDir());
		if (socketServer != null) {
			socketServer.addConnection((PlayerMP) player, loginPacket);
		}
		loginPacket.writeData(socketClient);
	}

	public synchronized void start() {
		running = true;

		// Initialize Server Options
		Object[] choices = { "Start a Server", "Join a Server" };
		Object defaultChoice = choices[0];
		// If the user decides to create a server
		if (JOptionPane.showOptionDialog(this, "Play with friends!", "Multiplayer Options",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, choices, defaultChoice) == 0) {
			socketServer = new GameServer(this, 1331);
			socketServer.start();
		}

		String ipAddress = JOptionPane.showInputDialog("Please enter the IP address");
		socketClient = new GameClient(this, ipAddress, 1331);
		socketClient.start();

		// The Thread is an instance of Runnable
		// When the thread has started it's going to run the run function which has the
		// main game loop
		// This is creating a new thread so that we don't take away from the system's
		// main thread and we can just start a side thread which allows us to multitask
		new Thread(this).start();
	}

	public synchronized void stop() {
		running = false;
	}

	public void run() {
		// We want to limit the run loop to only 60 updates a second because otherwise
		// the game would run faster on other machines

		// This gets the current time in nanoseconds
		long lastTime = System.nanoTime();
		// This is going to come out with how many nanoseconds are in one tick
		double nsPerTick = 1000000000.0 / 60.0;

		int ticks = 0;
		int frames = 0;

		long lastTimer = System.currentTimeMillis();
		// Delta is how many nano-seconds have gone by so far. Once we hit 1 we will
		// subtract 1 from it
		double delta = 0;

		init();

		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / nsPerTick;
			lastTime = now;
			boolean shouldRender = true;

			while (delta >= 1) {
				ticks += 1;
				tick();
				delta -= 1;
				shouldRender = true;
			}

			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (shouldRender) {
				frames += 1;
				render();
			}

			if (System.currentTimeMillis() - lastTimer >= 1000) {
				lastTimer += 1000;
				frame.setTitle(NAME + " | Ticks: " + ticks + ", Frames: " + frames);
				frames = 0;
				ticks = 0;
			}
		}
	}

	// This updates the game, it updates the internal variables and the logic of the
	// game
	public void tick() {
		tickCount += 1;
		level.tick();
	}

	public void render() {
		// This is an object to organize the data on this canvas.
		// It allows you to organize what you're putting on here.
		BufferStrategy bs = getBufferStrategy();
		// If the bufferStrategy is null then we're going to create one
		if (bs == null) {
			// This is tripple buffering which should be good at reducing tearing (where you
			// can see the process when you clear the image and replace it)
			createBufferStrategy(3);
			return;
		}

		// These are the coordinates of the center of the screen
		int xOffset = player.x - (screen.width / 2);
		int yOffset = player.y - (screen.height / 2);

		level.renderTiles(screen, xOffset, yOffset);

		level.renderEntities(screen);

		for (int y = 0; y < screen.height; y++) {
			for (int x = 0; x < screen.width; x++) {
				int ColourCode = screen.pixels[x + y * screen.width];
				if (ColourCode < 255) {
					pixels[x + y * WIDTH] = colors[ColourCode];
				}
			}
		}

		Graphics g = bs.getDrawGraphics();
		// This is drawing the BufferedImage image
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
		// This will free up the memory and resources of the graphics
		g.dispose();
		// This shows the contents of the buffer
		bs.show();
	}

	public static void main(String[] args) {
		new Game().start();
	}

}
