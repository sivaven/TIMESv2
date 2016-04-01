package ec.app.izhikevich.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

import ec.app.izhikevich.spike.ModelSpikePatternData;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.util.GeneralUtils;

public class IzhikevichSolverMC {	
	//Default values for solver parameters
	public static final float SS = 0.1f; // step size
	protected  static final float T_0 = 0f ; // initial time	
	
	//
	protected float sS ; // step size
	protected float t_0 ; // initial time
	protected float t_N ;			
	protected Izhikevich9pModelMC model;
	
	protected boolean displayStatus;
	
	public IzhikevichSolverMC(Izhikevich9pModelMC model) {
		this.model = model;
		//override TN
		float tn = (float) ((model.getTimeMin()*2) + model.getDurationOfCurrent());				
		this.setParameters(SS, T_0, tn);		
	}
	
	
	public void setParameters(float ss, float t0, float tn) {
		this.sS = ss;
		this.t_0 = t0;
		this.t_N = tn;
	}
	
	public SpikePatternAdapting[] solveAndGetSpikePatternAdapting() {
		if(displayStatus)
			System.out.println("	RK starting..");
			ODESolution[] solution = this.solveModelUsingRKInt();
			if(displayStatus)
			System.out.println("	RK Ended..");
			if(solution !=null) {
				SpikePatternAdapting[] modelSpikePattern = new SpikePatternAdapting[model.getNCompartments()];
				
				for(int i=0;i<modelSpikePattern.length;i++){					
					ModelSpikePatternData spikePatternData = new ModelSpikePatternData(solution[i].getTime(), 
																				solution[i].getVoltage(), 
																				solution[i].getSpikeTimes());	
					modelSpikePattern[i] = new SpikePatternAdapting(spikePatternData, 
							model.getAppCurrent(i), 
							model.getTimeMin(), 
							model.getDurationOfCurrent(),
							model.getvR()[i]
							);
				}
				return modelSpikePattern;
			}
			else return null;
	}
	
	public SpikePatternAdapting[] getSpikePatternAdapting(ODESolution solution) {	
		/*
		 * only setting up somatic spike pattern -- 
		 * no plan to read spike patterns from carlsim
		 * therefore, set null to dendritic modelSpikePatterns
		 */
			if(solution !=null) {
				SpikePatternAdapting[] modelSpikePattern = new SpikePatternAdapting[model.getNCompartments()];
				
				for(int i=0;i<modelSpikePattern.length;i++){	
					if(i==0){
						ModelSpikePatternData spikePatternData = new ModelSpikePatternData(solution.getTime(), 
								solution.getVoltage(), 
								solution.getSpikeTimes());	
						modelSpikePattern[i] = new SpikePatternAdapting(spikePatternData, 
															model.getAppCurrent(i), 
															model.getTimeMin(), 
															model.getDurationOfCurrent(),
															model.getvR()[0]
															);
					}
					else{
						modelSpikePattern[i] = new SpikePatternAdapting();// dummy for dendritic compartments - nothing passed from external sim.
					}
				}
				return modelSpikePattern;
			}
			else return null;
	}
	
	public ODESolution[] solveModelUsingRKInt() {
		ClassicalRungeKuttaIntegrator rkt = new ClassicalRungeKuttaIntegrator(getsS());
		FirstOrderDifferentialEquations ode = model;		
		
		double[] y0 = model.getInitialStateForSolver();//new double[] {v0, u0, v1,u1 }; // initial state
		double[] y = new double[y0.length];		
		StateRecorder stateRecorder = new StateRecorder(model.getStateIdxToRecordForSolver());	
		rkt.addStepHandler(stateRecorder);
		
		SpikeEventHandler[] spikeEventHandler = new SpikeEventHandler[model.getNCompartments()];
		/*
		 * Event handler parameters : IMPORTANT!!
		 */
		double maxCheckInterval = 0.01; // maximal time interval between switching function checks 
									  //(this interval prevents missing sign changes in case the integration steps becomes very large)
		double convergenceThreshold = 0.001; //  convergence threshold in the event time search
		int maxIterationCount = 100; // upper limit of the iteration count in the event time search
		for(int i=0;i<spikeEventHandler.length;i++) {
			spikeEventHandler[i] = new SpikeEventHandler(model.getvPeak()[i], 
					model.getvMin()[i], 
					model.getD()[i], 
					model.getAllVidxForSolver()[i],
					model.getAllUidxForSolver()[i]);
			rkt.addEventHandler(spikeEventHandler[i], maxCheckInterval, convergenceThreshold , maxIterationCount); // Univariate Solver - default root finding algorithm; 5th parm
		}		
		if(displayStatus)
			System.out.println("		Integrate begin..");
		try{
			//rkt.setMaxEvaluations(100000);
			//System.out.println(rkt.getMaxEvaluations());
			rkt.integrate(ode, t_0, y0, t_N, y);
			//System.out.println(rkt.getEvaluations());
		}catch(NoBracketingException nbe) {
			return null;		
		}
		if(displayStatus)
		System.out.println("		Integrate Ended..");
		
		ODESolution[] solution = new ODESolution[model.getNCompartments()];
		for(int i=0;i<solution.length;i++) {
			solution[i] = new ODESolution(stateRecorder.getX(), stateRecorder.getY(i), spikeEventHandler[i].spikeTimes);
		}
		return solution;
	}
	
