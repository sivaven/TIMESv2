package ec.app.izhikevich.plot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.FillStyle;
import com.panayotis.gnuplot.style.FillStyle.Fill;
import com.panayotis.gnuplot.style.PlotColor;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.RgbPlotColor;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.terminal.ImageTerminal;

public class PlotGnu {
	private static final float[][] dataSetColorSchemes = new float[][] {{0f,0f,0f},
																		{1f,0f,0f},
																		{0f,0f,1f},
																		{0f,1f,0f},
																		{1f,1f,0f},
																		{0f,1f,1f}};
	
	JavaPlot plot;
	ArrayList<DataSetPlot> dataSet;
	
	public PlotGnu(String plotTitle, String xLabel, String yLabel){
		this(plotTitle, xLabel, yLabel, null,null);
	}
	
	public PlotGnu(String plotTitle, 
			String xLabel, String yLabel, 
			String xRange, String yRange){
		plot = new JavaPlot(false);
		 plot.setTitle(plotTitle);
		 plot.set("xlabel", xLabel);
		 plot.set("ylabel", yLabel);
		 if(xRange!=null)
			 plot.set("xrange", xRange);
		 if(yRange!=null)
			 plot.set("yrange", yRange);		
		dataSet = new ArrayList<>();
	}
	
	public PlotGnu(String plotTitle, 
			String xLabel, String yLabel, String zLabel, 
			String xRange, String yRange, String zRange){
		plot = new JavaPlot(true);		
		 plot.setTitle(plotTitle);
		 plot.set("xlabel", xLabel);
		 plot.set("ylabel", yLabel);
		 plot.set("zlabel", zLabel);		 
		 if(xRange!=null)
			 plot.set("xrange", xRange);
		 if(yRange!=null)
			 plot.set("yrange", yRange);
		 if(zRange!=null)
			 plot.set("zrange", zRange);
		
		dataSet = new ArrayList<>();
	}
	
	public void addDataSet(double[][] datasetPoints, String datasetTitle){
		DataSetPlot dataSet = new DataSetPlot(datasetPoints );		
		dataSet.setTitle(datasetTitle);
		this.dataSet.add(dataSet);
		this.plot.addPlot(dataSet);
	}
	
	public void plotDataSetPoints(){
		setPlotStyles(Style.POINTS);
		 //System.out.println("adf");
		this.plot.plot();	
	}
	
	public void plotDataSetPoints(Style style){
		setPlotStyles(style);
		 //System.out.println("adf");
		//this.plot.set(key, value);
		this.plot.plot();	
	}
	
	public void savePlot(String fileName){
		setPlotStyles(Style.POINTS);
		 
		ImageTerminal png = new ImageTerminal();
		
		File file = new File(fileName);
	    try {
	        file.createNewFile();
	        png.processOutput(new FileInputStream(file));
	    } catch (FileNotFoundException ex) {
	        System.err.print(ex);
	    } catch (IOException ex) {
	        System.err.print(ex);
	    }
	    
	    this.plot.setTerminal(png );
	    this.plot.plot();
		try {
	        ImageIO.write(png.getImage(), "png", file);
	    } catch (IOException ex) {
	        System.err.print(ex);
	    }
	}
	
	private void setPlotStyles(Style style){
		
		for(int i=0; i<this.dataSet.size();i++){
			PlotStyle _style = new PlotStyle(style);
			_style.setFill(new FillStyle(Fill.SOLID));
			
			_style.setPointType(5);	
			_style.setPointSize(1);
			if(i>=4)
				_style.setPointType(2);	
			
			PlotColor color = new RgbPlotColor(dataSetColorSchemes[i][0], dataSetColorSchemes[i][1], dataSetColorSchemes[i][2]);
			_style.setLineType(color);			
			this.dataSet.get(i).setPlotStyle(_style);
		}
		
	}
	public static void main(String[] args) {
		 
		 double[][] dataset = new double[][]{{1,1},
			 			{2.7,2},{3,3},{4,4}};
		 double[][] dataset2 = new double[][]{{10,10},
		 			{27,20},{33,31},{41,41}}; 
		
		 double[][] dataset3 = new double[][]{{4,10},
		 			{27,40},{33,21},{4,41}}; 
		
		
		 double[][] dataset3d_1 = new double[][]{{1,1,1},
		 			{2.7,2, 4},{3,3,6},{4,4, 8}};
		 double[][] dataset3d_2 = new double[][]{{1,1,10},
		 			{2.7,20, 4},{3,30,6},{4,40, 8}};
		 double[][] dataset3d_3 = new double[][]{{1,1,17},
		 			{27,20, 4},{3,31,6},{4,41, 8}};
		 double[][] dataset3d_4 = new double[][]{{1,1,19},
		 			{2.7,2, 44},{38,3,6},{4,46, 8}};
		 
	    PlotGnu plotter = new PlotGnu("Test", "'test x'", "'test y'", "[-50:50]", "[-50:50]");
	    plotter.addDataSet(dataset, "ds-one");
	    plotter.addDataSet(dataset2, "ds-two");
	    plotter.addDataSet(dataset3, "ds-3");
	  
	    PlotGnu plotter3d = new PlotGnu("Test", 
	    		"'test x'", "'test y'", "'test z'", 
	    		"[-50:50]", "[-50:50]", "[-50:50]");
		  
	    plotter3d.addDataSet(dataset3d_1, "1");
	    plotter3d.addDataSet(dataset3d_2, "2");
	    plotter3d.addDataSet(dataset3d_3, "3");
	    plotter3d.addDataSet(dataset3d_4, "4");
	    
	    
	    plotter3d.plotDataSetPoints();
	        
	}

}
