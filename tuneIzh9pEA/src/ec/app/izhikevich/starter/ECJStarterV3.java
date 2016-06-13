package ec.app.izhikevich.starter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ec.EvolutionState;
import ec.Evolve;
import ec.app.izhikevich.ModelEvaluatorWrapper;
import ec.app.izhikevich.inputprocess.labels.ModelParameterID;
import ec.app.izhikevich.model.neurontypes.mc.OneNeuronInitializer;
import ec.util.Output;
import ec.util.ParameterDatabase;

/*
 * v3 starter - with CARLsim
 */
public class ECJStarterV3 {
	public static final float EXP_NORM_RATE_NS = 10;
	public static final float EXP_NORM_RATE_SFA = 40;
	
	//public static final String PRIMARY_INPUT = "input/arb-TSWB_NASP.json";
//	public static final String PRIMARY_INPUT = "input/arb-D_NASP.json";
//	public static final String PRIMARY_INPUT = "input/arb-ASP_NASP.json";
//	public static final String PRIMARY_INPUT = "input/arb-NASP.json";
	
	public static final String PRIMARY_INPUT = "input/10_2_3/P4/N2/PT_0.json";
	private static final String ECJ_PARMS = "input/izhikevich_SO_carl.params";
	private static final boolean timer = true;
	
	public static final boolean MULTI_OBJ = false;
	public static void main2(String[] args) {
		assert (args != null);         
        Evolve.main(args);
	}
	public static void main(String[] args) {		
		//1 comps	
		//OneNeuronInitializer.init(1, null, PRIMARY_INPUT);
		OneNeuronInitializer.init(4, new int[] {0,0,0,2}, PRIMARY_INPUT);
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
			
			//ecjParamFile.displayParameterDB();
			int nJobs = ecjParamFile.getParameterFromDB("jobs");
			
			if(args.length==0){
				File opDir = new File("output//local");
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
						output.setFilePrefix("local//job."+i+".");
					}
					
					long timeStart = 0;
					if(timer) timeStart = System.nanoTime();
					
					final EvolutionState state = Evolve.initialize(parameterDB, i+1, output );						
					state.job = new Object[1];
					state.job[0] = Integer.valueOf(i);
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
