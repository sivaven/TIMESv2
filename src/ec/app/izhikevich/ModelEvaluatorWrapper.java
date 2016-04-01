/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.izhikevich;
import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.app.izhikevich.evaluator.ModelEvaluatorMC;
import ec.app.izhikevich.evaluator.qualifier.SpikePatternClassifier;
import ec.app.izhikevich.inputprocess.InputMCConstraint;
import ec.app.izhikevich.inputprocess.InputModelParameterRanges;
import ec.app.izhikevich.inputprocess.InputSpikePatternConstraint;
import ec.app.izhikevich.model.Izhikevich9pModel1C;
import ec.app.izhikevich.model.Izhikevich9pModel3C;
import ec.app.izhikevich.model.Izhikevich9pModel4C;
import ec.app.izhikevich.model.Izhikevich9pModelMC;
import ec.app.izhikevich.model.neurontypes.mc.EAGenes;
import ec.app.izhikevich.spike.labels.SpikePatternClass;
import ec.app.izhikevich.starter.ECJStarterV2;
import ec.simple.SimpleFitness;
import ec.simple.SimpleProblemForm;
import ec.vector.FloatVectorIndividual;

public class ModelEvaluatorWrapper extends Problem implements SimpleProblemForm
    {
	public static InputModelParameterRanges INPUT_MODEL_PARAMETER_RANGES = null;
	public static InputSpikePatternConstraint[] INPUT_SPIKE_PATTERN_CONS = null;
	public static InputMCConstraint[] INPUT_MC_CONS = null;
	public static double[] INPUT_PAT_REP_WEIGHTS = null;
	
	public void evaluate(final EvolutionState state,
        final Individual ind,
        final int subpopulation,
        final int threadnum)
        {
        if (ind.evaluated) return;
       
        if (!(ind instanceof FloatVectorIndividual))
            state.output.fatal("Whoa!  It's not a FloatVectorIndividual!!!",null);
        
        FloatVectorIndividual ind2 = (FloatVectorIndividual)ind;     
        EAGenes genes = new EAGenes(ind2.genome);
        
        if(!ECJStarterV2.MULTI_OBJ)	{
        	//SpikePatternClassifier mq = ;
        	float fitness=-Float.MAX_VALUE;
        	try{
        		fitness = evaluateInd(genes);   
        	}
        	catch(Exception e){
        		e.printStackTrace();
        	}
        	boolean shouldStop = false;//mq.doesQualify();
        	/*if(shouldStop){
        		fitness = SpikePatternClassifier.SHADOW_FITNESS;
        	}*/
            if (!(ind2.fitness instanceof SimpleFitness))
                state.output.fatal("Whoa!  It's not a SimpleFitness!!!",null);
            ((SimpleFitness)ind2.fitness).setFitness(state,
                /// ...the fitness...
                fitness,
                ///... is the individual ideal?  Indicate here...
                shouldStop);
            ind2.evaluated = true;
        	}else{
        		float[] fitnesses_float = evaluateIndForMultiObj(genes);   
        		double[] fitnesses = new double[fitnesses_float.length];
        		for(int i=0;i<fitnesses.length;i++){
        			fitnesses[i]=fitnesses_float[i];
        		}
        		//System.out.println(fitnesses.length);
        		
        		if(fitnesses.length==3)
	                {
	        		if (!(ind2.fitness instanceof IzhikevichMultiObjectiveFitness))
	                    state.output.fatal("Whoa!  It's not a IzhikevichMultiObjectiveFitness!!!",null);
	                ((IzhikevichMultiObjectiveFitness)ind2.fitness).setObjectives(state, fitnesses);	               
	                ((IzhikevichMultiObjectiveFitness)ind2.fitness).obj1 =  fitnesses[0];
	                ((IzhikevichMultiObjectiveFitness)ind2.fitness).obj2 =  fitnesses[1];
	                ((IzhikevichMultiObjectiveFitness)ind2.fitness).obj3 =  fitnesses[2];
	         //       ((IzhikevichMultiObjectiveFitness)ind2.fitness).fitness = fitnesses[0]+ fitnesses[1] + fitnesses[2];
	                ind2.evaluated = true;
	                }
                if(fitnesses.length==2)
	                {
	        		if (!(ind2.fitness instanceof IzhikevichMultiObjectiveFitness))
	                    state.output.fatal("Whoa!  It's not a IzhikevichMultiObjectiveFitness!!!",null);
	                ((IzhikevichMultiObjectiveFitness)ind2.fitness).setObjectives(state, fitnesses);
	                ((IzhikevichMultiObjectiveFitness)ind2.fitness).obj1 =  fitnesses[0];
	                ((IzhikevichMultiObjectiveFitness)ind2.fitness).obj2 =  fitnesses[1];
	      //          ((IzhikevichMultiObjectiveFitness)ind2.fitness).fitness = fitnesses[0]+ fitnesses[1];
	                ind2.evaluated = true;
	                }
                if(fitnesses.length==1) {
	        		if (!(ind2.fitness instanceof IzhikevichMultiObjectiveFitness))
	                    state.output.fatal("Whoa!  It's not a IzhikevichMultiObjectiveFitness!!!",null);
	                ((IzhikevichMultiObjectiveFitness)ind2.fitness).setObjectives(state, fitnesses);
	                ((IzhikevichMultiObjectiveFitness)ind2.fitness).obj1 =  fitnesses[0];	               
	                ((IzhikevichMultiObjectiveFitness)ind2.fitness).fitness = fitnesses[0];
	                ind2.evaluated = true;
	                }
               }
        
        }
	
	private float evaluateInd(EAGenes genes){
		Izhikevich9pModelMC model = getRightInstanceForModel(); 
        model.setK(genes.getK());
		model.setA(genes.getA());
		model.setB(genes.getB());
		model.setD(genes.getD());	
		model.setcM(genes.getCM());
		model.setvR(genes.getVR());
		model.setvT(genes.getVT());		
		model.setvMin(genes.getVMIN());//		
        model.setvPeak(genes.getVPEAK());

        model.setG(genes.getG()); 
        model.setP(genes.getP());
        
        float[] currents = genes.getI();
        float[] weight = genes.getW();
      /*  
        System.out.println(ECJStarterSlave.count++);
        
        try {
            Thread.sleep(2000);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        */
        ModelEvaluatorMC evaluator = new ModelEvaluatorMC(model,
        								INPUT_SPIKE_PATTERN_CONS,
        								INPUT_PAT_REP_WEIGHTS,
        								INPUT_MC_CONS,
        								currents,
        								weight);
   //   evaluator.setDisplayStatus(true);
       return evaluator.getFitness();
        
	}
	
	private float[] evaluateIndForMultiObj(EAGenes genes){
		Izhikevich9pModelMC model = getRightInstanceForModel(); 
        model.setK(genes.getK());
		model.setA(genes.getA());
		model.setB(genes.getB());
		model.setD(genes.getD());	
		model.setcM(genes.getCM());
		model.setvR(genes.getVR());
		model.setvT(genes.getVT());		
		model.setvMin(genes.getVMIN());//		
        model.setvPeak(genes.getVPEAK());

        model.setG(genes.getG()); 
        model.setP(genes.getP());
        
        float[] currents = genes.getI();
        float[] weight = genes.getW();
      /*  
        System.out.println(ECJStarterSlave.count++);
        
        try {
            Thread.sleep(2000);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        */
        ModelEvaluatorMC evaluator = new ModelEvaluatorMC(model,
        								INPUT_SPIKE_PATTERN_CONS,
        								INPUT_PAT_REP_WEIGHTS,
        								INPUT_MC_CONS,
        								currents,
        								weight);
        
         //evaluator.setDisplayStatus(true);
        return evaluator.getMultiObjFitnesses(); 
	}
	
	private Izhikevich9pModelMC getRightInstanceForModel(){
		if(EAGenes.nComps==1){
			return new Izhikevich9pModel1C(1);
		}
		if(EAGenes.nComps==2){
			return new Izhikevich9pModelMC(2);
		}
		if(EAGenes.nComps==3){
			return new Izhikevich9pModel3C(3);
		}
		if(EAGenes.nComps==4){
			return new Izhikevich9pModel4C(4);
		}
		System.out.println("rightModel needs to be instantiated!!--ModelEvaluatorWrapper.java");
		return null;	
	}
	
    }
