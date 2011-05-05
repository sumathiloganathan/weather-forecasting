import java.io.IOException;

import net.sourceforge.*;
import org.encog.*;

public class Main {
	
	public static void main(String[] args) throws IOException{
		DataParser dp = new DataParser();
		dp.parse("SMHI_3hours_clim_7142.txt", false);
		System.out.println("number of entries:"+dp.size());
		System.out.print("entry number:2 date:"+dp.getDate(2)+ " time:"+dp.getTime(2));
		double[] data = dp.getData(2);
		for (int i=0; i<data.length; i++){
			System.out.print(" "+DataParser.NAMES[i]+":"+data[i]);
		}
		System.out.println();
	}
}
