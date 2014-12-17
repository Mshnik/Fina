package util;

/** Extra math package */
public class Mth {

	/** No instantiation */
	private Mth(){}
	
	public static double roundTo(double n, double digits){
		int n2 = (int) (n / Math.pow(10.0, digits));
		return ((double)n2) * Math.pow(10.0, digits);
	}
	
}
