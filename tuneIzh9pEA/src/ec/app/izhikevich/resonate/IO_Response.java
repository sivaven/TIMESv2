package ec.app.izhikevich.resonate;

import ec.app.izhikevich.model.Izhikevich9pModel;
import ec.app.izhikevich.model.IzhikevichModel;
import ec.app.izhikevich.model.IzhikevichSolver;
import ec.app.izhikevich.spike.SpikePattern;

public class IO_Response {

	Izhikevich9pModel model;
	double timeMin;
	double durationOfCurrent;
	
	public IO_Response(Izhikevich9pModel model) {
		this.model = model;
		timeMin = 100;
		durationOfCurrent = 200;
	}
	
	public double[][] getIFPoints(int minCurrent, int maxCurrent, int noOfCurrentInputs, boolean plotSpikes) {
		
		int[] inputs = getCurrentInputs(minCurrent, maxCurrent, noOfCurrentInputs);
		double[][] IF = new double[2][inputs.length];
		
		//GeneralUtils.displayArray(inputs);
		for(int i=0; i<inputs.length; i++) {			
			SpikePattern model_spike_pattern = solveModel(inputs[i], plotSpikes, false);
	        IF[0][i]= inputs[i];
	        IF[1][i] = model_spike_pattern.getAverageFrequency();
		}
        return IF;
	}
	
	public SpikePattern solveModel(double current, boolean plotSpikes, boolean addEPSPHandler) {
		model.setInputParameters(current, timeMin, durationOfCurrent);	 
		IzhikevichSolver solver = null;
		if(addEPSPHandler) {			
			solver = new IzhikevichSolver(model, IzhikevichSolver.getEPEpspHandler(model, timeMin+50, 10));	
		}
		else{
			solver = new IzhikevichSolver(model);	        
		}
		SpikePattern model_spike_pattern = solver.getSpikePatternAdapting();
        
        if(plotSpikes) {
        	plotSpikes(model_spike_pattern);
        }
        return model_spike_pattern;
	}
	public int[] getCurrentInputs(int min, int max, int n) {
		int step = (max-min)/n;
		int[] current_inputs = new int [n];
		int j=0;
		for(int i=min; i<max; i+=step) {
			current_inputs[j++] = i;
		}
		return current_inputs;
	}
	
	public void plotSpikes(SpikePattern model_spike_pattern) {
		double[] time = model_spike_pattern.getSpikePatternData().getTime();
		double[] voltage = model_spike_pattern.getSpikePatternData().getVoltage();
	//	PlottingUtils.plot(time, voltage);
	}
	public void plotIFCurve(double[][] IF) {		
	//	PlottingUtils.plot(IF[0], IF[1]);
	}
	
	public static Izhikevich9pModel getSample9pResonator() {
		Izhikevich9pModel model1 = new Izhikevich9pModel();
		model1.setK(1);
		model1.setA(0.15);
		model1.setB(8);
		model1.setD(200);		
		model1.setcM(80);
		model1.setvR(-55);			
		model1.setvMin(-55);//
		model1.setvT(-40);	
		model1.setvPeak(25);
		return model1;
	}
	
	public static Izhikevich9pModel getSample9pIntegrator() {
		Izhikevich9pModel model1 = new Izhikevich9pModel();
		model1.setK(0.316);
		model1.setA(0.749);
		model1.setB(-0.651);
		model1.setD(63);		
		model1.setcM(1);
		model1.setvR(-56.77);			
		model1.setvMin(-63.77);//
		model1.setvT(-31.17);	
		model1.setvPeak(43.23);
		return model1;
	}
	
	public static IzhikevichModel getSample5pIntegrator() {
		/*
		 *  0.02f, 0.2f, -65.0f, and 8.0f correspond to the Izhikevich parameters a, b, c, and d, respectively. 
		 *   These parameter choices would generate a regular-spiking (RS) neuron (class 1-excitable)
		 *   a=0.02,b=0.2, c=-65, d=8) - 2014 carsim paper RS parms
		 *   a=0.1, b=0.2, c=-65, d=2  - same as above - FS neurons - class 2
		 */
		IzhikevichModel model1 = new IzhikevichModel();		
		model1.setA(0.02);
		model1.setB(0.2);
		model1.setD(8);					
		model1.setvMin(-65.0);//		
		model1.setvPeak(30);
		return model1;
	}
	
	public static IzhikevichModel getSample5pResonator() {
		/*
		 *  0.02f, 0.2f, -65.0f, and 8.0f correspond to the Izhikevich parameters a, b, c, and d, respectively. 
		 *   These parameter choices would generate a regular-spiking (RS) neuron (class 1-excitable)
		 *   a=0.02,b=0.2, c=-65, d=8) - 2014 carsim paper RS parms
		 *   a=0.1, b=0.2, c=-65, d=2  - same as above - FS neurons - class 2
		 */
		IzhikevichModel model1 = new IzhikevichModel();		
		model1.setA(0.1);
		model1.setB(0.2);
		model1.setD(2);					
		model1.setvMin(-65.0);//		
		model1.setvPeak(30);
		return model1;
	}
	
	public static void main(String[] args) {
		int minCurrent = 0;
		int maxCurrent = 10;
		int noOfCurrentInputs = 10;
		
		Izhikevich9pModel model1 = getSample9pIntegrator();
		
		IO_Response ioResponse = new IO_Response(model1 );
		/*
		double[][] IF = ioResponse.getIFPoints(minCurrent, maxCurrent, noOfCurrentInputs, false);
		GeneralUtils.displayArray(IF);
		ioResponse.plotIFCurve(IF);
		*/
	    ioResponse.solveModel(0, true, true);
	}

}
