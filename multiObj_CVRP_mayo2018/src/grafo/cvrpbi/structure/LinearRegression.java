package grafo.cvrpbi.structure;

import java.util.List;

import org.apache.commons.math3.stat.regression.SimpleRegression;

public class LinearRegression {

	private List<Double> x;
	private List<Double>  y;
	private SimpleRegression sr = new SimpleRegression();
	
	public LinearRegression(List<Double> dataX,List<Double> dataY){
		this.x = dataX;
		this.y = dataY;
		for(int p = 0;p<x.size();p++){
			this.sr.addData(this.x.get(p),this.y.get(p));
		}
	}
	public SimpleRegression getSR(){
		return this.sr;
	}
}
