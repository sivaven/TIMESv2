package ec.app.izhikevich.evaluator.qualifier;

import java.util.HashMap;
import java.util.Map;

import ec.app.izhikevich.inputprocess.labels.PatternFeatureID;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.spike.labels.SpikePatternClass;
import ec.app.izhikevich.spike.labels.SpikePatternComponent;
import ec.app.izhikevich.util.GeneralUtils;
import ec.app.izhikevich.util.StatUtil;

public class SpikePatternClassifier {
	public static boolean DISABLED = true;
	public static float SHADOW_FITNESS = 1;
	
	private SpikePatternAdapting somaSpikePattern;
//	private float fitness;	
	private SpikePatternClass patternClass;
	private SolverResultsStat[] solverStats;
	private Map<PatternFeatureID, Float> dynamicFeatWeightMatrix;
	
	private static final double DELAY_FACTOR = 2d;
	private static final double SLN_FACTOR = 2d;
	private static final double BURST_FACTOR = 2d;
	private static final double SWA_FACTOR = 5d;

	/* The following defines whether sfa1 (1) or sfa2 (2) or both (1,2) or none (0) 
	 * should be given high weight under different scenarios.
	 * 
	 * Observed / target	X (0parm)		NASP 	ASP		ASP.NASP	ASP.ASP
				X (0 parm)		0			1		1		1,2			1,2
				NASP			1			0		1		1,2			1,2
				ASP				1			1		0		2			2
				ASP.NASP		1,2			1,2		2		0			2
				ASP.ASP			1,2			1,2		2		2			0
	 */
	private static final int[][] SFA_WEIGHT_DET_SCENARIOS = new int[][] {	{0,1,1,3,3},
																			{1,0,1,3,3},
																			{1,1,0,2,2},
																			{3,3,2,0,2},
																			{3,3,2,2,0}
																		  };
	
	/*public SpikePatternClassifier(float _fitness, SpikePatternAdapting _somaPattern){
		this.fitness = _fitness;
		this.somaSpikePattern = _somaPattern;
		this.patternClass = null;
	}*/
	public SpikePatternClassifier(SpikePatternAdapting _somaPattern){
		this();
		this.somaSpikePattern = _somaPattern;
		dynamicFeatWeightMatrix = new HashMap<>();
	}
	/*
	 * for experimental trace classifier
	 */
	public SpikePatternClassifier(){
		somaSpikePattern = null;
		patternClass = new SpikePatternClass();
		SolverResultsStat init = new SolverResultsStat(0, 0, 0, 0, new double[]{}, 0);
		solverStats = new SolverResultsStat[] {init,init,init,init};
	}
	
	public void classifySpikePattern(){
		/*
		 * 0. Check for valid ISIs
		 */	
		if(somaSpikePattern.getISIs()==null){
			return;
		}	
		/*
		 * I. check for Delay
		 *   if no isis OR fsl > criterion, it's a delay
		 */
		if(somaSpikePattern.getISIs().length == 0){			
			return;
		}
		if(hasDelay()){
			patternClass.addComponent(SpikePatternComponent.D);
		}
		
		/*
		 * II. classify Spikes
		 */
		if(somaSpikePattern.getISIs().length == 1){			
			patternClass.addComponent(SpikePatternComponent.X);
		}
		if(somaSpikePattern.getISIs().length > 1){
			classifySpikes();
		}			
		
		/*
		 * III. check for SLN 
		 */
		if(hasPostSLN()){
			patternClass.addComponent(SpikePatternComponent.SLN);
		}
	}
	
	public void determineWeightsForFeatures(SpikePatternClass targetClass){		
		/*
		 * 4 weights are determined here, depending on the cases	
		 */
		
		
		dynamicFeatWeightMatrix.put(PatternFeatureID.fsl, delayWeight(targetClass));
	
		if(targetClass.contains(SpikePatternComponent.ASP) ||
				targetClass.contains(SpikePatternComponent.NASP)){
			addAdaptationWeight(targetClass);					
		}
		
		dynamicFeatWeightMatrix.put(PatternFeatureID.pss, postSlnWeight(targetClass));
		
		if(targetClass.contains(SpikePatternComponent.TSTUT) 		||
				targetClass.contains(SpikePatternComponent.PSTUT) 	||
				targetClass.contains(SpikePatternComponent.TSWB) 	||
				targetClass.contains(SpikePatternComponent.PSWB) ){
			dynamicFeatWeightMatrix.put(PatternFeatureID.nbursts, stutWeight());
		}
	}
	/*
	 * Delay > 2*ISIavg(1,2)
	 */
	private boolean hasDelay(){
		double isiFilt=somaSpikePattern.getISI0();
		if(somaSpikePattern.getISIs().length>1){
			isiFilt = (somaSpikePattern.getISI0()+somaSpikePattern.getISI(1))/2d;
		}		
		if(somaSpikePattern.getFSL() > DELAY_FACTOR*isiFilt){					
			return true;
		}else
			return false;
	}
	private float delayWeight(SpikePatternClass targetClass){
		if((targetClass.contains(SpikePatternComponent.D) && 
				!patternClass.contains(SpikePatternComponent.D))
			||
			(!targetClass.contains(SpikePatternComponent.D) && 
					patternClass.contains(SpikePatternComponent.D)))
			{
				double fsl = somaSpikePattern.getFSL();
				
				double isiFilt=somaSpikePattern.getISI0();
				if(somaSpikePattern.getISIs().length>1){
					isiFilt = (somaSpikePattern.getISI0()+somaSpikePattern.getISI(1))/2d;
				}	
				return (float) (1f + StatUtil.calculateObsNormalizedError(DELAY_FACTOR, (fsl/isiFilt)));
			}
		else return 1f;
		
	}
	
