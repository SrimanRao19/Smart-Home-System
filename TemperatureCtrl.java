import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

//
// This is the <<control>> class for TemperatureCtrl
// This class will be the primary focus for the implementation assignment
// You will need to override the run() method and add other methods as necessary
// TemperatureCtrl should check the queue from Central Control for mode change messages
// While in any mode other than OFF, you will be monitoring current/desired temperatures
//    and controlling the fan, cooling, primary heating, and backup heating units
//    through the respective <<boundary>> classes.

public class TemperatureCtrl extends Thread {

	private enum TemperatureCtrlState { 
		Off(Mode.OFF), 
		IdleHeating(Mode.HEATING),
		BasicHeating(Mode.HEATING), 
		EmergencyHeating(Mode.HEATING),
		IdleCooling(Mode.COOLING),
		BasicCooling(Mode.COOLING);
		
		private Mode mode;
		
		private TemperatureCtrlState(Mode mode) {
			this.mode = mode;
		}

		public Mode getMode() {
			return mode;
		}
	};
	
	private BlockingQueue<Mode> queue = null; 
	private TemperatureCtrlState currentState = TemperatureCtrlState.Off;
	private CentralControlUI centralControlUI;
	
	private Fan fan = new Fan();
	private BackupHeat backupHeat = new BackupHeat();
	private PrimaryHeat primaryHeat = new PrimaryHeat();
	private Cooling cooling = new Cooling();
	
	private final ScheduledExecutorService startEmergencyHeatScheduler = Executors.newScheduledThreadPool(1);
	private final ScheduledExecutorService stopEmergencyHeatScheduler = Executors.newScheduledThreadPool(1);
	private ScheduledFuture<?> startEmergencyHeatTaskHandle = null;
	private ScheduledFuture<?> stopEmergencyHeatTaskHandle = null;
	
	// Constructor assigns queue
	public TemperatureCtrl(BlockingQueue<Mode> queue, CentralControlUI centralControlUI){
		this.queue = queue;
		this.centralControlUI = centralControlUI;
	}
	
	public void run() {
		//  Need to override this
		//  Need to keep track of your state (don't use the SystemState enum or SystemStatus class - those are for instructor use only
		//  Need to check the queue, but if no messages are waiting (you can wait up to 
		//     1 second), then continue checking the current/desired temperatures 
		//     every second.  You can Google for patterns on how to do this.
		//   
		
		// Temporary stub to show thread was started - can remove
		System.out.println("TemperatureCtrl Started");
		
		while(true) {
			try {
				executeTemperatureControl();
				Thread.sleep(250L);
			} 
			catch (InterruptedException e) {
				System.out.println("InterruptedException encountered in temperature control.");
				e.printStackTrace();
			}
		}
	}
	
	private synchronized void executeTemperatureControl() {
		//System.out.println("Checking for mode update. Current mode is: " + currentState.getMode().toString());
		Mode newSystemMode = queue.poll();
		if(newSystemMode != null && (currentState.getMode() != newSystemMode)) {
			
			System.out.println("New mode requested: " + newSystemMode.toString());
			if(TemperatureCtrlState.Off != currentState) {
				System.out.println("System is turning off.");
				transitionSystemToOffMode();
				updateCurrentState(TemperatureCtrlState.Off);
			}
			
			if(Mode.HEATING == newSystemMode) {
				updateCurrentState(TemperatureCtrlState.IdleHeating);
			}
			else if(Mode.COOLING == newSystemMode) {
				updateCurrentState(TemperatureCtrlState.IdleCooling);
			}
		}
		
		if(isStateActive(currentState)) {
			monitorTemperature();
		}
	}
	
	private void monitorTemperature() {
		int currentTemp = Temperature.getCurrent();
		int desiredTemp = Temperature.getDesired();
		
		if(TemperatureCtrlState.IdleHeating == currentState) {
			if(currentTemp < desiredTemp) {
				enterBasicHeatingState(currentState);
				updateCurrentState(TemperatureCtrlState.BasicHeating);
			}
		}
		else if(TemperatureCtrlState.BasicHeating == currentState) {
			if(currentTemp >= desiredTemp) {
				exitBasicHeatingState(TemperatureCtrlState.IdleHeating);
				updateCurrentState(TemperatureCtrlState.IdleHeating);
			}
		}
		else if(TemperatureCtrlState.EmergencyHeating == currentState) {
			if(currentTemp >= desiredTemp) {
				exitEmergencyHeatingState(TemperatureCtrlState.IdleHeating);
				updateCurrentState(TemperatureCtrlState.IdleHeating);
			}
		}
		else if(TemperatureCtrlState.IdleCooling == currentState) {
			if(currentTemp > desiredTemp) {
				enterBasicCoolingState();
				updateCurrentState(TemperatureCtrlState.BasicCooling);
			}
		}
		else if(TemperatureCtrlState.BasicCooling == currentState) {
			if(currentTemp <= desiredTemp) {
				exitBasicCoolingState(TemperatureCtrlState.IdleCooling);
				updateCurrentState(TemperatureCtrlState.IdleCooling);
			}
		}
	}
	
	private void controlPrimaryHeat(boolean newStateIsOn) {
		if(newStateIsOn) {
			primaryHeat.on();
		}
		else {
			primaryHeat.off();
		}
		
		centralControlUI.updatePrimaryHeatingUnitDisplay(newStateIsOn);
	}
	
	private void controlFan(boolean newStateIsOn) {
		if(newStateIsOn) {
			fan.on();
		}
		else {
			fan.off();
		}
		
		centralControlUI.updateFanDisplay(newStateIsOn);
	}
	
