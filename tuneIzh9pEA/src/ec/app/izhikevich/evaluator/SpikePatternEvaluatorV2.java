package ec.app.izhikevich.evaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import ec.app.izhikevich.evaluator.qualifier.SolverResultsStat;
import ec.app.izhikevich.evaluator.qualifier.SpikePatternClassifier;
import ec.app.izhikevich.evaluator.qualifier.StatAnalyzer;
import ec.app.izhikevich.inputprocess.InputSpikePatternConstraint;
import ec.app.izhikevich.inputprocess.labels.BurstFeatureID;
import ec.app.izhikevich.inputprocess.labels.PatternFeatureID;
import ec.app.izhikevich.model.IzhikevichSolver;
import ec.app.izhikevich.spike.BurstFeature;
import ec.app.izhikevich.spike.PatternFeature;
import ec.app.izhikevich.spike.PatternType;
import ec.app.izhikevich.spike.SpikePatternAdapting;
import ec.app.izhikevich.starter.ECJStarterV2;
import ec.app.izhikevich.util.GeneralUtils;
import ec.app.izhikevich.util.StatUtil;

public class SpikePatternEvaluatorV2{
/*
 * Remember, all error methods here return weighted 0-1 normed errors
 */
	SpikePatternAdapting modelSpikePattern;	//This will also receive the child AdaptingSpikePatterns, therefore define sfaError here itself.
	InputSpikePatternConstraint expSpikePatternData;
	double[] patternRepairWeights;
	float modelVmin;
	float modelVr;
	float modelVt;
	
	private boolean checkForPatternValidity;
	private boolean display;
	
	SpikePatternClassifier patternClassifier;
	
	public SpikePatternEvaluatorV2(SpikePatternAdapting modelSpikePattern, InputSpikePatternConstraint expSpikePatternData, double[] patternRepWeights, 
			float modelVmin, float modelVr, float modelVt,
			boolean display) {
		this.modelSpikePattern = modelSpikePattern;
		this.expSpikePatternData = expSpikePatternData;	
		this.patternRepairWeights = patternRepWeights;
		this.modelVmin = modelVmin;
		this.modelVr = modelVr;
		this.modelVt = modelVt;
		this.display = display;
		
		/*if(display){
			StatAnalyzer.display_stats = true;
		}*/
		patternClassifier = new SpikePatternClassifier(modelSpikePattern);
		patternClassifier.classifySpikePattern();
		if(display){
			patternClassifier.getSpikePatternClass().display();
		}
		patternClassifier.determineWeightsForFeatures(expSpikePatternData.getSpikePatternClass());
		
	}
	private void displayRoutine(PatternFeatureID featureID, PatternFeature feature, double modelValue, double error) {
		if(display) {
			String displayString = "\t"+featureID;
			if(feature.isRange()) {
				displayString += "\t("+GeneralUtils.formatTwoDecimal(feature.getValueMin())+", "
									+GeneralUtils.formatTwoDecimal(feature.getValueMax())+")";
			}else{
				displayString += "\t"+GeneralUtils.formatTwoDecimal(feature.getValue());
			}				
			displayString += "\t"+GeneralUtils.formatTwoDecimal(modelValue);
			displayString += "\t"+GeneralUtils.formatThreeDecimal(error)+"\t/";
			System.out.println(displayString);
		}
	}
	
	private void displayRoutineForSFA(double[] exp, double[] model, 
			double error) {		
			String displayString = "\ty="+GeneralUtils.formatThreeDecimal(exp[0])+"x+"+GeneralUtils.formatThreeDecimal(exp[1]);	
			displayString += "\tnISIs: "+exp[2];
			displayString += "\ty="+GeneralUtils.formatThreeDecimal(exp[3])+"x+"+GeneralUtils.formatThreeDecimal(exp[4]);	
			displayString += "\tnISIs: "+exp[5];
			
			displayString +="\n\ty="+GeneralUtils.formatThreeDecimal(model[0])+"x+"+GeneralUtils.formatThreeDecimal(model[1]);	
			displayString += "\tnISIs: "+model[2];
			displayString +="\ty="+GeneralUtils.formatThreeDecimal(model[3])+"x+"+GeneralUtils.formatThreeDecimal(model[4]);	
			displayString += "\tnISIs: "+model[5];
			displayString += "\t"+GeneralUtils.formatThreeDecimal(error)+"\t/";
			System.out.println(displayString);
		
	}
	
