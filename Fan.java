//
// This is the <<boundary>> class for Fan
// This class acts as the device driver for the fan unit.
// For this assignment, the on() and off() methods are just 
//   stubs.
//
// Students - Do Not Modify
//
// R. Pettit - 2016
//

public class Fan {
	
	private static boolean isOn = false;
	
	public void on(){
		
		if (isOn){
			System.out.println("Fan was already on");
		}
		else{
			isOn = true;
			System.out.println("Fan is On");
		}
	}
	
	public void off(){
		if (!isOn){
			System.out.println("Fan was already off");
		}
		else{
			isOn = false;
			System.out.println("Fan is Off");
		}
	}

}
