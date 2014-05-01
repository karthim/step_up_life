package hcc.stepuplife;

import java.util.ArrayList;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Events;
import android.util.Log;

public class CalendarEventManager {

	private static final String LOGTAG = "C/CalendarEventManager";
	private AlarmManager alarmManager;
	private PendingIntent pendingIntent;
	private ArrayList<IntentTriggerEvent> events;
	public static final String ALARM_INTENT_START_ACTION = "hcc.stepuplife.meeting_start";
	public static final String ALARM_INTENT_STOP_ACTION = "hcc.stepuplife.meeting_stop";
	private static CalendarEventManager mgr;
	Context appContext;

	private CalendarEventManager(Context c) {
		this.appContext = c;
		alarmManager = (AlarmManager) appContext
				.getSystemService(Context.ALARM_SERVICE);
		populateEventsList();
		setAlarmForNextEvent();
	}

	public static void init(Context c) {
		if (mgr == null)
			mgr = new CalendarEventManager(c);
	}

	/**
	 * populate events list from the calendar
	 */

	/**
	 * @author devj1988 Contains calendar event time of trigger.
	 */
	class IntentTriggerEvent {
		public long timeOfTrigger; // RTC time
		public boolean isStartEvent;

		public IntentTriggerEvent(long timeOfTrigger, boolean isStartEvent) {
			this.timeOfTrigger = timeOfTrigger;
			this.isStartEvent = isStartEvent;
		}
	}

	private void populateEventsList() {
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
		ContentResolver cr = appContext.getContentResolver();
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

	/**
	 * Get next event from event list and set AlarmManager to send intent alarm.
	 */
	public static void notifyEventReceived() {
		mgr.setAlarmForNextEvent();
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
		long timeOfTrigger = event.timeOfTrigger;
		boolean eventypeStart = event.isStartEvent;
		Intent alarmIntent;
		if (eventypeStart)
			alarmIntent = new Intent(ALARM_INTENT_START_ACTION);
		else
			alarmIntent = new Intent(ALARM_INTENT_STOP_ACTION);

		pendingIntent = PendingIntent.getBroadcast(this.appContext, 0,
				alarmIntent, 0);
		alarmManager.set(AlarmManager.RTC, timeOfTrigger, pendingIntent);
		Log.d(LOGTAG, "Added new event to alarmManager");
	}
}
