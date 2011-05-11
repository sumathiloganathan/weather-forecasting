import java.io.IOException;
import java.util.Arrays;

import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;

public class Main {
	
	public static void main(String[] args) throws IOException{
		//EncogExample.run();
		
		DataParser dp = new DataParser();
		dp.parse("SMHI_3hours_clim_7142.txt", true);
		System.out.println("number of entries:"+dp.size());
		for (int j=0; j < 10; j++){
			System.out.print("entry number:"+j+" date:"+dp.getDate(j)+ " time:"+dp.getTime(j));
			double[] data = dp.getData(j);
			for (int i=0; i<data.length; i++){
				System.out.print(" "+DataParser.NAMES[i]+":"+data[i]);
			}
			System.out.println();
		}
		
		System.out.println();
		
		//since we now use SimpleAdjuster we will get one extra input values since it splits the wind direction into two values
		DataAdjuster da = new SimpleAdjuster(dp.getRawData());
		MLDataSet trainData = new BasicMLDataSet(dp.asVerySimpleTrainingData(da));
		
		//findGoodLayers(trainData);
		
		NeuralNetwork nn = new NeuralNetwork(DataParser.NAMES.length+1, 1, new int[]{4,3});
		System.out.println(nn.train(trainData, 40));
	}
	
	/**
	 * Will try a bunch of different layers setup and train them and compare the error to find the best layers.
	 * @param trainData the data to train on.
	 */
	static void findGoodLayers(MLDataSet trainData){
		double bestError = Double.MAX_VALUE;
		int[] bestLayer = new int[0];
		
		for (int nLay = 1; nLay<6; nLay++){//try between 1 and 5 hidden layers
			
			int[] layer = new int[nLay];
			
			for (int i=0; i<5; i++){//try 5 different setups with nLay number of layers
				System.out.print("[");
				for (int j=0; j<nLay; j++){
					layer[j] = (int)(Math.random()*6.0)+1;//each layers has between 1 and 7 nodes
					System.out.print(","+layer[j]);
				}
				System.out.print("] error:");
				
				//setup a network with those layers and train it 50 times
				NeuralNetwork nn = new NeuralNetwork(DataParser.NAMES.length+1, 1, layer);
				double error = nn.train(trainData, 50);
				
				System.out.println(error);
				
				//see if its better then our current best
				if (error<bestError){
					bestError = error;
					bestLayer = Arrays.copyOf(layer, layer.length);
				}
			}
		}
		
		System.out.println("best error:"+bestError);
		System.out.print("[");
		for (int i: bestLayer){
			System.out.print(","+i);
		}
		System.out.println("]");
	}
}
