package demo;
import java.util.Iterator;

import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.neural.networks.BasicNetwork;

/**
 * Four weather stations, the output is always station number 2.
 * @author erik
 *
 */
public abstract class Weather implements MLDataSet{
	
	protected abstract MLDataPair generateData(MLDataPair in);
	public abstract BasicNetwork getGoodNetwork();
	
	private class WeatherGenerator implements Iterator<MLDataPair>{
		
		@Override
		public boolean hasNext() {
			return true;
		}

		@Override
		public MLDataPair next() {
			return generateData(null);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("remove");
		}
	}
	
	@Override
	public Iterator<MLDataPair> iterator() {
		System.out.println("iterator");
		return new WeatherGenerator();
	}

	@Override
	public void add(MLData arg0) {
		System.out.println("add(MLData arg0)");
	}

	@Override
	public void add(MLDataPair arg0) {
		System.out.println("add(MLDataPair arg0)");
	}

	@Override
	public void add(MLData arg0, MLData arg1) {
		System.out.println("add(MLData arg0, MLData arg1)");
	}

	@Override
	public void close() {
		System.out.println("close");
	}

	@Override
	public void getRecord(long arg0, MLDataPair arg1) {
		generateData(arg1);
	}

	@Override
	public long getRecordCount() {
		return 10000;//this is weird, effects getError somehow
	}

	@Override
	public boolean isSupervised() {
		System.out.println("isSupervised");
		return true;
	}

	@Override
	public MLDataSet openAdditional() {
		System.out.println("openAdditional");
		return this;
	}

}
