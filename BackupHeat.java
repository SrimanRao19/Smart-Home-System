//
// This is the <<boundary>> class for BackupHeat
// This class acts as the device driver for the backup heating unit.
// For this assignment, the on() and off() methods are just 
//   stubs.
//
// Students - Do Not Modify
//
// R. Pettit - 2016
//

public class BackupHeat {
	
	private static boolean isOn = false;
	
	public void on(){
		
		if (isOn){
			System.out.println("Backup Heater was already on");
		}
		else{
			SystemStatus.setState(SystemState.BACKUP);
			isOn = true;
			System.out.println("Backup Heater is On");
			
		}
	}
	
	public void off(){
		if (!isOn){
			System.out.println("Backup Heater was already off");
		}
		else{
			SystemStatus.setState(SystemState.OFF);
			isOn = false;
			System.out.println("Backup Heater is Off");
			
		}
	}

}
