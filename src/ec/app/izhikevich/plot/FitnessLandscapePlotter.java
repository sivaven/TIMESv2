package ec.app.izhikevich.plot;

import com.panayotis.gnuplot.style.Style;

import ec.app.izhikevich.ModelEvaluatorWrapper;
import ec.app.izhikevich.evaluator.ModelEvaluatorMC;
import ec.app.izhikevich.inputprocess.labels.ModelParameterID;
import ec.app.izhikevich.model.Izhikevich9pModelMC;
import ec.app.izhikevich.model.neurontypes.mc.EAGenes;
import ec.app.izhikevich.model.neurontypes.mc.OneNeuronInitializer;
import ec.app.izhikevich.starter.ECJStarterV2;
import ec.app.izhikevich.util.DisplayUtilMcwSyn;
import ec.app.izhikevich.util.ECStatOutputReader;
import ec.app.izhikevich.util.GeneralUtils;

public class FitnessLandscapePlotter {
	
	public static void main(String[] args) {
		OneNeuronInitializer.init(1, null, ECJStarterV2.PRIMARY_INPUT);		
		
		/*
		 * plot model parms in space: go through all exps results and do a single plot
		 */
		DataSetOfModels.nParmsToPlot=2;
		DataSetOfModels.x = ModelParameterID.A;
		String xRange = "[-0.05:0.2]";	
		
		DataSetOfModels.y = ModelParameterID.K;
		String yRange = "[-1:3]";
		/*
		DataSetOfModels.z= ModelParameterID.B;
		String zRange = "[0:25]";
			*/			
		String fitnessRange="[-1.5:0]";
		Izhikevich9pModelMC startingModel = DisplayUtilMcwSyn.readModel("local", 0);		
		
		DataSetOfModels dataSet = constructDataSetWithStepModelsAndFitness(startingModel , 
				DataSetOfModels.x, 0.00001f, 500);
		//,DataSetOfModels.y, 0.001f, 50);					
			
	//	GeneralUtils.displayArray(dataSet.mergeFitnessAndModelParmsFor3dPlot());
		plotFitnessLandscape(dataSet ,xRange, 
				//yRange, 
				fitnessRange);		
	}
	static DataSetOfModels constructDataSetWithStepModelsAndFitness(Izhikevich9pModelMC model, 
				ModelParameterID id, float stepSize, int nStepsOnBothDirections){
		
		Izhikevich9pModelMC[] models = new Izhikevich9pModelMC[1+2*nStepsOnBothDirections];
		float[] fitnesses = new float[1+2*nStepsOnBothDirections];		
		
		/*
		 * parm x
		 */
		models[0] = model;		
		ModelEvaluatorMC evaluator = new ModelEvaluatorMC(models[0],
				ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS,
				ModelEvaluatorWrapper.INPUT_PAT_REP_WEIGHTS,
				ModelEvaluatorWrapper.INPUT_MC_CONS,
				new float[]{(float) model.getAppCurrent()[0]}, null);  	
		float tempFitness = evaluator.getFitness();
		if(tempFitness<-1) tempFitness = -1;
		fitnesses[0]=  tempFitness;
		//System.out.println(fitnesses[0]);
		float preParmVal = model.getParm(id)[0];		
		/*
		 * nsteps higher than parm x
		 */
		int idxOffset=1;
		for(int i=0;i<nStepsOnBothDirections;i++){
			models[idxOffset+i] = models[idxOffset+i-1].cloneModelWith(id, 0, preParmVal+stepSize);
			evaluator = new ModelEvaluatorMC(models[idxOffset+i],
					ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS,
					ModelEvaluatorWrapper.INPUT_PAT_REP_WEIGHTS,
					ModelEvaluatorWrapper.INPUT_MC_CONS,
					new float[]{(float) model.getAppCurrent()[0]}, null);  
			tempFitness = evaluator.getFitness();
			if(tempFitness<-1) tempFitness = -1;
			fitnesses[idxOffset+i]= tempFitness;
			//System.out.println(fitnesses[idxOffset+i]);
			preParmVal = models[idxOffset+i].getParm(id)[0];
		}
		
		/*
		 * nsteps lower than parm x
		 */
		preParmVal = model.getParm(id)[0];
		idxOffset=1 + nStepsOnBothDirections;
		for(int i=0;i<nStepsOnBothDirections;i++){
			models[idxOffset+i] = models[idxOffset+i-1].cloneModelWith(id, 0, preParmVal-stepSize);
			evaluator = new ModelEvaluatorMC(models[idxOffset+i],
					ModelEvaluatorWrapper.INPUT_SPIKE_PATTERN_CONS,
					ModelEvaluatorWrapper.INPUT_PAT_REP_WEIGHTS,
					ModelEvaluatorWrapper.INPUT_MC_CONS,
					new float[]{(float) model.getAppCurrent()[0]}, null);  
			tempFitness = evaluator.getFitness();
			if(tempFitness<-1) tempFitness = -1;
			fitnesses[idxOffset+i]=  tempFitness;
			//System.out.println(fitnesses[idxOffset+i]);
			preParmVal = models[idxOffset+i].getParm(id)[0];
		}
		
		DataSetOfModels dataSet = new DataSetOfModels(models, fitnesses, "fitLandscape");
		return dataSet;
	}
	
