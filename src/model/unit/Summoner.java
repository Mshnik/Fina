package model.unit;

public interface Summoner {
	/** Returns true if summoning a model.unit is currently ok - checks surrounding area for free space */
	public boolean hasSummonSpace();
	
	/** Returns true if building a model.unit is currently ok - checks surrounding area for free ancient ground */
	public boolean hasBuildSpace();
}
