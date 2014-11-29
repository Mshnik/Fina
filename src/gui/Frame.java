package gui;

import java.awt.BorderLayout;
import java.util.EmptyStackException;
import java.util.Stack;

import javax.swing.JFrame;

import unit.dummy.*;
import game.*;
import board.*;


/** The visual frame that manages showing the game */
public class Frame extends JFrame {

	/***/
	private static final long serialVersionUID = 1L;

	
	//Some nice fonts to consider:
	//Apple Chancery
	//Ayuthaya
	//Damascus
	//Herculanum
	//Kokonar
	//Libian Sc
	//Monotype corsiva
	//Papyrus
	/** The font to use for all text */
	protected static final String FONTNAME = "Damascus";
	
	/** The headerPanel this Frame is drawing, if any */
	protected HeaderPanel headerPanel;
	
	/** The gamePanel this Frame is drawing, if any */
	protected GamePanel gamePanel;
	
	/** The animator for this Frame */
	protected Animator animator;
	
	/** The current active cursor */
	@SuppressWarnings("rawtypes")
	private Cursor activeCursor;
	
	/** Different possiblities for toggle options */
	protected enum Toggle{
		NONE,
		PATH_SELECTION,
		DECISION
	}
	
	/** The layers of active toggles. Topmost is the current toggle */
	private Stack<Toggle> toggle;
	
	public Frame(){
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		setResizable(false);
		setLocation(100, 100);
		animator = new Animator();
		toggle = new Stack<Toggle>();
		KeyboardListener.setFrame(this);
	}
	
	/** Sets this Frame to show GamePanel bp. Removes previous gamePanel if any.
	 * Also triggers a packing and repainting.
	 * @param g - the game to show.
	 * @param rows - the number of rows to draw of g (height of window).
	 * @param cols - the number of cols to draw of g (width of window).
	 */
	public void setGame(Game g, int rows, int cols){
		
		//Removal
		if(gamePanel != null){
			remove(gamePanel);
			remove(headerPanel);
			animator.removeAnimatable(gamePanel.boardCursor);
		}
		
		//New Adding
		g.setFrame(this);
	    GamePanel gp = new GamePanel(g, rows, cols);
		add(gp, BorderLayout.CENTER);
		gamePanel = gp;
		activeCursor = gamePanel.boardCursor;
		animator.addAnimatable(gamePanel.boardCursor);
		HeaderPanel hp = new HeaderPanel(g, gp);
		add(hp, BorderLayout.NORTH);
		headerPanel = hp;
		pack();
		repaint();
		setVisible(true);
	}
	
	/** Returns the current active cursor that is moved by arrow keys.
	 * Because cursor has many different implementations, casting is needed */
	@SuppressWarnings("rawtypes")
	public Cursor getActiveCursor(){
		return activeCursor;
	}
	
	/** Returns the current active cursor that is moved by arrow keys.
	 * Because cursor has many different implementations, casting is needed */
	@SuppressWarnings("rawtypes")
	protected void setActiveCursor(Cursor c){
		activeCursor = c;
		animator.addAnimatable(c);
	}
	
	/** Returns the current Toggle setting.
	 * Returns Toggle.NONE if there are no current toggles open */
	public Toggle getToggle(){
		try{
			return toggle.peek();
		}catch(EmptyStackException e){
			return Toggle.NONE;
		}
	}
	
	/** Sets the current Toggle setting by adding it to the top of the stack */
	protected void addToggle(Toggle t){
		toggle.push(t);
	}
	
	/** Removes the top-most Toggle setting. 
	 * Returns the removed setting for checking purposes */
	protected Toggle removeTopToggle(){
		return toggle.pop();
	}
	
	/** Returns the object responsible for animating things for this frame */
	public Animator getAnimator(){
		return animator;
	}
	
	/** Simple main method to test out Frame features */
	public static void main(String[] args){
	    Frame f = new Frame();
	    Terrain[][] t = new Terrain[20][20];
	    for(int i = 0; i < t.length; i++){
	    	for(int j = 0; j < t[i].length; j++){
	    		double d = Math.random();
	    		if(d <= 0.15) t[i][j] = Terrain.MOUNTAIN;
	    		else if (d <= 0.55) t[i][j] = Terrain.WOODS;
	    		else t[i][j] = Terrain.GRASS;
	    	}
	    }
	    Game g = new Game(new Board(t), true);
	    Player p1 = new HumanPlayer(g);
	    new DummyCommander(p1, g.board.getTileAt(0, 0));

	    f.setGame(g, 8, 15);
	    
	    Thread th = new Thread(g);
	    th.start();
	}
}
