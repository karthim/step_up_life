package hcc.stepuplife;

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

	public static void createSummaryNotification(Context context) {
		// TODO Auto-generated method stub
		Log.d(LOGTAG, "createSummaryNotification() entered");
		Intent intent = new Intent(context, SummaryActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent,
				Intent.FLAG_ACTIVITY_NEW_TASK);

		// Build notification
		
		
		Notification noti = new Notification.Builder(context)
				.setContentTitle("Step Up Life").setContentText("You have reached your goal !!!")
				.setSmallIcon(R.drawable.smalllogo).setContentIntent(pIntent)
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
