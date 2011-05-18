import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;

public class Main {
	
	public static void main(String[] args) throws IOException{
		
		// This creates a new WeatherData and print some nice information about it
		long start = System.currentTimeMillis();
		WeatherData wd = DataParser.parse("SMHI_3hours_clim_7142.txt");
		System.out.println("parsed in:"+(System.currentTimeMillis()-start));
		
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
		System.out.println("\twind speed/direction, temp, humidity, rain\n");
		
		start = System.currentTimeMillis();
		SortedSet<DataPoint> summer = wd.getDataBetweenMonths(Calendar.MAY, Calendar.AUGUST);
		System.out.println("summer data: "+summer.size()+" obtained in:"+(System.currentTimeMillis()-start));
		
		start = System.currentTimeMillis();
		wd.purgeData(summer, new WeatherData.Value[]{
				WeatherData.Value.WIND_SPEED,
				WeatherData.Value.WIND_DIRECTION,
				WeatherData.Value.TEMPERATURE,
				WeatherData.Value.HUMIDITY,
				WeatherData.Value.RAIN});
		
		System.out.println("summer data cleaned: "+summer.size()+" obtained in:"+(System.currentTimeMillis()-start));
		
		SimpleAdjuster adjuster = new SimpleAdjuster(summer);
		List<MLDataPair> trainingData = adjuster.makeTraningData(summer);
		
		//Cut out 10 % of the training set for validation
		List<MLDataPair> validationData = trainingData.subList( (int) (0.6 * trainingData.size()), trainingData.size());
		
		List<MLDataPair> trainDataNew = trainingData.subList(0, (int) (0.6 * trainingData.size() - 1));
		
		System.out.println("training data size:"+trainDataNew.size());
		
		//do some clean up before we start some heavy training
		summer=null;
		wd = null;
		adjuster = null;
		System.gc();
		

		MLDataSet train = new BasicMLDataSet(trainingData);
		NeuralNetwork nn = new NeuralNetwork(SimpleAdjuster.NUMBER_OF_INPUT, 1, new int[] {15, 6});

		
		System.out.print("training");
		
		//train on all the training data
		nn.train(train, trainDataNew.size());
		System.out.println();
		
		Iterator<MLDataPair> it = trainingData.iterator();
		/*
		//try the network some
		for (int i=0; i<10; i++){
			MLDataPair mldp = it.next();
			
			//data with rain is not working atm, skip stuff with less than 0.5 rain
			while(mldp.getIdealArray()[0]<0.5) mldp=it.next();
			
			double r = nn.predictRain(mldp.getInputArray());
			System.out.println("in:"+toString(mldp.getInputArray())+" out:"+r+" ideal:"+toString(mldp.getIdeal().getData()));
		}
		*/
		float correct = 0.0f;
		float correctLow = 0.0f;
		float wrong = 0.0f;
		float wrongLow = 0.0f;
		//loop through the validation set and check accuracy
		for (MLDataPair mldp : validationData) {
			double r = nn.predictRain(mldp.getInputArray());
			double compare = mldp.getIdeal().getData()[0];
			if ((compare == 1.0f && r > 0.5f)) {
				correct++;
			}
			else if ((compare == 0.0f && r <= 0.5f)) {
				correctLow++;
			}
			else if ((compare == 0.0f && r > 0.5f)){
				wrongLow++;
			}
			else {
				wrong++;
			}
			System.out.println(/* "in:"+toString(mldp.getInputArray())+*/"out:"+r+" ideal:"+toString(mldp.getIdeal().getData()));
		}
		
		System.out.println("correct: " + (correct + correctLow) + "\n" + "wrong: " + (wrong + wrongLow));
		System.out.println("accuracy: " + ((correct + correctLow) / (correct + correctLow + wrong + wrongLow)));
		System.out.println("accuracyLow : " + (correctLow / (correctLow + wrongLow)));
		System.out.println("accuracyHigh: " + (correct / (correct + wrong)));
	}
	
	public static String toString(double[] a){
		String s = "[";
		for (double d:a){
			s = s+d+",";
		}
		return s+"]";
	}
	
	/**
	 * Will try a bunch of different layers setup and train them and compare the error to find the best layers.
	 * @param trainData the data to train on.
	 */
	static int[] findGoodLayer(MLDataSet trainData){
		double bestError = Double.NEGATIVE_INFINITY;
		int[] bestLayer = new int[0];
		
		for (int nLay = 1; nLay<=3; nLay++){//try between 1 and 5 hidden layers
			
			int[] layer = new int[nLay];
			
			for (int i=0; i<20; i++){//try 20 different setups with nLay number of layers
				System.out.print("[");
				for (int j=0; j<nLay; j++){
					layer[j] = (int)(Math.random()*15.0)+1;//each layers has between 1 and 7 nodes
					System.out.print(","+layer[j]);
				}
				System.out.println("] ");
				
				//setup a network with those layers and train it 50 times
				NeuralNetwork nn = new NeuralNetwork(5, 1, layer);
				double error = nn.train(trainData, 1);
				
				System.out.println("\terror:"+error);
				
				//see if its better then our current best
				if (error > bestError){
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
