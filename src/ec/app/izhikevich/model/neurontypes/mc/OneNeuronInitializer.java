package ec.app.izhikevich.model.neurontypes.mc;

import java.util.HashMap;
import java.util.Map;

import ec.app.izhikevich.ModelEvaluatorWrapper;
import ec.app.izhikevich.evaluator.MultiCompConstraintEvaluator;
import ec.app.izhikevich.inputprocess.InputParser;
import ec.app.izhikevich.inputprocess.labels.ModelParameterID;
import ec.app.izhikevich.spike.PatternFeature;
import ec.app.izhikevich.starter.EAParmsOfModelParm;

public class OneNeuronInitializer{
	private static String primaryInput;	
	private static final String COMMON_MUT_RATE = "0.3"; //overridden for some cases; see below
	public static final Map<ModelParameterID, EAParmsOfModelParm> geneParms = new HashMap<>();
	private static int nCompartments =0;
	private static int nCurrentGenes=0;
	
	public static void init(int nComp, int[] forwardConnIdcs, String primary_input){
		primaryInput = primary_input;
		
		initModelParmRanges();
		nCompartments=nComp;
		initForwardConnectionIdcs(forwardConnIdcs);		
		initExpSpikePatternData();
		nCurrentGenes=ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS.length;
		initPatRepWeights();
		if(nComp>1)
			initMcConstraintData();
		
		initGeneLayout();
		initGeneParmsOfModelParmsExceptI();
		initGeneParmForI();		
		initGeneParmForIdur();
	}	
	
	private static void initExpSpikePatternData() {
		ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS = InputParser.getExpSpikePatternData(primaryInput);		
	}
	
	private static void initMcConstraintData() {
		ModelEvaluatorWrapper.INPUT_MC_CONS= InputParser.getMCConstraintData(primaryInput);
	}
	
	private static void initModelParmRanges() {
		ModelEvaluatorWrapper.INPUT_MODEL_PARAMETER_RANGES= InputParser.getInputModelParameterRanges(primaryInput);	
	}
	
	private static void initPatRepWeights() {
		ModelEvaluatorWrapper.INPUT_PAT_REP_WEIGHTS = InputParser.getPatternRepairWeights(primaryInput);
	}
	private static void initForwardConnectionIdcs(int[] forwardConnIdcs) {
		MultiCompConstraintEvaluator.forwardConnectionIdcs = forwardConnIdcs;
	}
	private static void initGeneLayout() {
		/*
		 * setup GENE layout
		 */
		EAGenes.setupEAGene(getNCompartments(), getNCurrentGenes());
	}
	
	private static void initGeneParmsOfModelParmsExceptI() {
		/*
		 * load default ranges for model parameters: 
		 * compartments share same parms except mutation bounded: 
		 * dendritic compartments have unbounded mutation
		 */
		String[] minMax = ModelEvaluatorWrapper.INPUT_MODEL_PARAMETER_RANGES.getMinMax(ModelParameterID.K);
		initGeneParmForK(minMax[0], minMax[1], minMax[0], minMax[1]);
		//	geneParms.put(ModelParameter.K, new EAParmsOfModelParm("1",		"3",	"gauss",				"0.1",	"0.3",	"true" ));		
		
		minMax = ModelEvaluatorWrapper.INPUT_MODEL_PARAMETER_RANGES.getMinMax(ModelParameterID.A);
		geneParms.put(ModelParameterID.A, new EAParmsOfModelParm(minMax[0], minMax[1],"reset", 				COMMON_MUT_RATE,	"0.05",	"false" ));		
		
		minMax = ModelEvaluatorWrapper.INPUT_MODEL_PARAMETER_RANGES.getMinMax(ModelParameterID.B);
		geneParms.put(ModelParameterID.B, new EAParmsOfModelParm(minMax[0], minMax[1],	"reset", 				COMMON_MUT_RATE,	"25", 	"false" ));		
		
		minMax = ModelEvaluatorWrapper.INPUT_MODEL_PARAMETER_RANGES.getMinMax(ModelParameterID.D);
		geneParms.put(ModelParameterID.D, new EAParmsOfModelParm(minMax[0], minMax[1],	"integer-random-walk",	COMMON_MUT_RATE,	"0.0", 	"false" ));		
		
		minMax = ModelEvaluatorWrapper.INPUT_MODEL_PARAMETER_RANGES.getMinMax(ModelParameterID.CM);
		geneParms.put(ModelParameterID.CM, new EAParmsOfModelParm(minMax[0], minMax[1],	"integer-random-walk", 	COMMON_MUT_RATE, 	"0.0",	"false" ));		
		
		minMax = ModelEvaluatorWrapper.INPUT_MODEL_PARAMETER_RANGES.getMinMax(ModelParameterID.VR);
		geneParms.put(ModelParameterID.VR, new EAParmsOfModelParm(minMax[0], minMax[1],	"reset", 				COMMON_MUT_RATE,	"2", 	"true" ));	
		
		minMax = ModelEvaluatorWrapper.INPUT_MODEL_PARAMETER_RANGES.getMinMax(ModelParameterID.VMIN);
		geneParms.put(ModelParameterID.VMIN, new EAParmsOfModelParm(minMax[0], minMax[1],	"reset", 				COMMON_MUT_RATE,	"3", 	"true" ));	//vr + gene
		
		minMax = ModelEvaluatorWrapper.INPUT_MODEL_PARAMETER_RANGES.getMinMax(ModelParameterID.VT);
		geneParms.put(ModelParameterID.VT, new EAParmsOfModelParm(minMax[0], minMax[1],	"reset", 				COMMON_MUT_RATE,	"3", 	"true" ));	//vr + gene
		
		minMax = ModelEvaluatorWrapper.INPUT_MODEL_PARAMETER_RANGES.getMinMax(ModelParameterID.VPEAK);
		initGeneParmForVpeak(minMax[0], minMax[1], "1", "20");
		
		//geneParms.put(ModelParameter.VPEAK, new EAParmsOfModelParm("58","60",	"gauss", 				"0.1", 	"0.3", 	"true" ));	//vr + gene	
		minMax = ModelEvaluatorWrapper.INPUT_MODEL_PARAMETER_RANGES.getMinMax(ModelParameterID.G);
		geneParms.put(ModelParameterID.G, new EAParmsOfModelParm(	minMax[0], minMax[1],	"integer-random-walk", 	COMMON_MUT_RATE, 	"0.0", 	"true" ));	
		
		minMax = ModelEvaluatorWrapper.INPUT_MODEL_PARAMETER_RANGES.getMinMax(ModelParameterID.P);
		geneParms.put(ModelParameterID.P, new EAParmsOfModelParm(	minMax[0], minMax[1],	"reset", 				COMMON_MUT_RATE, 	"0.05", "true" ));	
		
		minMax = ModelEvaluatorWrapper.INPUT_MODEL_PARAMETER_RANGES.getMinMax(ModelParameterID.W);
		geneParms.put(ModelParameterID.W, new EAParmsOfModelParm(	minMax[0], minMax[1],	"gauss", 				COMMON_MUT_RATE, 	"0.2", 	"true" ));	
	}
	
	
	private static void initGeneParmForK(String somaMinGene, String somaMaxGene, String dendMinGene, String dendMaxGene){
		//setup for k. ranges are different for soma and dendrite
		String[] minGenes = new String[getNCompartments()];
		String[] maxGenes = new String[getNCompartments()];
		for(int i=0;i<minGenes.length;i++) {
			if(i==0) {
				minGenes[i] = somaMinGene;
				maxGenes[i] = somaMaxGene;
			}else{
				minGenes[i] = dendMinGene;
				maxGenes[i] = dendMaxGene;
			}
			
		}
		geneParms.put(ModelParameterID.K, new EAParmsOfModelParm(	minGenes, maxGenes,	"reset", 				COMMON_MUT_RATE, 	"7", 	"false" ));
		
	}
	
