import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataPair;


public class MultipleDataPointAdjuster implements DataAdjuster{
	
	private final int NUMBER_OF_INPUT = 12;
	
	private final int TEMP = 0, HUMI = 1, WIND = 2, RAIN = 3;

	private final double[] min = new double[4];
	private final double[] max = new double[4];
	
	private double min(DataPoint dp, int thisIndex, int thatIndex){
		return dp.isValid(thatIndex) ? Math.min(dp.get(thatIndex), min[thisIndex]) : min[thisIndex];
	}
	
	public MultipleDataPointAdjuster(Set<DataPoint> data){
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
	
	
	@Override
	public double adjustOutput(MLDataPair mldp) {
		return 0;
	}

	@Override
	public int numberOfInputs() {
		return NUMBER_OF_INPUT;
	}
	
	private void asInput(DataPoint dp, double[] ret, int offs) {
		
		ret[offs] = normalize(dp, DataPoint.HUMIDITY, HUMI);
		ret[offs+1] = normalize(dp, DataPoint.TEMPERATURE, TEMP);
		ret[offs+2] = normalize(dp, DataPoint.WIND_SPEED, WIND);
		ret[offs+3] = Math.abs(dp.get(DataPoint.WIND_DIRECTION)-180.0)/180;//the amount north
		if (dp.get(DataPoint.WIND_DIRECTION) <= 90.0){//the amount east
			ret[offs+4] = (dp.get(DataPoint.WIND_DIRECTION)+90.0)/180.0;
		}
		else if (dp.get(DataPoint.WIND_DIRECTION) >= 270){
			ret[offs+4] = (dp.get(DataPoint.WIND_DIRECTION)-270.0)/180.0;
		}
		else{
			ret[offs+4] = 1.0-((dp.get(DataPoint.WIND_DIRECTION)-90)/270);
		}
		ret[offs+5] = normalize(dp, DataPoint.RAIN, RAIN);
		
	}
	
	private boolean canUse(DataPoint dp){
		return dp.isValid(DataPoint.RAIN) && dp.isValid(DataPoint.HUMIDITY) && dp.isValid(DataPoint.TEMPERATURE)
			&& dp.isValid(DataPoint.WIND_SPEED) && dp.isValid(DataPoint.WIND_DIRECTION);
	}
	
	private double[] asInput(SortedSet<DataPoint> points) {
		DataPoint dp1 = null, dp2 = null;
		for (DataPoint d : points){
			if (canUse(d)){
				if (dp1==null)
					dp1 = d;
				else{
					dp2 = d;
					break;
				}
			}
		}
		if (dp2==null) return null;
		double[] ret = new double[NUMBER_OF_INPUT];
		asInput(dp1,ret,0);
		asInput(dp2,ret,NUMBER_OF_INPUT/2);
		return ret;
	}
	
	private double normalize(DataPoint dp, int dpIndex, int i){
		return (dp.get(dpIndex)-min[i])/(max[i]-min[i]);
	}
	
	@Override
	public List<MLDataPair> makeTraningData(SortedSet<DataPoint> data) {
		
		List<MLDataPair> result = new LinkedList<MLDataPair>();
		GregorianCalendar time2 = new GregorianCalendar();
		
		int withRain = 0;
		int skipped = 0;
		
		for (DataPoint dp : data){
			if ((dp.getMonth() == Calendar.JUNE || dp.getMonth() == Calendar.JULY) && dp.isValid(DataPoint.RAIN)){
				withRain++;
				time2.setTimeInMillis(dp.getTime() - (12*60*60*1000+1));//12 hours before
				double[] inpt = asInput(data.subSet(new DataPoint(time2), dp));
				if (inpt!=null){
					result.add(new BasicMLDataPair(
							new BasicMLData(inpt),
							new BasicMLData(new double[]{dp.get(DataPoint.RAIN)<=0.1f ? 0.0f : 1.0f })));
				}
				else{
					skipped++;
				}
			}
		}
		
		System.out.println("with rain and june/july:"+withRain);

		System.out.println("skipped:"+skipped);
		
		return result;
	}
	
}
