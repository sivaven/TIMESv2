package ec.app.izhikevich.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ec.app.izhikevich.model.Izhikevich9pModelMC;

public class BrianInvoker {
	//private static final String BRIAN_PYPLOT_WRITE_PATH = "C:\\Users\\Siva\\TIME\\ECJ Izhikevich\\output";
	private static final String BRIAN_MOD_PATH = "C:\\Anaconda\\Lib\\site-packages\\brian\\aSamples";
	private static final String C2_MODULE = "_2cFromJava.py";
	private static final String C3_MODULE = "_3cFromJava.py";
	private static final String C1_MODULE = "_1cFromJava.py";
	private static final String C4_MODULE = "_4cFromJava.py";
	
	String opFolder;
	float[] durations;
	
	boolean displayErrStream;
	
	float[][] inputCurrents;
	// single compartment
	/*public BrianInvoker(String outputFolder, float[] inputCurrents, float[] durations) {
		this.opFolder= outputFolder;
		this.durations = durations;
		this.inputCurrents=new float[1][];
		this.inputCurrents[0] = inputCurrents;		
		this.displayErrStream = false;
	}*/
	//multi compartments
	public BrianInvoker(String outputFolder, float[][] inputCurrents, float[] durations) {
		this.opFolder= outputFolder;
		this.durations = durations;
		this.inputCurrents = inputCurrents;		
		this.displayErrStream = false;
	}
	public void invoke(Izhikevich9pModelMC model){
		List<String> command = new ArrayList<String>();
		command.add("python");
		if(model.getNCompartments()==1){
			command.add(BRIAN_MOD_PATH+"/"+C1_MODULE);	
		}
		if(model.getNCompartments()==2){
			command.add(BRIAN_MOD_PATH+"/"+C2_MODULE);	
		}
		if(model.getNCompartments()==3){
			command.add(BRIAN_MOD_PATH+"/"+C3_MODULE);
		}
		if(model.getNCompartments()==4){
			command.add(BRIAN_MOD_PATH+"/"+C4_MODULE);
		}
		
		loadCommands(model, command);
		String brianOutputAsString = "";
		if(displayErrStream)
			System.out.println(command);
			
		ProcessBuilder pb = new ProcessBuilder(command);
		try {
			Process p = pb.start();		
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			int idx = 0;
			while(true){
				String retVal = in.readLine();				
				if(retVal!=null) {
					brianOutputAsString+=retVal;	
				}else{
					break;
				}	
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}	
		if(displayErrStream)
			System.out.println(brianOutputAsString);
	}
	public void setDisplayErrorStream(boolean displayErrStream){
		this.displayErrStream = displayErrStream;
	}
	private void loadCommands(Izhikevich9pModelMC model, List<String> command){
		int nComps = model.getNCompartments();		
		command.add(String.valueOf(inputCurrents[0].length));
		
		for(int i=0;i<nComps;i++){
			command.add(String.valueOf(model.getK()[i]));
			command.add(String.valueOf(model.getA()[i]));
			command.add(String.valueOf(model.getB()[i]));
			command.add(String.valueOf(model.getD()[i]));
			command.add(String.valueOf(model.getcM()[i]));
			command.add(String.valueOf(model.getvR()[i]));
			command.add(String.valueOf(model.getvT()[i]));
			command.add(String.valueOf(model.getvPeak()[i]));
			command.add(String.valueOf(model.getvMin()[i]));
		}
		for(int i=0;i<nComps-1;i++){
			command.add(String.valueOf(model.getG()[i]));
			command.add(String.valueOf(model.getP()[i]));
//dummy syn weight
			command.add(String.valueOf(-1));
		}
		for(int i=0;i<inputCurrents.length;i++){
			for(int j=0;j<inputCurrents[i].length;j++)
				command.add(String.valueOf(inputCurrents[i][j]));
		}		
		for(int i=0;i<durations.length;i++){
			command.add(String.valueOf(durations[i]));
		}		
		command.add(opFolder);
	}
	
	public static void main(String[] args) {
		//BrianInvoker invoker = new BrianInvoker("10.1.5.1/3c/7", 255, 1000);
	//	invoker.invoke();
	}

}
