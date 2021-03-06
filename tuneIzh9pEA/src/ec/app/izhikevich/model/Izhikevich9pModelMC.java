package ec.app.izhikevich.model;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.stat.StatUtils;

import ec.app.izhikevich.inputprocess.labels.ModelParameterID;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.util.GeneralUtils;

public class Izhikevich9pModelMC implements FirstOrderDifferentialEquations{
	
	public static final int SOMA_IDX = 0;
	public static final int DEND_IDX = 1;
	
	protected int nCompartments;
	//attributes	
	protected 	double appCurrent[];
	protected	double durationOfCurrent;
	protected   double timeMin;
	protected	double timeMax; // set based on the duration of current
	//private boolean dendriticInjection;
	
	protected   float k[];	
	protected   float a[] ; 
	protected   float b[] ;	
	protected   float d[];
	
	protected   float cM[] ;	
	protected   float vR[] ;
	protected   float vT[] ;	
	protected   float vPeak[] ;	
	protected   float vMin[];
		
	//Attributes
	
	protected float g[];
	protected float p[];
	
	//compartmental rheobases
	private float rheoBases[];
	
	public Izhikevich9pModelMC(int nComps) {	
		this.nCompartments = nComps;
		rheoBases = new float[nComps];
	}
	
	public void setInputParameters(double[] appCurrent, double time_min, double duration_of_current) {
		this.appCurrent = appCurrent;
		this.timeMin = time_min;
		this.timeMax = time_min + duration_of_current;	
		this.setDurationOfCurrent(duration_of_current);
		//setDendriticInjection(false);
	}
	
	/*
	 * this method not generic.. implementing '2' compartment without synapse..
	 */
	@Override
	public void computeDerivatives(double t, double[] y, double[] dy)
			throws MaxCountExceededException, DimensionMismatchException {
		double appCurrentSoma;
		double appCurrentDend;
		if(t>=timeMin && t<=timeMax) {
			appCurrentSoma = appCurrent[0];
			appCurrentDend = appCurrent[1];
		}else{
			appCurrentSoma = 0;		
			appCurrentDend = 0;
		}
		
		// Soma
		double V0 = y[0];
		double U0 = y[1];	
		//Dendrite
		double V1 = y[2];
		double U1 = y[3];
		
		//Dendrite
		double iSoma = g[0] * (1-p[0])*(V0 - V1);
		dy[2] = ((k[DEND_IDX] * (V1 - vR[SOMA_IDX]) * (V1 - vT[DEND_IDX]))  - U1 + iSoma + appCurrentDend) / cM[DEND_IDX];
		dy[3] = a[DEND_IDX] * ((b[DEND_IDX] * (V1 - vR[SOMA_IDX])) - U1);	
						
		// Soma	
		double iDend = g[0] * (p[0])*(V1 - V0);
		dy[0] = ((k[SOMA_IDX] * (V0 - vR[SOMA_IDX]) * (V0 - vT[SOMA_IDX]))  - U0 + iDend + appCurrentSoma) / cM[SOMA_IDX];
		dy[1] = a[SOMA_IDX] * ((b[SOMA_IDX] * (V0 - vR[SOMA_IDX])) - U0);		
		
		
	}
	//must override
	public double[] getInitialStateForSolver(){
		//System.out.println("chk2");
		return new double[] {this.vR[0], 0, this.vR[1], 0 };
	}
	//must override
	public int[] getStateIdxToRecordForSolver() {
		return new int[] {0, 2};
	}
	//must override
	public int[] getAllVidxForSolver() {
		return new int[] {0, 2};
	}
	//must override
	public int[] getAllUidxForSolver() {
		return new int[] {1, 3};
		
	}
	public float[] getK() {	return k;}
	public void setK(float[] k) {this.k = k;	}
	public float[] getA() {	return a;}
	public void setA(float[] a) {this.a = a;	}
	public float[] getB() {	return b;}
	public void setB(float[] b) {this.b = b;	}
	public float[] getvR() {	return vR;}
	public void setvR(float vR) {			
		this.vR = new float[nCompartments];
		for(int i=0;i<this.vR.length;i++) {
			this.vR[i] = vR;
		}
	}
	public float[] getvT() {	return vT;}
	public void setvT(float[] vT) {this.vT = vT;}
	public float[] getcM() {	return cM;}
	public void setcM(float[] cM) {this.cM = cM;	}
	//public double[] getvMinOffset() {return this.vMin-this.vR;}
	public float[] getG() { return g; }
	public void setG(float[] g) { this.g = g; }
	public float[] getP() { return p; }
	public void setP(float[] p) { this.p = p; }
	
