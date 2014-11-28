package game;

/** Holder for game defining constants, though notably ignoring GUI constants */
public class Const {

	/** Prevent construction of class Const */
	private Const(){}
	
	/** Perform checks on constants to check that none of them are out of bounds
	 * with one another
	 */
	static{
		
	}
	
	//COMMANDERS
	/** Base level of health for lvl1 commanders */
	public static final int BASE_HEALTH = 100;
	
	/** Base amount of max health gained per level */
	public static final int SCALE_HEALTH = 20;
	
	/** Base starting amount of mana for lvl1 commanders */
	public static final int START_MANA = 20;
	
	/** Base starting level of mana per turn for lvl1 commanders */
	public static final int BASE_MANA_PT = 10;
	
	/** Base amount of mana per turn gained per level */
	public static final int SCALE_MANA_PT = 5;
	
	/** The amount of research required to get to the next level for free.
	 * Index i = cost to get from level i+1 to i+2 (because levels are 1 indexed). */
	public static final int[] RESEARCH_REQS = {
		100, 250, 600, 1500
	};
	
	/** The highest level commanders can achieve */
	public static final int MAX_LEVEL = RESEARCH_REQS.length + 1;
	
	/** The ratio of manaCost -> research for the owner of the killing unit */
	public static final double MANA_COST_TO_RESEARCH_RATIO = 0.3;
	
}
