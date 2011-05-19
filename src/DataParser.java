import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;


public class DataParser {
	//indexes of different values in each row in the data file
	private static final int
		AIR_PRESSURE_INDEX = 28,
		WIND_SPEED_INDEX = 14,
		WIND_DIRECTION_INDEX = 11,
		TEMP_INDEX = 38,
		RAIN_INDEX = 30,
		HUMITIDY_INDEX = 45;
	
	/*
	DATUM 
	TE time 
	DPS air pressure 28
	DFF wind speed 14
	DDD wind direction 11
	DTT temperature 38
	DRR rain 30
	UU humidity 45
	*/
	
	//0,DATUM 1,TE 2,DA 3,DCFX 4,DCG1 5,DCG2 6,DCG3 7,DCG4 8,DCH 9,DCL 10,DCM 11,DDD 12,DE 13,DES 14,DFF 15,DH 16,DHS1 17,DHS2 
	//18,DHS3 19,DHS4 20,DN 21,DNH 22,DNS1 23,DNS2 24,DNS3 25,DNS4 26,DPP 27,DPR 28,DPS 29,DQRR 30,DRR 31,DRRC1 32,DRRC2 33,DRRC3
	//34,DSSS 35,DTD 36,DTG 37,DTN 38,DTT 39,DTX 40,DW1 41,DW2 42,DVV 43,DWW 44,EE 45,UU

	
	public static WeatherData parse(String file) throws IOException{
		return new WeatherData(parseRaw(file));
	}
	
	public static SortedSet<DataPoint> parseRaw(String file) throws IOException{
		File f = new File(file);
		BufferedReader s = null;
		String line = null;
		LinkedList<DataPoint> data = new LinkedList<DataPoint>();
		
		try{
			s = new BufferedReader(new FileReader(f));
			
			while ((line=s.readLine())!=null){
				String[] values = line.trim().split("\\s+");
				data.add(new DataPoint(getDate(values), splitLine(values)));
			}
		}
		finally{
			if(s!=null)
				s.close();
		}
		
		return new TreeSet<DataPoint>(data);
	}
	
	private static int[] getDate(String[] values) {
		
		int date = Integer.parseInt(values[0]);
		int time = Integer.parseInt(values[1]);
		
		return new int[]{date, time};
	}
	
	private static float[] splitLine(String[] values) {
		float[] data = new float[6];
		
		data[DataPoint.AIR_PRESSURE] =	Float.parseFloat(values[AIR_PRESSURE_INDEX]);
		data[DataPoint.WIND_SPEED] =	Float.parseFloat(values[WIND_SPEED_INDEX]);
		data[DataPoint.WIND_DIRECTION] =Float.parseFloat(values[WIND_DIRECTION_INDEX]);
		data[DataPoint.TEMPERATURE] =	Float.parseFloat(values[TEMP_INDEX]);
		data[DataPoint.RAIN] =			Float.parseFloat(values[RAIN_INDEX]);
		if (data[DataPoint.RAIN]==-1.0f) data[DataPoint.RAIN] = 0.0f;
		data[DataPoint.HUMIDITY] =		Float.parseFloat(values[HUMITIDY_INDEX]);
		
		return data;
	}
}
