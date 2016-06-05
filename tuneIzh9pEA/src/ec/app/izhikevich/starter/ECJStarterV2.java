package ec.app.izhikevich.starter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import ec.EvolutionState;
import ec.Evolve;
import ec.app.izhikevich.ModelEvaluatorWrapper;
import ec.app.izhikevich.inputprocess.labels.ModelParameterID;
import ec.app.izhikevich.model.neurontypes.mc.EAGenes;
import ec.app.izhikevich.model.neurontypes.mc.OneNeuronInitializer;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

public class ECJStarterV2 {
	public static final float EXP_NORM_RATE_NS = 10;
	public static final float EXP_NORM_RATE_NB = 2;
	public static final float EXP_NORM_RATE_SFA = 40;
	
	/*
	 * input setup
	 */
	//Input path
	public static final String Phen_Category = "10_2_3";
	public static final String Phen_Num = "P4";//
	public static final String Neur = //"N1/CA2_Pyramidal_2333";
									//	 "N2/CA3 LMR-Targeting";
									//	"N3/CA1_Pyramidal_2223";
									//	"N1/CA1_Pyramidal_2223";
									//	"N2/CA1_Pyramidal_2223";
									"N2";//"N14"
	//Single or Multi objective?
	public static final boolean MULTI_OBJ = false;
	//layout info
	public static final int N_COMP = 1;
	public static final int[] CONN_IDCS =new int[] {0,0};//new int[]{0, 0, 0};// int[]{0, 0, 0, 2}; 
	/*
	 * end of input setup
	 */	
	
	public static final String PRIMARY_INPUT = "input/"+Phen_Category+"/"+Phen_Num+"/"+Neur+"/"+"/PT_0.json";
	private static final boolean timer = true;
		
	//public static final String PRIMARY_INPUT = "input/arb-TSWB_NASP.json";
//	public static final String PRIMARY_INPUT = "input/arb-D_NASP.json";
//	public static final String PRIMARY_INPUT = "input/arb-ASP_NASP.json";
//	public static final String PRIMARY_INPUT = "input/arb-NASP.json";
	
	//public static final String PRIMARY_INPUT = "input/10_2/DG_AIPRIM_2333/PT.json";
//public static final String PRIMARY_INPUT = "input/10_2/DG_MOSSY_0103/PT.json";
//	public static final String PRIMARY_INPUT = "input/older_input/TSTUT_PSTUT.json";
	//public static final String PRIMARY_INPUT = "input/10_2/CA1_Bistratified_0333/ASP_PSTUT-NASP.json";
	//public static final String PRIMARY_INPUT = "input/CA1_NGF_3000.json";
	
	//public static final String PRIMARY_INPUT = "input/NASP.json";
	//public static final String PRIMARY_INPUT = "input/D_NASP.json";
	//public static final String PRIMARY_INPUT = "input/ASP_NASP.json";
//	public static final String PRIMARY_INPUT = "input/D_ASP_NASP.json";
		
	private static String ECJ_PARMS;	
	static {
		if(MULTI_OBJ) ECJ_PARMS = "input/izhikevich_MO.params";
		else	ECJ_PARMS = "input/izhikevich_SO.params";
	}
	
