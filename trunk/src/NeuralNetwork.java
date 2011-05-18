import org.encog.engine.network.activation.ActivationGaussian;
import org.encog.ml.BasicML;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.train.MLTrain;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;


public class NeuralNetwork {
	
	public final BasicNetwork network = new BasicNetwork();
	
	/**
	 * Construct a new neural network with the given inputs, outputs and hidden layers
	 * @param inputs
	 * @param outputs
	 * @param hidden
	 */
	public NeuralNetwork(int inputs, int outputs, int[] hidden){
		
		//ActivationGaussian activation = new ActivationGaussian(1.0, 1.0, 2.0);
		
		//first layer contains the number of inputs from the data parser
		//will have to change if we use input in some other form than raw data from the parser
		network.addLayer(new BasicLayer(inputs));
		
		for (int i : hidden){
			network.addLayer(new BasicLayer(i));
		}
		
		//final layer contains just one output
		//could possible contain same as input layer if we want to apply fuzzy logic to the output
		network.addLayer(new BasicLayer(outputs));
		
		network.getStructure().finalizeStructure();
		network.reset();
	}
	
	/**
	 * Trains the network with the given data and given number of times.
	 * @param data The data to train with.
	 * @param times The number of times to train.
	 * @return The resulting error after training.
	 */
	public double train(MLDataSet data, int times){
		final MLTrain train = new ResilientPropagation(network, data);
		
		int print = 0;
		for (int i=0; i<times; i++, print++){
			train.iteration();
			//System.out.println(""+i+": "+train.getError());
			/*if (train.getError() < 0.0001){
				System.out.println("\tstopped after:"+i+" ");
				return train.getError();
			}*/
			if (print >= 500){
				System.out.print(".");
				print = 0;
			}
		}
		return train.getError();
	}
	
	private final double[] tmpOut = new double[1];
	
	public double predictRain(double[] input){
		network.compute(input, tmpOut);
		return tmpOut[0];
	}
	
}