	public float[] getParm(ModelParameterID id){
		if(id.equals(ModelParameterID.K)) return getK();
		if(id.equals(ModelParameterID.A)) return getA();
		if(id.equals(ModelParameterID.B)) return getB();
		if(id.equals(ModelParameterID.D)) return getD();
		if(id.equals(ModelParameterID.CM)) return getcM();
		if(id.equals(ModelParameterID.VR)) return getvR();
		if(id.equals(ModelParameterID.VT)) return getvT();
		if(id.equals(ModelParameterID.VMIN)) return getvMin();
		if(id.equals(ModelParameterID.VPEAK)) return getvPeak();
		//if(id.equals(ModelParameterID.I)) return new float[]{(float) this.appCurrent[0]};
		System.out.println("Not a valid model parameter id-- Izhikevich9pModelMC -- getParm()");
		System.exit(1);
		return null;
	}

	public void setParm(ModelParameterID id, float[] newVal){
		if(id.equals(ModelParameterID.K)) setK(newVal);
		if(id.equals(ModelParameterID.A)) setA(newVal);
		if(id.equals(ModelParameterID.B)) setB(newVal);
		if(id.equals(ModelParameterID.D)) setD(newVal);
		if(id.equals(ModelParameterID.CM)) setcM(newVal);
		if(id.equals(ModelParameterID.VR)) setvR(newVal[0]);
		if(id.equals(ModelParameterID.VT)) setvT(newVal);
		if(id.equals(ModelParameterID.VMIN)) setvMin(newVal);
		if(id.equals(ModelParameterID.VPEAK)) setvPeak(newVal);
		
		//System.out.println("Not a valid model parameter id-- Izhikevich9pModelMC -- setParm()");
		//System.exit(1);
	}
	
	@Override
	public int getDimension() {		
		return   (nCompartments*2);				//izh state variables
	}

	public float[] getD() {
		return d;
	}

	public void setD(float d[]) {
		this.d = d;
	}

	public float[] getvPeak() {
		return vPeak;
	}

	public void setvPeak(float vPeak[]) {
		this.vPeak = vPeak;
	}

	public float[] getvMin() {
		return vMin;
	}

	public void setvMin(float vMin[]) {
		this.vMin = vMin;
	}

	public int getNCompartments(){
		return this.nCompartments;
	}
	public double[] getvMinOffset() {
		return new double[] { this.vMin[0]-this.vR[0], this.vMin[1]-this.vR[1]};
	}	
	
	public double getDurationOfCurrent() {
		return durationOfCurrent;
	}
	public double getTimeMin() {
		return timeMin;
	}
	public double[] getAppCurrent(){
		return this.appCurrent;
	}
	public double getAppCurrent(int compIdx){
		return this.appCurrent[compIdx];
	}
	public void setDurationOfCurrent(double durationOfCurrent) {
		this.durationOfCurrent = durationOfCurrent;
	}
	
	
	public void determineRheobases(float currDur, float iMin, float iMax, float iSearchStep) {
		//return LinearSearchForRheo( compIdx,  currDur,  iMin,  iMax,  iSearchStep);
		for(int i=0;i<nCompartments;i++){
			Izhikevich9pModel isolatedCompartment = getIsolatedCompartment(i);
			float rheo = isolatedCompartment.getRheo(currDur, iMin, iMax, iSearchStep);
			this.rheoBases[i]=rheo;
		}
		
		//return rheo;
		//return binarySearchForRheo( compIdx,  currDur,  iMin,  iMax, iSearchStep);
	}
	
