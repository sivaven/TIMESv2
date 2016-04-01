package ec.app.izhikevich.exputils;



import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ec.app.izhikevich.spike.labels.SpikePatternClass;

enum ExcelLabels{
	/*
	 * label(row_idx)
	 * row_idx is 1 based idx; getRowIdx() returns 0 based idx;
	 */
	REGION(1), NEURON_TYPE(2), UNIQUE_ID(3),  // for NeuronType class
	PMID(4), I(6), I_DUR(7), PATTERN_CLASS(16), FSL(18), PSS(19), N_ISI(22), M1(46), C1(47); // for spikePatternTrace class
	
	private int rowIdx;
	ExcelLabels(int row_idx){
		rowIdx = row_idx;
	}
	public int getRowIdx(){
		return rowIdx-1;
	}
	
}
public class EphysData {		 
		private static final String file_pfx="input\\AK\\";
		private static final String file1 = "Firing pattern parameters - 5.xlsx";		
		private static final int MAX_N_COLS = 1000;
		
		public static Map<NeuronType, List<SpikePatternTrace>> readExcelData(){
			return  readExcelData(file_pfx+file1);
		}
	    public static Map<NeuronType, List<SpikePatternTrace>> readExcelData(String fileName) {
	    	Map<NeuronType, List<SpikePatternTrace>> map = new HashMap<>();
	         
	        try {
	            //Create the input stream from the xlsx/xls file	        	
	            FileInputStream fis = new FileInputStream(fileName);
	             
	            //Create Workbook instance for xlsx/xls file input stream
	            Workbook workbook = null;
	            if(fileName.toLowerCase().endsWith("xlsx")){
	                workbook = new XSSFWorkbook(fis);
	            }else if(fileName.toLowerCase().endsWith("xls")){
	                workbook = new HSSFWorkbook(fis);
	            }  
	             
	         
	            //build map   
	            boolean allRead = false;
	            ExcelLabels[] allLabels = ExcelLabels.values();	            
	            for(int col=1;col<MAX_N_COLS;col++){	            	
	            	Sheet sheet = workbook.getSheetAt(0);                 
		            Iterator<Row> rowIterator = sheet.iterator();    
		            int rowIdx = -1;
		            
		            String neuronTypeName=null;
		            String uniqueID = null;
		            String patternClass = null;
		            SpikePatternTrace trace = new SpikePatternTrace();
		            
		            for(ExcelLabels label: allLabels){	//assuming idcs are in asc. order
		            	Row row = null;
                		while(rowIterator.hasNext()){
                			rowIdx++;
    		                row = rowIterator.next();	
    		                if(label.getRowIdx()==rowIdx){
    		                	break;
    		                }
                		}
                		String item = readString(row, col);
                		
                		if(rowIdx ==0 && item ==null){
                			allRead = true;
                			break;
                		}                		
                		if(rowIdx == 1){
		                	neuronTypeName = item;
		                }		                
		                if(rowIdx == 2){
		                	uniqueID = item;
		                }		                
		                if(rowIdx>3){
		                	//populate 'a' trace		                	
		                	 if(rowIdx == ExcelLabels.PATTERN_CLASS.getRowIdx()){// needs special processing
				                	patternClass = readString(row, col);
				                	if(patternClass.equals("-")){
				                		patternClass = "X";
				                	}	
				                	SpikePatternClass _class = new SpikePatternClass(patternClass, ".");    
				                	trace.setSpikePatternClass(_class);
				             }else{
				            	 trace.addData(label, item);
				             }
		                }
	                }
		            if(allRead){
		            	break;
		            }
		            NeuronType neuronType = new NeuronType(neuronTypeName, uniqueID);  		            
                	
					if(map.containsKey(neuronType)){
                		List<SpikePatternTrace> traces = map.get(neuronType);
                		traces.add(trace);
                	}else{
                		List<SpikePatternTrace> traces = new ArrayList<>();
                		traces.add(trace);
                		map.put(neuronType, traces);
                	}
	            }  
	            //close file input stream
	            fis.close();
	             
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	         
	       return map;
	    }

	    private static String readString(Row row, int colIdx){
	    	String item = null;
	    	Cell cell = row.getCell(colIdx);		
	    	if(row.getRowNum()==0 && (cell == null || cell.getCellType()==cell.CELL_TYPE_BLANK)){
            	return item;
            }
            if(row.getRowNum()!=0 && (cell == null || cell.getCellType()==cell.CELL_TYPE_BLANK)){
            	return "EMPTY";
            }
            if(cell.getCellType() == cell.CELL_TYPE_ERROR){
            	return "ERR";
        	}            
        	if(cell.getCellType()==cell.CELL_TYPE_NUMERIC){
        		item = String.valueOf(cell.getNumericCellValue()) ;
        	}else{
        		item = cell.getStringCellValue();
        	}
            return item;
	    }
	    
	    public static Map<NeuronType, List<SpikePatternTrace>> fetchSingleBehaviorTypes(Map<NeuronType, List<SpikePatternTrace>> mp){
	    	Map<NeuronType, List<SpikePatternTrace>> singleBehaviorTypes = new HashMap<>();
	    	Iterator it = mp.entrySet().iterator();
	        while (it.hasNext()) {
	            Map.Entry pair = (Map.Entry)it.next();
	            NeuronType nt = (NeuronType) pair.getKey();	           
	            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
	            if(isSingleBehavior(traces)){
	            	singleBehaviorTypes.put(nt, traces);
	            }
	        }
	        return singleBehaviorTypes;	            
	    }
	    
	    public static Map<NeuronType, List<SpikePatternTrace>> fetchAllByRegion(Map<NeuronType, List<SpikePatternTrace>> mp, Region rg){
	    	Map<NeuronType, List<SpikePatternTrace>> byRegion = new HashMap<>();
	    	Iterator it = mp.entrySet().iterator();
	        while (it.hasNext()) {
	            Map.Entry pair = (Map.Entry)it.next();
	            NeuronType nt = (NeuronType) pair.getKey();	           
	            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
	            if(nt.getRegion().equals(rg)){
	            	byRegion.put(nt, traces);
	            }
	        }
	        return byRegion;	            
	    }
	    
	    /*
	     * search by sp type. (at least 'one' of the traces should be of type.
	     */
	    public static Map<NeuronType, List<SpikePatternTrace>> fetchAllByClass(Map<NeuronType, List<SpikePatternTrace>> mp, SpikePatternClass _class){
	    	Map<NeuronType, List<SpikePatternTrace>> byClass = new HashMap<>();
	    	Iterator it = mp.entrySet().iterator();
	        while (it.hasNext()) {
	            Map.Entry pair = (Map.Entry)it.next();
	            NeuronType nt = (NeuronType) pair.getKey();	           
	            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
	            boolean found = false;
	            for(SpikePatternTrace _trace: traces){
	            	if(_trace.getPatternClass().equals(_class)){
	            		found = true;
	            	}
	            }
	            if(found)
	            	byClass.put(nt, traces);
	        }
	        return byClass;	            
	    }
	    private static boolean isSingleBehavior(List<SpikePatternTrace> traces){
	    	SpikePatternTrace firstTrace = traces.get(0);
	    	for(SpikePatternTrace trace: traces){
	    		if(!firstTrace.getPatternClass().equals(trace.getPatternClass())) return false;
	    	}	    	
	    	return true;
	    }
		
	   
	    public static void main(String[] args) {
			//System.out.println("1-000".equals("1-000"));
			Map<NeuronType, List<SpikePatternTrace>> map = readExcelData();
			map = fetchSingleBehaviorTypes(map);
			SpikePatternClass _class = new SpikePatternClass("NASP", ".");
			map = fetchAllByClass(map, _class);
			//printMap(fetchSingleBehaviorTypes(map));
			printMap(fetchAllByRegion(map, Region.CA1));
			
			ExcelLabels[] labelsToPrint = new ExcelLabels[] {ExcelLabels.I, ExcelLabels.I_DUR, 
															ExcelLabels.FSL, ExcelLabels.PSS, 
															ExcelLabels.M1, ExcelLabels.C1, ExcelLabels.N_ISI};
			//printMap(map, labelsToPrint);
		}
	    
	    /*
	     * Only print neuron type and sp classes associated with it!
	     */
		 public static void printMap( Map<NeuronType, List<SpikePatternTrace>> mp) {
		        Iterator it = mp.entrySet().iterator();
		        while (it.hasNext()) {
		            Map.Entry pair = (Map.Entry)it.next();
		            NeuronType nt = (NeuronType) pair.getKey();
		            System.out.print(nt.getName()+"\t\t\t");
		          //  nt.display();
		            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
		            for(SpikePatternTrace _trace: traces){
		            	_trace.getPatternClass().display();
		            	System.out.print("\t");
		            }	         
		           System.out.println();
		        }
		  }
		 
		 /*
		     * print neuron types and constraints for model
		     */
			 public static void printMap( Map<NeuronType, List<SpikePatternTrace>> mp, ExcelLabels[] labelsToPrint) {
			        Iterator it = mp.entrySet().iterator();
			        while (it.hasNext()) {
			            Map.Entry pair = (Map.Entry)it.next();
			            NeuronType nt = (NeuronType) pair.getKey();
			            
			          //  nt.display();
			            List<SpikePatternTrace> traces = (List<SpikePatternTrace>)pair.getValue();
			            for(SpikePatternTrace _trace: traces){
			            	 System.out.print(nt.getUniqueID()+"\t");
			            	System.out.print(nt.getRegion()+"\t");
			            	System.out.print(nt.getName()+"\t");				           				            
			            	_trace.getPatternClass().display();  	System.out.print("\t");
			            	
			            	for(ExcelLabels label: labelsToPrint){
			            		String item = _trace.getMappedData().get(label);
			            		System.out.print(item+"\t");
			            	}
			            	 System.out.println();
			            }	         
			          
			        }
			  }

}
