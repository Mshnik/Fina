package board;

import gui.KeyboardListener;

/** Cardinal directions */
public enum Direction {
	LEFT,
	UP,
	RIGHT,
	DOWN;

	/** The row difference caused by this direction*/
	public int dRow(){
		switch(this){
		case DOWN: return 1;
		case LEFT: return 0;
		case RIGHT: return 0;
		case UP:	return -1;
		default:	return 0;
		}
	}
	
	/** The col difference caused by this direction */
	public int dCol(){
		switch(this){
		case DOWN: return 0;
		case LEFT: return -1;
		case RIGHT: return 1;
		case UP: return 0;
		default: return 0;
		}
	}
	
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
