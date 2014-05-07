package hcc.stepuplife;

import hcc.stepuplife.ActivityUtils.REQUEST_TYPE;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
	private UserStats mStats;
	private SharedPreferences mPrefs;
	private static final String LOGTAG = "S/StepUpLife";
	static int activityCount = 0;
	static int stillactivityCount = 0;
	static int cancelCounter = 0;
	private int activityIntentCount = 0;
	private static int idleTime = 45; // 45 minutes
	private static int idleTimeNotifications = idleTime * 6;

	private int exerciseCount;
	private int target = 10;

	private PendingIntent snoozeWakeupPendingIntent;
	private PendingIntent exerciseTimeoutPendingIntent;

	private NotificationManager notificationManager;
	private REQUEST_TYPE mRequestType;

	public static final String ACTIVITY_GOT_INTENT_STRING = "hcc.stepuplife.gotactivity";
	public static final String UPDATE_SETTINGS = "hcc.steplife.updatesettings";

	// Formats the timestamp in the log
	private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSSZ";

	// Delimits the timestamp from the log info
	private static final String LOG_DELIMITER = ";;";
	private static final String SNOOZE_WAKEUP_INTENT_STRING = "hcc.stepuplife.snooze_wakeup";

	private static final long MILLISECS_PER_MIN = 60 * 1000;
	public static int SNOOZE_MIN = 1;
	// private static int msnoozemin = 1;
	public static long IDLE_TIMEOUT_MIN = 1;
	private static long SNOOZE_TIMEOUT_MILLISECS = SNOOZE_MIN
			* MILLISECS_PER_MIN;
	private static final String EXERCISE_TIMEOUT_INTENT_STRING = "hcc.stepuplife.exercise_timeout";
	private static long EXERCISE_TIMEOUT_MIN = 1;
	private static final long EXERCISE_TIMEOUT_MILLISECS = EXERCISE_TIMEOUT_MIN
			* MILLISECS_PER_MIN;
	private static long IDLE_TIMEOUT_MILLISECS = IDLE_TIMEOUT_MIN
			* MILLISECS_PER_MIN;
	private static final String IDLE_TIMEOUT_INTENT_STRING = "hcc.stepuplife.idle_timeout";
	private static final String CALENDAR_EVENT_ON = "isCalendarEventOn";
	private static final long AFTER_MEETING_END_GAP_MIN = 5;
	private static final int ACTIVITY_NON_STILL_THRESHOLD_SECS = 5;
	private boolean mIdleTimeOut = false;
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

	private void setDebugerTimerValues() {
		if (StepUpLifeUtils.DEBUG == false)
			return;
		SNOOZE_MIN = 1;
		IDLE_TIMEOUT_MIN = 1;
		EXERCISE_TIMEOUT_MIN = 1;
	}

	private BroadcastReceiver stepUpLifeReceiver = new BroadcastReceiver() {
		private final String LOGTAG = "B/stepUpLifeReceiver";

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub

			// Log.d(LOGTAG, "Received intent with action " +
			// intent.getAction());
			if (intent.getAction().equals(
					CalendarEventManager.ALARM_INTENT_START_ACTION)) {
				// StepUpLifeService.this.processAlarmCalendarEvent(true);
			} else if (intent.getAction().equals(
					CalendarEventManager.ALARM_INTENT_STOP_ACTION)) {
				CalendarEventManager.notifyEndMeetingNotificationReceived();
				StepUpLifeService.this.handleEndMeetingEvent();
				// StepUpLifeService.this.processAlarmCalendarEvent(false);
			} else if (intent.getAction().equals(SNOOZE_WAKEUP_INTENT_STRING)) {
				Log.i(LOGTAG, "Snooze timeout");
				if (!CalendarEventManager
						.isCalendarEventOn(StepUpLifeService.this))
					notifyUserForExercise();
				else {
					StepUpLifeService.this.mIdleTimeOut = true;
					StepUpLifeService.this.stopMonitoringActivity(false);
				}
			} else if (intent.getAction()
					.equals(EXERCISE_TIMEOUT_INTENT_STRING)) {
				// notifyUser();
				Log.i(LOGTAG, "Exercise timeout");
				// exerciseTimeoutPendingIntent = null;
				startMonitoringActivity();
			} else if (intent.getAction().equals(IDLE_TIMEOUT_INTENT_STRING)) {
				Log.i(LOGTAG, "Idle timeout");
				if (!CalendarEventManager
						.isCalendarEventOn(StepUpLifeService.this))
					notifyUserForExercise();
				else {
					StepUpLifeService.this.mIdleTimeOut = true;
					StepUpLifeService.this.stopMonitoringActivity(false);
				}
			} else if (intent.getAction().equals(ACTIVITY_GOT_INTENT_STRING)) {
				if (activityIntentCount < 5) {
					Log.i(LOGTAG, "got activity intent");
					activityIntentCount++;
				}
				if (!isMonitoring() || StepUpLifeService.this.mIdleTimeOut) {
					// Log.i(LOGTAG,
					// "not processing activity intent as monitoring is disabled");
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

					if (mostProbableActivity.getType() != DetectedActivity.STILL) {
						incrementActivityCount();
						Log.d(LOGTAG,
								"Most probable activity detected is "
										+ activityTypeToString(mostProbableActivity
												.getType()));
						if (isUserMoving()) {
							startTimer(IDLE_TIMEOUT_INTENT_STRING); // will
																	// reset
																	// idle
																	// timer;
						} else
							incrementStillActivityCount();

					} else {
						// do noting, idle timer is still running;
						incrementStillActivityCount();
					}

					// getNameFromType(mostProbableactivityType);
					// if (activityCount > 2) {
					// float stillConfidence = 1.0f;
					// stillConfidence = (stillactivityCount * (1.0f))
					// / (activityCount * 1.0f);
					// if (stillConfidence > 0.7f) {
					// stillactivityCount = activityCount = 0;
					// Log.d(LOGTAG, "Creating notification");
					// // notifyUserForExercise();
					// }
					//
					// } else {
					// // restart the timer
					// startTimer(IDLE_TIMEOUT_INTENT_STRING);
					// }

				}
			} else if (intent.getAction().equals(UPDATE_SETTINGS)) {
				// notifyUser();
				Log.i(LOGTAG, "Update Settings");
				updateSettings();
			}
		}
	};

	public void updateSettings() {
		IDLE_TIMEOUT_MIN = settings.getInt(hcc.stepuplife.Settings.IDLE_TIME,
				(int) IDLE_TIMEOUT_MIN);
		idleTimeNotifications = (int) (idleTime * 6); // 1 update in 10 secs so
														// 6 updates per minute
		SNOOZE_MIN = settings.getInt(hcc.stepuplife.Settings.SNOOZE_TIME,
				SNOOZE_MIN); // Should use msnoozemin
		stopMonitoringActivity(false);
		startMonitoringActivity();
	}

	private boolean isUserMoving() {
		// TODO Auto-generated method stub

		if (activityCount > 2) {
			float stillConfidence = 1.0f;
			stillConfidence = (stillactivityCount * (1.0f))
					/ (activityCount * 1.0f);

			if (stillConfidence > 0.7f) {
				stillactivityCount = activityCount = 0;
				Log.d(LOGTAG, "Creating notification");
				Log.i(LOGTAG, "Seems user is still");
				return false;
			}
			// notifyUserForExercise();
		}
		Log.i(LOGTAG, "Seems user is moving");
		return true;

	}

	protected void handleEndMeetingEvent() {
		// TODO Auto-generated method stub
		Log.d(LOGTAG, "Calendar event alarm received");

		mCalendarEventOn = false;
		if (mIdleTimeOut) {
			Log.d(LOGTAG,
					"Idle timeout had occured, so will notify after 5 secs");
			startTimer(IDLE_TIMEOUT_INTENT_STRING, AFTER_MEETING_END_GAP_MIN
					* MILLISECS_PER_MIN);
		}
		startMonitoringActivity();

	}

	private PendingIntent idleTimerPendingIntent;
	private long mIdleTimerStartTime;
	private boolean mCalendarEventOn = false;

	private long getElapsedIdleTimeinMillisecs() {
		if (mIdleTimerStartTime == 0) {
			return -1;
		}
		Calendar calendar = Calendar.getInstance();
		long timeNow = calendar.getTimeInMillis();
		if (timeNow - mIdleTimerStartTime <= 0) {
			Log.d(LOGTAG, "WATCH OUT !!! timeNow - mIdleTimerStartTime < 0 !!!");
			return -1;
		} else
			return (timeNow - mIdleTimerStartTime);
	}

	public long getElapsedIdleTimeinMin() {
		return getElapsedIdleTimeinMillisecs() / MILLISECS_PER_MIN;
	}

	private void startTimer(String intentString, long duration) {

		if (intentString.equals(IDLE_TIMEOUT_INTENT_STRING)) {
			long timeSinceLastIntent = getElapsedIdleTimeinMillisecs();
			if (timeSinceLastIntent != -1
					&& timeSinceLastIntent < ACTIVITY_NON_STILL_THRESHOLD_SECS * 1000) {
				Log.d(LOGTAG,
						"No need to restart idle timerintent as timeSinceLastIntent < 30 secs");
				return;
			}
			if (idleTimerPendingIntent != null)
				stopTimer(IDLE_TIMEOUT_INTENT_STRING);
			Calendar calendar = Calendar.getInstance();

			long timeToWakeup;

			if (duration != 0) {
				timeToWakeup = calendar.getTimeInMillis() + duration;
			} else
				timeToWakeup = calendar.getTimeInMillis()
						+ IDLE_TIMEOUT_MILLISECS;

			Intent idleTimerIntent = new Intent(IDLE_TIMEOUT_INTENT_STRING);
			idleTimerPendingIntent = PendingIntent.getBroadcast(this, 0,
					idleTimerIntent, 0);
			alarmManager.set(AlarmManager.RTC_WAKEUP, timeToWakeup,
					idleTimerPendingIntent);
			mIdleTimerStartTime = calendar.getTimeInMillis();
			Log.i(LOGTAG, "(Re)started idle timer");

		} else if (intentString.equals(SNOOZE_WAKEUP_INTENT_STRING)) {
			if (snoozeWakeupPendingIntent != null)
				stopTimer(SNOOZE_WAKEUP_INTENT_STRING);
			Calendar calendar = Calendar.getInstance();

			long timeToWakeup;
			if (duration != 0) {
				timeToWakeup = calendar.getTimeInMillis() + duration;
			} else
				timeToWakeup = calendar.getTimeInMillis()
						+ SNOOZE_TIMEOUT_MILLISECS;

			Intent idleTimerIntent = new Intent(SNOOZE_WAKEUP_INTENT_STRING);
			snoozeWakeupPendingIntent = PendingIntent.getBroadcast(this, 0,
					idleTimerIntent, 0);
			alarmManager.set(AlarmManager.RTC_WAKEUP, timeToWakeup,
					snoozeWakeupPendingIntent);
			Log.i(LOGTAG, "Started snooze timer");
		} else if (intentString.equals(EXERCISE_TIMEOUT_INTENT_STRING)) {
			if (exerciseTimeoutPendingIntent != null)
				stopTimer(EXERCISE_TIMEOUT_INTENT_STRING);
			Calendar calendar = Calendar.getInstance();

			long timeToWakeup;
			if (duration != 0) {
				timeToWakeup = calendar.getTimeInMillis() + duration;
			} else
				timeToWakeup = calendar.getTimeInMillis()
						+ EXERCISE_TIMEOUT_MILLISECS;

			Intent idleTimerIntent = new Intent(EXERCISE_TIMEOUT_INTENT_STRING);
			exerciseTimeoutPendingIntent = PendingIntent.getBroadcast(this, 0,
					idleTimerIntent, 0);
			alarmManager.set(AlarmManager.RTC_WAKEUP, timeToWakeup,
					exerciseTimeoutPendingIntent);
			Log.i(LOGTAG, "Started exercise timer");
		}

	}

	// public void createNotification() {
	// // Prepare intent which is triggered if the
	// // notification is selected
	// Log.d(LOGTAG, "createNotification() entered");
	// Intent intent = new Intent(this, NotificationActivity.class);
	// PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
	// Intent.FLAG_ACTIVITY_NEW_TASK);
	//
	// // Build notification
	//
	// Notification noti = new Notification.Builder(this)
	// .setContentTitle("Step Up Life")
	// .setContentText("Time for some exercise !!!")
	// .setSmallIcon(R.drawable.ic_launcher).setContentIntent(pIntent)
	// .build();
	// // .addAction(R.drawable.ic_launcher, "Call", pIntent)
	// // .addAction(R.drawable.ic_launcher, "More", pIntent)
	// // .addAction(R.drawable.ic_launcher, "And more", pIntent)
	//
	// // hide the notification after its selected
	// noti.sound = RingtoneManager
	// .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	// noti.flags |= Notification.FLAG_AUTO_CANCEL;
	// Log.d(LOGTAG,
	// "sent to notificationManager, exiting createNotification()");
	//
	// notificationManager.notify(0, noti);
	//
	// }

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
			mCalendarEventOn = true;
		} else {
			Log.d(LOGTAG, "Starting activity monitoring");
			mCalendarEventOn = false;
			if (mIdleTimeOut) {
				startTimer(IDLE_TIMEOUT_INTENT_STRING,
						AFTER_MEETING_END_GAP_MIN * MILLISECS_PER_MIN);
			}
			startMonitoringActivity();
		}
		CalendarEventManager.notifyEventReceived();
	}

	/**
	 * @author devj1988 encapsulates service state
	 */
	public enum ServiceState {
		STARTED, STOPPED, RUNNING_MONITORING, RUNNING_SUSPENDED;

		public String toString() {
			switch (this) {
			case STARTED:
				return "StepUpLifeService started";
			case STOPPED:
				return "StepUpLifeService stopped";
			case RUNNING_MONITORING:
				return "StepUpLifeService running and monitoring activity";
			case RUNNING_SUSPENDED:
				return "StepUpLifeService running and not monitoring activity";
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
		case RUNNING_SUSPENDED:
			return true;
		case STOPPED:
		default:
			return false;
		}
	}

	public boolean isMonitoring() {
		switch (this.serviceState) {
		case RUNNING_MONITORING:
			return true;
		case RUNNING_SUSPENDED:
		case STARTED:
		case STOPPED:
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

		// mBroadcastManager = LocalBroadcastManager.getInstance(this);

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

		// notificationManager = (NotificationManager)
		// getSystemService(NOTIFICATION_SERVICE);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		// CalendarEventManager.init(this);

		String[] intentFilterStringArray = {
				CalendarEventManager.ALARM_INTENT_START_ACTION,
				CalendarEventManager.ALARM_INTENT_STOP_ACTION,
				ACTIVITY_GOT_INTENT_STRING, SNOOZE_WAKEUP_INTENT_STRING,
				IDLE_TIMEOUT_INTENT_STRING, EXERCISE_TIMEOUT_INTENT_STRING };

		for (String intentFilterString : intentFilterStringArray) {
			IntentFilter intentFiler = new IntentFilter(intentFilterString);
			registerReceiver(stepUpLifeReceiver, intentFiler);
		}

		mStats = UserStats.loadActivityStats(this);
		mCalendarEventOn = settings.getBoolean(CALENDAR_EVENT_ON, false);
		if (mStats == null)
			mStats = new UserStats();

		if (intent != null && intent.getBooleanExtra("start_monitoring", false))
			startMonitoringActivity();

		Log.d(LOGTAG, "Service onStartCommand ends");

		onStartUpdates();
		// createNotification();

		return Service.START_STICKY;
	}

	private void notifyUserForExercise() {
		mIdleTimeOut = false;
		stopMonitoringActivity(false);
		StepUpLifeUtils.createNotification(this, "Step Up Life",
				"Time for an exercise !!!");
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
		settings.edit().putBoolean(CALENDAR_EVENT_ON, mCalendarEventOn)
				.commit();
		stopMonitoringActivity(true);
		StepUpLifeUtils.cancelAllNotifications(this);
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
		setDebugerTimerValues();
		settings.edit().putBoolean("monitoring", true);
		serviceState = ServiceState.RUNNING_MONITORING;
		Log.d(LOGTAG, "Started activity monitoring");
		startTimer(IDLE_TIMEOUT_INTENT_STRING, 5000L);

	}

	private void startTimer(String idleTimeoutIntentString) {
		// TODO Auto-generated method stub
		startTimer(idleTimeoutIntentString, 0);
	}

	private void stopTimer(String intentString) {
		PendingIntent pendingIntentToCancel = null;
		if (intentString.equals(IDLE_TIMEOUT_INTENT_STRING)) {
			pendingIntentToCancel = idleTimerPendingIntent;
			mIdleTimerStartTime = 0;
			Log.d(LOGTAG, "Stopping idle timer");
		} else if (intentString.equals(SNOOZE_WAKEUP_INTENT_STRING)) {
			pendingIntentToCancel = snoozeWakeupPendingIntent;
			Log.d(LOGTAG, "Stopped idle timer");
		} else if (intentString.equals(EXERCISE_TIMEOUT_INTENT_STRING)) {
			pendingIntentToCancel = exerciseTimeoutPendingIntent;
			Log.d(LOGTAG, "Stopped idle timer");
		}

		if (pendingIntentToCancel != null)
			alarmManager.cancel(pendingIntentToCancel);
	}

	public void stopMonitoringActivity(boolean endService) {
		// TODO Auto-generated method stub
		settings.edit().putBoolean("monitoring", false).commit();
		serviceState = ServiceState.RUNNING_SUSPENDED;
		if (endService) {
			UserStats.saveActivityStats(this, mStats);
			// launch summary screen
		}
		stopTimer(IDLE_TIMEOUT_INTENT_STRING);
		stopTimer(SNOOZE_WAKEUP_INTENT_STRING);
		stopTimer(EXERCISE_TIMEOUT_INTENT_STRING);

		Log.d(LOGTAG, "Stopped activity monitoring");
	}

	public void snoozeExercise() {
		// TODO Auto-generated method stub
		// serviceState = ServiceState.SNOOZE;
		// Intent intent = new Intent(Home.SNOOZE);
		// sendBroadcast(intent);
		startTimer(SNOOZE_WAKEUP_INTENT_STRING);
		StepUpLifeUtils.showToast(this, "OK ! I will remind you again in "
				+ SNOOZE_MIN + " minutes");
	}

	public int getExerciseImageId() {
		// return ExerciseRecommender.getRandomExerciseId();
		return ExerciseRecommender.getRandomExerciseAnimId();
	}

	public void doExercise(int id) {
		if (UserStats.ExerciseType.getExerciseTypeFromAnimId(id) == UserStats.ExerciseType.LUNGES)
			mStats.incrementLungesCount();
		if (UserStats.ExerciseType.getExerciseTypeFromAnimId(id) == UserStats.ExerciseType.PUSHUPS)
			mStats.incrementPushupsCount();

		if (mStats.isGoalReached()) {
			StepUpLifeUtils.createSummaryNotification(this);
			return;
		}
		startMonitoringActivity();
		if (StepUpLifeUtils.DISABLE_EXERCISE_TIMEOUT == false) {
			startTimer(EXERCISE_TIMEOUT_INTENT_STRING);
			Log.d(LOGTAG, "monitoring is off, will resume after "
					+ EXERCISE_TIMEOUT_MIN + " minutes");
		}
		StepUpLifeUtils.showToast(this, "Right choice !!!");
	}

	public void cancelRecommendedExercise() {
		// TODO Auto-generated method stub
		mStats.incrementCancelCount();
		StepUpLifeUtils.showToast(this, "OK ! Later then...");
		Log.d(LOGTAG, "Update cancel counter, starting activity monitoring");
		startMonitoringActivity();

	}

	public void doCleanUp() {
		Log.d(LOGTAG, "Unregistering broadcast receiver");
		unregisterReceiver(stepUpLifeReceiver);
		// CalendarEventManager.release();
	}

	/**
	 * Map detected activity types to strings
	 * 
	 * @param activityType
	 *            The detected activity type
	 * @return A user-readable name for the type
	 */
	private String activityTypeToString(int activityType) {

		switch (activityType) {
		case DetectedActivity.IN_VEHICLE:
			return "in_vehicle";
		case DetectedActivity.ON_BICYCLE:
			return "on_bicycle";
		case DetectedActivity.ON_FOOT:
			return "on_foot";
		case DetectedActivity.STILL:
			return "still";
		case DetectedActivity.UNKNOWN:
			return "unknown";
		case DetectedActivity.TILTING:
			return "tilting";
		}
		return "unknown";
	}

	private void incrementActivityCount() {
		activityCount++;
	};

	private void incrementStillActivityCount() {
		stillactivityCount++;
	};

}
