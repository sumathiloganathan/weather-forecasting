
/**
 * An interface for classes that can adjust the input and output to the a neural network in some way.
 */
public interface DataAdjuster {
	/**
	 * Adjusts the given inputs.
	 * @param input
	 * @return
	 */
	public double[] adjustInput(double[] input);
	/**
	 * 
	 * @param d
	 * @return
	 */
	public double adjustOutput(double d);
}