	private void controlBackupHeat(boolean newStateIsOn) {
		if(newStateIsOn) {
			backupHeat.on();
		}
		else {
			backupHeat.off();
		}
		
		centralControlUI.updateBackupHeatingUnitDisplay(newStateIsOn);
	}
	
	private void controlCooling(boolean newStateIsOn) {
		if(newStateIsOn) {
			cooling.on();
		}
		else {
			cooling.off();
		}
		
		centralControlUI.updateCoolingUnitDisplay(newStateIsOn);
	}
	
	private void enterEmergencyHeatingState() {
		controlBackupHeat(true);
		initializeStopEmergencyHeatTimer();
	}
	
	private void exitEmergencyHeatingState(TemperatureCtrlState nextState) {
		if(TemperatureCtrlState.Off == nextState || TemperatureCtrlState.IdleHeating == nextState) {
			controlBackupHeat(false);
			controlPrimaryHeat(false);
			controlFan(false);
			cancelStopEmergencyHeatTask();
		}
		else if(TemperatureCtrlState.BasicHeating == nextState) {
			controlBackupHeat(false);
			cancelStopEmergencyHeatTask();
		}
	}
	
	private void enterBasicHeatingState(TemperatureCtrlState oldState) {
		
		if(TemperatureCtrlState.EmergencyHeating != oldState) {
			controlFan(true);
			controlPrimaryHeat(true);
		}
		
		initializeStartEmergencyHeatTimer();
	}

	private synchronized void startEmergencyHeatTimerEvent() {
		System.out.println("Timer completed - entering emergency heating state.");
		if(TemperatureCtrlState.BasicHeating == currentState) {
			exitBasicHeatingState(TemperatureCtrlState.EmergencyHeating);
			enterEmergencyHeatingState();
			updateCurrentState(TemperatureCtrlState.EmergencyHeating);
		}
	}
	
	private void initializeStartEmergencyHeatTimer() {
		
		Runnable myRunnable = new Runnable() {
			@Override
			public void run() {
				startEmergencyHeatTimerEvent();
			}
		};
		
		startEmergencyHeatTaskHandle = startEmergencyHeatScheduler.schedule(myRunnable, 5L, TimeUnit.SECONDS);
		System.out.println("Scheduled emergency heat to start in 5 seconds.");
	}
	
	private synchronized void stopEmergencyHeatTimerEvent() {
		System.out.println("Timer completed - exiting emergency heating state.");
		if(TemperatureCtrlState.EmergencyHeating == currentState) {
			exitEmergencyHeatingState(TemperatureCtrlState.BasicHeating);
			enterBasicHeatingState(currentState);
			updateCurrentState(TemperatureCtrlState.BasicHeating);
		}
	}

	private void initializeStopEmergencyHeatTimer() {
		
		Runnable myRunnable = new Runnable() {
			@Override
			public void run() {
				stopEmergencyHeatTimerEvent();
			}
		};
		
		stopEmergencyHeatTaskHandle = stopEmergencyHeatScheduler.schedule(myRunnable, 3L, TimeUnit.SECONDS);
		System.out.println("Scheduled emergency heat to finish in 3 seconds.");
	}
	
	private void exitBasicHeatingState(TemperatureCtrlState nextState) {
		if(TemperatureCtrlState.Off == nextState || TemperatureCtrlState.IdleHeating == nextState) {
			controlPrimaryHeat(false);
			controlFan(false);
			cancelStartEmergencyHeatTask();
		}
		else if(TemperatureCtrlState.EmergencyHeating == nextState) {
			cancelStartEmergencyHeatTask();
		}
	}
	
	private void enterBasicCoolingState() {
		controlFan(true);
		controlCooling(true);
	}
	
	private void exitBasicCoolingState(TemperatureCtrlState nextState) {
		if(TemperatureCtrlState.Off == nextState || TemperatureCtrlState.IdleCooling == nextState) {
			controlCooling(false);
			controlFan(false);
		}
	}
	
	private void transitionSystemToOffMode() {
		if(TemperatureCtrlState.EmergencyHeating == currentState) {
			exitEmergencyHeatingState(TemperatureCtrlState.Off);
		}
		else if(TemperatureCtrlState.BasicHeating == currentState) {
			exitBasicHeatingState(TemperatureCtrlState.Off);
		}
		else if(TemperatureCtrlState.BasicCooling == currentState) {
			exitBasicCoolingState(TemperatureCtrlState.Off);
		}
	}
	
	private void cancelStartEmergencyHeatTask() {
		System.out.println("Canceling task to start emergency heat.");
		if(startEmergencyHeatTaskHandle != null && !startEmergencyHeatTaskHandle.isDone()) {
			startEmergencyHeatTaskHandle.cancel(true);
		}
	}
	
	private void cancelStopEmergencyHeatTask() {
		System.out.println("Canceling task to stop emergency heat.");
		if(stopEmergencyHeatTaskHandle != null && !stopEmergencyHeatTaskHandle.isDone()) {
			stopEmergencyHeatTaskHandle.cancel(true);
		}
	}
	
	private void updateCurrentState(TemperatureCtrlState newState) {
		System.out.println("Updating current system state. Old state was: " + currentState.toString() + ", new state is: " + newState.toString());
		centralControlUI.updateTemperatureControlStateText(newState.toString());
		currentState = newState;
	}
	
	private boolean isStateActive(TemperatureCtrlState state) {
		return (state != TemperatureCtrlState.Off);
	}
}
