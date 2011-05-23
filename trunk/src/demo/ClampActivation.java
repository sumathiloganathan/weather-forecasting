package demo;

import org.encog.engine.network.activation.ActivationFunction;

public class ClampActivation implements ActivationFunction{

	public ActivationFunction clone(){
		return this;
	}
	
	@Override
	public void activationFunction(final double[] x, final int start, final int size) {
		for (int i = start; i < start + size; i++) {
            if (x[i] < 0.0) x[i] = 0.0;
            else if(x[i] < 0.5) x[i] = x[i]*x[i];
		}
	}

	@Override
	public double derivativeFunction(double arg0) {
		if (arg0<0.0)
			return 0.0;
		if (arg0<0.5)
			return 2*arg0;
		else
			return 1.0;
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
