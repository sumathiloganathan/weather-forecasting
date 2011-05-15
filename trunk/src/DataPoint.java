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
	/**
	 * The different data values.
	 */
	public final float air_pressure, wind_speed, wind_direction, temperature, rain, humidity;
	
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
		
		air_pressure =	 Float.parseFloat(values[AIR_PRESSURE_INDEX]);
		wind_speed =	 Float.parseFloat(values[WIND_SPEED_INDEX]);
		wind_direction = Float.parseFloat(values[WIND_DIRECTION_INDEX]);
		temperature =	 Float.parseFloat(values[TEMP_INDEX]);
		float rain =	 Float.parseFloat(values[RAIN_INDEX]);
		humidity =		 Float.parseFloat(values[HUMITIDY_INDEX]);
		
		this.rain = rain==-1.0f ? 0.0f : rain;
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
	
	public static boolean isValid(float value){
		return value!=ERROR_VALUE && value!=MISSING_VALUE;
	}
	
}
