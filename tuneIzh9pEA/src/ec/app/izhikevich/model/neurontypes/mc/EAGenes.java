package ec.app.izhikevich.model.neurontypes.mc;

import java.util.HashMap;
import java.util.Map;

import ec.app.izhikevich.inputprocess.labels.ModelParameterID;
import ec.app.izhikevich.util.GeneralUtils;

public final class EAGenes {
	
	private static Map<ModelParameterID, Integer[]> parmToGeneIdxMap;
	public static int geneLength;
	public static int nComps;
	public static int nCurrents;
	
	private float[] genes;
	
	public static void setupEAGene(int n_Comps, int n_Other){		
		nComps = n_Comps;
		
		nCurrents = n_Other;
		buildMap();
	}
	
	
	private static void buildMap(){
		parmToGeneIdxMap = new HashMap<ModelParameterID, Integer[]>();			
		int offset = 0;
		ModelParameterID[] parameters = ModelParameterID.values();
		for(ModelParameterID parm: parameters) {
			if(parm.equals(ModelParameterID.VR)) {
				parmToGeneIdxMap.put(parm, getIndices(offset, 1));	//VR should be same for all compartments
				offset += 1;
			}else
			if(parm.equals(ModelParameterID.G)) {
				parmToGeneIdxMap.put(parm, getIndices(offset, nComps-1));
				offset += (nComps-1);
			}else
			if(parm.equals(ModelParameterID.P)) {
				parmToGeneIdxMap.put(parm, getIndices(offset, nComps-1));
				offset += (nComps-1);
			}else
			if(parm.equals(ModelParameterID.W)) {
				parmToGeneIdxMap.put(parm, getIndices(offset, nComps-1));
				offset += (nComps-1);
			}else
			if(parm==ModelParameterID.I) {
				parmToGeneIdxMap.put(parm, getIndices(offset, nCurrents));
				offset += nCurrents;
			}else
				if(parm==ModelParameterID.I_dur) 
				{
					parmToGeneIdxMap.put(parm, getIndices(offset, nCurrents));
					offset += nCurrents;
				}else{
				parmToGeneIdxMap.put(parm, getIndices(offset, nComps));
				offset += nComps;
			}
		}
		geneLength = offset;
	}
	
	public EAGenes(float[] genes) {
		this.genes = genes;
	}
	public EAGenes(double[] genes) {
		this.genes = new float[genes.length];
		for(int i=0;i<genes.length;i++)
			this.genes[i] = (float) genes[i];	
	}
	private static Integer[] getIndices(int startIdx, int length) {
		if(length<1) return null;
		Integer[] indices = new Integer[length];
		for(int i=0;i<length;i++) {
			indices[i] = startIdx;
			startIdx++;
		}
		return indices;
	}
	
	private float[] getParmValues(ModelParameterID parm) {
		Integer[] idces = parmToGeneIdxMap.get(parm);
		if(idces==null) return null;
		float[] parmVals = new float[idces.length];
		for(int i=0;i<idces.length;i++) {
			parmVals[i] = this.genes[idces[i]];
		}
		return parmVals;
	}
	
	public float[] getK(){
		return getParmValues(ModelParameterID.K);	
	}
	
	public float[] getA(){
		return getParmValues(ModelParameterID.A);	
	}
	
	public float[] getB(){
		return getParmValues(ModelParameterID.B);	
	}
	public float[] getD(){
		return getParmValues(ModelParameterID.D);	
	}
	
	public float[] getCM(){
		return getParmValues(ModelParameterID.CM);	
	}
	public float getVR(){
		return getParmValues(ModelParameterID.VR)[0];	
	}
	public float[] getVMIN(){
		float vr = getVR();
		float[] vmin = getParmValues(ModelParameterID.VMIN); //this is offset
		float somVmin = vmin[0];
		for(int i=0;i<nComps;i++)
			vmin[i] = vr+vmin[i];	
		return 	vmin;
	}
	public float[] getVT(){
		float[] vmin = getVMIN();
		float vr = getVR();
		float[] vt = getParmValues(ModelParameterID.VT); //this is offset
		for(int i=0;i<nComps;i++)
			//vt[i] = vt[i]+vmin[i];
			vt[i] = vt[i]+vr;
		return 	vt;	
	}
	public float[] getVPEAK(){
		float vr = getVR();
		float[] vpeak = getParmValues(ModelParameterID.VPEAK); //this is offset from vr for soma; offset from somavpeak for dendrite
		//float somVpeak = vpeak[0];
		vpeak[0] = vpeak[0] + vr;		
		for(int i=1;i<nComps;i++)
			vpeak[i] = vpeak[0]-vpeak[i];
		return 	vpeak;	
	}
	
	
	public float[] getG(){
		return getParmValues(ModelParameterID.G);	
	}
	
	public float[] getP(){
		return getParmValues(ModelParameterID.P);	
	}
	
	public float[] getW(){
		return getParmValues(ModelParameterID.W);	
	}
	
	public float[] getI(){
		return getParmValues(ModelParameterID.I);	
	}
	
	public static Integer[] getIndices(ModelParameterID parameter) {
		return parmToGeneIdxMap.get(parameter);
	}
	
	public static void displayMap(){
		ModelParameterID[] parameters = ModelParameterID.values();
		for(ModelParameterID parm: parameters) {
			Integer[] indices = getIndices(parm);
			if(indices==null) continue;
			System.out.print(parm.name()+"\t");
			for(int i=0;i<indices.length;i++) {
				System.out.print(indices[i]+", ");
			}
			System.out.println();
		}	
	}
}
