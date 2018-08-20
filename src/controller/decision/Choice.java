package controller.decision;

import controller.game.MatrixElement;

public final class Choice implements MatrixElement{
	/** The standard seperator character for name to mana cost */
	public static final char SEPERATOR = ':';

	/** True if this is selectable, false otherwise */
	private boolean selectable;

	/** The object this choice corresponds to (if any) */
	private Object val;
	
	/** The string drawn and represented by this actionDecision */
	private String message;
	
	/** The decision this choice belongs to */
	protected Decision decision;

	public Choice(boolean selectable, String message){
		this(selectable, message, null);
	}
	
	public Choice(boolean selectable, String message, Object val){
		this.selectable = selectable;
		this.message = message;
		this.val = val;
	}

	/** Returns the index of this decision */
	public int getIndex(){
		return decision.indexOf(this);
	}

	/** Returns the index of this Decision in its decisionPanel */
	@Override
	public int getRow() {
		return getIndex();
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
	
	/** Returns the object associated with this choice. May be null if none */
	public Object getVal(){
		return val;
	}

	/** Returns the message split before the first seperator. If no such, returns whole message */
	public String getShortMessage(){
		if(getMessage().indexOf(SEPERATOR) == -1)
			return getMessage();
		
		return getMessage().substring(0, getMessage().indexOf(SEPERATOR));
	}
	
	/** Returns the message of this string */
	public String getMessage(){
		return message;
	}

	/** Returns a simple toString of the index and the message */
	@Override
	public String toString(){
		return message + " : " + getIndex();
	}
}