	private static void initGeneParmForVpeak(String somVpeakMin, String somVpeakMax, String dendOffsetMin, String dendOffsetMax){
		//setup for vpeak, since vpeak ranges are different for soma and dendrite
		String[] minGenes = new String[getNCompartments()];
		String[] maxGenes = new String[getNCompartments()];
		for(int i=0;i<minGenes.length;i++) {
			if(i==0) {
				minGenes[i] = somVpeakMin;
				maxGenes[i] = somVpeakMax;
			}else{
				minGenes[i] = (dendOffsetMin);
				maxGenes[i] =(dendOffsetMax);   
			}
			
		}
		geneParms.put(ModelParameterID.VPEAK, new EAParmsOfModelParm(	minGenes, maxGenes,	"reset", 				COMMON_MUT_RATE, 	"3", 	"false" ));
		
	}
	
	private static void initGeneParmForI(){
		//setup for current genes requires extra setup
		int nCurrentGenes = EAGenes.nCurrents;
		String[] minGenes = new String[nCurrentGenes];
		String[] maxGenes = new String[nCurrentGenes];		
		
		int idx=0;
		for(int i=0;i<ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS.length; i++) {
			PatternFeature current = ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS[i].getCurrent();
			if(current.isRange()) {
				minGenes[idx] = String.valueOf(current.getValueMin());
				maxGenes[idx++] = String.valueOf(current.getValueMax());
			}
		}
		geneParms.put(ModelParameterID.I, new EAParmsOfModelParm(	minGenes, maxGenes,	"integer-random-walk", 	COMMON_MUT_RATE, 	"0.0", 	"true" ));
		
	}
	
	private static void initGeneParmForIdur(){
		//setup for current durations genes requires similar setup to current genes
		int nCurrentGenes = EAGenes.nCurrents;
		
		String[] minGenesForIdur = new String[nCurrentGenes];
		String[] maxGenesForIdur = new String[nCurrentGenes];
		
		int idx=0;
		for(int i=0;i<ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS.length; i++) {
			double expIdur = ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS[i].getCurrentDuration();			
			minGenesForIdur[idx] = String.valueOf(expIdur);
			maxGenesForIdur[idx++] = String.valueOf(expIdur);
		}
		geneParms.put(ModelParameterID.I_dur, new EAParmsOfModelParm(	minGenesForIdur, minGenesForIdur,	"reset", 	COMMON_MUT_RATE, 	"0.0", 	"true" ));

	}
	private static int getNCompartments() {
		return nCompartments;
	}
	
	private static int getNCurrentGenes() {
		return nCurrentGenes;
	}
}