	static DataSetOfModels constructDataSetWithStepModelsAndFitness(Izhikevich9pModelMC model, 
			ModelParameterID id1, float stepSize1, int nStepsOnBothDirections1,
			ModelParameterID id2, float stepSize2, int nStepsOnBothDirections2){
	
		float percentOfCompletion = 0;
		DataSetOfModels dataSet2d = new DataSetOfModels("fitLandscape");
		
	//	Izhikevich9pModelMC[][] models = new Izhikevich9pModelMC[1+2*nStepsOnBothDirections1][1+2*nStepsOnBothDirections2];
	//	float[][] fitnesses = new float[1+2*nStepsOnBothDirections1][1+2*nStepsOnBothDirections2];		
		
		/*
		 * center parm 1
		 */
		DataSetOfModels tempDataSet = constructDataSetWithStepModelsAndFitness(model, id2, stepSize2, nStepsOnBothDirections2);
		dataSet2d.add1dModels(tempDataSet.modelsOfStochasticTrials);
		dataSet2d.add1dFitnesses(tempDataSet.fitness);		
		Izhikevich9pModelMC preModel = model;
		float preParmVal = preModel.getParm(id1)[0];
		
		percentOfCompletion = (1.0f)/(nStepsOnBothDirections1*2.0f);
		/*
		 * nsteps higher than parm 1
		 * 	   - nest parm y both directions by calling the existing method
		 */		
		for(int i=0;i<nStepsOnBothDirections1;i++){
			Izhikevich9pModelMC newModel1 = preModel.cloneModelWith(id1, 0, preParmVal+stepSize1);			
			tempDataSet = constructDataSetWithStepModelsAndFitness(newModel1, id2, stepSize2, nStepsOnBothDirections2);
			dataSet2d.add1dModels(tempDataSet.modelsOfStochasticTrials);
			dataSet2d.add1dFitnesses(tempDataSet.fitness);		
			preModel = newModel1;	
			preParmVal = preModel.getParm(id1)[0];
			percentOfCompletion = ((i)*1.0f)/(nStepsOnBothDirections1*2.0f);
			//if(percentOfCompletion%10==0){
				System.out.println(percentOfCompletion+" completed");
			//}
		}
		
		/*
		 * nsteps lower than parm 1
		 *  - nest like before
		 */
		preModel = model;
		preParmVal = preModel.getParm(id1)[0];		
		for(int i=0;i<nStepsOnBothDirections1;i++){
			Izhikevich9pModelMC newModel1 = preModel.cloneModelWith(id1, 0, preParmVal-stepSize1);			
			tempDataSet = constructDataSetWithStepModelsAndFitness(newModel1, id2, stepSize2, nStepsOnBothDirections2);
			dataSet2d.add1dModels(tempDataSet.modelsOfStochasticTrials);
			dataSet2d.add1dFitnesses(tempDataSet.fitness);		
			preModel = newModel1;	
			preParmVal = preModel.getParm(id1)[0];
			percentOfCompletion = ((i+nStepsOnBothDirections1+1)*1.0f)/(nStepsOnBothDirections1*2.0f);
			//if(percentOfCompletion%10==0){
				System.out.println(percentOfCompletion+" completed");
			//}
		}
		return dataSet2d;
}
	
	public static void plotFitnessLandscape(DataSetOfModels dataSet,
								String xRange,
								String yRange,
								String zFitnessRange
								){
		String x = DataSetOfModels.x.name();
		String y = DataSetOfModels.y.name();		
		PlotGnu plotter = new PlotGnu("Fitness Landscape with "+x+" and "+y, 
									"'"+x+"'", "'"+y+"'", "'fitness'",
									xRange, yRange, zFitnessRange);		
		
		double[][] PointsOfFitnessInSpace = dataSet.mergeFitnessAndModelParmsFor3dPlot();
		plotter.addDataSet(PointsOfFitnessInSpace, "Fitness");		
		plotter.plotDataSetPoints(Style.LINES);
	}
	
	public static void plotFitnessLandscape(DataSetOfModels dataSet,
			String xRange,			
			String yFitnessRange
			){
		String x = DataSetOfModels.x.name();			
		PlotGnu plotter = new PlotGnu("Fitness Landscape with "+x, 
						"'"+x+"'","'fitness'",
						xRange, yFitnessRange);		
		
		double[][] PointsOfFitnessInSpace = dataSet.mergeFitnessAndModelParmsFor2dPlot();
		//GeneralUtils.displayArray(PointsOfFitnessInSpace);
		plotter.addDataSet(PointsOfFitnessInSpace, "Fitness");		
		plotter.plotDataSetPoints(Style.LINES);
	}
}