	public float calculatePatternError() {		
		float patternError = Float.MAX_VALUE; 			
		float patternValidityErrorVmin = 0;
		float patternValidityErrorVrest =0;		
	        
	        if(modelSpikePattern == null) {   	 if(display) {System.out.println("**NULL compartment SPIKE PATTERN**");  	 }
	        	return patternError;
	        }
	        //&& Math.abs(Float.MAX_VALUE - modelError)>10.0f
	        if(checkForPatternValidity){
		        if(expSpikePatternData.getType() == PatternType.SPIKES ){  			       
					    if(!modelSpikePattern.isValidSpikesPattern(modelVmin, modelVr)) { if(display) {  System.out.println("**Invalid SPIKEs Pattern**");	   }					    	
					    	return patternError;				    	
					    } else{
				//	    	if(!expSpikePatternData.getFeaturesToEvaluate().contains(PatternFeatureID.bursts))
					    	{
					    //		patternValidityErrorVmin = nonBurstBelowVminError(modelVmin);
					    	}				    		
					        patternValidityErrorVrest = spikesBelowVrestError(modelVr);
					    }
			        	if(modelSpikePattern.getISIs() == null ) { if(display) {System.out.println("**Null ISIs**");      }	
			        		return patternError;
			        	}		        					         	
				 }
		        
		        if(expSpikePatternData.getType() ==PatternType.RBASE){	        	
		        	if(!modelSpikePattern.isValidSpikesPattern(modelVmin, modelVr)) { if(display) { System.out.println("**Invalid RBASE Pattern**");  }	
		        		return patternError;
				    }else{
				  // 	if(!expSpikePatternData.getFeaturesToEvaluate().contains(PatternFeatureID.bursts))
				    	{
				    //		patternValidityErrorVmin = nonBurstBelowVminError( modelVmin);
				    	}			    	
				        patternValidityErrorVrest = spikesBelowVrestError( modelVr);
				    }	 				         	
		        }
		        
		        if(expSpikePatternData.getType() == PatternType.SUBSSVOLT){	        	
			        PatternFeature expSubSSVolt = expSpikePatternData.getSsSubVoltage();
			        double expSubSSVoltValue;
			        if(!expSubSSVolt.isRange()) {
			        	expSubSSVoltValue = expSubSSVolt.getValue();
			        }else{
			        	expSubSSVoltValue = expSubSSVolt.getValueMin();
			        }
				    if(!modelSpikePattern.isValidSubSSVoltPattern(expSubSSVoltValue,modelVmin, modelVr, expSpikePatternData.getValidMaxV().getValue())) { if(display) { System.out.println("**Invalid SUBSSVOLT Pattern**"); }	
				    	return patternError;
				    }		         	
		        }	   
	        }else{
	        	/*
	        	 * this should be removed once voltage is used from carl sim-- done in previous checks!!
	        	 */
	        	if(modelSpikePattern.getSpikeTimes().length < 1) {
	    			return patternError;
	    		}	
	        	if(modelSpikePattern.getISIs() == null ) { if(display) {System.out.println("**Null ISIs**");      }	
        			return patternError;
	        	}	
	        }
	        //System.out.println("SPE:: patternWeighted Avg Error will..");
		        
	        patternError = patternWeightedAvgError();  
	       // System.out.println("SPE:: patternWeighted Avg Error done..");
		       		        
	        float avgPatternError;	        
	        if(expSpikePatternData.getType() == PatternType.SPIKES || expSpikePatternData.getType() == PatternType.RBASE) {
	              avgPatternError = (float) (patternRepairWeights[0]*patternError + 
	            		  patternRepairWeights[1] * patternValidityErrorVmin + 
		        		patternRepairWeights[2] * patternValidityErrorVrest);		         
	        }else {
	        	avgPatternError = patternError;	
	        }
	        if(display) {
	   	 		System.out.print("\nPatternFeatError\t"+GeneralUtils.formatThreeDecimal(patternError)+
	   	 						"\nPatternValidityErrorVmin\t"+GeneralUtils.formatThreeDecimal(patternValidityErrorVmin)+
	   	 						"\nPatternValidityErrorVrest\t"+GeneralUtils.formatThreeDecimal(patternValidityErrorVrest));
	   	 	}  
	        return avgPatternError * expSpikePatternData.getPatternWeight();
	}
	
