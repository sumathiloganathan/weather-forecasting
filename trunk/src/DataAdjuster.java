import java.util.List;
import java.util.SortedSet;

import org.encog.ml.data.MLDataPair;


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
	
	public double[] asInput(DataPoint dp);
	public double asOutput(DataPoint dp);
	
	public List<MLDataPair> makeTraningData(SortedSet<DataPoint> data);
}
