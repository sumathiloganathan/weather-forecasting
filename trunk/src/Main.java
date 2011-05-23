import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import org.encog.engine.network.activation.ActivationLinear;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.Train;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;

import demo.SimpleWeather;
import demo.SimpleWindWeather;
import demo.Weather;

public class Main {
	
	public static void main(String[] args) throws IOException{
		Weather weather = new SimpleWindWeather();
		BasicNetwork network = weather.getGoodNetwork();
		final Train train = new ResilientPropagation(network, weather);//does not work well with non linear stuff?
		
		int epoch = 1;
		
		do {
			train.iteration();
			epoch++;
		} while (epoch < 5000 && (train.getError() > 0.01));
		train.finishTraining();
		System.out.println("Epoch #" + epoch + " Error:" + train.getError());

		System.out.println(network.dumpWeights());
		
		// test the neural network
		System.out.println("Neural Network Results:");

		Iterator<MLDataPair> it = weather.iterator();
		for (int i=0; i<10; i++){
			MLDataPair pair = it.next();
			final MLData output = network.compute(pair.getInput());
			System.out.println(toString(pair.getInputArray())+"\n\tactual = "
					+ output.getData(0) + "\n\tideal  = "
					+ pair.getIdeal().getData(0));
		}
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
			double r = nn.predictRain(mldp.getInputArray());
			double compare = mldp.getIdeal().getData()[0];
			
			if (compare == 1.0f){
				if (r > 0.5f) {
					correct++;
				}
				total++;
			}
			i++;
			if (i>50)
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
			
			for (int i=0; i<3; i++){//try 20 different setups with nLay number of layers
				System.out.print("[");
				for (int j=0; j<nLay; j++){
					layer[j] = (int)(Math.random()*15.0)+1;//each layers has between 1 and 7 nodes
					System.out.print(","+layer[j]);
				}
				System.out.println("] ");
				
				//setup a network with those layers and train it 50 times
				NeuralNetwork nn = new NeuralNetwork(5, 1, layer);
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
}
