package game;

import gui.GamePanel;

import java.util.LinkedList;

import board.Board;

/** Overall controlling class that unites all classes.
 * Should be run in its own thread, because some methods may cause arbitrary
 * waiting, hopefully not to clog the EVT
 * 
 * @author MPatashnik
 *
 */
public class Game implements Runnable{

	/** The players currently playing this game */
	private LinkedList<Player> players;
	
	/** The board that represents this game */
	public final Board board;
	
	/** The panel that is drawing this Game */
	private GamePanel panel;
	
	/** True if this game is currently running, false otherwise */
	private boolean running;
	
	public Game(Board b){
		board = b;
		players = new LinkedList<Player>();
		running = false;
	}
	
	/** Sets the GamePanel of this game */
	public void setGamePanel(GamePanel gp){
		panel = gp;
	}
	
	/** Runs this game - has the players take turns until the game is over */
	@Override
	public void run() {
		running = true;
		try{
			while(! isGameOver()){
				panel.repaint();
				nextTurn();
			}
		}
		finally{
			running = false;
		}
	}
	
	/** Adds this player to the game at the end of the player list.
	 * Throws a runtimeException if the game is already underway. */
	protected void addPlayer(Player p) throws RuntimeException{
		if(running)
			throw new RuntimeException("Can't add " + p + " to " + this 
					+ " because game is already started");
		players.add(p);
	}
	
	/** Returns the player who's currently taking his turn.
	 * Returns null if the game isn't running. */
	public Player getCurrentPlayer(){
		if(! running)
			return null;
		return players.getFirst();
	}
	
	/** Returns true if this game is ended (one of the termination conditions
	 * is true), false otherwise
	 */
	public boolean isGameOver(){
		//TODO
		return false;
	}
	
	/** Takes the current player's turn, then advances the player.
	 * Throws a runtimeException if this is called when the game isn't running. */
	private void nextTurn() throws RuntimeException{
		if(! running)
			throw new RuntimeException("Can't take turn for player - " + this + " isn't started");
		Player p = getCurrentPlayer();
		p.turnStart();
		p.turn();
		p.turnEnd();
		
		//Move this player to the end
		players.remove(0);
		players.add(p);
	}
}
