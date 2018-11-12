package com.Julian.game;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class InputHandler implements KeyListener {

	public InputHandler(Game game) {
		game.addKeyListener(this);
	}
	
	public class Key {
		public boolean pressed = false;
		
		public boolean isPressed() {
			return pressed;
		}
		
		public void toggle(boolean isPressed) {
			pressed = isPressed;
		}
	}
	
	//-----Movement Keys-----
	public Key up = new Key();
	public Key down = new Key();
	public Key left = new Key();
	public Key right = new Key();
	// Fire the laser
	public Key space = new Key();
	// Interact with item or self
	public Key D = new Key();
	
	@Override
	public void keyPressed(KeyEvent e) {
		toggleKey(e.getKeyCode(), true);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		toggleKey(e.getKeyCode(), false);
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		
	}
	
	public void toggleKey(int keyCode, boolean isPressed) {
		//-----Movement Keys-----
		if (keyCode == KeyEvent.VK_UP) {
			up.toggle(isPressed); 
		}
		if (keyCode == KeyEvent.VK_DOWN) { 
			down.toggle(isPressed); 
		}
		if (keyCode == KeyEvent.VK_LEFT) {
			left.toggle(isPressed); 
		}
		if (keyCode == KeyEvent.VK_RIGHT) {
			right.toggle(isPressed);
		}
		//----Fire the laser Key-----
		if (keyCode == KeyEvent.VK_SPACE) {
			space.toggle(isPressed);
		}
		// Interact with item or self Key
		if (keyCode == KeyEvent.VK_D) {
			D.toggle(isPressed);
		}
	}
	
}
