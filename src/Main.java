import java.io.IOException;

import net.sourceforge.*;
import org.encog.*;

public class Main {
	
	public static void main(String[] args) throws IOException{
		DataParser dp = new DataParser();
		dp.parse("SMHI_3hours_clim_7142.txt", true);
		System.out.println("number of entries:"+dp.size());
		for (int j=0; j < 10; j++){
			System.out.print("entry number:"+j+" date:"+dp.getDate(j)+ " time:"+dp.getTime(j));
			double[] data = dp.getData(j);
			for (int i=0; i<data.length; i++){
				System.out.print(" "+DataParser.NAMES[i]+":"+data[i]);
			}
			System.out.println();
		}
		
	}
}
