package game;

/** A human controlled player */
public class HumanPlayer extends Player {

	/** Wait time between endAction checks */
	public static final int SLEEP_TIME = 500;

	/** True while the player is performing action, false once they perform the
	 * end turn action.
	 */
	private boolean takingAction;
	
	/** Constructs a human player for the given game */
	public HumanPlayer(Game g) {
		super(g);
	}

	@Override
	protected void turn() {
		takingAction = true;
		while(takingAction){
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				takingAction = false;
			}
		}
	}
	
	/** Called by the gui when the player selects the end of turn action.
	 * Causes the turn loop to end. 
	 */
	@Override
	public void turnEnd(){
		takingAction = false;
	}

}
