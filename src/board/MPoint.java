package board;

import java.awt.Point;
import java.util.Objects;

public class MPoint{
	public final int row;
	public final int col;
	
	/** Constructor for MPoint
	 * @param r - row of point
	 * @param c - col of point
	 */
	public MPoint(int r, int c){
		row = r;
		col = c;
	}
	
	/** Duplication constructor
	 * @param p - the point to clone
	 */
	public MPoint(MPoint p){
		row = p.row;
		col = p.col;
	}
	
	/** Conversion constructor 
	 * @param p - the Point to convert. (x,y) -> (col, row)
	 */
	public MPoint(Point p){
		row = p.y;
		col = p.x;
	}
	
	/** Creates a new point from adding the row and col components of p to this */
	public MPoint add(MPoint p){
		return new MPoint(row + p.row, col + p.col);
	}
	
	/** Two points are equal if they have the same row and col */
	public boolean equals(Object o){
		if(! (o instanceof MPoint))
			return false;
		
		return ((MPoint) o).row == row && ((MPoint) o).col == col;
	}
	
	/** Hashes an MPoint based on its row and col */
	public int hashCode(){
		return Objects.hash(row, col);
	}
	
	/** A toString  - {@code (row, col)} */
	public String toString(){
		return "(" + row + "," + col + ")";
	}
}
