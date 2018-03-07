//
// Temperature Sensor <<boundary>> class
// For HVAC implementation assignment framework
// Periodically activated by driver
//
// R. Pettit - 2016
//


import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class TempSensor extends TimerTask {
	
	private SystemState hvacMode = SystemState.OFF;
	
	// Method to simulate monitoring temperature sensor
	private void monitor(){
	    
		// Start with the current temperature
		int curr; 
		curr = Temperature.getCurrent();

		
		// Do some simple simulation based on system state
		hvacMode = SystemStatus.getState();
		//System.out.println("Updating temperature. Current state is: " + hvacMode.toString());

		switch(hvacMode){
			case OFF: Temperature.setCurrent(curr+ThreadLocalRandom.current().nextInt(3) - 1);
					  break;
			case COOLING: Temperature.setCurrent(curr+ThreadLocalRandom.current().nextInt(2) - 1);
						  break;
			case HEATING: Temperature.setCurrent(curr+ThreadLocalRandom.current().nextInt(2));
						  break;
			case BACKUP:  Temperature.setCurrent(curr+ThreadLocalRandom.current().nextInt(3));
						  break;
		}
	
		  
    }
	public void run() {
		monitor();
	}

}
