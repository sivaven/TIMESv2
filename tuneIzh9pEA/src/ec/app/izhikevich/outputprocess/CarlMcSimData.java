package ec.app.izhikevich.outputprocess;

import ec.app.izhikevich.util.GeneralUtils;

public class CarlMcSimData {
	private double[] rampRheos;
	private double[] vDefs;
	private boolean[] spikeProped;
	private double[] epsps;
	public CarlMcSimData(double[] _rampRheos, double[] _vDefs, boolean[] _spikeProped, double[] _epsps){
		rampRheos = _rampRheos;
		vDefs = _vDefs;
		spikeProped = _spikeProped;
		epsps = _epsps;
	}
	
	public double[] getRampRheos(){
		return rampRheos;
	}
	public double[] getVdefs(){
		return vDefs;
	}
	public boolean[] getSpikeProped(){
		return spikeProped;
	}
	public double[] getEpsps(){
		return epsps;
	}
	
	public void display(){
		System.out.print("Ramp Rheos.\t"); GeneralUtils.displayArray(rampRheos);
		System.out.print("V Defs.\t"); GeneralUtils.displayArray(vDefs);
		System.out.print("spike Proped? .\t"); GeneralUtils.displayArray(spikeProped);
		System.out.print("soma Epsps.\t"); GeneralUtils.displayArray(epsps);
	}
	
}
