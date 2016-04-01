package ec.app.izhikevich.spike.labels;

import java.util.StringTokenizer;

public class SpikePatternClass {

	private SpikePatternComponent[] components;	
	private static final int MAX_COMP = 4; //3 is the current max experimental length
	private int length;
	
	public SpikePatternClass(){
		components = new SpikePatternComponent[MAX_COMP];
		length = 0;
	}	
	public SpikePatternClass(String patternClass, String compDelimitor){
		this();
		StringTokenizer st = new StringTokenizer(patternClass, compDelimitor);
		while(st.hasMoreTokens()){
			components[length++] = SpikePatternComponent.valueOf(st.nextToken());
		}
	}
	public void addComponent(SpikePatternComponent component){
		components[length++]=component;
	}
	public SpikePatternComponent[] getSpikePatternLabel(){
		return this.components;
	}
	public int getLength() {
		return length;
	}
	public boolean contains(SpikePatternComponent component){
		for(int i=0;i<length;i++){
			if(components[i].equals(component))
				return true;
		}
		return false;
	}
	
	public int getnPieceWiseParms() {
		int nPieceWiseParms=0;
		for(int i=0;i<length;i++){
			if(components[i].equals(SpikePatternComponent.ASP)){
				nPieceWiseParms += 2;
			}
			if(components[i].equals(SpikePatternComponent.NASP)){
				nPieceWiseParms += 1;
			}
			if(components[i].equals(SpikePatternComponent.X)){
				nPieceWiseParms += 1;
			}
		}
	return nPieceWiseParms;
	}
	public void display(){
		for(int i=0;i<length;i++){
			System.out.print(components[i]+".");
		}			
	}
	public boolean equals(SpikePatternClass targetClass){
		if(targetClass.length != this.length)
			return false;
		
		for(int i=0;i<length;i++){
			if(!components[i].equals(targetClass.components[i]))
				return false;
		}
		return true;		
	}
}
