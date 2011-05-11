import org.encog.ml.BasicML;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.train.MLTrain;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;


public class NeuralNetwork {
	
	private final BasicNetwork network = new BasicNetwork();
	
	public NeuralNetwork(){
		//first layer contains the number of inputs from the data parser
		//will have to change if we use input in some other form than raw data from the parser
		network.addLayer(new BasicLayer(DataParser.NAMES.length));
		
		//hidden layers
		network.addLayer(new BasicLayer(DataParser.NAMES.length));
		
		//final layer contains just one output
		//could possible contain same as input layer if we want to apply fuzzy logic to the output
		network.addLayer(new BasicLayer(1));
		
		network.getStructure().finalizeStructure();
		network.reset();
	}
	
	public void train(MLDataSet data){
		final MLTrain train = new ResilientPropagation(network, data);
		
		for (int i=0; i<7000; i++){
			train.iteration();
			//System.out.println(""+i+": "+train.getError());
		}
		for (int i=7001; i<7100; i++){
			train.iteration();
			System.out.println(""+i+": "+train.getError());
		}
		
	}
	
	private final double[] tmpOut = new double[1];
	
	public double predictRain(double[] input){
		network.compute(input, tmpOut);
		return tmpOut[0];
	}
	
}
