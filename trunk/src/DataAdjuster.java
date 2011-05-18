import java.util.List;
import java.util.SortedSet;

import org.encog.ml.data.MLDataPair;


/**
 * An interface for classes that can adjust the input and output to the a neural network in some way.
 */
public interface DataAdjuster {
	/**
	 * Given an adjusted data pair returns the original value of the output, before it was adjusted by this data adjuster.
	 * @param mldp
	 * @return
	 */
	public double adjustOutput(MLDataPair mldp);
	
	/**
	 * The number of inputs a neural networks needs to have to be able to receive data from this adjuster.
	 * @return
	 */
	public int numberOfInputs();
	
	/**
	 * Given a set of DataPoints returns it as training data for a neural network.
	 * @param data
	 * @return
	 */
	public List<MLDataPair> makeTraningData(SortedSet<DataPoint> data);
}
