package hcc.stepuplife;

import java.util.ArrayList;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class StepUpLifeService extends Service {

	public static final String PREFS_NAME = "MyPrefsFile";
	SharedPreferences settings;

	AlarmManager alarmManager;
	PendingIntent pendingIntent;
	ArrayList<IntentTriggerEvent> events;

	class IntentTriggerEvent{
		public long timeOfTrigger; //RTC time
		public boolean isStartEvent;
	}
	
	private void populateEvents() {
		// Query Calendar Provider, and populate today's events in events list.
		events = new ArrayList<IntentTriggerEvent>();
	}

	private void setAlarmForNextEvent() {
		if (events.isEmpty()) {
			Log.d("INFO", "No more events in today's calendar");
			return;
		}
		IntentTriggerEvent event = events.remove(0);
		long timeOfTrigger = 0;
		boolean eventypeStart = true;
		Intent alarmIntent = new Intent("STEP_UP_LIFE_MEETING_ALARM");
		if(eventypeStart)
			alarmIntent.putExtra("start", true);
		else
			alarmIntent.putExtra("start", false);
		pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
		alarmManager.set(AlarmManager.RTC, timeOfTrigger, pendingIntent);
		Log.d("INFO", "Added new event to alarmManager");
	}

	private void processAlarmCalendarEvent(boolean stopMonitoring) {
		Log.d("INFO", "Calendar event alarm received");
		if(stopMonitoring){
			Log.d("INFO", "Stopping activity monitoring");
			setAlarmForNextEvent();
			stopMonitoringActivity(false);
		}
		else
		{
			Log.d("INFO", "Starting activity monitoring");
			setAlarmForNextEvent();
			startMonitoringActivity();
		}
	}

	public enum ServiceState {
		STARTED, STOPPED, RUNNING_MONITORING, RUNNING_NOT_MONITORING, SNOOZE, SUSPENDED;

		public String toString() {
			switch (this) {
			case STARTED:
				return "StepUpLifeService started";
			case STOPPED:
				return "StepUpLifeService stopped";
			case RUNNING_MONITORING:
				return "StepUpLifeService running and monitoring activity";
			case RUNNING_NOT_MONITORING:
				return "StepUpLifeService running and not monitoring activity";
			case SNOOZE:
				return "StepUpLifeService running and snooze";
			case SUSPENDED:
				return "Suspended monitoring due to meeting/location";
			default:
				return "undefined";
			}
		}
	}

	private ServiceState serviceState;

	public boolean isRunning() {
		switch (this.serviceState) {
		case STARTED:
		case RUNNING_MONITORING:
		case RUNNING_NOT_MONITORING:
		case SUSPENDED:
			return true;
		case SNOOZE:
		case STOPPED:
			return false;
		default:
			return false;
		}
	}

	public StepUpLifeService() {
		serviceState = ServiceState.STOPPED;
	}

	private final IBinder stepUpLifeServiceBinder = new StepUpLifeServiceBinder();

	public class StepUpLifeServiceBinder extends Binder {
		StepUpLifeService getService() {
			return StepUpLifeService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		return stepUpLifeServiceBinder;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		settings = getSharedPreferences(PREFS_NAME, 0);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		serviceState = ServiceState.STOPPED;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (intent == null)
			Log.d("INFO", "restarted service Step Up Life");
		else
			Log.d("INFO", "started service Step Up Life");
		settings.edit().putBoolean("serviceRunning", true);
		serviceState = ServiceState.STARTED;
		if (intent.getBooleanExtra("start_monitoring", false))
			startMonitoringActivity();
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		settings.edit().putBoolean("serviceRunning", false);
		stopMonitoringActivity(true);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}

	@Override
	public void onRebind(Intent intent) {
		// TODO Auto-generated method stub
		super.onRebind(intent);
	}

	public void startMonitoringActivity() {
		// TODO Auto-generated method stub
		settings.edit().putBoolean("monitoring", true);
		serviceState = ServiceState.RUNNING_MONITORING;
		setAlarmForNextEvent();
	}

	public void stopMonitoringActivity(boolean endService) {
		// TODO Auto-generated method stub
		settings.edit().putBoolean("monitoring", false);
		serviceState = ServiceState.RUNNING_NOT_MONITORING;
		if (endService) {
			// save summary
		}
	}

	public void snoozeActivity() {
		// TODO Auto-generated method stub
		serviceState = ServiceState.SNOOZE;
	}

	public void getExerciseRecco() {
		// TODO Auto-generated method stub
	}

	public void startExercise() {
		// TODO Auto-generated method stub
	}

	private void updateCancelCounter() {
		// TODO Auto-generated method stub
	}

	public void cancelRecommendedExercise() {
		// TODO Auto-generated method stub
		updateCancelCounter();
		startMonitoringActivity();
	}
}
