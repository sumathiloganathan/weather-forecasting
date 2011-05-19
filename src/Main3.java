import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.basic.BasicMLDataSet;


public class Main3 {

	public static void main(String[] args) throws IOException{		
		SortedSet<DataPoint> totalData = DataParser.parseRaw("SMHI_3hours_clim_7142.txt");
		
		DataAdjuster adjuster = new MultipleDataPointAdjuster(totalData);
		List<MLDataPair> trainingData = new ArrayList<MLDataPair>(adjuster.makeTraningData(totalData));
		
		//Cut out 40 % of the training set for validation
		List<MLDataPair> validationData = trainingData.subList( (int) (0.6 * trainingData.size()), trainingData.size());
		List<MLDataPair> trainDataNew = trainingData.subList(0, (int) (0.6 * trainingData.size() - 1));
		
		System.out.println("total data:"+totalData.size());
		System.out.println("data pairs:"+trainingData.size());
		System.out.println("training data size:"+trainDataNew.size());
		System.out.println("validation data size:"+validationData.size());
		
		BasicMLDataSet train = new BasicMLDataSet(trainingData);
		NeuralNetwork nn = new NeuralNetwork(adjuster.numberOfInputs(), 1, new int[]{13,3});//findGoodLayer(train, validationData));
		
		//do some clean up before we start some heavy training
		//summer = null;
		//wd = null;
		adjuster = null;
		System.gc();
		
		System.out.print("training ");
		
		//train on all the training data
		nn.train(train, trainDataNew.size());
		System.out.println();
		
		float correct = 0.0f;
		float correctLow = 0.0f;
		float wrong = 0.0f;
		float wrongLow = 0.0f;
		float correctSw = 0.0f;
		float totalSw = 0.0f;
		//loop through the validation set and check accuracy
		int fkf = 100;
		for (MLDataPair mldp : validationData) {
			double r = nn.predictRain(mldp.getInputArray());
			double compare = mldp.getIdeal().getData()[0];
			double past = mldp.getInputArray()[11];
			if (mldp.getInputArray()[5]==0.0f) past = 0.0;
			
			boolean wasSwitch = (past != 0.0 && compare==0.0) || (past == 0.0 && compare==1.0);
			if (wasSwitch){
				totalSw++;
			}
			
			if ((compare == 1.0f && r > 0.4f)) {
				correct++;
				if (wasSwitch) correctSw++;
			}
			else if ((compare == 0.0f && r <= 0.4f)) {

				if (wasSwitch) correctSw++;
				correctLow++;
			}
			else if ((compare == 0.0f && r > 0.4f)){
				wrongLow++;
			}
			else {
				wrong++;
			}
			if (fkf > 0){
				System.out.println("past:"+past+" out:"+r+" ideal:"+toString(mldp.getIdeal().getData()));
				fkf--;
			}
		}
		
		System.out.println("correct: " + (correct + correctLow) + "\n" + "wrong: " + (wrong + wrongLow));
		System.out.println("accuracy: " + ((correct + correctLow) / (correct + correctLow + wrong + wrongLow)));
		System.out.println("accuracyLow : " + (correctLow / (correctLow + wrongLow)));
		System.out.println("accuracyHigh: " + (correct / (correct + wrong)));
		System.out.println("switch acc: "+(correctSw/totalSw)+" total switches:"+totalSw);
	}
	
	private static String dateToString(DataPoint dp){
		return dp.getYear()+"/"+dp.getMonth()+"/"+dp.getDay()+"/"+dp.getHour();
	}
	
	public static String toString(double[] a){
		String s = "[";
		for (double d:a){
			s = s+d+",";
		}
		return s+"]";
	}
	
	/**
	 * Trains a network a little bit, then validates it on just a few data points to see if the network setup is any good.
	 * @param nn
	 * @param trainData
	 * @param validData
	 * @return The amount a accuracy for rain prediction when the network was trained a little bit.
	 */
	public static float findAccHigh(NeuralNetwork nn, BasicMLDataSet trainData, List<MLDataPair> validData){
		
		nn.train(trainData, 500);
		
		float correct = 0.0f;
		float total = 0.0f;
		//loop through the validation set and check accuracy
		int i=0;
		for (MLDataPair mldp : validData) {
			double compare = mldp.getIdeal().getData()[0];
			
			if (compare == 1.0f){
				double r = nn.predictRain(mldp.getInputArray());
				if (r > 0.5f) {
					correct++;
				}
				i++;
				total++;
			}
			if (i>100)
				break;
		}
		return correct/total;
		
	}
	
