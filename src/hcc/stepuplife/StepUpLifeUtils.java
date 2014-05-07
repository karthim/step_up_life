package hcc.stepuplife;

import java.util.Calendar;
import java.util.TimeZone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.util.Log;
import android.widget.Toast;

public class StepUpLifeUtils {
	private static final String LOGTAG = "hcc.stepuplife.utils";
	private static final int EVENING_THRESHOLD_PM = 18;
	private static final int NOON_PM = 12;
	private static final int MORNING_THRESHOLD = 6;

	private static final int SLEEP_THRESHOLD_PM = 21;
	private static final String SLEEP_MSG = "\"Time to sleep, buddy !\"";
	private static final String STOPPED_GOOD_MORNING_MSG = "\"Good Morning ! Ready to start ?\"";
	private static final String STARTED_GOOD_MORNING_MSG = "\"How is the morning going ?\"";
	private static final String STARTED_GOOD_AFTERNOON_MSG = "\"How is the afternoon going ?\"";
	private static final String STOPPED_GOOD_AFTERNOON_MSG = "\"How is the afternoon going ?\"";
	private static final String STARTED_EVENING_MSG = "\"Having a great evening ?\"";
	private static final String STOPPED_EVENING_MSG = "\"Having a great evening ?\"";

	public static final boolean DEBUG = true;
	public static final boolean DISABLE_EXERCISE_TIMEOUT = true;

	public static void createNotification(Context context, String title,
			String text) {
		// Prepare intent which is triggered if the
		// notification is selected
		Log.d(LOGTAG, "createNotification() entered");
		Intent intent = new Intent(context, NotificationActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent,
				Intent.FLAG_ACTIVITY_NEW_TASK);

		// Build notification

		Notification noti = new Notification.Builder(context)
				.setContentTitle(title).setContentText(text)
				.setSmallIcon(R.drawable.tinylogo).setContentIntent(pIntent)
				.build();

		// hide the notification after its selected
		noti.sound = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		noti.defaults |= Notification.DEFAULT_VIBRATE;

		Log.d(LOGTAG,
				"sent to notificationManager, exiting createNotification()");

		NotificationManager notificationManager = ((NotificationManager) context
				.getSystemService(context.NOTIFICATION_SERVICE));

		notificationManager.cancelAll();
		notificationManager.notify(0, noti);

	}

	public static void showToast(Context context, String text) {
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		toast.show();
	}

	public static void cancelAllNotifications(Context context) {
		NotificationManager notificationManager = ((NotificationManager) context
				.getSystemService(context.NOTIFICATION_SERVICE));

		notificationManager.cancelAll();
	}

	public static int getBgImage() {
		Calendar rightNow = Calendar.getInstance();
		int hourOfday = rightNow.get(Calendar.HOUR_OF_DAY);

		if (hourOfday < NOON_PM && hourOfday >= MORNING_THRESHOLD) {
			return R.drawable.sunrise;
		} else if (hourOfday >= NOON_PM && hourOfday <= EVENING_THRESHOLD_PM) {
			return R.drawable.afternoon;
		} else {
			return R.drawable.evening;
		}
	}

	public static String getGreetingText(boolean isStarted) {
		Calendar rightNow = Calendar.getInstance();
		rightNow.setTimeZone(TimeZone.getDefault());
		int hourOfday = rightNow.get(Calendar.HOUR_OF_DAY);
		Log.d("INFO", "Hour of day is" + hourOfday);

		if (hourOfday < NOON_PM && hourOfday >= MORNING_THRESHOLD) {
			if (isStarted)
				return STARTED_GOOD_MORNING_MSG;
			else
				return STOPPED_GOOD_MORNING_MSG;
		} else if (hourOfday >= NOON_PM && hourOfday <= EVENING_THRESHOLD_PM) {
			if (isStarted)
				return STARTED_GOOD_AFTERNOON_MSG;
			else
				return STOPPED_GOOD_AFTERNOON_MSG;
		} else if (hourOfday >= EVENING_THRESHOLD_PM
				&& hourOfday <= SLEEP_THRESHOLD_PM) {
			if (isStarted)
				return STARTED_EVENING_MSG;
			else
				return STOPPED_EVENING_MSG;
		} else
			return SLEEP_MSG;

	}

	// Service sends notification to notify user that goal has been reached
	public static void createSummaryNotification(Context context) {
		// TODO Auto-generated method stub
		Log.d(LOGTAG, "createSummaryNotification() entered");
		Intent intent = new Intent(context, SummaryActivity.class);
		intent.putExtra("fromService", true);
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent,
				Intent.FLAG_ACTIVITY_NEW_TASK);

		// Build notification

		Notification noti = new Notification.Builder(context)
				.setContentTitle("Step Up Life")
				.setContentText("You have reached your goal !!!")
				.setSmallIcon(R.drawable.tinylogo).setContentIntent(pIntent)
				.build();

		// hide the notification after its selected
		noti.sound = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		noti.defaults |= Notification.DEFAULT_VIBRATE;

		Log.d(LOGTAG,
				"sent to notificationManager, exiting createNotification()");

		NotificationManager notificationManager = ((NotificationManager) context
				.getSystemService(context.NOTIFICATION_SERVICE));

		notificationManager.cancelAll();
		notificationManager.notify(0, noti);

	}

}
