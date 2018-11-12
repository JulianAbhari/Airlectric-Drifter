package com.Julian.game;

public class JMath {
	// Purpose: Re-maps a number from one range to another. For example
	// the number '25' is converted from a value in the range 0..100 into
	// a value that ranges from the left edge (0) to the right edge (width) of
	// the screen. In this example I will have the width be 600. So 25 mapped to
	// 600 will end up being 150. However, it can also map values inverted. For
	// example 25, who's min is 0 and max is 100 mapped to max 0, min 100, is
	// 75.

	// Contract: map: float (value), float (valMin), float (valMax), float
	// (mapMax) -> float
	public static float map(float value, float valMin, float valMax, float mapMin, float mapMax) {
		// Inventory:
		// value - a float that will then be converted to the range specified.
		// valMin - a float that is the lower bound of the value's current range
		// valMax - a float that is the upper bound of the value's current range
		// mapMin - a float that is the lower bound of the value's target range
		// mapMax - a float that is the upper bound of the value's target range
		return mapMin + (mapMax - mapMin) * ((value - valMin) / (valMax - valMin));
	}

	public static float dist(double startingX, double startingY, double endingX, double endingY) {
		float distance = (float) Math.sqrt(Math.pow((endingX - startingX), 2.0) + Math.pow((endingY - startingY), 2.0));
		return distance;
	}
}
