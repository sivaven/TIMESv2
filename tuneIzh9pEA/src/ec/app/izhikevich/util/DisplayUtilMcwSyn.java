package ec.app.izhikevich.util;

import java.io.File;

import ec.app.izhikevich.ModelEvaluatorWrapper;
import ec.app.izhikevich.evaluator.ModelEvaluatorMC;
import ec.app.izhikevich.evaluator.MultiCompConstraintEvaluator;
import ec.app.izhikevich.evaluator.qualifier.StatAnalyzer;
import ec.app.izhikevich.inputprocess.labels.MCConstraintAttributeID;
import ec.app.izhikevich.model.Izhikevich9pModel1C;
import ec.app.izhikevich.model.Izhikevich9pModel3C;
import ec.app.izhikevich.model.Izhikevich9pModel4C;
import ec.app.izhikevich.model.Izhikevich9pModelMC;
import ec.app.izhikevich.model.neurontypes.mc.EAGenes;
import ec.app.izhikevich.model.neurontypes.mc.OneNeuronInitializer;
import ec.app.izhikevich.starter.ECJStarterV2;

public class DisplayUtilMcwSyn {		
	static boolean displayParms = true;
	static boolean displayErrors = true;
	static boolean plotVtraces = false;	
	public static final boolean displayPatternForExternalPlot = false;
	
	
	static int nComp;
	private static int[] forConnIdcs;// = new int[] {0,0};//new int[]{0,0,0};;//new int[] {0,0,0,2};//
	//public static int fitnessLine;
	
	public static int nJobs;
	public static int nGens;
	public static String opFolder;
	public static int forceTrial;
	
	static {
		StatAnalyzer.display_stats = true;
		nJobs =3;		
		String phen_category = "10_2_3";//ECJStarterV2.Phen_Category;
		String phen_num = "P4";//ECJStarterV2.Phen_Num;
		String Neur = "N2";//ECJStarterV2.Neur;//"N2";		
		String exp = "carl"; 
		
		forceTrial = -1;
		
		nComp = 2;//ECJStarterV2.N_COMP;
		forConnIdcs = new int[] {0,0};//ECJStarterV2.CONN_IDCS;
				 
	/*	if(!ECJStarterV2.MULTI_OBJ)		fitnessLine = nGens*7 + 5;
			//+1; // if single objective terminated in the middle
		else		fitnessLine = nGens*11 - 1;
	*/	
		opFolder =//"local";
				phen_category+"/"+phen_num+"/"+Neur+"/"+exp;
		
		 // "10_2/1_1/7b";
	}
	
