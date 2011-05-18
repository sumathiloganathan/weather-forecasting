import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataPair;


public class DataParser {
	//indexes of different values in each row in the data file
	private static final int
		AIR_PRESSURE_INDEX = 28,
		WIND_SPEED_INDEX = 14,
		WIND_DIRECTION_INDEX = 11,
		TEMP_INDEX = 38,
		RAIN_INDEX = 30,
		HUMITIDY_INDEX = 45;
	
	private static int[] VALUES_INDEXES = {28,14,11,38,30,45};//unless this is different between files we can have a static value
	
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
	
	public static final String[] NAMES = {"air pressure","wind speed","wind direction","temperature","rain","humidity"};
	
	//0,DATUM 1,TE 2,DA 3,DCFX 4,DCG1 5,DCG2 6,DCG3 7,DCG4 8,DCH 9,DCL 10,DCM 11,DDD 12,DE 13,DES 14,DFF 15,DH 16,DHS1 17,DHS2 
	//18,DHS3 19,DHS4 20,DN 21,DNH 22,DNS1 23,DNS2 24,DNS3 25,DNS4 26,DPP 27,DPR 28,DPS 29,DQRR 30,DRR 31,DRRC1 32,DRRC2 33,DRRC3
	//34,DSSS 35,DTD 36,DTG 37,DTN 38,DTT 39,DTX 40,DW1 41,DW2 42,DVV 43,DWW 44,EE 45,UU
	
	private double[][] data = new double[0][0];
	private int[] times;
	private int[] dates;
	
	public static WeatherData parse(String file) throws IOException{
		File f = new File(file);
		BufferedReader s = null;
		String line = null;
		LinkedList<DataPoint> data = new LinkedList<DataPoint>();
		
		try{
			s = new BufferedReader(new FileReader(f));
			
			while ((line=s.readLine())!=null){
				data.add(new DataPoint(getDate(line), splitLine(line)));
			}
		}
		finally{
			if(s!=null)
				s.close();
		}
		
		return new WeatherData(data);
	}
	
	private static int[] getDate(String line) {
		String[] values = line.trim().split("\\s+");
		
		int date = Integer.parseInt(values[0]);
		int time = Integer.parseInt(values[1]);
		
		return new int[]{date, time};
	}
	
	private static float[] splitLine(String line) {
		String[] values = line.trim().split("\\s+");
		float[] data = new float[6];
		
		data[DataPoint.AIR_PRESSURE] =	Float.parseFloat(values[AIR_PRESSURE_INDEX]);
		data[DataPoint.WIND_SPEED] =		Float.parseFloat(values[WIND_SPEED_INDEX]);
		data[DataPoint.WIND_DIRECTION] =	Float.parseFloat(values[WIND_DIRECTION_INDEX]);
		data[DataPoint.TEMPERATURE] =		Float.parseFloat(values[TEMP_INDEX]);
		data[DataPoint.RAIN] =			Float.parseFloat(values[RAIN_INDEX]);
		if (data[DataPoint.RAIN]==-1.0f) data[DataPoint.RAIN] = 0.0f;
		else if (data[DataPoint.RAIN] > 0) {data[DataPoint.RAIN] = 1.0f;}
		data[DataPoint.HUMIDITY] =		Float.parseFloat(values[HUMITIDY_INDEX]);
		
		return data;
	}
	
	/**
	 * Tries to parse a data file and stores the result in this data parser where it later can be accessed.
	 * @deprecated Use static function instead.
	 * @param file The file from SMHI to parse.
	 * @param skipBadData Skips data entries where at least one element is either missing or wrong.
	 * @throws IOException If there is something wrong.
	 */
	public void parse(String file, boolean skipBadData) throws IOException{
		
		File f = new File(file);
		BufferedReader s = null;
		String line = null;
		LinkedList<double[]> dataList = new LinkedList<double[]>();
		LinkedList<String> timesList = new LinkedList<String>();
		LinkedList<String> datesList = new LinkedList<String>();
		
		try{
			s = new BufferedReader(new FileReader(f));
			
			double[] post = new double[VALUES_INDEXES.length];
			//loop all lines in the file
			while ((line=s.readLine())!=null){
				
				String[] values = line.trim().split("\\s+");
				
				boolean good = true;
				
				//obtain data elements we are interested in
				for (int i=0 ; i<VALUES_INDEXES.length ; i++){
					double v = Double.parseDouble(values[VALUES_INDEXES[i]]);
					if (skipBadData && (v==DataPoint.MISSING_VALUE || v==DataPoint.ERROR_VALUE)){
						good = false;
						break;
					}
					else{
						if (i == DataPoint.RAIN && v < 0.0){
							post[i] = 0.0;
						}
						else{
							post[i] = v;
						}
					}
				}
				
				if (good){
					
					if (post[DataPoint.WIND_DIRECTION]==0.0){//if this is 0 there is no wind, make direction random
						//post[WIND_DIRECTION] = Math.random()*360;
						post[DataPoint.WIND_DIRECTION] = dataList.getLast()[DataPoint.WIND_DIRECTION];
						
					}
					
					datesList.add(values[0]);
					timesList.add(values[1]);
					dataList.add(post);
					post = new double[VALUES_INDEXES.length];
				}
				//else{
				//	bad data... skip
				//}
			}
		}
		finally{
			if(s!=null)
				s.close();
		}
		
		data = dataList.toArray(data);
		dates = toIntList(datesList);
		times = toIntList(timesList);
	}
	
	private int[] toIntList(LinkedList<String> origList){
		int[] list = new int[origList.size()];
		Iterator<String> it = origList.iterator();
		for (int i=0 ; i<list.length; i++)
			list[i] = Integer.parseInt(it.next());
		return list;
	}
	
	/**
	 * Returns the number of data entries from the last parse.
	 * @deprecated Use weather data instead.
	 * @return The total number of data entries.
	 */
	public int size(){
		return data.length;
	}
	
	/**
	 * Gets a specific data entry. The returned data is in the form of an array where each element 
	 * represents a type of weather data, to see what index is what data look at static ints in this class.
	 * @deprecated Use weather data instead.
	 * @param i The index for the data entry to get.
	 * @return A data entry.
	 */
	public double[] getData(int i){
		return data[i];
	}
	
	/**
	 * @deprecated Use weather data instead.
	 * @param i
	 * @return
	 */
	public int getDate(int i){
		return dates[i];
	}
	
	/**
	 * @deprecated Use weather data instead.
	 * @param i
	 * @return
	 */
	public int getTime(int i){
		return times[i];
	}
	
	/**
	 * @deprecated Use weather data instead.
	 * @return
	 */
	public double[][] getRawData(){
		return data;
	}
	
	/**
	 * Simply returns the data as training data where the input will be one element and the 
	 * ideal output will be the rain variable in the next data element.
	 * @deprecated Use weather data instead.
	 * @return
	 */
	public List<MLDataPair> asVerySimpleTrainingData(DataAdjuster da){
		//LinkedList<MLDataPair> retList = new LinkedList<MLDataPair>();
		ArrayList<MLDataPair> retList = new ArrayList<MLDataPair>(data.length-1);
		
		for (int i=1; i<data.length; i++){
			//if input is data point i the ideal output should be RAIN in data point i+1
			//retList.add(new BasicMLDataPair(
			//		new BasicMLData(da.adjustInput(data[i-1])),
			//		new BasicMLData(new double[]{da.adjustOutput(data[i][DataPoint.RAIN])})));
		}
		
		return retList;
	}
}
