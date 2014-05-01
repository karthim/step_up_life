package hcc.stepuplife;

import java.util.ArrayList;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.util.Log;

/**
 * @author devj1988 CalendarEventManager reads the user's calendar given the
 *         gmail id and creates a list of intents which are registered with
 *         AlarmManager to be fired at start and end of meetings.
 */
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
		this.alarmManager = (AlarmManager) appContext
				.getSystemService(Context.ALARM_SERVICE);
	}

	/**
	 * @param Application
	 *            context
	 * 
	 */
	public static void init(Context c) {
		if (mgr == null) {
			mgr = new CalendarEventManager(c);
			mgr.populateEventsList();
			mgr.setAlarmForNextEvent();
		}
	}

	public static void repopulateEventsList() {
		if (mgr == null) {
			Log.d(LOGTAG, "Need to init first !");
			return;
		}
		mgr.populateEventsList();
	}

	/**
	 * @author devj1988 Contains calendar event time of trigger.
	 */
	class IntentTriggerEvent {
		public long timeOfTrigger; // RTC time since epoch
		public boolean isStartEvent;

		public IntentTriggerEvent(long timeOfTrigger, boolean isStartEvent) {
			this.timeOfTrigger = timeOfTrigger;
			this.isStartEvent = isStartEvent;
		}
	}

	/**
	 * populate events list from the calendar
	 */
	private void populateEventsList() {
		// Query Calendar Provider, and populate today's events in events list.
		events = new ArrayList<IntentTriggerEvent>();
		String userGmailID = "androidflair@gmail.com";
		String[] EVENT_PROJECTION = new String[] { Calendars._ID, // 0
				Calendars.ACCOUNT_NAME, // 1
				Calendars.CALENDAR_DISPLAY_NAME, // 2
				Calendars.OWNER_ACCOUNT // 3
		};

		Cursor cur = null;
		ContentResolver cr = appContext.getContentResolver();
		assert cr != null;
		Uri uri = Calendars.CONTENT_URI;

		// Find a calendar
		String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND ("
				+ Calendars.ACCOUNT_TYPE + " = ?) AND ("
				+ Calendars.OWNER_ACCOUNT + " = ?))";
		String[] selectionArgs = new String[] { userGmailID, "com.google",
				userGmailID };

		int PROJECTION_ID_INDEX = 0;
		int PROJECTION_ACCOUNT_NAME_INDEX = 1;
		int PROJECTION_DISPLAY_NAME_INDEX = 2;
		int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

		// // Submit the query and get a Cursor object back.
		Long calID = -1L;
		cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
		assert cur != null;
		while (cur.moveToNext()) {
			// Get the calendar id
			calID = cur.getLong(PROJECTION_ID_INDEX);
		}
		if (calID == -1) {
			Log.d(LOGTAG, "No calID for given gmail id found ");
			return;
		}

		Long now = System.currentTimeMillis();
		Long twelveHoursFromNow = now + 3600 * 12 * 1000;
		EVENT_PROJECTION = new String[] { Events.CALENDAR_ID, Events.DTSTART, // 0
				Events.DTEND // 3
		};

		int PROJECTION_STARTTIME_INDEX = 1;
		int PROJECTION_STOPTIME_INDEX = 2;

		uri = Events.CONTENT_URI;
		selection = "((" + Events.CALENDAR_ID + " = ?) AND (" + Events.DTSTART
				+ " > ?) AND (" + Events.DTEND + " < ?))";
		selectionArgs = new String[] { calID.toString(), now.toString(),
				twelveHoursFromNow.toString() };

		// selection = "(("+Events.CALENDAR_ID + " = ?))";
		// selectionArgs = new String[] { calID.toString() };

		// Submit the query and get a Cursor object back.
		cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
		assert cur != null;
		long startTime = 0;
		long stopTime = 0;
		// Note, assuming there are no overlapping events

		// DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		// format.setTimeZone(TimeZone.getTimeZone("UTC-6:00"));

		while (cur.moveToNext()) {
			// Get the field values
			startTime = cur.getLong(PROJECTION_STARTTIME_INDEX);
			stopTime = cur.getLong(PROJECTION_STOPTIME_INDEX);
			// Log.d(LOGTAG,
			// "Got event with start time = "
			// + format.format(new Date(startTime))
			// + "and end time = , "
			// + format.format(new Date(stopTime)));

			events.add(new IntentTriggerEvent(startTime, true));
			events.add(new IntentTriggerEvent(stopTime, false));
			// Do something with the values...
		}
		if (events.isEmpty()) {
			Log.d(LOGTAG, "No events in next 12 hours!");
		} else
			Log.d(LOGTAG, "Added all events !");
	}

	/**
	 * Get next event from event list and set AlarmManager to send intent alarm.
	 */
	public static void notifyEventReceived() {
		mgr.setAlarmForNextEvent();
	}

	public static void release() {
		if (mgr != null) {
			mgr.events.clear();
			mgr.events = null;
			mgr = null;
		}
		Log.d(LOGTAG, "freed calendar manager resource");
	}

	private boolean setAlarmForNextEvent() {
		if (events == null) {
			Log.d(LOGTAG, "Events list is null !");
			return false;
		}
		if (events.isEmpty()) {
			Log.d(LOGTAG, "No more events in today's calendar");
			return false;
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
		return true;
	}
}
