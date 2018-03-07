
public class SystemStatus {
	
	private static SystemState ss = SystemState.OFF;
	
	public static synchronized void setState(SystemState newState){
		ss = newState;
	}

	public static synchronized SystemState getState(){
		return ss;
	}
}
