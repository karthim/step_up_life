package hcc.stepuplife;

import java.util.ArrayList;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.util.Log;

public class StepUpLifeService extends Service {

	public static final String PREFS_NAME = "stepuplifePrefs";
	private SharedPreferences settings;
	private static final String LOGTAG = "S/StepUpLife";

	private int exerciseCount;
	private int target = 10;

	/**
	 * @return - integer denoting percent of goal reached
	 */
	public int percentGoalReached() {
		int percentGoalReached = 100 * (exerciseCount / target);
		return percentGoalReached < 100 ? percentGoalReached : 100;
	}

	private BroadcastReceiver meetingReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			boolean stopMonitoring = true;
			if (intent.getAction().equals(
					CalendarEventManager.ALARM_INTENT_START_ACTION))
				stopMonitoring = false;
			StepUpLifeService.this.processAlarmCalendarEvent(stopMonitoring);
		}

	};

	/**
	 * @param stopMonitoring
	 *            : whether alarm event requires monitoring to be stoppped
	 *            Depending on stopMonitoring, will either stop activity
	 *            monitoring or resume monitoring.
	 */
	private void processAlarmCalendarEvent(boolean stopMonitoring) {
		Log.d(LOGTAG, "Calendar event alarm received");
		if (stopMonitoring) {
			Log.d(LOGTAG, "Stopping activity monitoring");
			stopMonitoringActivity(false);
		} else {
			Log.d(LOGTAG, "Starting activity monitoring");
			startMonitoringActivity();
		}
		CalendarEventManager.notifyEventReceived();
	}

	/**
	 * @author devj1988 encapsulates service state
	 */
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

	/**
	 * @return true if service is running, else false
	 */
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
		serviceState = ServiceState.STOPPED;
		Log.d(LOGTAG, "Service created");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d(LOGTAG, "Service onStartCommand begins");
		if (intent == null)
			Log.d(LOGTAG, "restarted service Step Up Life");
		else
			Log.d(LOGTAG, "started service Step Up Life");

		settings.edit().putBoolean("serviceRunning", true).commit();
		serviceState = ServiceState.STARTED;

		if (intent.getBooleanExtra("start_monitoring", false))
			startMonitoringActivity();

		// Initing calendar manager and registering receiver for meeting event
		// intents
		CalendarEventManager.init(this);
		IntentFilter startMeetingIntentFiler = new IntentFilter(
				CalendarEventManager.ALARM_INTENT_START_ACTION);
		registerReceiver(meetingReceiver, startMeetingIntentFiler);

		IntentFilter stopMeetingIntentFiler = new IntentFilter(
				CalendarEventManager.ALARM_INTENT_STOP_ACTION);
		registerReceiver(meetingReceiver, stopMeetingIntentFiler);

		Log.d(LOGTAG, "Service onStartCommand ends");
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(LOGTAG, "Destroying service");
		super.onDestroy();
		settings.edit().putBoolean("serviceRunning", false).commit();
		stopMonitoringActivity(true);
		doCleanUp();
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
		Log.d(LOGTAG, "Started activity monitoring");
		settings.edit().putBoolean("monitoring", true);
		serviceState = ServiceState.RUNNING_MONITORING;
		Log.d(LOGTAG, "Started activity monitoring");
	}

	public void stopMonitoringActivity(boolean endService) {
		// TODO Auto-generated method stub
		settings.edit().putBoolean("monitoring", false);
		serviceState = ServiceState.RUNNING_NOT_MONITORING;
		if (endService) {
			// save summary

		}
		Log.d(LOGTAG, "Stopped activity monitoring");
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

	public void doCleanUp() {
		Log.d(LOGTAG, "Unregistering broadcast receiver");
		unregisterReceiver(meetingReceiver);
	}

}