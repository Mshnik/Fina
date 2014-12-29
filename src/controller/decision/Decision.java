package controller.decision;

import java.util.ArrayList;
import java.util.Collection;

public class Decision extends ArrayList<Choice>{

	private static final long serialVersionUID = 1L;

	/** The types of decisions that can be made */
	public enum DecisionType{
		ACTION_DECISION,
		SUMMON_DECISION,
		CAST_DECISION,
		NEW_ABILITY_DECISION,
		END_OF_TURN_DECISION
	}
	
	/** The type of decision currently in progress - what kind of decision it is making. Null if none */
	private DecisionType type;
	
	/** True iff the current decision is manditory. false if no decision is underway */
	private boolean manditory;
	
	
	public Decision(DecisionType type, boolean manditory, Collection<Choice> choices){
		this(type, manditory, choices.toArray(new Choice[choices.size()]));
	}
	
	public Decision(DecisionType type, boolean manditory, Choice...choices ){
		for(Choice c : choices){
			if(c != null) add(c);
		}
		this.type = type;
		this.manditory = manditory;
	}
	
	
	/** Sets this decision as the choice's decision in addition to adding it */
	@Override
	public boolean add(Choice c){
		c.decision = this;
		return super.add(c);
	}
	
	/** Removes this as the choice's decision in addition to removing it */
	@Override
	public boolean remove(Object o){
		boolean ok = super.remove(o);
		if(ok && o instanceof Choice) ((Choice) o).decision = null;
		return ok;
	}
	
	/** Removes this as the choice's decision in addition to removing it */
	@Override
	public Choice remove(int i){
		Choice removed = super.remove(i);
		if(removed != null && removed instanceof Choice) ((Choice) removed).decision = null;
		return removed;
	}
	
	/** Returns the type of decision */
	public DecisionType getType(){
		return type;
	}
	
	/** Returns iff this decision is manditory */
	public boolean isManditory(){
		return manditory;
	}
}
