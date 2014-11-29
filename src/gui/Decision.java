package gui;

import game.MatrixElement;

/** Represents an element in a decisionPanel that can be selected */
public class Decision implements MatrixElement{
	
	/** The index of this Decision in its decisionPanel */
	private int index;
	
	/** The string drawn and represented by this actionDecision */
	private String message;
	
	public Decision(int index, String message){
		this.index = index;
		this.message = message;
	}
	
	/** Returns the index of this Decision in its decisionPanel */
	@Override
	public int getRow() {
		return index;
	}

	/** Returns 0 */
	@Override
	public int getCol() {
		return 0;
	}
	
	/** Returns the message of this string */
	public String getMessage(){
		return message;
	}

	/** Returns a simple toString of the index and the message */
	@Override
	public String toString(){
		return message + " : " + index;
	}
	
}
