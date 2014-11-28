package game;

/** Holder for game defining constants, though notably ignoring GUI constants */
public class Const {

	/** Prevent construction of class Const */
	private Const(){}
	
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
	
}
