package gui;

import gui.decision.Decision;
import gui.decision.DecisionPanel;
import gui.panel.GamePanel;
import gui.panel.GamePanel.SummonType;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import board.Direction;

/** Manager for all keyboard input. Only singleton instance is allowed. */
public class KeyboardListener implements KeyListener{

	/** The up key code */
	public static final int UP = KeyEvent.VK_UP;
	/** The left key code */
	public static final int LEFT = KeyEvent.VK_LEFT;
	/** The down key code */
	public static final int DOWN = KeyEvent.VK_DOWN;
	/** The right key code */
	public static final int RIGHT = KeyEvent.VK_RIGHT;
	/** The "a" (main button / weak confirm button) key code */
	public static final int A = KeyEvent.VK_Z;
	/** The "b" (secondary button / weak decline button) key code */
	public static final int B = KeyEvent.VK_X;
	/** The "start" (tertiary / strong confirm button) key code */
	public static final int START = KeyEvent.VK_ENTER;
	/** The "esc" (tertiary / strong decline button) key code */
	public static final int ESC = KeyEvent.VK_ESCAPE;

	/** The singleton instance of keyboardListener to be used by all frames */
	protected static final KeyboardListener instance = new KeyboardListener();

	/** Constructor for KeyboardListener */
	private KeyboardListener(){}

	/** The frame this KeyboardListener is listening to */
	private Frame frame;

	/** Set the Frame the KeyboardListener is listening to */
	public static void setFrame(Frame f){
		instance.frame = f;
		instance.frame.addKeyListener(instance);
	}

	/** Unused */
	@Override
	public void keyTyped(KeyEvent e) {}

	/** Handle key presses */
	@Override
	public void keyPressed(KeyEvent e) {
		GamePanel gp = frame.gamePanel;
		int keyCode = e.getKeyCode();

		//Cursors - respond to arrow keys
		Direction d = Direction.fromKeyCode(keyCode);
		if(frame.getActiveCursor() != null && d != null){
			@SuppressWarnings("rawtypes")
			Cursor c = frame.getActiveCursor();
			c.move(d);
		}

		//Check different toggles - if non-none, handle
		if((keyCode == A || keyCode == B)){
			switch(gp.getToggle()){
				//No toggle currently open. Maybe open one.
			case NONE:
				if(keyCode == A){
					gp.startActionDecision();
				} else{
					gp.startEndTurnDecision();
				}
				break;
				// Process the decision.
			case DECISION:
				if(keyCode == A){
					switch(gp.getDecisionPanel().type){
					case ACTION_DECISION:
						gp.processActionDecision();
						break;
					case END_OF_TURN_DECISION:
						gp.processEndTurnDecision();
						break;
					case SUMMON_DECISION:
						gp.startSummonSelection((Decision) frame.getActiveCursor().getElm());
						break;
					case NEW_ABILITY_DECISION:
						gp.processNewAbilityDecision();
						break;
					default:
						break;
					}
				} else{
					if(! gp.getDecisionPanel().manditory){
						boolean resetDecision = gp.getDecisionPanel().type == DecisionPanel.Type.SUMMON_DECISION;
						gp.cancelDecision();
						if(resetDecision) gp.startActionDecision();
					} else{
						//TODO - tried to cancel manditory decision.
					}
				}
				break;
			case SUMMON_SELECTION:
				if(keyCode == A) {
					gp.processSummonSelection();
				}
				else{
					boolean summoningUnit = gp.getSummonType().equals(SummonType.UNIT);
					gp.cancelSummonSelection();
					if(summoningUnit) gp.startSummonCombatantDecision();
					else gp.startSummonBuildingDecision();
				}
				break;
			case ATTACK_SELECTION:
				if(keyCode == A) {
					gp.processAttackSelection();
				}
				else{
					gp.cancelAttackSelection();
					gp.startActionDecision();
				}
				break;
				//Path selection - should only be the case after pathSelection already started
			case PATH_SELECTION:
				if(keyCode == A) {
					gp.processPathSelection();
				}
				else{
					gp.cancelPathSelection();
					gp.startActionDecision();
				}
				break;
			default:
				break;

			}




		}
	}

	/** Unused */
	@Override
	public void keyReleased(KeyEvent e) {};

}