	public static EPSPHandler getEPEpspHandler(Izhikevich9pModel model, double time, double mag) {
		EPSPHandler epspHandler = new EPSPHandler(model.getvR(), time, mag);
		return epspHandler;
	}
	/*
	public static EPSPHandler getEPEpspHandler(IzhikevichModel model, double time, double mag) {
		EPSPHandler epspHandler = new EPSPHandler(-65, time, mag);
		return epspHandler;
	}
	*/
	public double getsS() {
		return sS;
	}

	public void setsS(double sS) {
		this.sS = (float) sS;
	}


	public boolean isDisplayStatus() {
		return displayStatus;
	}


	public void setDisplayStatus(boolean displayStatus) {
		this.displayStatus = displayStatus;
	}
}
/*
 * continuous output model of numeric solver
 * for all compartments
 */
class StateRecorder implements StepHandler {
	ArrayList<Double> x0; 
	List<ArrayList<Double>> y;
	int[] stateIdx;
	
	public StateRecorder(int[] stateIdx) {
		this.stateIdx = stateIdx;
		x0 = new ArrayList<>();
		y = new ArrayList<ArrayList<Double>>();
		for(int i=0;i<stateIdx.length;i++){
			y.add(new ArrayList<Double>());
		}
	}		
	@Override
	public void handleStep(StepInterpolator interpolator, boolean isLast)
			throws MaxCountExceededException {		
		x0.add(interpolator.getCurrentTime());
		for(int i=0;i<y.size();i++){
			y.get(i).add(interpolator.getInterpolatedState()[stateIdx[i]]);
		}	        
	}
	@Override
	public void init(double arg0, double[] arg1, double arg2) {	}	
    
	public double[] getX() {  	return GeneralUtils.listToArrayDouble(x0);   }    
   
    public double[] getY(int i) { 	
    	return  GeneralUtils.listToArrayDouble(y.get(i));     	
    }
}
/*
 * compartment specific
 */
class SpikeEventHandler implements EventHandler {
	double c;
	double d;
	double vPeak;	
	ArrayList<Double> spikeTimes; 
	int vIdx;
	int uIdx;
	
	public SpikeEventHandler(double vpeak, double c, double d, int vIdx, int uIdx) {
		this.c = c;
		this.d = d;
		this.vPeak = vpeak;
		spikeTimes = new ArrayList<>();
		this.vIdx = vIdx;
		this.uIdx = uIdx;
	}
	
	@Override
	public Action eventOccurred(double t, double[] y, boolean increasing) {		
		//return Action.CONTINUE;
		
	//		if(y[vIdx]>vPeak+100)
	//			return Action.STOP;
				
		//System.out.println(y[0]+"\t"+y[1]+"\t"+y[2]);
		if(!increasing)
			return Action.RESET_STATE;
		else {
			return Action.CONTINUE;		}
	}

	@Override
	public double g(double t, double[] y) {
		return vPeak - y[vIdx];
	}

	@Override
	public void init(double arg0, double[] arg1, double arg2) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void resetState(double t, double[] y) {
	//	System.out.println(t);
		
		spikeTimes.add(t);
		y[vIdx] = c;
		y[uIdx] += d;
	}	
}

