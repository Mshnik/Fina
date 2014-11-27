package game;

import java.util.LinkedList;

import board.Board;

/** Overall controlling class that unites all classes
 * 
 * @author MPatashnik
 *
 */
public class Game implements Runnable{

	/** The players currently playing this game */
	private LinkedList<Player> players;
	
	/** The board that represents this game */
	public final Board board;
	
	public Game(Board b){
		board = b;
		players = new LinkedList<Player>();
	}
	
	/** Runs this game - has the players take turns until the game is over */
	@Override
	public void run() {
		while(! isGameOver()){
			nextTurn();
		}
	}
	
	/** Adds this player to the game at the end of the player list */
	protected void addPlayer(Player p){
		players.add(p);
	}
	
	/** Returns the player who's currently taking his turn */
	public Player getCurrentPlayer(){
		return players.getFirst();
	}
	
	/** Returns true if this game is ended (one of the termination conditions
	 * is true), false otherwise
	 */
	public boolean isGameOver(){
		//TODO
		return false;
	}
	
	/** Takes the current player's turn, then advances the player */
	private void nextTurn(){
		Player p = getCurrentPlayer();
		p.turnStart();
		p.turn();
		p.turnEnd();
		
		//Move this player to the end
		players.remove(0);
		players.add(p);
	}
}
