//
// Driver class for the HVAC Smart Home implementation assignment
//
// R. Pettit 2016
//

import java.util.Timer;
import java.util.concurrent.*;

public class HVAC {
	
	public static void main(String[] args) {

		Timer tempUpdate = new Timer(); // For the periodically activated Temperature Sensor
		tempUpdate.schedule(new TempSensor(), 0, 1000); //1 Hz activation period
		
		CentralControlUI centralControlUI = new CentralControlUI();
		
		// Setup queue between CentralControl and TemperatureControl
		// Implemented as a BlockingQueue (specifically ArrayBlockingQueue)
		//   with a capacity of 100 elements
		BlockingQueue<Mode> tcQueue = new ArrayBlockingQueue<Mode>(100); 
		CentralControl centralCtrl = new CentralControl(tcQueue, centralControlUI);
		TemperatureCtrl tempCtrl = new TemperatureCtrl(tcQueue, centralControlUI);
		
		// Start the tasks
		centralCtrl.start();
		tempCtrl.start();
		
		centralControlUI.setCentralControl(centralCtrl);
		centralControlUI.display();
	}

}
