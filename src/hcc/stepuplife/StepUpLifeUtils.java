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
	

	public static void createNotification(Context context, String title, String text) {
		// Prepare intent which is triggered if the
		// notification is selected
		Log.d(LOGTAG, "createNotification() entered");
		Intent intent = new Intent(context, NotificationActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent,
				Intent.FLAG_ACTIVITY_NEW_TASK);

		// Build notification

		Notification noti = new Notification.Builder(context)
				.setContentTitle(title)
				.setContentText(text)
				.setSmallIcon(R.drawable.ic_launcher).setContentIntent(pIntent)
				.build();

		// hide the notification after its selected
		noti.sound = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		Log.d(LOGTAG,
				"sent to notificationManager, exiting createNotification()");
		((NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE)).notify(0, noti);

	}
	
	public static void showToast(Context context, String text){
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		toast.show();
	}

}