	/*
	 * look at last 2 isis, and max isis
	 */
	private boolean hasPostSLN(){
		double isiFilt=somaSpikePattern.getISILast();
		if(somaSpikePattern.getISIs().length>1){
			isiFilt = (somaSpikePattern.getISILast()+
					somaSpikePattern.getISI(somaSpikePattern.getISIs().length-2))
					/2d;
		}		
		if(somaSpikePattern.getPSS() > SLN_FACTOR * isiFilt &&
				somaSpikePattern.getPSS() > SLN_FACTOR * somaSpikePattern.getMaxISI()){					
			return true;
		}else
			return false;
	}
	private float postSlnWeight(SpikePatternClass targetClass){
		if((targetClass.contains(SpikePatternComponent.SLN) && 
				!patternClass.contains(SpikePatternComponent.SLN))
			||
			(!targetClass.contains(SpikePatternComponent.SLN) && 
					patternClass.contains(SpikePatternComponent.SLN)))
			{
				double isiFilt=somaSpikePattern.getISILast();
				if(somaSpikePattern.getISIs().length>1){
					isiFilt = (somaSpikePattern.getISILast()+
							somaSpikePattern.getISI(somaSpikePattern.getISIs().length-2))
							/2d;
				}
				double pss = somaSpikePattern.getPSS() ; 
				
			
				 float error  = (float) StatUtil.calculateObsNormalizedError(SLN_FACTOR, (pss/isiFilt));
				 error += (float) (1+StatUtil.calculateObsNormalizedError(SLN_FACTOR, (pss/somaSpikePattern.getMaxISI())));
				 
				 return error;
			}
		else return 1f;
	}
	
