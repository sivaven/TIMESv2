package ec.app.izhikevich.evaluator.qualifier;

import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.TTest;

import ec.app.izhikevich.util.GeneralFileReader;


public class StatAnalyzer {
	public static boolean display_stats = false;
	
	public static void main(String[] args) {
		String fileName = "input/other_test/sample1.txt";
		double[] sample1 = GeneralFileReader.readDoublesSepByLine(fileName);
		fileName = "input/other_test/sample2.txt";
		double[] sample2 = GeneralFileReader.readDoublesSepByLine(fileName);
		System.out.println(isSignificantImprovement(sample1, sample2));
	}
	
	public static boolean isSignificantImprovement(double[] sample1, double[] sample2, int from, int to){
		if(display_stats){
			System.out.println(from +"p - "+to+"p");
		}
		return isSignificantImprovement(sample1, sample2);
	}
	public static boolean isSignificantImprovement(double[] sample1, double[] sample2){		
		double degOfFreedom1 = sample1.length-1;
		double degOfFreedom2 = sample2.length-1;		
		double variance1 = (new DescriptiveStatistics(sample1)).getPopulationVariance();
		double variance2 = (new DescriptiveStatistics(sample2)).getPopulationVariance();
		
		double tTest_pOneTail = 0;
		
		if(isEqualVariance(variance1, variance2, degOfFreedom1, degOfFreedom2)){
			//t-Test: Paired Two Sample for Means
			tTest_pOneTail = (new TTest()).pairedTTest(sample1, sample2)/2;		
			if(display_stats){
				System.out.println("tTest_pOneTail (Equal Variance): "+tTest_pOneTail);
			}
		}else{
			//t-Test: Two-Sample Assuming Unequal Variances
			tTest_pOneTail = (new TTest()).tTest(sample1, sample2)/2;
			if(display_stats){
				System.out.println("tTest_pOneTail (Unequal Variance): "+tTest_pOneTail);
			}
		}
		if(tTest_pOneTail < 0.05){
			return true;
		}else
			return false;
	}
	

	/*
	 * perform single tail F test!
	 * The larger variance should always be placed in the numerator
	 */
	public static boolean isEqualVariance(double variance1, double variance2, 
			double degOfFreedom1, double degOfFreedom2){
		/*
		 * 
			The hypothesis that the two variances are equal is rejected if 			
			F>Fα,N1−1,N2−1      for an upper one-tailed test  
			F<F1−α,N1−1,N2−1      for a lower one-tailed test  
 
		 */
		
		double alpha = 0.05;	
		double numVar;
		double denVar; 
		double numDf;
		double denDf;
		if(variance1>variance2){
			numVar = variance1;
			numDf = degOfFreedom1;
			denVar = variance2;
			denDf = degOfFreedom2;
		}else
		{
			numVar = variance2;
			numDf = degOfFreedom2;
			denVar = variance1;
			denDf = degOfFreedom1;
		}
		
		FDistribution fd = new FDistribution(numDf, denDf);		
		double F = numVar/denVar;	
		double pOneTail = 1-fd.cumulativeProbability(F); // good
		double fCritical = fd.inverseCumulativeProbability(1-alpha);
		if(display_stats){
			System.out.println("F: "+F+"\tfCritical: "+fCritical);
		}
		if(F<fCritical){			
			/*
			 * If F<Fcrit  then automatically p>0.05. Correspondingly, if F≥Fcrit then automatically p≤0.05. 
			 */
			if(!(pOneTail>0.05)){
				System.out.println("StatAnalyzer.java -- isEqualVariance() -- something's wrong!!");
				System.exit(0);
			}
			return true;  
		}
		else {
			return false;
		}		
	}
}
