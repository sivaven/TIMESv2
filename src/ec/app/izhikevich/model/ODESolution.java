package ec.app.izhikevich.model;

import java.util.ArrayList;

import ec.app.izhikevich.util.GeneralUtils;

public class ODESolution {
	
		private double[] time;
		private double[] voltage;
		private double[] spikeTimes;
		
		public ODESolution(double[] time, double[] voltage, ArrayList<Double> spikeTimes) {
			this.setTime(time);
			this.setVoltage(voltage);		
			this.setSpikeTimes(GeneralUtils.listToArrayDouble(spikeTimes));
		}
		
		public ODESolution(double[] time, double[] voltage, double[] spikeTimes) {
			this.setTime(time);
			this.setVoltage(voltage);
			this.setSpikeTimes(spikeTimes);
		}
		public double[] getTime() {
			return time;
		}
		public void setTime(double[] time) {
			this.time = time;
		}
		public double[] getSpikeTimes() {
			return spikeTimes;
		}
		public void setSpikeTimes(double[] spikeTimes) {
			this.spikeTimes = spikeTimes;
		}
		public double[] getVoltage() {
			return voltage;
		}
		public void setVoltage(double[] voltage) {
			this.voltage = voltage;
		}
		public void display(){
			System.out.print("time:\t");GeneralUtils.displayArray(time);
			System.out.print("voltage:\t"); GeneralUtils.displayArray(voltage);
			System.out.print("spikeTimes:\t"); GeneralUtils.displayArray(spikeTimes);
		}
	
}
