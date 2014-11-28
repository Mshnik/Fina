package game;

/** Elements are aware of their presence in a matrix, and so keep track of their row, col */
public interface MatrixElement {
	
	/** Returns the row this occupies in its matrix */
	public int getRow();
	
	/** Returns the col this occupies in its matrix */
	public int getCol();
}