	public static void main(String[] args) {		
		//1 comps	
	//	OneNeuronInitializer.init(4, new int[]{0, 0, 0, 2}, PRIMARY_INPUT);
	//	OneNeuronInitializer.init(2, new int[]{0, 0}, PRIMARY_INPUT);
		OneNeuronInitializer.init(N_COMP, CONN_IDCS, PRIMARY_INPUT);
		Map<ModelParameterID, EAParmsOfModelParm> geneParms = OneNeuronInitializer.geneParms;
		
        MCParamFile ecjParamFile;
		try {	
			/*
			 * setup Gene parms - neuron type dependent
			 */
			ecjParamFile = new MCParamFile(ECJ_PARMS, geneParms);
			ParameterDatabase parameterDB = ecjParamFile.getLoadedParameterDB();
			if(MULTI_OBJ){
				ecjParamFile.addParametertoDB("multi.fitness.num-objectives", ""+ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS.length);
			}
			
			ecjParamFile.displayParameterDB();
			int nJobs = ecjParamFile.getParameterFromDB("jobs");
			
			if(args.length==0){
				File opDir = new File("output//"+Phen_Category+"//"+Phen_Num+"//"+Neur+"//"+"local");
				if(!(new File("output//"+Phen_Category).exists())){
					new File("output//"+Phen_Category).mkdir();
				}
				if(!(new File("output//"+Phen_Category+"//"+Phen_Num).exists())){
					new File("output//"+Phen_Category+"//"+Phen_Num).mkdir();
				}
				if(!(new File("output//"+Phen_Category+"//"+Phen_Num+"//"+Neur).exists())){
					new File("output//"+Phen_Category+"//"+Phen_Num+"//"+Neur).mkdir();
				}
				if(!opDir.exists()){
					opDir.mkdir();
				}	
			}else{	
				//on ARGO
				File opDir = new File("output//"+args[0]);
				if(!opDir.exists()){
					opDir.mkdir();
				}			
			}
				
				
			for(int i=0; i<nJobs; i++)
				{
					Output output = Evolve.buildOutput();
					if(args.length==2){
						//on ARGO
						output.setFilePrefix(args[0]+"//job."+args[1]+".");
					}
					if(args.length==1){
						//on KRASNOW //not anymore needs fix on krasnow
						output.setFilePrefix(args[0]+"//job."+i+".");
					}
					if(args.length==0){
						//on KRASNOW //not anymore needs fix on krasnow
						output.setFilePrefix(Phen_Category+"//"+Phen_Num+"//"+Neur+"//local//job."+i+".");
					}
					
					long timeStart = 0;
					if(timer) timeStart = System.nanoTime();
					
					final EvolutionState state = Evolve.initialize(parameterDB, i+1, output );	
					state.run(EvolutionState.C_STARTED_FRESH);
					
					if(timer)  {
						  TimeUnit unit = TimeUnit.MILLISECONDS;
						  System.out.println("Total Time in ms: "+unit.convert(System.nanoTime()-timeStart, TimeUnit.NANOSECONDS));
					  }
				}
	    
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
}
class MCParamFile {
	private ParameterDatabase parameterDB;
	Map<ModelParameterID, EAParmsOfModelParm> geneParms;
	
	public MCParamFile(String parentfileName, Map<ModelParameterID, EAParmsOfModelParm> geneParms) throws FileNotFoundException, IOException {		
		parameterDB = new ParameterDatabase(new File(parentfileName));
		this.geneParms = geneParms;
	}
	
	public ParameterDatabase getLoadedParameterDB() {
		loadNonGeneParmsToDB();
		loadGeneParmsToDB();
		return parameterDB;
	}
	
	private void loadNonGeneParmsToDB(){
		addParametertoDB("vector.species.genome-size",""+EAGenes.geneLength);
		addParametertoDB("vector.species.min-gene","0");						// Float vector species muhst have default min
		addParametertoDB("vector.species.max-gene","1");						//max genes
		addParametertoDB("vector.species.mutation-prob","0.1");				// default - overriden at gene level
		addParametertoDB("vector.species.mutation-type","integer-reset");		//default - overriden at gene level
		//addParametertoDB("pop.subpop.0.species.mutation-type","integer-reset");
		//pop.subpop.0.species.mutation-stdev.3
	}
	private void loadGeneParmsToDB(){
		ModelParameterID[] modelParams = ModelParameterID.values();		
		for(ModelParameterID modelParam: modelParams) {
			//System.out.println(modelParam.toString());
			if(EAGenes.nComps<2){
				if(modelParam.equals(ModelParameterID.G) || modelParam.equals(ModelParameterID.P) || modelParam.equals(ModelParameterID.W))
					continue;
			}
			Integer[] modelParamIndices = EAGenes.getIndices(modelParam);			
			boolean isDendrite = false;										//first index always soma
			int baseIdx = -1;
			if(ModelParameterID.I.equals(modelParam) || ModelParameterID.I_dur.equals(modelParam) || ModelParameterID.VPEAK.equals(modelParam) || ModelParameterID.K.equals(modelParam)) {
				 baseIdx = modelParamIndices[0];
			}			
			for(int idx: modelParamIndices){				
				addGene(modelParam, idx, isDendrite, baseIdx);
				if(!ModelParameterID.I.equals(modelParam) && !ModelParameterID.I_dur.equals(modelParam)) 
					isDendrite = true;
			}
		}
	}
	
	private void addGene(ModelParameterID modelParam, int idx, boolean isDendrite, int baseIdx) {
		EAParmsOfModelParm eaParams = geneParms.get(modelParam);	
		if(!ModelParameterID.I.equals(modelParam) && !ModelParameterID.I_dur.equals(modelParam) && !ModelParameterID.VPEAK.equals(modelParam) && !ModelParameterID.K.equals(modelParam)) {
			addParametertoDB("vector.species.min-gene."+idx, eaParams.getMinGene());
			addParametertoDB("vector.species.max-gene."+idx, eaParams.getMaxGene());
		}else{
			addParametertoDB("vector.species.min-gene."+idx, eaParams.getMinGenes()[idx-baseIdx]);
			addParametertoDB("vector.species.max-gene."+idx, eaParams.getMaxGenes()[idx-baseIdx]);
		}
		addParametertoDB("vector.species.mutation-type."+idx, eaParams.getMutType());
		if(!eaParams.getMutSD().equalsIgnoreCase("0.0")){
			addParametertoDB("vector.species.mutation-stdev."+idx, eaParams.getMutSD());
		}
		
		if(eaParams.getMutType().equals("integer-random-walk")){
			addParametertoDB("vector.species.random-walk-probability."+idx, eaParams.getMutRate());
		}else{
			addParametertoDB("vector.species.mutation-prob."+idx, eaParams.getMutRate());
		}
		
		if(isDendrite && 
				!ModelParameterID.K.equals(modelParam) &&
				!ModelParameterID.CM.equals(modelParam) &&
				!ModelParameterID.VPEAK.equals(modelParam)&&
				!ModelParameterID.P.equals(modelParam)&&
				!ModelParameterID.G.equals(modelParam)&&
				!ModelParameterID.W.equals(modelParam)) { // for dendrite bounded bw (0 and 10), hence set to true
			addParametertoDB("vector.species.mutation-bounded."+idx, "false");
		}else{
			addParametertoDB("vector.species.mutation-bounded."+idx, eaParams.getMutBounded());
		}
	}
	void addParametertoDB(String name, String value) {
		parameterDB.set(new Parameter(name), value);
	}
	public int getParameterFromDB(String name) {
		return parameterDB.getInt(new Parameter(name), new Parameter("jobs"));
	}
	public void displayParameterDB(){
		Set<String> propNames = parameterDB. stringPropertyNames();
		TreeSet<String> propNamesSorted = new TreeSet<>(propNames);
		for(String key: propNamesSorted)
			System.out.println(key+" = "+parameterDB.getProperty(key));
	}
}
