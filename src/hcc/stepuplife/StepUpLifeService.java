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
	private AlarmManager alarmManager;
	private PendingIntent pendingIntent;
	private ArrayList<IntentTriggerEvent> events;
	private final String ALARM_INTENT_START_ACTION = "hcc.stepuplife.meeting_start";
	private final String ALARM_INTENT_STOP_ACTION = "hcc.stepuplife.meeting_stop";

	private BroadcastReceiver meetingReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			boolean stopMonitoring = true;
			if (intent.getAction().equals(
					StepUpLifeService.this.ALARM_INTENT_START_ACTION))
				stopMonitoring = false;
			StepUpLifeService.this.processAlarmCalendarEvent(stopMonitoring);
		}

	};

	class IntentTriggerEvent {
		public long timeOfTrigger; // RTC time
		public boolean isStartEvent;

		public IntentTriggerEvent(long timeOfTrigger, boolean isStartEvent) {
			this.timeOfTrigger = timeOfTrigger;
			this.isStartEvent = isStartEvent;
		}
	}

	private void populateEvents() {
		// Query Calendar Provider, and populate today's events in events list.
		events = new ArrayList<IntentTriggerEvent>();
		Long now = System.currentTimeMillis();
		Long twelveHoursFromNow = now + 43200 * 1000;
		String[] EVENT_PROJECTION = new String[] { Events.DTSTART, // 0
				Events.DTEND // 3
		};
		int PROJECTION_STARTTIME_INDEX = 0;
		int PROJECTION_STOPTIME_INDEX = 1;
		Cursor cur = null;
		ContentResolver cr = getContentResolver();
		Uri uri = Events.CONTENT_URI;
		String selection = "((" + Events.DTSTART + " > ?) AND (" + Events.DTEND
				+ " <= ?)";
		String[] selectionArgs = new String[] { now.toString(),
				twelveHoursFromNow.toString() };
		// Submit the query and get a Cursor object back.
		cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
		// Note, assuming there are no overlapping events
		while (cur.moveToNext()) {
			// Get the field values
			long startTime = cur.getLong(PROJECTION_STARTTIME_INDEX);
			long stopTime = cur.getLong(PROJECTION_STOPTIME_INDEX);

			events.add(new IntentTriggerEvent(startTime, true));
			events.add(new IntentTriggerEvent(stopTime, false));
			// Do something with the values...
		}
	}

	private void setAlarmForNextEvent() {
		if (events == null) {
			Log.d(LOGTAG, "Events list is null !");
			return;
		}
		if (events.isEmpty()) {
			Log.d(LOGTAG, "No more events in today's calendar");
			return;
		}
		IntentTriggerEvent event = events.remove(0);
		long timeOfTrigger = 0;
		boolean eventypeStart = true;
		Intent alarmIntent;
		if (eventypeStart)
			alarmIntent = new Intent(ALARM_INTENT_START_ACTION);
		else
			alarmIntent = new Intent(ALARM_INTENT_STOP_ACTION);

		pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
		alarmManager.set(AlarmManager.RTC, timeOfTrigger, pendingIntent);
		Log.d(LOGTAG, "Added new event to alarmManager");
	}

	private void processAlarmCalendarEvent(boolean stopMonitoring) {
		Log.d(LOGTAG, "Calendar event alarm received");
		if (stopMonitoring) {
			Log.d(LOGTAG, "Stopping activity monitoring");
			setAlarmForNextEvent();
			stopMonitoringActivity(false);
		} else {
			Log.d(LOGTAG, "Starting activity monitoring");
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

		IntentFilter startMeetingIntentFiler = new IntentFilter(
				ALARM_INTENT_START_ACTION);
		registerReceiver(meetingReceiver, startMeetingIntentFiler);

		IntentFilter stopMeetingIntentFiler = new IntentFilter(
				ALARM_INTENT_STOP_ACTION);
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
		setAlarmForNextEvent();
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
