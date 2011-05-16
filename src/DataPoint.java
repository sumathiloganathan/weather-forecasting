import java.util.Calendar;
import java.util.GregorianCalendar;


public class DataPoint implements Comparable<DataPoint>{
	
	public final static float MISSING_VALUE = -999.0f, ERROR_VALUE = -998.0f;
	
	//indexes of different values in each row in the data file
	private static final int
		AIR_PRESSURE_INDEX = 28,
		WIND_SPEED_INDEX = 14,
		WIND_DIRECTION_INDEX = 11,
		TEMP_INDEX = 38,
		RAIN_INDEX = 30,
		HUMITIDY_INDEX = 45;
	
	private final int hashCode;
	private final GregorianCalendar date;
	
	public static final int RAIN = 0, AIR_PRESSURE = 1, WIND_SPEED = 2, WIND_DIRECTION = 3, TEMPERATURE = 4, HUMIDITY = 5;
	private final float[] data = new float[6];
	
	public DataPoint(String line){
		String[] values = line.trim().split("\\s+");
		
		int date = Integer.parseInt(values[0]);
		int time = Integer.parseInt(values[1]);
		
		hashCode = date*100+time;
		
		int year = date/10000;
		date -= year*10000;
		int month = date/100;
		date -= month*100;
		int day = date;
		
		this.date = new GregorianCalendar(year, month, day, time, 0);
		
		data[AIR_PRESSURE] =	Float.parseFloat(values[AIR_PRESSURE_INDEX]);
		data[WIND_SPEED] =		Float.parseFloat(values[WIND_SPEED_INDEX]);
		data[WIND_DIRECTION] =	Float.parseFloat(values[WIND_DIRECTION_INDEX]);
		data[TEMPERATURE] =		Float.parseFloat(values[TEMP_INDEX]);
		data[RAIN] =			Float.parseFloat(values[RAIN_INDEX]);
		if (data[RAIN]==-1.0f) data[RAIN] = 0.0f;
		data[HUMIDITY] =		Float.parseFloat(values[HUMITIDY_INDEX]);
	}
	
	/**
	 * Creates a dummy date point to get stuff from weather data.
	 * @param date
	 */
	public DataPoint(GregorianCalendar date){
		for (int i=0; i<data.length; i++){
			data[i] = ERROR_VALUE;
		}
		this.date = date;
		hashCode = -1;
	}
	
	public float get(int value){
		return data[value];
	}
	
	public long getTime(){
		return date.getTimeInMillis();
	}
	
	public int getMonth(){
		return date.get(Calendar.MONTH);
	}
	
	public int getYear(){
		return date.get(Calendar.YEAR);
	}
	
	public int hashCode(){
		return hashCode;
	}
	
	public boolean equals(DataPoint dp){
		return dp!=null && hashCode==dp.hashCode;
	}
	
	public boolean equals(Object o){
		return false;
	}

	@Override
	public int compareTo(DataPoint dp) {
		return date.compareTo(dp.date);
	}
	
	public boolean isValid(int value){
		return data[value]!=ERROR_VALUE && data[value]!=MISSING_VALUE;
	}
	
	public String toString(){
		return "DataPoint["+date.get(Calendar.YEAR)+"/"+date.get(Calendar.MONTH)+"/"+date.get(Calendar.DAY_OF_MONTH)+
			"/"+date.get(Calendar.HOUR_OF_DAY)+"]";
	}
}
