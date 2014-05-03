package hcc.stepuplife;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class StepUpLifeService extends Service {

	public static final String PREFS_NAME = "stepuplifePrefs";
	private SharedPreferences settings;
	private SharedPreferences mPrefs;
	private static final String LOGTAG = "S/StepUpLife";
	static int activityCount = 0;
	static int stillactivityCount = 0;

	private int exerciseCount;
	private int target = 10;
	private NotificationManager notificationManager;
	public static final String ACTIVITY_GOT_INTENT_STRING = "hcc.stepuplife.gotactivity";
	// Formats the timestamp in the log
	private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSSZ";

	// Delimits the timestamp from the log info
	private static final String LOG_DELIMITER = ";;";

	// A date formatter
	private SimpleDateFormat mDateFormat;

	/**
	 * @return - integer denoting percent of goal reached
	 */
	public int percentGoalReached() {
		int percentGoalReached = 100 * (exerciseCount / target);
		return percentGoalReached < 100 ? percentGoalReached : 100;
	}

	private BroadcastReceiver meetingReceiver = new BroadcastReceiver() {
		private final String LOGTAG = "B/meetingReceiver";

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			boolean stopMonitoring = false;
			Log.d(LOGTAG, "Received intent with action " + intent.getAction());
			if (intent.getAction().equals(
					CalendarEventManager.ALARM_INTENT_START_ACTION)) {
				stopMonitoring = true;
				StepUpLifeService.this.processAlarmCalendarEvent(true);
			} else if (intent.getAction().equals(
					CalendarEventManager.ALARM_INTENT_START_ACTION)) {
				StepUpLifeService.this.processAlarmCalendarEvent(false);
			} else if (intent.getAction().equals(ACTIVITY_GOT_INTENT_STRING)) {
				Log.d(LOGTAG, "got activity intent");
				if (ActivityRecognitionResult.hasResult(intent)) 
				{

					// Get the update
					ActivityRecognitionResult result = ActivityRecognitionResult
							.extractResult(intent);

					// Get the most probable activity from the list of
					// activities in the update
					DetectedActivity mostProbableActivity = result
							.getMostProbableActivity();

					// Get the confidence percentage for the most probable
					// activity
					int confidence = mostProbableActivity.getConfidence();

					// Get the type of activity
					int activityType = mostProbableActivity.getType();
					getNameFromType(activityType);
					if(activityCount > 6)
					{
						float stillConfidence = stillactivityCount/activityCount;
						if(stillConfidence > 0.7f)
						{
							stillactivityCount = activityCount = 0 ;
							createNotification();
						}
					}

				}
			}
		}

	};

	public void createNotification() {
		// Prepare intent which is triggered if the
		// notification is selected
		Intent intent = new Intent(this, ReminderActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
				Intent.FLAG_ACTIVITY_NEW_TASK);

		// Build notification

		Notification noti = new Notification.Builder(this)
				.setContentTitle("Step Up Life")
				.setContentText("Time for some exercise !!!")
				.setSmallIcon(R.drawable.ic_launcher).setContentIntent(pIntent)
				.build();
		// .addAction(R.drawable.ic_launcher, "Call", pIntent)
		// .addAction(R.drawable.ic_launcher, "More", pIntent)
		// .addAction(R.drawable.ic_launcher, "And more", pIntent)

		// hide the notification after its selected
		noti.flags |= Notification.FLAG_AUTO_CANCEL;

		notificationManager.notify(0, noti);

	}

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
		mPrefs = getApplicationContext().getSharedPreferences(
				ActivityUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);
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

		// Initing calendar manager and registering receiver for meeting event
		// intents

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		CalendarEventManager.init(this);
		IntentFilter startMeetingIntentFiler = new IntentFilter(
				CalendarEventManager.ALARM_INTENT_START_ACTION);
		registerReceiver(meetingReceiver, startMeetingIntentFiler);

		IntentFilter stopMeetingIntentFiler = new IntentFilter(
				CalendarEventManager.ALARM_INTENT_STOP_ACTION);
		registerReceiver(meetingReceiver, stopMeetingIntentFiler);

		IntentFilter gotActivityIntentFiler = new IntentFilter(
				ACTIVITY_GOT_INTENT_STRING);
		registerReceiver(meetingReceiver, gotActivityIntentFiler);

		if (intent.getBooleanExtra("start_monitoring", false))
			startMonitoringActivity();

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

		Log.d(LOGTAG, "Creating notification");
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
		CalendarEventManager.release();
	}

	// *************Copied from the IntentService****************//
	/**
	 * Get a content Intent for the notification
	 * 
	 * @return A PendingIntent that starts the device's Location Settings panel.
	 */
	private PendingIntent getContentIntent() {

		// Set the Intent action to open Location Settings
		Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

		// Create a PendingIntent to start an Activity
		return PendingIntent.getActivity(getApplicationContext(), 0, gpsIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

	/**
	 * Tests to see if the activity has changed
	 * 
	 * @param currentType
	 *            The current activity type
	 * @return true if the user's current activity is different from the
	 *         previous most probable activity; otherwise, false.
	 */
	private boolean activityChanged(int currentType) {

		// Get the previous type, otherwise return the "unknown" type
		int previousType = mPrefs.getInt(
				ActivityUtils.KEY_PREVIOUS_ACTIVITY_TYPE,
				DetectedActivity.UNKNOWN);

		// If the previous type isn't the same as the current type, the activity
		// has changed
		if (previousType != currentType) {
			return true;

			// Otherwise, it hasn't.
		} else {
			return false;
		}
	}

	/**
	 * Determine if an activity means that the user is moving.
	 * 
	 * @param type
	 *            The type of activity the user is doing (see DetectedActivity
	 *            constants)
	 * @return true if the user seems to be moving from one location to another,
	 *         otherwise false
	 */
	private boolean isMoving(int type) {
		switch (type) {
		// These types mean that the user is probably not moving
		case DetectedActivity.STILL:
		case DetectedActivity.TILTING:
		case DetectedActivity.UNKNOWN:
			return false;
		default:
			return true;
		}
	}

	/**
	 * Write the activity recognition update to the log file
	 * 
	 * @param result
	 *            The result extracted from the incoming Intent
	 */
	private void logActivityRecognitionResult(ActivityRecognitionResult result) {
		// Get all the probably activities from the updated result
		for (DetectedActivity detectedActivity : result.getProbableActivities()) {

			// Get the activity type, confidence level, and human-readable name
			int activityType = detectedActivity.getType();
			int confidence = detectedActivity.getConfidence();
			String activityName = getNameFromType(activityType);

			// Make a timestamp
			String timeStamp = mDateFormat.format(new Date());

			// Get the current log file or create a new one, then log the
			// activity
			/*
			 * LogFile.getInstance(getApplicationContext()).log( timeStamp +
			 * LOG_DELIMITER + getString(R.string.log_message, activityType,
			 * activityName, confidence) );
			 */
		}
	}

	/**
	 * Map detected activity types to strings
	 * 
	 * @param activityType
	 *            The detected activity type
	 * @return A user-readable name for the type
	 */
	private String getNameFromType(int activityType) {
		activityCount++;
		switch (activityType) {
		case DetectedActivity.IN_VEHICLE:
			return "in_vehicle";
		case DetectedActivity.ON_BICYCLE:
			return "on_bicycle";
		case DetectedActivity.ON_FOOT:
			return "on_foot";
		case DetectedActivity.STILL:
			stillactivityCount++;
			return "still";
		case DetectedActivity.UNKNOWN:
			return "unknown";
		case DetectedActivity.TILTING:
			return "tilting";
		}
		return "unknown";
	}

}
