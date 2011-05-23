package demo;

import org.encog.engine.network.activation.ActivationLinear;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataPair;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;

public class SimpleWeather extends Weather{

	private final double[] input = new double[4];
	private final double[] output = new double[1];
	
	@Override
	public int getIdealSize() {
		return 1;
	}

	@Override
	public int getInputSize() {
		return 4;
	}

	@Override
	protected MLDataPair generateData(MLDataPair in) {
		for (int i=0; i<input.length; i++){
			input[i] = Math.random();
		}
		output[0] = input[1];
		
		if (in==null){
			in = new BasicMLDataPair(new BasicMLData(input), new BasicMLData(output));
		}
		else{
			in.setInputArray(input);
			in.setIdealArray(output);
		}
		return in;
	}

	@Override
	public BasicNetwork getGoodNetwork() {
		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(new ActivationLinear(),false,4));
		network.addLayer(new BasicLayer(new ActivationLinear(),false,1));
		network.getStructure().finalizeStructure();
		network.reset();
		
		return network;
	}

}
