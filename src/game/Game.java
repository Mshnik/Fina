package game;

import gui.Frame;

import java.awt.Color;
import java.util.HashMap;
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

	/** Text for canceling a decision */
	public static final String CANCEL = "Cancel";
	
	/** Text for ending a turn */
	public static final String END_TURN = "End Turn";
	
	/** The players currently playing this game */
	private LinkedList<Player> players;
	
	/** The players remaining in the game */
	private HashMap<Player, Boolean> remainingPlayers;
	
	/** A hashmap from each player to the color to tint their units */
	private static final HashMap<Player, Color> PLAYER_COLORS  = new HashMap<Player, Color>();;
	
	/** The index of the current player.
	 * -1 before the game has started, in range [0 ... players.size() - 1] afterwards */
	private int index;
	
	/** The board that represents this game */
	public final Board board;
	
	/** True if this game currently has fog of war, false otherwise */
	private boolean fogOfWar;
	
	/** The Frame that is drawing this Game */
	private Frame frame;
	
	/** True if this game is currently running, false otherwise */
	private boolean running;
	
	public Game(Board b, boolean fog){
		board = b;
		fogOfWar = fog;
		players = new LinkedList<Player>();
		remainingPlayers = new HashMap<Player, Boolean>();
		running = false;
		index = -1;
	}
	
	/** Returns the Frame this game is drawn in */
	public Frame getFrame(){
		return frame;
	}
	
	/** Sets the Frame of this game */
	public void setFrame(Frame f){
		frame = f;
	}
	
	/** Instructs this game to repaint its frame */
	public void repaint(){
		frame.repaint();
	}
	
	/** Runs this game - has the players take turns until the game is over */
	@Override
	public void run() {
		running = true;
		index = 0;
		try{
			while(! isGameOver()){
				frame.repaint();
				nextTurn();
			}
		}
		finally{
			running = false;
		}
	}
	
	/** Adds this player to the game at the end of the player list.
	 * Throws a runtimeException if the game is already underway.
	 * Returns the number of players after adding p */
	protected int addPlayer(Player p, Color c) throws RuntimeException{
		if(running)
			throw new RuntimeException("Can't add " + p + " to " + this 
					+ " because game is already started");
		players.add(p);
		remainingPlayers.put(p, true);
		PLAYER_COLORS.put(p, c.darker());
		return players.size();
	}
	
	/** Returns the player who's currently taking his turn.
	 * Returns null if the game isn't running. */
	public Player getCurrentPlayer(){
		if(! running)
			return null;
		return players.getFirst();
	}
	
	/** Gets the color for the given player */
	public Color getColorFor(Player p){
		return PLAYER_COLORS.get(p);
	}
	
	/** Returns the index of the current player,
	 * which rotates through as the players rotate. */
	public int getPlayerIndex(){
		return index;
	}
	
	/** @return if there is fogOfWar in this game
	 */
	public boolean isFogOfWar() {
		return fogOfWar;
	}

	/** @param fogOfWar the fogOfWar to set.
	 * Also repaints if this causes a change.
	 */
	public void setFogOfWar(boolean fOG) {
		boolean oldFog = fogOfWar; 
		fogOfWar = fOG;
		if(oldFog != fOG)
			frame.repaint();
	}

	/** Returns true if this game is ended (one of the termination conditions
	 * is true), false otherwise.
	 * Returns true if there are more than 1 remaining player.
	 */
	public boolean isGameOver(){
		int i = 0;
		for(Boolean b : remainingPlayers.values()){
			if(b) i++;
		}
		return i <= 1;
	}
	
	/** Takes the current player's turn, then advances the player.
	 * Throws a runtimeException if this is called when the game isn't running. */
	private void nextTurn() throws RuntimeException{
		if(! running)
			throw new RuntimeException("Can't take turn for player - " + this + " isn't started");
		Player p = getCurrentPlayer();
		boolean ok = p.turnStart();
		if(ok){
			p.turn();
			p.turnEnd();
		}
		else{
			remainingPlayers.put(p, false);
		}
		
		//Move this player to the end, inc players index
		players.remove(0);
		players.add(p);
		index = (index + 1) % players.size();
	}
}