	private float patternWeightedAvgError() {
		float weightedError = 0;
		int count=0;
		//SpikePatternEvaluator evaluator = new SpikePatternEvaluator(modelSpikePattern, expSpikePatternData, display);
		Set<PatternFeatureID> patternFeatureIDs = expSpikePatternData.getFeaturesToEvaluate();
		if(patternFeatureIDs.contains(PatternFeatureID.bursts) || 
			patternFeatureIDs.contains(PatternFeatureID.nbursts) ||
			patternFeatureIDs.contains(PatternFeatureID.stuts)){
			modelSpikePattern.initBurstPattern(2d);
		}
		for (PatternFeatureID feature: patternFeatureIDs){
			/*
			 * non spiking
			 */
			if(feature==PatternFeatureID.fsl) {
				weightedError += FSLErrorObsNormed();
				//System.out.println("SPE:: fsl Error done..");
				count++;
			}
			if(feature==PatternFeatureID.pss) {
				weightedError += PSSErrorObsNormed();
				//System.out.println("SPE:: pss Error done..");
				count++;
			}		
			
			/*
			 * regular / Adaptation
			 */
			if(feature==PatternFeatureID.n_sfa_isis1) {
				weightedError += sfaErrorObsNormed();
				//System.out.println("SPE:: n_sfa1 Error done.."+weightedError);
				count++;
			}	
			
			if(feature==PatternFeatureID.n_spikes) {
				weightedError += NspikesError();
				//System.out.println("SPE:: nspikes Error done..");
				count++;
			}			
			/*
			 * stut
			 */
			if(feature==PatternFeatureID.nbursts) {				
				weightedError += NburstsError(ECJStarterV2.EXP_NORM_RATE_NB);
				count++;
			}
			if(feature==PatternFeatureID.bursts || feature==PatternFeatureID.stuts ) {				
				weightedError += burstFeatureError(ECJStarterV2.EXP_NORM_RATE_SFA, ECJStarterV2.EXP_NORM_RATE_NS*10);
				count++;
			}
			if(feature==PatternFeatureID.vmin_offset) {
				weightedError += vMinOffsetError(modelVmin-modelVr);
				count++;
			}
		}
		
		return weightedError;
	//	return (weightedError/(count*1.0f));
	}
	
	
	public double FSLError() {
	//	System.out.println("SPE:: fsl Error entry..");
		PatternFeature feature = expSpikePatternData.getFsl();
		double minError = 0;
		double maxError = expSpikePatternData.getCurrentDuration();
		double error = patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.fsl)
							*	NormalizedError(feature, modelSpikePattern.getFSL(), minError, maxError);
		displayRoutine(PatternFeatureID.fsl,feature, modelSpikePattern.getFSL(), error);
		return error;
	}
	public double FSLErrorObsNormed() {
		//	System.out.println("SPE:: fsl Error entry..");
			PatternFeature feature = expSpikePatternData.getFsl();
			double minError = 0;
			double maxError = expSpikePatternData.getCurrentDuration();
			double error = patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.fsl)
								*	NormalizedErrorObsNormed(feature, modelSpikePattern.getFSL(), minError, maxError);
			displayRoutine(PatternFeatureID.fsl,feature, modelSpikePattern.getFSL(), error);
			return error;
		}
	/*public double FSLError(float expRate) {	
		//System.out.println("SPE:: fsl Error entry..");
		PatternFeature feature = expSpikePatternData.getFsl();		
		double error = patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.fsl)
				*	NormalizedErrorExp(feature, modelSpikePattern.getFSL(), expRate);
		displayRoutine(PatternFeatureID.fsl,feature, modelSpikePattern.getFSL(), error);
		return error;
	}*/
	
	public double PSSErrorObsNormed() {
		PatternFeature feature = expSpikePatternData.getPss();
		double minError = 0;
		double maxError = expSpikePatternData.getCurrentDuration();
		double error = patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.pss)
				*	NormalizedErrorObsNormed(feature, modelSpikePattern.getPSS(), minError, maxError);	
		displayRoutine(PatternFeatureID.pss,feature, modelSpikePattern.getPSS(), error);
		return error;
	}
	public double PSSError() {
		PatternFeature feature = expSpikePatternData.getPss();
		double minError = 0;
		double maxError = expSpikePatternData.getCurrentDuration();
		double error = patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.pss)
				*	NormalizedError(feature, modelSpikePattern.getPSS(), minError, maxError);	
		displayRoutine(PatternFeatureID.pss,feature, modelSpikePattern.getPSS(), error);
		return error;
	}
	/*public double PSSError(float expRate) {
		PatternFeature feature = expSpikePatternData.getPss();		
		double error = patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.pss)
				*	NormalizedErrorExp(feature, modelSpikePattern.getPSS(), expRate);	
		displayRoutine(PatternFeatureID.pss,feature, modelSpikePattern.getPSS(), error);
		return error;
	}*/
	
	
	public double NspikesError() {
		PatternFeature feature = expSpikePatternData.getnSpikes();
		double minError = 0;
		double maxError = expSpikePatternData.getCurrentDuration() * 0.5;// / IzhikevichSolver.SS;
		double error = patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.n_spikes)
				*	NormalizedError(feature, modelSpikePattern.getNoOfSpikes(), minError, maxError);
		displayRoutine(PatternFeatureID.n_spikes,feature, modelSpikePattern.getNoOfSpikes(), error);
		return error;
	}
	public double NspikesError(float expRate) {
		PatternFeature feature = expSpikePatternData.getnSpikes();
		
		double error = patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.n_spikes)
				*	NormalizedErrorExp(feature, modelSpikePattern.getNoOfSpikes(), expRate);
		displayRoutine(PatternFeatureID.n_spikes,feature, modelSpikePattern.getNoOfSpikes(), error);
		return error;
	}
	
	public double NburstsError(float expRate) {
		PatternFeature feature = expSpikePatternData.getnBursts();
		double error = patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.nbursts)
				*	NormalizedErrorExp(feature, modelSpikePattern.getBurstPattern().getNBursts(), expRate);
		displayRoutine(PatternFeatureID.nbursts,feature, modelSpikePattern.getBurstPattern().getNBursts(), error);
		return error;
	}
	
	/*
	 * the following finds the perpendicular distance of points from the linear fit. therefore, no need to calculate intercept and all like the previous method
	 */
	/*
	public double sfaLinearFitError1() {
		PatternFeature nSfaISIs = expSpikePatternData.getNSfaISIs1();
		double sfaLinearM = expSpikePatternData.getSfaLinearM1().getValue();
		double sfaLinearB = expSpikePatternData.getSfaLinearb1().getValue();
		
		double[][] latAndISIs = ((SpikePatternAdapting)modelSpikePattern).getFirstNISIsAndTheirLatenciesForRegression((int)nSfaISIs.getValue());
		double error = 0;
		double minDistance = 0;
		double maxDistance = StatUtil.calculatePerpendicularDistance(expSpikePatternData.getCurrentDuration(), 0, sfaLinearM, sfaLinearB);
		for(int i=0;i<latAndISIs.length;i++) {
			double distance = StatUtil.calculatePerpendicularDistance(latAndISIs[i][0], latAndISIs[i][1], sfaLinearM, sfaLinearB);
			error += StatUtil.normalize0to1(distance, minDistance, maxDistance);
		}
		error = patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.n_sfa_isis1)
				* (error/(1.0d*latAndISIs.length));
		if(display) {
			displayRoutineForSFA(sfaLinearM, sfaLinearB, 
					((SpikePatternAdapting)modelSpikePattern).calculateSfa((int)nSfaISIs.getValue()), 
					((SpikePatternAdapting)modelSpikePattern).calculateSfaYintrcpt((int)nSfaISIs.getValue()),
					error
					);
		}
		return error;
		
	}
	
	//should get nsfaisis from '1'
	public double sfaLinearFitError2() {		
		PatternFeature nSfaISIs2 = expSpikePatternData.getNSfaISIs2();
		PatternFeature nSfaISIs1 = expSpikePatternData.getNSfaISIs1();
		double sfaLinearM = expSpikePatternData.getSfaLinearM2().getValue();
		double sfaLinearB = expSpikePatternData.getSfaLinearb2().getValue();	

		double[][] latAndISIs = ((SpikePatternAdapting)modelSpikePattern).
									getFirstNISIsAndTheirLatenciesForRegression2((int)nSfaISIs1.getValue(),
											(int)nSfaISIs2.getValue());
		if(latAndISIs==null){
			return 1;
		}
		double error = 0;
		double minDistance = 0;
		double maxDistance = StatUtil.calculatePerpendicularDistance(expSpikePatternData.getCurrentDuration(), 0, sfaLinearM, sfaLinearB);
		for(int i=0;i<latAndISIs.length;i++) {
			double distance = StatUtil.calculatePerpendicularDistance(latAndISIs[i][0], latAndISIs[i][1], sfaLinearM, sfaLinearB);
			error += StatUtil.normalize0to1(distance, minDistance, maxDistance);
		}
		
		error = patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.n_sfa_isis2)
				* (error/(1.0d*latAndISIs.length));
		if(display) {
			displayRoutineForSFA(sfaLinearM, sfaLinearB, 
					((SpikePatternAdapting)modelSpikePattern).calculateSfa2((int)nSfaISIs1.getValue(),
							(int)nSfaISIs2.getValue()), 
					((SpikePatternAdapting)modelSpikePattern).calculateSfaYintrcpt2((int)nSfaISIs1.getValue(), 
							(int)nSfaISIs2.getValue()),
					error
					);
		}
		return error;
		
	}
	
	public double sfaLinearFitError1(double expRate) {		
		PatternFeature nSfaISIs = expSpikePatternData.getNSfaISIs1();
		double sfaLinearM = expSpikePatternData.getSfaLinearM1().getValue();
		double sfaLinearB = expSpikePatternData.getSfaLinearb1().getValue();		
		double[][] latAndISIs = ((SpikePatternAdapting)modelSpikePattern).getFirstNISIsAndTheirLatenciesForRegression((int)nSfaISIs.getValue());
		
		double error = 0;
	//	double minDistance = 0;
	//	double maxDistance = StatUtil.calculatePerpendicularDistance(expSpikePatternData.getCurrentDuration(), 0, sfaLinearM, sfaLinearB);
		for(int i=0;i<latAndISIs.length;i++) {
			double distance = StatUtil.calculatePerpendicularDistance(latAndISIs[i][0], latAndISIs[i][1], sfaLinearM, sfaLinearB);
			error += StatUtil.normalize0to1Exp(distance, expRate);
		}
		error = patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.n_sfa_isis1)
				* (error/(1.0d*latAndISIs.length));
		if(display) {
			displayRoutineForSFA(sfaLinearM, sfaLinearB, 
					((SpikePatternAdapting)modelSpikePattern).calculateSfa((int)nSfaISIs.getValue()), 
					((SpikePatternAdapting)modelSpikePattern).calculateSfaYintrcpt((int)nSfaISIs.getValue()),
					error
					);
		}
		return error;
		
	}
	//should get nsfaisis from '1'
	public double sfaLinearFitError2(double expRate) {		
		PatternFeature nSfaISIs2 = expSpikePatternData.getNSfaISIs2();
		PatternFeature nSfaISIs1 = expSpikePatternData.getNSfaISIs1();
		double sfaLinearM = expSpikePatternData.getSfaLinearM2().getValue();
		double sfaLinearB = expSpikePatternData.getSfaLinearb2().getValue();	

		double[][] latAndISIs = ((SpikePatternAdapting)modelSpikePattern).
									getFirstNISIsAndTheirLatenciesForRegression2((int)nSfaISIs1.getValue(),
											(int)nSfaISIs2.getValue());
		if(latAndISIs==null){
			return 1;
		}
		double error = 0;
	//	double minDistance = 0;
	//	double maxDistance = StatUtil.calculatePerpendicularDistance(expSpikePatternData.getCurrentDuration(), 0, sfaLinearM, sfaLinearB);
		for(int i=0;i<latAndISIs.length;i++) {
			double distance = StatUtil.calculatePerpendicularDistance(latAndISIs[i][0], latAndISIs[i][1], sfaLinearM, sfaLinearB);
			error += StatUtil.normalize0to1Exp(distance, expRate);
		}
		
		error = patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.n_sfa_isis2)
				* (error/(1.0d*latAndISIs.length));
		if(display) {
			displayRoutineForSFA(sfaLinearM, sfaLinearB, 
					((SpikePatternAdapting)modelSpikePattern).calculateSfa2((int)nSfaISIs1.getValue(),
							(int)nSfaISIs2.getValue()), 
					((SpikePatternAdapting)modelSpikePattern).calculateSfaYintrcpt2((int)nSfaISIs1.getValue(), 
							(int)nSfaISIs2.getValue()),
					error
					);
		}
		return error;
		
	}
	/*
	 * //classify should hve been invoked;
	 */
	
	public double sfaError() {		
		
		int nPieceWiseParmsExp = expSpikePatternData.getSpikePatternClass().getnPieceWiseParms();
		int nPieceWiseParmsModel = patternClassifier.getSpikePatternClass().getnPieceWiseParms(); 
		double possibleWeightForFutureNEAR = nPieceWiseParmsExp - nPieceWiseParmsModel; //coarse!!
		
		double error = 0;
		double minError = 0;
		double maxMerror = 1;		
		double maxCerror = 2;
		double maxNsfaError = expSpikePatternData.getCurrentDuration() * 0.3;
		
		SolverResultsStat modelStats = patternClassifier.getSolverResultsStats()[nPieceWiseParmsExp-1];
		int modelNsfaISIs1 = modelStats.getBreakPoint();
		int modelNsfaISIs2 = modelSpikePattern.getISIs().length - modelNsfaISIs1;
		double[] model = new double[] {modelStats.getM1(), modelStats.getM2(), 
				modelStats.getC1(), modelStats.getC2(),
				modelNsfaISIs1, modelNsfaISIs2
				};
		double[] exp = new double[] {expSpikePatternData.getSfaLinearM1().getValue(),				
				expSpikePatternData.getSfaLinearM2().getValue(),
				expSpikePatternData.getSfaLinearb1().getValue(),
				expSpikePatternData.getSfaLinearb2().getValue(),
				expSpikePatternData.getNSfaISIs1().getValue(),
				expSpikePatternData.getNSfaISIs2().getValue(),
				};
		
		for(int i=0;i<6; i++){
			if(i<2){//slopes
				error += StatUtil.calculate0to1NormalizedError(exp[i], 
						model[i]
								, 
						minError, 
						maxMerror
						);
			}else{
				if(i<4){//intercepts
				error += StatUtil.calculate0to1NormalizedError(exp[i], 
						model[i]
								, 
						minError, 
						maxCerror
								);
				}else{
					error += StatUtil.calculate0to1NormalizedError(exp[i], 
							model[i]
									, 
							minError, 
							maxNsfaError
									);
				}
			}			
		}		
		
		error = patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.n_sfa_isis2)
				* error;
		
		if(display) {
			displayRoutineForSFA(exp,model,	error);
		}
		return error;
		
	}
	
