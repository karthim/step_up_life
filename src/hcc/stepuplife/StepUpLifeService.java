package hcc.stepuplife;

import hcc.stepuplife.ActivityUtils.REQUEST_TYPE;

import java.text.SimpleDateFormat;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Spanned;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class StepUpLifeService extends Service {

	public static final String PREFS_NAME = "stepuplifePrefs";
	private SharedPreferences settings;
	private SharedPreferences mPrefs;
	private static final String LOGTAG = "S/StepUpLife";
	static int activityCount = 0;
	static int stillactivityCount = 0;
	static int cancelCounter = 0;

	private int exerciseCount;
	private int target = 10;

	PendingIntent snoozeWakeupPendingIntent;

	private NotificationManager notificationManager;
	private REQUEST_TYPE mRequestType;

	public static final String ACTIVITY_GOT_INTENT_STRING = "hcc.stepuplife.gotactivity";

	// Formats the timestamp in the log
	private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSSZ";

	// Delimits the timestamp from the log info
	private static final String LOG_DELIMITER = ";;";
	private static final String SNOOZE_WAKEUP_INTENT_STRING = "hcc.stepuplife.snooze_wakeup";

	private static final long MILLISECS_PER_MIN = 60 * 1000;
	private static final long SNOOZE_MIN = 3;
	private static final long SNOOZE_TIMEOUT = SNOOZE_MIN * MILLISECS_PER_MIN;

	// A date formatter
	private SimpleDateFormat mDateFormat;

	private ArrayAdapter<Spanned> mStatusAdapter;
	/*
	 * Intent filter for incoming broadcasts from the IntentService.
	 */
	IntentFilter mBroadcastFilter;
	// Instance of a local broadcast manager
	private LocalBroadcastManager mBroadcastManager;

	// The activity recognition update request object
	private DetectionRequester mDetectionRequester;

	// The activity recognition update removal object
	private DetectionRemover mDetectionRemover;
	private StepUpLifeService stepUpLifeService;
	private AlarmManager alarmManager;

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
			} else if (intent.getAction().equals(SNOOZE_WAKEUP_INTENT_STRING)) {
				notifyUser();
				snoozeWakeupPendingIntent = null;
			} else if (intent.getAction().equals(ACTIVITY_GOT_INTENT_STRING)) {
				Log.d(LOGTAG, "got activity intent");
				if (serviceState == ServiceState.RUNNING_NOT_MONITORING) {
					Log.d(LOGTAG,
							"not processing activity intent as monitoring is disabled");
					return;
				}

				if (ActivityRecognitionResult.hasResult(intent)) {

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
					if (activityCount > 2) {
						float stillConfidence = 1.0f;
						stillConfidence = (stillactivityCount * (1.0f))
								/ (activityCount * 1.0f);
						if (stillConfidence > 0.7f) {
							stillactivityCount = activityCount = 0;
							Log.d(LOGTAG, "Creating notification");
							notifyUser();
						}
					}

				}
			}
		}

	};

	public void createNotification() {
		// Prepare intent which is triggered if the
		// notification is selected
		Log.d(LOGTAG, "createNotification() entered");
		Intent intent = new Intent(this, NotificationActivity.class);
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
		noti.sound = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		Log.d(LOGTAG,
				"sent to notificationManager, exiting createNotification()");

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

		mBroadcastManager = LocalBroadcastManager.getInstance(this);

		// Create a new Intent filter for the broadcast receiver
		mBroadcastFilter = new IntentFilter(Home.SNOOZE);

		// Get detection requester and remover objects
		mDetectionRequester = new DetectionRequester(this);
		mDetectionRemover = new DetectionRemover(this);

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
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

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

		IntentFilter snoozeWakeupIntentFiler = new IntentFilter(
				SNOOZE_WAKEUP_INTENT_STRING);
		registerReceiver(meetingReceiver, snoozeWakeupIntentFiler);

		if (intent != null && intent.getBooleanExtra("start_monitoring", false))
			startMonitoringActivity();

		Log.d(LOGTAG, "Service onStartCommand ends");

		onStartUpdates();
		// createNotification();

		return Service.START_STICKY;
	}

	private void notifyUser() {
		stopMonitoringActivity(false);
		createNotification();
	}

	private void saveSummary() {

	}

	public void onStartUpdates() {

		// Check for Google Play services
		if (!servicesConnected()) {

			return;
		}

		/*
		 * Set the request type. If a connection error occurs, and Google Play
		 * services can handle it, then onActivityResult will use the request
		 * type to retry the request
		 */
		mRequestType = ActivityUtils.REQUEST_TYPE.ADD;

		// Pass the update request to the requester object
		mDetectionRequester.requestUpdates();
	}

	public void onStopUpdates() {

		// Check for Google Play services
		if (!servicesConnected()) {

			return;
		}

		/*
		 * Set the request type. If a connection error occurs, and Google Play
		 * services can handle it, then onActivityResult will use the request
		 * type to retry the request
		 */
		mRequestType = ActivityUtils.REQUEST_TYPE.REMOVE;

		// Pass the remove request to the remover object
		mDetectionRemover.removeUpdates(mDetectionRequester
				.getRequestPendingIntent());

		/*
		 * Cancel the PendingIntent. Even if the removal request fails,
		 * canceling the PendingIntent will stop the updates.
		 */
		mDetectionRequester.getRequestPendingIntent().cancel();
	}

	private boolean servicesConnected() {

		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);

		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {

			// In debug mode, log the status
			// Log.d(ActivityUtils.APPTAG,
			// getString(R.string.play_services_available));

			// Continue
			return true;

			// Google Play services was not available for some reason
		} else {

			// Display an error dialog
			// GooglePlayServicesUtil.getErrorDialog(resultCode, this,
			// 0).show();
			Log.d(LOGTAG, "Google Play services was not available !");
			return false;
		}
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
			saveSummary();
			// @TODO: and launch Summary screen
		}
		if (snoozeWakeupPendingIntent != null)
			alarmManager.cancel(snoozeWakeupPendingIntent);
		Log.d(LOGTAG, "Stopped activity monitoring");
	}

	public void snoozeExercise() {
		// TODO Auto-generated method stub
		// serviceState = ServiceState.SNOOZE;
		// Intent intent = new Intent(Home.SNOOZE);
		// sendBroadcast(intent);
		Intent snoozeIntent = new Intent(SNOOZE_WAKEUP_INTENT_STRING);
		PendingIntent snoozeWakeupPendingIntent = PendingIntent.getBroadcast(
				this, 0, snoozeIntent, 0);
		alarmManager.set(AlarmManager.ELAPSED_REALTIME, SNOOZE_TIMEOUT,
				snoozeWakeupPendingIntent);
	}

	public void getExerciseRecco() {

		// TODO Auto-generated method stub
	}

	public void doExercise() {
	}

	public void startExercise() {
		// TODO Auto-generated method stub
	}

	private void updateCancelCounter() {
		// TODO Auto-generated method stub
		cancelCounter++;
	}

	public void cancelRecommendedExercise() {
		// TODO Auto-generated method stub
		updateCancelCounter();
		Log.d(LOGTAG, "Update cancel counter, starting activity monitoring");
		startMonitoringActivity();
	}

	public void doCleanUp() {
		Log.d(LOGTAG, "Unregistering broadcast receiver");
		unregisterReceiver(meetingReceiver);
		CalendarEventManager.release();
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
