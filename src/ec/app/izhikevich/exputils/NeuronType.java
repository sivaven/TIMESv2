package ec.app.izhikevich.exputils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import ec.app.izhikevich.spike.labels.SpikePatternClass;


public class NeuronType {
	private Region region;
	private String name;
	private String uniqueID;	
	public NeuronType(String name, String uniqueID){
		StringTokenizer st = new StringTokenizer(uniqueID, "-");
		String str = st.nextToken();
		this.region = Region.getRegionLabel(Integer.valueOf(str));		
		
		this.name = name;
		this.uniqueID = uniqueID;
	}
	public String getName(){
		return name;
	}
	public String getUniqueID(){
		return uniqueID;
	}
	public Region getRegion(){
		return region;
	}
	public void display(){
		System.out.println(region+"\t"+name+"\t"+uniqueID);
	}
	public static void main(String[] args){
		NeuronType nt = new NeuronType("Granule", "2-002");
		nt.display();
	}
	@Override
	public boolean equals(Object o){
		if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;

		if(((NeuronType)o).uniqueID.equals(this.uniqueID)){
			return true;
		}else
		return false;
	}
	@Override
    public int hashCode() {
        return uniqueID.hashCode();
    }

}

class SpikePatternTrace {
	private Map<ExcelLabels, String> data;	
	private SpikePatternClass patternClass;	
	//private ArrayList<Double> ISIs;	
	
	SpikePatternTrace(){
		this.data = new HashMap<>();
	}
	public void addData(ExcelLabels label, String data){
		this.data.put(label, data);
	}
	public Map<ExcelLabels, String> getMappedData(){
		return data;
	}
	public SpikePatternClass getPatternClass(){
		return patternClass;
	}
	public void setSpikePatternClass(SpikePatternClass _class){
		this.patternClass = _class;
	}
	public void display(){
		patternClass.display();
	}
}

enum Region{
	DG, CA3, CA2, CA1, SUB, EC;
	
	public static Region getRegionLabel(int regCode){
		switch(regCode){
			case 1: return Region.DG; 
			case 2: return Region.CA3; 
			case 3: return Region.CA2; 
			case 4: return Region.CA1; 
			case 5: return Region.SUB; 
			case 6: return Region.EC; 			
		}
		System.out.println("Invalid region code in Enum Region");
		System.exit(-1);
		return null;
	}	
}