	public float[] determineVDeflections(float I, float currDur, float vAt) {
		float[] vDefs=new float[this.nCompartments];
		float I_forTest = I;//GeneralUtils.findMin(this.rheoBases)-10;
		
		for(int i=0;i<nCompartments;i++){
			Izhikevich9pModel isolatedCompartment = getIsolatedCompartment(i);
			float v = isolatedCompartment.getVDefAt(I_forTest, currDur, vAt);
			vDefs[i]=v;
		}		
		return vDefs;
	}
	
	private Izhikevich9pModel getIsolatedCompartment(int compIdx){
		Izhikevich9pModel model = new Izhikevich9pModel(); 
        model.setK(this.getK()[compIdx]);
		model.setA(this.getA()[compIdx]);
		model.setB(this.getB()[compIdx]);
		model.setD(this.getD()[compIdx]);	
		model.setcM(this.getcM()[compIdx]);
		model.setvR(this.getvR()[compIdx]);
		model.setvT(this.getvT()[compIdx]);		
		model.setvMin(this.getvMin()[compIdx]);//		
        model.setvPeak(this.getvPeak()[compIdx]);
        return model;
	}
	
	public Izhikevich9pModelMC cloneModelWith(ModelParameterID id, int compIdx, float newVal){
		Izhikevich9pModelMC newModel = new Izhikevich9pModel1C(this.nCompartments);
		newModel.setK(getK());
		newModel.setA(getA());
		newModel.setB(getB());
		newModel.setD(getD());	
		newModel.setcM(getcM());
		newModel.setvR(getvR()[0]);
		newModel.setvT(getvT());		
		newModel.setvMin(getvMin());	
		newModel.setvPeak(getvPeak());
		newModel.setG(getG()); 
		newModel.setP(getP());   
       newModel.setInputParameters(appCurrent, timeMin, durationOfCurrent);    
     
       float[] oldVal = getParm(id);
       float[] newVals = oldVal.clone();
       newVals[compIdx] = newVal;
       
       newModel.setParm(id, newVals);
       return newModel;
	}
	

	/*
	 * this module hangs krasnow node.. something wrong with high frequency spike events
	 * also not generic.. applies only to 2 compartment models
	 */
	public int[] getNSpikesForDendFrequency(float dendFreq, float currDur, float iMin, float iMax, float incStep, boolean display)
	   {
		 int[] nSpikes = new int[2];
	     float iForDendFreq = Float.MAX_VALUE;
	     while(iMin <= iMax)    {	
	    	iForDendFreq = (iMin + iMax) / 2;	
	    	//System.out.println(iMin +"\t"+ iMax);
			this.setInputParameters(new double[]{0, iForDendFreq}, timeMin, currDur);			
			IzhikevichSolverMC solver = new IzhikevichSolverMC(this);
			solver.setsS(0.01);
			SpikePatternAdapting[] modelSpikePattern = solver.solveAndGetSpikePatternAdapting();			
			if(modelSpikePattern == null || modelSpikePattern[0] == null || modelSpikePattern[1] == null){				
				iMin = iForDendFreq + incStep;   
				continue;
				//return Float.MAX_VALUE;	 										
			}
			nSpikes[0] = modelSpikePattern[0].getNoOfSpikes();
			nSpikes[1] = modelSpikePattern[1].getNoOfSpikes();
			
			//System.out.println(iForDendFreq +"\t"+ nSpikes[0]+"\t"+nSpikes[1]);
			
			/*if( nSpikes == 1){
				return rheo;
			}else{*/
				 if (modelSpikePattern[1].getFiringFrequencyBasedOnSpikesCount() > dendFreq){                                             
		              iMax = iForDendFreq - incStep;   
		         } else {                                                        
		              iMin = iForDendFreq + incStep;   
		         }		       
			//}			
	     } 
	     if(display){
	    	 System.out.print("\n"+iForDendFreq +"for dend. "+dendFreq+" Hz.\t");
	     }
	    // System.out.println(iForDendFreq);
	 /*  if(GeneralUtils.isCloseEnough(holdIMax, iMax, 1.0)) {
		   return nSpikes;
	   }*/
	   return nSpikes;	         
	  }

	public float[] getRheoBases() {
		return rheoBases;
	}

	
}
