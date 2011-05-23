package demo;

import org.encog.engine.network.activation.ActivationFunction;

public class ClampActivation implements ActivationFunction{

	public ActivationFunction clone(){
		return this;
	}
	
	@Override
	public void activationFunction(final double[] x, final int start, final int size) {
		for (int i = start; i < start + size; i++) {
            if (x[i] < 0) x[i] = 0;
		}
	}

	@Override
	public double derivativeFunction(double arg0) {
		return arg0 < 0 ? 0.0 :1.0;
	}

	@Override
	public String[] getParamNames() {
		System.out.println("getParamNames");
		return null;
	}

	@Override
	public double[] getParams() {
		System.out.println("getParams");
		return null;
	}

	@Override
	public boolean hasDerivative() {
		return true;
	}

	@Override
	public void setParam(int arg0, double arg1) {
		System.out.println("setParam");
	}

}
