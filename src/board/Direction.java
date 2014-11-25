package board;

import gui.KeyboardListener;

/** Cardinal directions */
public enum Direction {
	LEFT,
	UP,
	RIGHT,
	DOWN;

	/** Return the direction for the given keycode, or null
	 * if not a direction
	 * @param code - the keycode. See KeyboardListener constansts.
	 * @return - a direction, if possible
	 */
	public static Direction fromKeyCode(int code){
		switch(code) { 
		case KeyboardListener.UP:	 return Direction.UP;
		case KeyboardListener.DOWN:  return Direction.DOWN;
		case KeyboardListener.LEFT:  return Direction.LEFT;
		case KeyboardListener.RIGHT: return Direction.RIGHT;
		default: 					 return null;
		}
	}
}