	private void classifySpikes(){
		
		//System.out.println("called");
		solverStats = new SolverResultsStat[4];
		
		double[] latencies = somaSpikePattern.getISILatenciesToSecondSpike();
		double[] ISIs = somaSpikePattern.getISIs();
		
		/*
		 * to simplify -- so that stat tests match across ACM and CARLsim
		 * 	- because CARLsim only returns integer spike times
		 *  - stat tests are sensitive to small decimal values some times
		 */
		//latencies = GeneralUtils.roundOff(latencies);
		//ISIs = GeneralUtils.roundOff(ISIs);
		
		double scaleFactor = somaSpikePattern.getMinISI();
		double[] X = StatUtil.shiftLeftToZeroAndScaleSimple(latencies, scaleFactor);
		double[] Y = StatUtil.scaleSimple(ISIs, scaleFactor);
		
		if(StatAnalyzer.display_stats){
			GeneralUtils.display2DArrayVerticalUnRounded(new double[][]{X,Y});
		}
		LeastSquareSolverUtil l = new LeastSquareSolverUtil(X, Y);
		
		/*
		 * 1. SSF
		 */
		
		solverStats[0] = l.solveFor1Parm(1);			
		solverStats[1] = l.solveFor2Parms(0, 1);	
		solverStats[2] = l.solveFor3Parms(solverStats[1].getM1(), 1, 1);
		solverStats[3] = l.solveFor4Parms(solverStats[2].getM1(), 1, 0, 1);
				
		if(!StatAnalyzer.isSignificantImprovement(solverStats[0].getFitResidualsAbs(), solverStats[1].getFitResidualsAbs(), 1, 2)) {
			patternClass.addComponent(SpikePatternComponent.NASP);
			return;
		}				
		/*
		 * 2. ASP
		 */		
		if(!StatAnalyzer.isSignificantImprovement(solverStats[1].getFitResidualsAbs(), solverStats[2].getFitResidualsAbs(), 2, 3)) {
			patternClass.addComponent(SpikePatternComponent.ASP);
			return;
		}			
		/*
		 * 3. ASP.NASP / ASP.ASP
		 */		
		if(!StatAnalyzer.isSignificantImprovement(solverStats[2].getFitResidualsAbs(), solverStats[3].getFitResidualsAbs(), 3, 4)){
			patternClass.addComponent(SpikePatternComponent.ASP);
			patternClass.addComponent(SpikePatternComponent.NASP);
			return;
		}else{
			patternClass.addComponent(SpikePatternComponent.ASP);
			patternClass.addComponent(SpikePatternComponent.ASP);
			return;
		}
	}
	private void addAdaptationWeight(SpikePatternClass targetClass){
		float constWeight = 2f;
		
		int modelNpwParms = patternClass.getnPieceWiseParms();
		int targetNpwParms = targetClass.getnPieceWiseParms();
		
		if(SFA_WEIGHT_DET_SCENARIOS[modelNpwParms][targetNpwParms] == 0){
			dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis1, 1f);
			dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis2, 1f);
		}
		if(SFA_WEIGHT_DET_SCENARIOS[modelNpwParms][targetNpwParms] == 1){
			dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis1, constWeight);
			dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis2, 1f);
		}
		if(SFA_WEIGHT_DET_SCENARIOS[modelNpwParms][targetNpwParms] == 2){
			dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis1, 1f);
			dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis2, constWeight);
		}
		if(SFA_WEIGHT_DET_SCENARIOS[modelNpwParms][targetNpwParms] == 3){
			dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis1, constWeight);
			dynamicFeatWeightMatrix.put(PatternFeatureID.n_sfa_isis2, constWeight);
		}
	}
	
	private float stutWeight(){
		return 0;
	}
	
	private boolean hasBurst(){
		int maxISIidx = GeneralUtils.findMaxIdx(somaSpikePattern.getISIs());
		if(maxISIidx == somaSpikePattern.getISIs().length-1) {
			//last ISI is max ISI
			return false;
		}
		double maxISI = somaSpikePattern.getISI(maxISIidx);		
		double post_maxISI = somaSpikePattern.getISI(maxISIidx+1);
		if(maxISI > BURST_FACTOR * post_maxISI){
			return true;
		}
		
		return false;
	}
	
	private boolean hasSTUT(){
		//if(hasBurst() && swa)
		return false;
	}
	
	public Map<PatternFeatureID, Float> getDynamicFeatWeightMatrix(){
		return dynamicFeatWeightMatrix;
	}
	
	public SolverResultsStat[] getSolverResultsStats(){
		return solverStats;
	}
	public SpikePatternClass getSpikePatternClass(){
		return this.patternClass;
	}
	private void classifySpikes_MYLOGIC(){
		
		//System.out.println("called");
		solverStats = new SolverResultsStat[4];
		
		double[] latencies = somaSpikePattern.getISILatenciesToSecondSpike();
		double[] ISIs = somaSpikePattern.getISIs();
		
		double scaleFactor = somaSpikePattern.getMinISI();
		double[] X = StatUtil.shiftLeftToZeroAndScaleSimple(latencies, scaleFactor);
		double[] Y = StatUtil.scaleSimple(ISIs, scaleFactor);
		
		if(StatAnalyzer.display_stats){
			GeneralUtils.display2DArrayVerticalUnRounded(new double[][]{X,Y});
		}
		LeastSquareSolverUtil l = new LeastSquareSolverUtil(X, Y);
		
		/*
		 * 1. SSF
		 */
		
		solverStats[0] = l.solveFor1Parm(1);			
		solverStats[1] = l.solveFor2Parms(0, 1);	
		solverStats[2] = l.solveFor3Parms(solverStats[1].getM1(), 1, 1);
		solverStats[3] = l.solveFor4Parms(solverStats[2].getM1(), 1, 0, 1);
				
		if(!StatAnalyzer.isSignificantImprovement(solverStats[0].getFitResidualsAbs(), solverStats[1].getFitResidualsAbs(), 1, 2) &&
				!StatAnalyzer.isSignificantImprovement(solverStats[0].getFitResidualsAbs(), solverStats[2].getFitResidualsAbs(), 1, 3) &&
				!StatAnalyzer.isSignificantImprovement(solverStats[0].getFitResidualsAbs(), solverStats[3].getFitResidualsAbs(), 1, 4)){
			patternClass.addComponent(SpikePatternComponent.NASP);
			return;
		}				
		/*
		 * 2. ASP
		 */		
		if(!StatAnalyzer.isSignificantImprovement(solverStats[1].getFitResidualsAbs(), solverStats[2].getFitResidualsAbs(), 2, 3) &&
				!StatAnalyzer.isSignificantImprovement(solverStats[1].getFitResidualsAbs(), solverStats[3].getFitResidualsAbs(), 2, 4)){
			patternClass.addComponent(SpikePatternComponent.ASP);
			return;
		}			
		/*
		 * 3. ASP.NASP / ASP.ASP
		 */		
		if(!StatAnalyzer.isSignificantImprovement(solverStats[2].getFitResidualsAbs(), solverStats[3].getFitResidualsAbs(), 3, 4)){
			patternClass.addComponent(SpikePatternComponent.ASP);
			patternClass.addComponent(SpikePatternComponent.NASP);
			return;
		}else{
			patternClass.addComponent(SpikePatternComponent.ASP);
			patternClass.addComponent(SpikePatternComponent.ASP);
			return;
		}
	}
	public static void main(String[] args) {
		

	}

	

}
