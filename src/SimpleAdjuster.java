
/**
 * A simple data adjuster that normalizes all the input. And splits the wind direction value into two values, 
 * the amount of east and north.
 */
public class SimpleAdjuster implements DataAdjuster{
	
	private final double[] min;
	private final double[] max;
	
	public SimpleAdjuster(double[][] inputData){
		min = new double[inputData[0].length];
		max = new double[min.length];
		
		for (int i=0; i<min.length; i++){
			min[i] = Double.MAX_VALUE;
			max[i] = Double.MIN_VALUE;
		}
		
		for (double[] in : inputData){
			for (int i=0; i<in.length; i++){
				min[i] = Math.min(min[i], in[i]);
				max[i] = Math.max(max[i], in[i]);
			}
		}
	}

	//DEBUG
	//private int dbc = 0;
	
	@Override
	public double[] adjustInput(double[] input) {
		
		double[] ret = new double[input.length+1];
		
		for (int i=0; i<input.length; i++){
			if (i==DataParser.WIND_DIRECTION){
				ret[i] = Math.abs(input[i]-180.0)/180;//the amount north
				if (ret[i] <= 90.0){//the amount east
					ret[ret.length-1] = (ret[i]+90.0)/180.0;
				}
				else if (ret[i] >= 270){
					ret[ret.length-1] = (ret[i]-270.0)/180.0;
				}
				else{
					ret[ret.length-1] = 1.0-((ret[i]-90)/270);
				}
				
			}
			else{
				ret[i] = (input[i]-min[i])/(max[i]-min[i]);
			}
		}
		
		/*if (dbc<15){
			dbc++;
			System.out.println("[");
			for (double d : ret){
				System.out.print(","+d);
			}
			System.out.println("]");
		}*/
		
		return ret;
	}

	@Override
	public double adjustOutput(double d) {
		return (d-min[DataParser.RAIN])/(max[DataParser.RAIN]-min[DataParser.RAIN]);
	}
}