	public static void main(String[] args) {
		
		int trial = getBestTrial(nJobs, opFolder);
	
		OneNeuronInitializer.init(nComp, forConnIdcs, ECJStarterV2.PRIMARY_INPUT);
				
		double[] nGens = new double[nJobs];
		int idx = 0;
		if(!ECJStarterV2.MULTI_OBJ)
		for(int i=0;i<nJobs;i++)
		{
			//if(i==8) continue;
			idx = i;
			if(forceTrial>-1){
				idx=forceTrial;
			}
		//	int i=0;
			System.out.println("\n******************************************************************");
			System.out.println("******************************************************************\t"+idx);
			System.out.println("******************************************************************");
			//runWithUserParmValues();
			float[] parms = readBestParms(opFolder, idx);
			nGens[i] = findNGen(opFolder, idx);
			runPrimary(parms, opFolder+"/"+idx, displayParms, displayErrors, plotVtraces);
		}
		
		double meanNGens = StatUtil.calculateMean(nGens);
		double nGensSD = StatUtil.calculateStandardDeviation(nGens, meanNGens);
		System.out.println("\nNGens.\t\tMean: "+meanNGens
				+"\tSD: "+nGensSD
				+"\tMin: "+GeneralUtils.findMin(nGens)
				+"\tMax: "+GeneralUtils.findMax(nGens));
		
		
		
		int[] js = {39,33};
		if(ECJStarterV2.MULTI_OBJ)			
		for(int i=0;i<nJobs;i++)		
		{
			//if(i<7) continue;
			//int i=js[0];
			System.out.println("\n******************************************************************");
			System.out.println("******************************************************************\t"+i);
			System.out.println("******************************************************************");	
			int nObj = ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS.length; //dummy for reading pareto file
			float[][] parms = readParmsOfParetoFront(opFolder, i, nObj);
			
			System.out.println("\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\");	
			System.out.println(parms.length+" solutions on Pareto Front");
			System.out.println("\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\");	
			for(int j=0;j<parms.length;j++)
			{
			//	int j=js[1];
				System.out.println("\t\t\t\t\\\\\\\\\\\\\\\\Soln. "+j +"////////////");
				runPrimary(parms[j], opFolder+"/"+i+"_"+j, displayParms, displayErrors, plotVtraces);
			}
			System.out.println("End of pareto Front\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\");
		}
	}

	private static void runPrimary(float[] parms, String opFolder, boolean displayParms, boolean displayErrors, boolean drawPlots){
		 Izhikevich9pModelMC model = getRightInstanceForModel();        
		 
		 
	        EAGenes genes = new EAGenes(parms);        
	        model.setK(genes.getK());
			model.setA(genes.getA());
			model.setB(genes.getB());
			model.setD(genes.getD());	
			model.setcM(genes.getCM());
			model.setvR(genes.getVR());
			model.setvT(genes.getVT());		
			model.setvMin(genes.getVMIN());	
	        model.setvPeak(genes.getVPEAK());
	        model.setG(genes.getG()); 
	        model.setP(genes.getP());   
	        float[] currents = genes.getI();
	  //      float[] newCurrents = new float[currents.length];
	   //     for(int i=0;i<newCurrents.length;i++)		
	  //      		newCurrents[i] = currents[i];
	        
	        float[] weights = genes.getW();
	      
	        ModelEvaluatorMC evaluator = new ModelEvaluatorMC(model,
	        									ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS,
	        									ModelEvaluatorWrapper.INPUT_PAT_REP_WEIGHTS,
	        									ModelEvaluatorWrapper.INPUT_MC_CONS,
	        									currents, weights);      
	        if(displayErrors){
	        	evaluator.setDisplayAll(true);	
				evaluator.getFitness();	
				System.out.println();
				//evaluator.get
				GeneralUtils.displayArray(evaluator.getModelSomaSpikePatternHolder().getSpikeTimes());
	        }	        		
		
			if(displayParms) {
				for(int idx=0;idx<model.getNCompartments();idx++){
					displayForBrian(model, idx);
				}
				for(int idx=0;idx<model.getNCompartments()-1;idx++){
					System.out.println("Gt_"+(idx+1)+"="+model.getG()[idx]+"/ms");
					System.out.println("P_"+(idx+1)+"="+model.getP()[idx]);
					System.out.println("W"+idx+"="+weights[idx]);
				}
				System.out.println();
				GeneralUtils.displayArrayUnformatWithComma(parms);
			}
			
			if(drawPlots){
				//float I = currents[0];
				float[][] Is = null;
				float[] Idurs = null;
				if(model.getNCompartments()>1) {
					Idurs = new float[ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS.length +4 ]; //exc., ir., sp., EPSP
					int i;
					for( i=0;i<ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS.length;i++){
						Idurs[i] = (float)ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS[i].getCurrentDuration();
					}					
					Idurs[i++]=(float) ModelEvaluatorWrapper.INPUT_MC_CONS[0].getAttribute(MCConstraintAttributeID.current_duration); //exc.
					Idurs[i++]=(float) ModelEvaluatorWrapper.INPUT_MC_CONS[1].getAttribute(MCConstraintAttributeID.current_duration); //ir.
					Idurs[i++]=(float) ModelEvaluatorWrapper.INPUT_MC_CONS[2].getAttribute(MCConstraintAttributeID.dend_current_duration); //sp.
					Idurs[i++]=(float) ModelEvaluatorWrapper.INPUT_MC_CONS[3].getAttribute(MCConstraintAttributeID.sim_duration); //epsp.
					
					//*** similarly additional mc currents
					float[] somaCurrents = new float[currents.length +4];
					float[] dend1Currents = new float[currents.length +4];
				    for(i=0;i<currents.length;i++){
				    	somaCurrents[i] = currents[i];//somatic scenarios
				    	dend1Currents[i]=0; //no dend current for somatic scenarios
				    }				    
				    float[] rheoComp = model.getRheoBases();
				    somaCurrents[i] = rheoComp[0]; //exc.
				    dend1Currents[i++] = rheoComp[1]; // exc. 
				    
				    somaCurrents[i] = (float) ModelEvaluatorWrapper.INPUT_MC_CONS[1].getAttribute(MCConstraintAttributeID.current);//ir
				    dend1Currents[i++] = (float) ModelEvaluatorWrapper.INPUT_MC_CONS[1].getAttribute(MCConstraintAttributeID.current);//ir
				    
				    			    
				    somaCurrents[i] = 0; // indirectly get the I required for single spike prop as below:	(have to go through unnecessary steps?!)	
				    float dend_curr_min = (float)ModelEvaluatorWrapper.INPUT_MC_CONS[2].getAttribute(MCConstraintAttributeID.dend_current_min);
					float dend_curr_max = (float)ModelEvaluatorWrapper.INPUT_MC_CONS[2].getAttribute(MCConstraintAttributeID.dend_current_max);
					float dend_current_time_min = (float)ModelEvaluatorWrapper.INPUT_MC_CONS[2].getAttribute(MCConstraintAttributeID.dend_current_time_min);
					float dend_current_duration = (float)ModelEvaluatorWrapper.INPUT_MC_CONS[2].getAttribute(MCConstraintAttributeID.dend_current_duration);
					float dend_current_step = (float)ModelEvaluatorWrapper.INPUT_MC_CONS[2].getAttribute(MCConstraintAttributeID.dend_current_step);
					float dend_target_spike_freq = (float)ModelEvaluatorWrapper.INPUT_MC_CONS[2].getAttribute(MCConstraintAttributeID.dend_target_spike_freq);
					for(int c=1;c<model.getNCompartments();c++){
						float[] spikeCounts = evaluator.getMcEvalholder().propagatedSpikeCounts(c, MultiCompConstraintEvaluator.forwardConnectionIdcs[c], 
																	dend_curr_min, 
																	dend_curr_max,
																	dend_current_time_min,
																	dend_current_duration,
																	dend_current_step,
																	dend_target_spike_freq);						
						dend1Currents[i++] = spikeCounts[2]; //MUST HAVE 2D ARRAY for dend currents for more than 2 comps!!!!
				    }
				    somaCurrents[i] = 0; //epsp; syn simulation
				    dend1Currents[i++] = 0; //epsp; syn simulation
						
				    Is = new float[model.getNCompartments()][];
				    Is[0]=somaCurrents;
				    Is[1]=dend1Currents;
				}else{
					Is = new float[1][];
					Idurs = new float[ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS.length];
					for(int i=0;i<ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS.length;i++){
						Idurs[i] = (float)ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS[i].getCurrentDuration();
					}
					Is[0]=currents;
				}
				
				BrianInvoker invoker = new BrianInvoker(opFolder, Is, Idurs);
				invoker.setDisplayErrorStream(false);
				invoker.invoke(model);
			}			
	}
	
	private static int getBestTrial(int nJobs, String nodes){
		float[] fitness = new float[nJobs];
		System.out.println("Jobs Fitness:");
		for(int job = 0; job<nJobs; job++) {
			fitness[job] = ECStatOutputReader.readBestFitness("output/"+nodes+"/job."+job+".Full");//"C:\\Users\\Siva\\TIME\\ECJ Izhikevich\\output\\I2, 3 - BEST so far\\job."+job+".Full", 5);//
			//fitness[job] = ECStatOutputReader.readBestFitness("output/"+nodes+"/$Full_0_"+job, 5);//"C:\\Users\\Siva\\TIME\\ECJ Izhikevich\\output\\I2, 3 - BEST so far\\job."+job+".Full", 5);//
			
			System.out.println(job+"\t"+fitness[job]);
		}
		//find min fitness:
		float maxFit = -Float.MAX_VALUE;
		int maxFitJob = -1;
		for(int job =0; job< fitness.length; job++) {
			if(fitness[job] > maxFit) {
				maxFit = fitness[job];
				maxFitJob = job;
			}
		}
		System.out.println("Best Job:\t"+maxFitJob);;
		System.out.println("**********************************************");
		
		return maxFitJob;
	}
	
	private static float[] readBestParms(String exp, int best_trial){
		float[] parms = ECStatOutputReader.readBestSolution("output/"+exp+"/job."+best_trial+".Full", 50);//"C:\\Users\\Siva\\TIME\\ECJ Izhikevich\\output\\I2, 3 - BEST so far\\job."+best_trial+".Full",6,11);//	
		return parms;
	}
	
	private static int findNGen(String exp, int best_trial){
		return ECStatOutputReader.findNGen("output/"+exp+"/job."+best_trial+".Stat");//"C:\\Users\\Siva\\TIME\\ECJ Izhikevich\\output\\I2, 3 - BEST so far\\job."+best_trial+".Full",6,11);//	
	}
	
	private static float[][] readParmsOfParetoFront(String exp, int trial,  int nObj){
		int nSolnsOnFront = ECStatOutputReader.readParetoFronts("output/"+exp+"/job."+trial+".pareto", nObj).length;
		return ECStatOutputReader.readParetoBestSolutions("output/"+exp+"/job."+trial+".Full", nSolnsOnFront, 50);
	}
	
	public static Izhikevich9pModelMC readModel(String exp, int trial){
		if(!(new File("output/"+exp+"/job."+trial+".Full")).exists()) 
		{ System.out.println("file not found! skipping.." +"file: "+exp+"/"+trial);return null;}
		float[] parms =  ECStatOutputReader.readBestSolution("output/"+exp+"/job."+trial+".Full",  50);
		Izhikevich9pModelMC model = getRightInstanceForModel();    			 
        EAGenes genes = new EAGenes(parms);        
        model.setK(genes.getK());
		model.setA(genes.getA());
		model.setB(genes.getB());
		model.setD(genes.getD());	
		model.setcM(genes.getCM());
		model.setvR(genes.getVR());
		model.setvT(genes.getVT());		
		model.setvMin(genes.getVMIN());	
        model.setvPeak(genes.getVPEAK());
        model.setG(genes.getG()); 
        model.setP(genes.getP());   
        float[] I = genes.getI();
       // System.out.println(I[0]);
        model.setInputParameters(new double[] {I[0]}, 0d, 0d);
        return model;
	}
	private static void displayForBrian(EAGenes genes, int idx) {
		String forBrian ="";		
		forBrian = "\n\nk"+idx+"="+genes.getK()[idx]+"/ms/mV\na"+idx+"="+genes.getA()[idx]+"\nb"+idx+"="+genes.getB()[idx]+
				"\nd"+idx+"="+genes.getD()[idx]+"\nC"+idx+"="+genes.getCM()[idx]+"\nvR"+idx+"="+genes.getVR()+"*mV\nvT"+idx+"="+genes.getVT()[idx]+
				"*mV\nvPeak"+idx+"="+genes.getVPEAK()[idx]+"*mV\nc"+idx+"="+genes.getVMIN()[idx]+"*mV";
		System.out.println(forBrian);
	}
	private static void displayForBrian(Izhikevich9pModelMC model, int idx) {
		String forBrian ="";		
		forBrian = "\n\nk"+idx+"="+model.getK()[idx]+"/ms/mV\na"+idx+"="+model.getA()[idx]+"\nb"+idx+"="+model.getB()[idx]+
				"\nd"+idx+"="+model.getD()[idx]+"\nC"+idx+"="+model.getcM()[idx]+"\nvR"+idx+"="+model.getvR()[idx]+"*mV\nvT"+idx+"="+model.getvT()[idx]+
				"*mV\nvPeak"+idx+"="+model.getvPeak()[idx]+"*mV\nc"+idx+"="+model.getvMin()[idx]+"*mV";
		System.out.println(forBrian);
	}
	private static void runWithUserParmValues(){
		Izhikevich9pModelMC model = getRightInstanceForModel();
		float k0, k1, k2;
		float a0, a1, a2;
		float b0, b1, b2;
		float d0, d1, d2;
		float C0, C1, C2;
		float vR0;
		float vT0,vT1, vT2;
		float vPeak0, vPeak1, vPeak2;
		float c0, c1, c2;
		
		float Gt_1, P_1, Gt_2, P_2;
		float[] newCurrents = new float[]{590};//, currents[1]};

		k0=3.9752178f;
		a0=0.0014259194f;
				b0=-11.811985f;
				d0=106.0f;
				C0=551.0f;
				vR0=-59.66081f;
				vT0=-53.16855f;
				vPeak0=34.926968f;
				c0=-54.317677f;
						k1=1.2633001f;
						a1=1.0446243f;
						b1=11.087972f;
						d1=62.0f;
						C1=555.0f;
						vT1=-22.832573f;
						vPeak1=27.636889f;
						c1=-29.978874f;
								k2=1.2054616f;
								a2=0.45259273f;
								b2=3.1462605f;
								d2=62.0f;
								C2=353.0f;							
								vT2=-17.870737f;
								vPeak2=26.31883f;
								c2=-34.255947f;
										Gt_1=26.0f;
										P_1=0.8004273f;
										//synW_1=12.026643f;
												Gt_2=43.0f;
												P_2=0.69688535f;
												//synW_2=7.7694597f;

						
        model.setK(getFloat1dArray(k0,k1, k2));
		model.setA(getFloat1dArray(a0,a1,a2));
		model.setB(getFloat1dArray(b0,b1,b2));
		model.setD(getFloat1dArray(d0,d1,d2));	
		model.setcM(getFloat1dArray(C0,C1,C2));
		model.setvR(vR0);
		model.setvT(getFloat1dArray(vT0,vT1,vT2));		
		model.setvMin(getFloat1dArray(c0,c1,c2));	
        model.setvPeak(getFloat1dArray(vPeak0,vPeak1,vPeak2));
        model.setG(getFloat1dArray(Gt_1,Gt_2)); 
        model.setP(getFloat1dArray(P_1,P_2));   
        
        
        float[] weights = new float[]{};
        ModelEvaluatorMC evaluator = new ModelEvaluatorMC(model,
        			ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS,
					ModelEvaluatorWrapper.INPUT_PAT_REP_WEIGHTS,
					ModelEvaluatorWrapper.INPUT_MC_CONS,
					newCurrents, weights);      
		evaluator.setDisplayAll(true);	
		evaluator.getFitness();
		System.out.println("\nSpike Times: ");
		GeneralUtils.displayArray(evaluator.getModelSomaSpikePatternHolder().getSpikeTimes());
      
	}
	private static float[] getMirroredDendComp(float[] genes){
		float[] newGenes = new float[genes.length+1];
		for(int i=0;i<genes.length;i++){
			newGenes[i]=genes[i];
		}
		newGenes[newGenes.length-1]=genes[genes.length-1];
		return newGenes;
	}
	public static Izhikevich9pModelMC getRightInstanceForModel(){
		if(nComp==1){
			return new Izhikevich9pModel1C(1);
		}
		if(nComp==2){
			return new Izhikevich9pModelMC(2);
		}
		if(nComp==3){
			return new Izhikevich9pModel3C(3);
		}
		if(nComp==4){
			return new Izhikevich9pModel4C(4);
		}
		System.out.println("rightModel needs to be instantiated!!");
		return null;	
	}
	private static float[] getFloat1dArray(float f){
		return new float[]{f};
	}
	private static float[] getFloat1dArray(float f1, float f2){
		return new float[]{f1,f2};
	}
	private static float[] getFloat1dArray(float f1, float f2, float f3){
		return new float[]{f1,f2,f3};
	}
}
