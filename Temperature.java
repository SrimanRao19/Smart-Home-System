//
// Temperature <<entity>> class
// Stores the current and desired temperatures for the HVAC subsystem
// Shared data, so methods must be synchronized
//
// R. Pettit - 2016
//

public class Temperature {
	
	private static int current = 70;
	private static int desired = 72;
	
	// Get Current Temperature
	public synchronized static int getCurrent(){
		return current;
	}
	
	//Set Current Temperature
	public synchronized static void setCurrent(int newCurrTemp){
		current = newCurrTemp;
	}
	
	//Get Desired Temperature
	public synchronized static int getDesired(){
		return desired;
	}
	
	//Set Desired Temperature
	public synchronized static void setDesired(int newDesiredTemp){
		desired = newDesiredTemp;
	}

}
