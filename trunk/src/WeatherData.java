import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
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
	
	private boolean addIfValid(Set<DataPoint> set, DataPoint dp, int value){
		if (dp.isValid(value)){
			set.add(dp);
			return true;
		}
		return false;
	}
	
	/**
	 * Creates new weather data from the given data. Constructs a bunch of sets of different types of data.
	 * @param data The weather data to create a structure for.
	 */
	public WeatherData(Collection<DataPoint> data){
		allData = new TreeSet<DataPoint>(data);
		Iterator<DataPoint> it = allData.iterator();
		
		while (it.hasNext()){
			DataPoint dp = it.next();
			
			boolean valid = addIfValid(rainData, dp, DataPoint.RAIN);
			valid = addIfValid(airPressureData, dp, DataPoint.AIR_PRESSURE) || valid;
			valid = addIfValid(windDirectionData, dp, DataPoint.WIND_DIRECTION) || valid;
			valid = addIfValid(windSpeedData, dp, DataPoint.WIND_SPEED) || valid;
			valid = addIfValid(humidityData, dp, DataPoint.HUMIDITY) || valid;
			valid = addIfValid(temperatureData, dp, DataPoint.TEMPERATURE) || valid;
			
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
	public SortedSet<DataPoint> getDataWith(Value[] values){
		SortedSet<DataPoint> result = null;
		//sort them because it's faster to start with a small set
		Arrays.sort(values, new Comparator<Value>(){
				@Override
				public int compare(Value arg0, Value arg1) {
					return getData(arg0).size()-getData(arg1).size();
				}
			});
		
		result = new TreeSet<DataPoint>(getData(values[0]));
		
		for (int i=1; i<values.length; i++){
			result.retainAll(getData(values[i]));
		}
		return result;
	}
	
	/**
	 * Removes all values from orig that is missing valid data for at least one of the given values.
	 * @param orig
	 * @param values
	 */
	public void purgeData(SortedSet<DataPoint> orig, Value[] values){
		//sort them because it's faster to start with a small set
		Arrays.sort(values, new Comparator<Value>(){
				@Override
				public int compare(Value arg0, Value arg1) {
					return getData(arg0).size()-getData(arg1).size();
				}
			});
		
		for (int i=0; i<values.length; i++){
			orig.retainAll(getData(values[i]));
		}
	}
	
	//between two times
	public SortedSet<DataPoint> getDataFromTo(DataPoint from, DataPoint to){
		return allData.subSet(from, to);
	}
	
	//faster version of month getting
	public SortedSet<DataPoint> getDataBetweenMonths(int firstMonth, int lastMonth){
		GregorianCalendar startDate, endDate;
		
		if (firstMonth > lastMonth){//think the constants are in order
			//this means over new year
			startDate = new GregorianCalendar(allData.first().getYear(), firstMonth, 0);
			endDate = new GregorianCalendar(startDate.get(Calendar.YEAR)+1, lastMonth+1, 0);
		}
		else{
			startDate = new GregorianCalendar(allData.first().getYear(), firstMonth, 0);
			endDate = new GregorianCalendar(startDate.get(Calendar.YEAR), lastMonth+1, 0);	
		}
		
		SortedSet<DataPoint> result = new TreeSet<DataPoint>();
		
		int lastYear = allData.last().getYear();
		
		while (endDate.get(Calendar.YEAR)<lastYear){
			result.addAll(allData.subSet(new DataPoint(startDate), new DataPoint(endDate)));
			
			startDate.add(Calendar.YEAR, 1);
			endDate.add(Calendar.YEAR, 1);
		}
		
		return result;
	}
	
	//slower version of month getting
	public SortedSet<DataPoint> getDataFromMonths(int[] months){
		SortedSet<DataPoint> result = new TreeSet<DataPoint>();
		Iterator<DataPoint> it = allData.iterator();
		while (it.hasNext()){
			DataPoint dp = it.next();
			for (int i=0; i<months.length; i++){
				if (dp.getMonth()==months[i]){
					result.add(dp);
					break;
				}
			}
		}
		return result;
	}
	
	public String info(){
		return "total:"+allData.size()+"\nrain:"+rainData.size()+"\nair"+airPressureData.size()+"\nwind speed:"+windSpeedData.size()
		+"\nwind diection"+windDirectionData.size()+"\ntemp:"+temperatureData.size()+"\nhumidity:"+humidityData.size();
	}
	
}