	/**
	 * Will try a bunch of different layers setup and train them and compare the error to find the best layers.
	 * @param trainData the data to train on.
	 */
	public static int[] findGoodLayer(BasicMLDataSet trainData, List<MLDataPair> validData){
		double bestAcc = Double.NEGATIVE_INFINITY;
		int[] bestLayer = new int[0];
		
		for (int nLay = 1; nLay<=3; nLay++){//try between 1 and 5 hidden layers
			
			int[] layer = new int[nLay];
			
			for (int i=0; i<5; i++){//try 20 different setups with nLay number of layers
				System.out.print("[");
				for (int j=0; j<nLay; j++){
					layer[j] = (int)(Math.random()*15.0)+1;//each layers has between 1 and 7 nodes
					System.out.print(","+layer[j]);
				}
				System.out.println("] ");
				
				//setup a network with those layers and train it 50 times
				NeuralNetwork nn = new NeuralNetwork(10, 1, layer);
				double acc = findAccHigh(nn, trainData, validData);
				
				System.out.println("\taccuracy:"+acc);
				
				//see if its better then our current best
				if (acc > bestAcc){
					bestAcc = acc;
					bestLayer = Arrays.copyOf(layer, layer.length);
				}
			}
		}
		
		System.out.println("best accuracy:"+bestAcc);
		System.out.print("[");
		for (int i: bestLayer){
			System.out.print(","+i);
		}
		System.out.println("]");
		
		return bestLayer;
	}
	
	/**
	 * If you make a Hashtable pairing an MLDataPair to the two data points this function can print
	 * some of those to see exactly what the adjuster does.
	 * @param d
	 */
	public static void printData(Hashtable<MLDataPair,DataPoint[]> d){
		int i=0;
		for (MLDataPair key : d.keySet()){
			if (i>500){
			DataPoint[] points = d.get(key);
			
			System.out.println("from:"+dateToString(points[0])+" to:"+dateToString(points[1]));
			System.out.println("\thumi: "+points[0].get(DataPoint.HUMIDITY)+" = "+key.getInputArray()[0]);
			System.out.println("\ttemp: "+points[0].get(DataPoint.TEMPERATURE)+" = "+key.getInputArray()[1]);
			System.out.println("\tsped: "+points[0].get(DataPoint.WIND_SPEED)+" = "+key.getInputArray()[2]);
			System.out.println("\tnorth: "+points[0].get(DataPoint.WIND_DIRECTION)+" = "+key.getInputArray()[3]);
			System.out.println("\twest: "+key.getInputArray()[4]);
			System.out.println("\ttime: "+points[0].getMonth()+" = "+key.getInputArray()[5]);
			System.out.println("\t----------");
			System.out.println("\tout: "+points[1].get(DataPoint.RAIN)+" = "+key.getIdealArray()[0]);
				i++;
				i=0;
			}
			else{
				i++;
			}
		}
	}
	
	/**
	 * Given a collection of MLDataPairs makes sure there are as many data points where it rained as data points without any rain.
	 * @param trainingData
	 * @param limit The definition of rain, above this value it is raining.
	 */
	public static void balanceRain(Collection<MLDataPair> trainingData, float limit){
		int nrain = 0;
		Iterator<MLDataPair> it = trainingData.iterator();
		while(it.hasNext()){
			if (it.next().getIdealArray()[0]>limit){
				nrain++;
			}else{
				if (nrain>0){
					nrain--;
				}
				else{
					it.remove();
				}
			}
		}
	}
	
	
	
	public static void NonAIPredict(SortedSet<DataPoint> totalData){
		System.out.println("total:"+totalData.size());
		int invalid = 0, nRain = 0, notRain = 0;
		Iterator<DataPoint> it = totalData.iterator();
		DataPoint dp;
		while (it.hasNext()){
			dp = it.next();
			if (dp.isValid(DataPoint.RAIN)){
				if (dp.get(DataPoint.RAIN)==0.0f){
					notRain++;
				}
				else{
					nRain++;
				}
			}
			else{
				it.remove();
				invalid++;
			}
		}
		
		System.out.println("invalid:"+invalid+"\nrain:"+nRain+"\nno rain:"+notRain);
		
		float correct = 0.0f;
		float correctLow = 0.0f;
		float wrong = 0.0f;
		float wrongLow = 0.0f;
		
		it = totalData.iterator();
		dp = it.next();
		while (it.hasNext()){
			DataPoint current = it.next();
			
			if ((current.getTime() - dp.getTime()) < 6*60*60*1000+1){
				
				if (current.get(DataPoint.RAIN)==1.0f){
					if (dp.get(DataPoint.RAIN)==1.0f){
						correct++;
					}
					else{
						wrong++;
					}
				}
				else{
					if (dp.get(DataPoint.RAIN)==1.0f){
						wrongLow++;
					}
					else{
						correctLow++;
					}
				}
			}
			
			dp = current;
		}
		
		System.out.println("\ncorrect: " + (correct + correctLow) + "\n" + "wrong: " + (wrong + wrongLow));
		System.out.println("accuracy: " + ((correct + correctLow) / (correct + correctLow + wrong + wrongLow)));
		System.out.println("accuracyLow : " + (correctLow / (correctLow + wrongLow)));
		System.out.println("accuracyHigh: " + (correct / (correct + wrong)));
		
	}
}
