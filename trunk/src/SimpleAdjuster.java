import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataPair;


/**
 * A simple data adjuster that normalizes all the input. And splits the wind direction value into two values, 
 * the amount of east and north.
 */
public class SimpleAdjuster implements DataAdjuster{
	
	public static final int NUMBER_OF_INPUT = 6;
	
	public final int TEMP = 0, HUMI = 1, WIND = 2, RAIN = 3;

	public final double[] min = new double[4];
	public final double[] max = new double[4];
	
	private double min(DataPoint dp, int thisIndex, int thatIndex){
		return dp.isValid(thatIndex) ? Math.min(dp.get(thatIndex), min[thisIndex]) : min[thisIndex];
	}
	
	public SimpleAdjuster(Set<DataPoint> data){
		for (int i=0; i<min.length; i++){
			min[i] = Double.POSITIVE_INFINITY;
			max[i] = Double.NEGATIVE_INFINITY;
		}
		
		for (DataPoint dp : data){
			min[HUMI] = min(dp, HUMI, (DataPoint.HUMIDITY));
			max[HUMI] = Math.max(max[HUMI], dp.get(DataPoint.HUMIDITY));
			
			min[WIND] = min(dp, WIND, (DataPoint.WIND_SPEED));
			max[WIND] = Math.max(max[WIND], dp.get(DataPoint.WIND_SPEED));
			
			min[TEMP] = min(dp, TEMP, (DataPoint.TEMPERATURE));
			max[TEMP] = Math.max(max[TEMP], dp.get(DataPoint.TEMPERATURE));
			
			min[RAIN] = min(dp, RAIN, (DataPoint.RAIN));
			max[RAIN] = Math.max(max[RAIN], dp.get(DataPoint.RAIN));
		}
	}
	
	
	
	
	private MLDataPair makePair(DataPoint curr, DataPoint next){
		if (next.isValid(DataPoint.RAIN) && (next.getTime()-curr.getTime()) < 21600001){//6 hours in milliseconds 6*60*60*1000
			return new BasicMLDataPair(new BasicMLData(asInput(curr)),new BasicMLData(new double[]{normalize(next, DataPoint.RAIN, RAIN)}));
		}
		return null;
	}
	
	
	@Override
	public List<MLDataPair> makeTraningData(SortedSet<DataPoint> data) {
		LinkedList<MLDataPair> result = new LinkedList<MLDataPair>();
		
		Iterator<DataPoint> it = data.iterator();
		DataPoint current = it.next();
		while (it.hasNext()){
			DataPoint next = it.next();
			
			MLDataPair pair = makePair(current,next);
			if (pair!=null)
				result.add(pair);
			
			current = next;
		}
		
		return result;
	}
	
	private double normalize(DataPoint dp, int dpIndex, int i){
		return (dp.get(dpIndex)-min[i])/(max[i]-min[i]);
	}
	
	private double[] asInput(DataPoint dp) {
		double[] ret = new double[NUMBER_OF_INPUT];
		
		ret[0] = normalize(dp, DataPoint.HUMIDITY, HUMI);
		ret[1] = normalize(dp, DataPoint.TEMPERATURE, TEMP);
		ret[2] = normalize(dp, DataPoint.WIND_SPEED, WIND);
		ret[3] = Math.abs(dp.get(DataPoint.WIND_DIRECTION)-180.0)/180;//the amount north
		if (dp.get(DataPoint.WIND_DIRECTION) <= 90.0){//the amount east
			ret[4] = (dp.get(DataPoint.WIND_DIRECTION)+90.0)/180.0;
		}
		else if (dp.get(DataPoint.WIND_DIRECTION) >= 270){
			ret[4] = (dp.get(DataPoint.WIND_DIRECTION)-270.0)/180.0;
		}
		else{
			ret[4] = 1.0-((dp.get(DataPoint.WIND_DIRECTION)-90)/270);
		}
		ret[5]  = normalize(dp, DataPoint.RAIN, RAIN);
		return ret;
	}
	
	@Override
	public double adjustOutput(MLDataPair dp) {
		return dp.getIdealArray()[0]*max[RAIN]+min[RAIN];
	}

	@Override
	public int numberOfInputs() {
		return NUMBER_OF_INPUT;
	}
}