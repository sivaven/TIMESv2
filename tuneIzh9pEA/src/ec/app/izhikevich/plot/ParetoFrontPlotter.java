package ec.app.izhikevich.plot;

import ec.app.izhikevich.inputprocess.labels.ModelParameterID;
import ec.app.izhikevich.model.Izhikevich9pModelMC;
import ec.app.izhikevich.model.neurontypes.mc.EAGenes;
import ec.app.izhikevich.model.neurontypes.mc.OneNeuronInitializer;
import ec.app.izhikevich.starter.ECJStarterV2;
import ec.app.izhikevich.util.DisplayUtilMcwSyn;
import ec.app.izhikevich.util.ECStatOutputReader;

public class ParetoFrontPlotter {
		
	public static void main(String[] args) {
		OneNeuronInitializer.init(1, null, ECJStarterV2.PRIMARY_INPUT);
		
		int[] nJobsAcrossExps = new int[] { 25, 25, 25
											,25, 25, 25};
		int[] nGensAcrossExps = new int[]{1000,1000,1000//};
				,DisplayUtilMcwSyn.nGens, 5000, 5000};
		int[] fitnessLinesAcrossExps = new int[]{nGensAcrossExps[0]*10-1, 
				nGensAcrossExps[1]*10-1, nGensAcrossExps[2]*10-1		
				,nGensAcrossExps[3]*11-1, nGensAcrossExps[4]*11-1, nGensAcrossExps[5]*11-1};
		
		//fitnessLine = nGens*7 + 5;   //for single objective ECJ setting
		
		
		String[][] inFixForExps = new String[][] {{"10.2/5/SO1", "TSTUT.PSTUT only", "",""}, 
					{"10.2/5/SO2", "ASP.NASP only","",""},
					{"10.2/5/SO3", "D.NASP only","",""},	
					{DisplayUtilMcwSyn.opFolder, "ASP.NASP + NASP", "ASP.NASP", "NASP"}, 
					{"10.2/5/MO12", "TSTUT.PSTUT + ASP.NASP","TSTUT.PSTUT", "ASP.NASP"},
					{"10.2/5/MO23", "ASP.NASP + D.NASP", "ASP.NASP", "D.NASP"}
				};
		
		
		
		/*
		 * plot pareto front: as of now, no need to do a single plot, plot individually for exps
		 */
		//for(int nExps=3;nExps<nJobsAcrossExps.length;nExps++)
		
		int nExps = 3;
		{	for(int j=0;j<DisplayUtilMcwSyn.nJobs;j++)
			{
			//int j=12;	
			double[][] fronts = ECStatOutputReader.readParetoFronts("output/"+inFixForExps[nExps][0]+"/job."+j+".Pareto");
				//GeneralUtils.displayArray(fronts);
				//System.out.println();
				plotParetoFront(fronts, 
						inFixForExps[nExps][2], inFixForExps[nExps][3], 
						"[-0.1:0]", "[-0.1:0]", 
						"output/"+inFixForExps[nExps][0]+"/"+j+"-Pareto.png",
						false);
			}
		}
	}
		
	public static void plotParetoFront(double[][] fronts,
			String xName,
			String yName,
			String xRange,
			String yRange,
			String fileName,
			boolean switchXY
			){
			
		if(!switchXY){
			PlotGnu plotter = new PlotGnu("Pareto front", "'"+xName+"'", "'"+yName+"'", xRange, yRange);		
			plotter.addDataSet(fronts,"");		
			//plotter.plotDataSetPoints();
			plotter.savePlot(fileName);
		}else{
			double[][] switchedPoints = new double[fronts.length][2];
			for(int i=0;i<fronts.length;i++){
				switchedPoints[i][0] = fronts[i][1];
				switchedPoints[i][1] = fronts[i][0];
			}
			PlotGnu plotter = new PlotGnu("Pareto front", "'"+yName+"'", "'"+xName+"'", yRange, xRange);		
			plotter.addDataSet(switchedPoints,"");		
		//	plotter.plotDataSetPoints();
			plotter.savePlot(fileName);
		}		
	}	
}