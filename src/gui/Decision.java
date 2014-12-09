package gui;

import game.MatrixElement;

/** Represents an element in a decisionPanel that can be selected */
public class Decision implements MatrixElement{
	
	/** The index of this Decision in its decisionPanel */
	private int index;
	
	/** True if this is selectable, false otherwise *
	 * 
	 */
	private boolean selectable;
	
	/** The string drawn and represented by this actionDecision */
	private String message;
	
	public Decision(int index, String message){
		this(index, true, message);
	}
	
	public Decision(int index, boolean selectable, String message){
		this.index = index;
		this.selectable = selectable;
		this.message = message;
	}
	
	/** Returns the index of this decision */
	public int getIndex(){
		return index;
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
	
	/** Returns iff this is selectable */
	public boolean isSelectable(){
		return selectable;
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