public double sfaErrorObsNormed() {		
		
		//int nPieceWiseParmsExp = expSpikePatternData.getSpikePatternClass().getnPieceWiseParms();
		int nPieceWiseParmsModel = patternClassifier.getSpikePatternClass().getnPieceWiseParms(); 
		//double possibleWeightForFutureNEAR = nPieceWiseParmsExp - nPieceWiseParmsModel; //coarse!!
	//	patternClassifier.getSpikePatternClass().display();
		double error = 0;
		
		SolverResultsStat modelStats = patternClassifier.getSolverResultsStats()[nPieceWiseParmsModel-1];
		int modelNsfaISIs1 = 1 + modelStats.getBreakPoint(); //0-based idx ; hence +1
		int modelNsfaISIs2 = modelSpikePattern.getISIs().length - modelNsfaISIs1; // remaining
		double[] model = new double[] {modelStats.getM1(), modelStats.getC1(), modelNsfaISIs1,
				modelStats.getM2(), modelStats.getC2(), modelNsfaISIs2
				};
		double[] exp = new double[] {expSpikePatternData.getSfaLinearM1().getValue(),					
				expSpikePatternData.getSfaLinearb1().getValue(),
				expSpikePatternData.getNSfaISIs1().getValue(),
				
				expSpikePatternData.getSfaLinearM2().getValue(),
				expSpikePatternData.getSfaLinearb2().getValue(),				
				expSpikePatternData.getNSfaISIs2().getValue(),
				};
		
		//System.out.println(patternClassifier.getDynamicFeatWeightMatrix().size());
		for(int i=0;i<6; i++){
			if(i<3)		error += patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.n_sfa_isis1) *
									StatUtil.calculateObsNormalizedError(exp[i], model[i]);
			else		error += patternClassifier.getDynamicFeatWeightMatrix().get(PatternFeatureID.n_sfa_isis2) *
									StatUtil.calculateObsNormalizedError(exp[i], model[i]);			
		}		
		
		if(display) {
			displayRoutineForSFA(exp,model,	error);
		}
		return error;
		
	}
	
	public double burstFeatureError(double sfaExpRate, double nspikesExpRate) {
		BurstFeature expBurstFeature = expSpikePatternData.getBurstFeatures();
		ArrayList<HashMap<BurstFeatureID, Double>> expBurstFeatures = expBurstFeature.getValue();
	
     //   modelSpikePattern.initBurstPattern(expBurstFeature.getAllNSpikes());
        
        
   //     ArrayList<ArrayList<Double>> isisDuringBurst = modelSpikePattern.getISIsDuringBurst();	        
   //     patternError += (float) StatUtil.calculate0to1NormalizedError(1, isisDuringBurst.size(), 0, 4);
   //     if(display) {
   //	 		System.out.print("#ofBursts.\t"+isisDuringBurst.size());
   //	 	}  
        
        
		
		double error = 0;
	/*	if(display) {
			System.out.print("\tBursts:");
			modelSpikePattern.getBurstPattern().displayBursts();
			System.out.println("\n");
		}*/
		for(int i=0;i<expBurstFeatures.size();i++){
			error += singleBurstError(i, expBurstFeatures.get(i), sfaExpRate, nspikesExpRate);
		}
		error = expBurstFeature.getTotalWeight() * (error/(1.0f*expBurstFeatures.size()));
		
		if(display) {	
			System.out.print("\t"+GeneralUtils.formatThreeDecimal(error)+"\n");
		}
		
		return error;
	}
	
	/*
	 * currently, nspikes and sfa errors; non sfa isi error should be?
	 */
	private double singleBurstError(int burstIdx, HashMap<BurstFeatureID, Double> expSingleBurstFeatures, double SfaExpRate, double NspikesSfaRate){
		if(burstIdx>=modelSpikePattern.getBurstPattern().getNBursts()){
			return 0;
		}
		double errorSFA = 0;
		double errorBW = 0;
		double errorIBI = 0;
		double errorNspikes = 0;
		double errorVmin = 0;
		
		double nSfaISIs = -1;
		double sfaLinearM = -1;
		double sfaLinearB = -1;
		
		double burstConsCount =0;
		
		
		if(expSingleBurstFeatures.containsKey(BurstFeatureID.nsfa)) {
			 nSfaISIs = expSingleBurstFeatures.get(BurstFeatureID.nsfa);
			 sfaLinearM = expSingleBurstFeatures.get(BurstFeatureID.sfa_m);
			 sfaLinearB = expSingleBurstFeatures.get(BurstFeatureID.sfa_b);
			
			//1. SFA error
			double[][] latAndISIs = modelSpikePattern.getBurstPattern().getFirstNISIsAndTheirLatenciesForRegression(burstIdx, (int)nSfaISIs);
			if(latAndISIs==null) 
				return 1;		
			for(int i=0;i<latAndISIs.length;i++) {
			//	System.out.println(i);
				double distance = StatUtil.calculatePerpendicularDistance(latAndISIs[i][0], latAndISIs[i][1], sfaLinearM, sfaLinearB);
				errorSFA += StatUtil.normalize0to1Exp(distance, SfaExpRate);
			}				
			errorSFA = (errorSFA/(1.0d*latAndISIs.length));
			burstConsCount++;
		}
		
		//2. BW error
		double maxIBI_BW_error = expSpikePatternData.getCurrentDuration();
		double minError = 0;
		double bw = -1;
		if(expSingleBurstFeatures.containsKey(BurstFeatureID.b_w)){
			 bw = expSingleBurstFeatures.get(BurstFeatureID.b_w);
			 double modelBw = 	modelSpikePattern.getBurstPattern().getBW(burstIdx);
			 errorBW = StatUtil.calculate0to1NormalizedError(bw,
						 	modelBw,
							minError, maxIBI_BW_error														
							);							
			burstConsCount++;
		}		
			
		//3. IBI error
		double pbi = -1;
		if(expSingleBurstFeatures.containsKey(BurstFeatureID.pbi)){
			 pbi = expSingleBurstFeatures.get(BurstFeatureID.pbi);	
			 double modelPbi = modelSpikePattern.getBurstPattern().getIBI(burstIdx);
			 errorIBI = StatUtil.calculate0to1NormalizedError(pbi,
							modelPbi,														
								minError, maxIBI_BW_error	
							);
			 			
			burstConsCount++;
		}
		
		
		// 4. N spikes error
		double nspikes = expSingleBurstFeatures.get(BurstFeatureID.nspikes);
		errorNspikes = StatUtil.calculate0to1NormalizedErrorExp(nspikes, 
				modelSpikePattern.getBurstPattern().getNSpikes(burstIdx), 
				NspikesSfaRate);
		burstConsCount++;
		
		//5. Vmin error
		double exp_pbi_vmin_offset = 0;
		double model_pbi_vMinOffset = 0;
		if(expSingleBurstFeatures.containsKey(BurstFeatureID.pbi_vmin_offset)){
			if(burstIdx >= modelSpikePattern.getBurstPattern().getISIsDuringBurst().size() ){
				errorVmin = 0;
			}
			else{
				exp_pbi_vmin_offset = expSingleBurstFeatures.get(BurstFeatureID.pbi_vmin_offset);		
					double pbi_timeMin = modelSpikePattern.getBurstPattern().getPbiTimeMin(modelSpikePattern.getTimeMin(), 
							modelSpikePattern.getFSL(), 
							burstIdx);
					double pbi_timeMax = modelSpikePattern.getBurstPattern().getPbiTimeMax(modelSpikePattern.getTimeMin(),
							modelSpikePattern.getFSL(), 
							burstIdx,
							modelSpikePattern.getTimeMin()+modelSpikePattern.getDurationOfCurrentInjection());		
					
				//	System.out.println(pbi_timeMin +"\t"+ modelSpikePattern.getTimeMin() +"\t"+ modelSpikePattern.getFSL() +"\t"+ pbi_timeMax);
					model_pbi_vMinOffset = this.modelVmin -
									modelSpikePattern.getSpikePatternData().getMinVoltage(pbi_timeMin, pbi_timeMax, IzhikevichSolver.SS);
					if(model_pbi_vMinOffset <= exp_pbi_vmin_offset){
						errorVmin = 0;
					}else{
						errorVmin = StatUtil.calculate0to1NormalizedError(exp_pbi_vmin_offset, 
							model_pbi_vMinOffset, 
							0, 
							SpikePatternAdapting.V_BELOW_ALLOWED_OFFSET_FROM_VMIN_DURING_SPIKE);
					}
			}
		}
		
		/*
		 * display 
		 */
		if(display) {
			System.out.print("\t");
			if(expSingleBurstFeatures.containsKey(BurstFeatureID.b_w)){
			String displayString = "\tb_w\t"+GeneralUtils.formatTwoDecimal(bw);						
			displayString += "\t"+GeneralUtils.formatTwoDecimal(modelSpikePattern.getBurstPattern().getBW(burstIdx));
			displayString += "\t"+GeneralUtils.formatThreeDecimal(errorBW)+"\t/";
			System.out.print(displayString);
			}
			
			if(expSingleBurstFeatures.containsKey(BurstFeatureID.pbi)){
				String displayString = "\tibi\t"+GeneralUtils.formatTwoDecimal(pbi);						
			displayString += "\t"+GeneralUtils.formatTwoDecimal(modelSpikePattern.getBurstPattern().getIBI(burstIdx));
			displayString += "\t"+GeneralUtils.formatThreeDecimal(errorIBI)+"\t/";
			System.out.print(displayString);
			}
			
			String displayString = "\tnspikes\t"+GeneralUtils.formatTwoDecimal(nspikes);						
			displayString += "\t"+GeneralUtils.formatTwoDecimal(modelSpikePattern.getBurstPattern().getNSpikes(burstIdx));
			displayString += "\t"+GeneralUtils.formatThreeDecimal(errorNspikes)+"\t/";
			System.out.print(displayString);
			
			if(expSingleBurstFeatures.containsKey(BurstFeatureID.pbi_vmin_offset)){
				displayString = "\tpbi_vmin_offset\t"+GeneralUtils.formatTwoDecimal(exp_pbi_vmin_offset);						
				displayString += "\t"+GeneralUtils.formatTwoDecimal(model_pbi_vMinOffset);
				displayString += "\t"+GeneralUtils.formatThreeDecimal(errorVmin)+"\t/";
				System.out.print(displayString);
			}
		/*	if(nSfaISIs > 1)
			displayRoutineForSFA(sfaLinearM, sfaLinearB, 					
					modelSpikePattern.getBurstPattern().calculateSfa(burstIdx, (int)nSfaISIs), 0,
					errorSFA
					);
			else
				System.out.println();
				*/
		}
		
	
		
		
		
		return ((expSpikePatternData.getBurstFeatures().getFeatureWeight(BurstFeatureID.nsfa)  		*  	errorSFA)+
				(expSpikePatternData.getBurstFeatures().getFeatureWeight(BurstFeatureID.b_w)  		*  	errorBW)+
				(expSpikePatternData.getBurstFeatures().getFeatureWeight(BurstFeatureID.pbi)   		*	errorIBI)+
				(expSpikePatternData.getBurstFeatures().getFeatureWeight(BurstFeatureID.nspikes)	*	errorNspikes)+
				(expSpikePatternData.getBurstFeatures().getFeatureWeight(BurstFeatureID.pbi_vmin_offset)	*	errorVmin)
				);

				///burstConsCount;
	}
	/*
	 * 
	 */

	public double vMinOffsetError(double modelVminOffset) {	
		PatternFeature feature = expSpikePatternData.getvMinOffset();
		double minError = 0;
		double maxError;
		/*
		 * the following holds only when constraining the model vminoffset within (0, expVmin)
		 * probably have to get the min gene value and calculate it!
		 */
		if(!feature.isRange()) {
			maxError= feature.getValue();
		}else{
			maxError = feature.getValueMin();
		}
		double error = feature.getWeight() * NormalizedError(feature, modelVminOffset, minError, maxError);
		displayRoutine(PatternFeatureID.vmin_offset,feature, modelVminOffset, error);
		return error;
	}
	
	
	
	private double weightedNormalizedError(PatternFeature feature, double[] modelValues, double minError, double maxError) {
		double w = feature.getWeight();		
		if(feature.isRange()) {
			return w * StatUtil.calculateAvgOf0to1NormalizedErrors(feature.getValueMin(), 
																	feature.getValueMax(), 
																	modelValues, 
																	minError, 
																	maxError);
		}else {
			return w * StatUtil.calculateAvgOf0to1NormalizedErrors(feature.getValue(), 
					modelValues, 
					minError, 
					maxError);
		}
	}
	
	private double NormalizedError(PatternFeature feature, 
			double modelValue, double minError, double maxError) {		
		if(feature.isRange()) {
			return StatUtil.calculate0to1NormalizedError(feature.getValueMin(), 
																	feature.getValueMax(), 
																	modelValue
																	, 
																	minError, 
																	maxError
																	);
		}else {
			return StatUtil.calculate0to1NormalizedError(feature.getValue(), 
					modelValue
					, 
					minError, 
					maxError
					);
		}
	}
	private double NormalizedErrorObsNormed(PatternFeature feature, 
			double modelValue, double minError, double maxError) {		
		if(feature.isRange()) {
			return StatUtil.calculateObsNormalizedError(feature.getValueMin(), 
																	feature.getValueMax(), 
																	modelValue
																	);
		}else {
			return StatUtil.calculateObsNormalizedError(feature.getValue(), 
					modelValue
					);
		}
	}
	private double NormalizedErrorExp(PatternFeature feature, double modelValue, double expRate) {			
		if(feature.isRange()) {
			return StatUtil.calculate0to1NormalizedErrorExp(feature.getValueMin(), 
																	feature.getValueMax(), 
																	modelValue, 
																	expRate);
		}else {
			return StatUtil.calculate0to1NormalizedErrorExp(feature.getValue(), 
					modelValue, 
					expRate);
		}
	}
	
	/*
	 * other non-feature errors; like avoid bursting...
	 */
	private float nonBurstBelowVminError(float vMin) {	
		//System.out.println("adf");
		double avgBelowVminOffset = modelSpikePattern.calculateAvgBelowVmin(vMin);
		double minError = 0;
		double maxError = SpikePatternAdapting.V_BELOW_ALLOWED_OFFSET_FROM_VMIN_DURING_SPIKE;
		return 
				(float) StatUtil.normalize0to1(avgBelowVminOffset, minError, maxError);
	}
	private float spikesBelowVrestError(float vR) {		
		double avgBelowVrestOffset = modelSpikePattern.calculateAvgBelowVrest(vR);
		double minError = 0;
		double maxError = SpikePatternAdapting.ALLOWED_OFFSET_FROM_VREST;
		return 
				(float) StatUtil.normalize0to1(avgBelowVrestOffset, minError, maxError);
	}
	public boolean isCheckForPatternValidity() {
		return checkForPatternValidity;
	}
	public void setCheckForPatternValidity(boolean checkForPatternValidity) {
		this.checkForPatternValidity = checkForPatternValidity;
	}
	
}
