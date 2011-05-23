package demo;

import org.encog.engine.network.activation.ActivationLinear;
import org.encog.engine.network.activation.ActivationRamp;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataPair;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;

public class SimpleWindWeather extends Weather{
	
	private final double[] input = new double[6];
	private final double[] output = new double[1];
	
	@Override
	public int getIdealSize() {
		return 1;
	}

	@Override
	public int getInputSize() {
		return 6;
	}

	@Override
	protected MLDataPair generateData(MLDataPair in) {
		for (int i=0; i<4; i++){
			input[i] = Math.random();
		}
		input[4] = (Math.random()-0.5)*2.0;
		input[5] = (Math.random()-0.5)*2.0;
		
		if (input[4]>0)
			output[0] = input[4]*input[0];
		else
			output[0] = input[4]*input[1]*-1;
			
		if (input[5]>0)
			output[0] += input[5]*input[2];
		else
			output[0] += input[5]*input[3]*-1;
		
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
		final BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(new ActivationLinear(),false,6));
		network.addLayer(new BasicLayer(new ClampActivation(),false,4));//new ActivationRamp(1.0, 0.0, 1.0, 0.0) ,false,4));
		network.addLayer(new BasicLayer(new ActivationLinear(),false,1));
		network.getStructure().finalizeStructure();
		network.reset();
		
		return network;
	}

}
