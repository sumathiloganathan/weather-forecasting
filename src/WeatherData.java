import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;


public class WeatherData {
	
	public enum Value {RAIN, TEMPERATURE, AIR_PRESSURE, WIND_SPEED, WIND_DIRECTION, HUMIDITY};
	
	private final SortedSet<DataPoint> allData;
	
	//give error... cannot do like this with static ints instead
	//private final Set<DataPoint>[] data = new HashSet<DataPoint>[6];
	
	private final Set<DataPoint>
		rainData = new HashSet<DataPoint>(25000,1.0f),
		airPressureData = new HashSet<DataPoint>(13000,1.0f),
		windSpeedData = new HashSet<DataPoint>(73500,1.0f),
		windDirectionData = new HashSet<DataPoint>(73500,1.0f),
		temperatureData = new HashSet<DataPoint>(73500,1.0f),
		humidityData = new HashSet<DataPoint>(73000,1.0f);
	
	private boolean addIfValid(Set<DataPoint> set, DataPoint dp, float value){
		if (DataPoint.isValid(value)){
			set.add(dp);
			return true;
		}
		return false;
	}
	
	/**
	 * Creates new weather data from the given data. Constructs a bunch of sets of different types of data.
	 * @param data The weather data to create a structure for.
	 */
	public WeatherData(List<DataPoint> data){
		allData = new TreeSet<DataPoint>(data);
		Iterator<DataPoint> it = allData.iterator();
		
		while (it.hasNext()){
			DataPoint dp = it.next();
			
			boolean valid = addIfValid(rainData, dp, dp.rain);
			valid = addIfValid(airPressureData, dp, dp.air_pressure) || valid;
			valid = addIfValid(windDirectionData, dp, dp.wind_direction) || valid;
			valid = addIfValid(windSpeedData, dp, dp.wind_speed) || valid;
			valid = addIfValid(humidityData, dp, dp.humidity) || valid;
			valid = addIfValid(temperatureData, dp, dp.temperature) || valid;
			
			//not added to any data set, no point in keeping it
			if (!valid)
				it.remove();
		}
	}
	
	/**
	 * Gets the set of data points that contains valid data for the given value.
	 * @param v The value for which to get a set for.
	 * @return 
	 */
	public Set<DataPoint> getData(Value v){
		switch(v){
		case RAIN: return rainData;
		case TEMPERATURE: return temperatureData;
		case AIR_PRESSURE: return airPressureData;
		case WIND_SPEED: return windSpeedData;
		case WIND_DIRECTION: return windDirectionData;
		case HUMIDITY: return humidityData;
		}
		return null;
	}
	
	/**
	 * Returns a set where each data point has valid data for all the given values.
	 * @param values
	 * @return
	 */
	public Set<DataPoint> getDataWith(Value[] values){
		Set<DataPoint> result = null;
		//sort them because it's faster to start with a small set
		Arrays.sort(values, new Comparator<Value>(){
				@Override
				public int compare(Value arg0, Value arg1) {
					return getData(arg0).size()-getData(arg1).size();
				}
			});
		
		result = new HashSet<DataPoint>(getData(values[0]));
		
		for (int i=0; i<values.length; i++){
			result.retainAll(getData(values[i]));
		}
		return result;
	}
	
	//between two time
	public SortedSet<DataPoint> getDataFromTo(DataPoint from, DataPoint to){
		return allData.subSet(from, to);
	}
	
	public String info(){
		return "total:"+allData.size()+"\nrain:"+rainData.size()+"\nair"+airPressureData.size()+"\nwind speed:"+windSpeedData.size()
		+"\nwind diection"+windDirectionData.size()+"\ntemp:"+temperatureData.size()+"\nhumidity:"+humidityData.size();
	}
	
}
