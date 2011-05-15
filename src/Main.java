import java.io.IOException;
import java.util.Arrays;

import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;

public class Main {
	
	public static void main(String[] args) throws IOException{
		//EncogExample.run();
		
		DataParser dp = new DataParser();
		dp.parse("SMHI_3hours_clim_7142.txt", true);
		System.out.println("number of entries:"+dp.size());
		/*for (int j=0; j < 10; j++){
			System.out.print("entry number:"+j+" date:"+dp.getDate(j)+ " time:"+dp.getTime(j));
			double[] data = dp.getData(j);
			for (int i=0; i<data.length; i++){
				System.out.print(" "+DataParser.NAMES[i]+":"+data[i]);
			}
			System.out.println();
		}*/
		
		System.out.println();
		
		//since we now use SimpleAdjuster we will get one extra input values since it splits the wind direction into two values
		DataAdjuster da = new SimpleAdjuster(dp.getRawData());
		BasicMLDataSet trainData = new BasicMLDataSet(dp.asVerySimpleTrainingData(da));
		
		for (int i=100;i<150;i++){
			double[] orig = dp.getData(i);
			double[] norm = trainData.getData().get(i).getInput().getData();
			System.out.println(""+i);
			for (int j=0; j<orig.length; j++){
				System.out.println("\t"+DataParser.NAMES[j]+" orig:"+orig[j]+" norm:"+norm[j]);
			}
			System.out.println();
		}
		
		/*int[] layers = findGoodLayer(trainData);
		
		NeuralNetwork nn = new NeuralNetwork(DataParser.NAMES.length+1, 1, layers);
		nn.train(trainData, 7000);
		
		System.out.println("\nSTARTING");
		
		for (int i=0; i<10; i++){
			MLDataPair pair = trainData.getData().get(7001+i);
			double result = nn.predictRain(pair.getInput().getData());
			System.out.println("result:"+result+" real:"+pair.getIdeal().getData()[0]);
		}*/
		
		//nn.network.compute(trainData.getData().)
		
		//NeuralNetwork nn = new NeuralNetwork(DataParser.NAMES.length+1, 1, new int[]{4,3});
		//System.out.println(nn.train(trainData, 40));
		
		
		
		// This would create a new WeatherData and print some nice information about it
		/*
		WeatherData wd = DataParser.parse("SMHI_3hours_clim_7142.txt");
		System.out.println(wd.info());
		System.out.println();
		System.out.print(wd.getDataWith(new WeatherData.Value[]{
				WeatherData.Value.WIND_SPEED,
				WeatherData.Value.WIND_DIRECTION,
				WeatherData.Value.TEMPERATURE,
				WeatherData.Value.HUMIDITY}).size());
		System.out.println("\twind speed/direction, temp, humidity");
		
		System.out.print(wd.getDataWith(new WeatherData.Value[]{
				WeatherData.Value.WIND_SPEED,
				WeatherData.Value.WIND_DIRECTION,
				WeatherData.Value.TEMPERATURE,
				WeatherData.Value.HUMIDITY,
				WeatherData.Value.RAIN}).size());
		System.out.println("\twind speed/direction, temp, humidity, rain");
		 */
	}
	
	/**
	 * Will try a bunch of different layers setup and train them and compare the error to find the best layers.
	 * @param trainData the data to train on.
	 */
	static int[] findGoodLayer(MLDataSet trainData){
		double bestError = Double.MAX_VALUE;
		int[] bestLayer = new int[0];
		
		for (int nLay = 1; nLay<5; nLay++){//try between 1 and 5 hidden layers
			
			int[] layer = new int[nLay];
			
			for (int i=0; i<20; i++){//try 20 different setups with nLay number of layers
				System.out.print("[");
				for (int j=0; j<nLay; j++){
					layer[j] = (int)(Math.random()*15.0)+1;//each layers has between 1 and 7 nodes
					System.out.print(","+layer[j]);
				}
				System.out.println("] ");
				
				//setup a network with those layers and train it 50 times
				NeuralNetwork nn = new NeuralNetwork(DataParser.NAMES.length+1, 1, layer);
				double error = nn.train(trainData, 500);
				
				System.out.println("\terror:"+error);
				
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
		
		return bestLayer;
	}
}
