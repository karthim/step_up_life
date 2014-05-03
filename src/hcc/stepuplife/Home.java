package hcc.stepuplife;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import hcc.stepuplife.DetectionRemover;
import hcc.stepuplife.DetectionRequester;
import hcc.stepuplife.ActivityUtils;
import hcc.stepuplife.R;

import com.google.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import hcc.stepuplife.ActivityUtils.REQUEST_TYPE;

import java.io.IOException;
import java.util.List;

public class Home extends Activity implements ActionBar.OnNavigationListener,
		OnClickListener {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	public static final String PREFS_NAME = "stepuplifePrefs";
	private SharedPreferences settings;
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	private final String START_TEXT = "Start !";
	private final String STOP_TEXT = "Stop !";
	private REQUEST_TYPE mRequestType;
	
	public static final String SNOOZE = "hcc.stepuplife.snooze";
	
	/*
	 * Holds activity recognition data, in the form of strings that can contain
	 * markup
	 */
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

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			stepUpLifeService = ((StepUpLifeService.StepUpLifeServiceBinder) service)
					.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			stepUpLifeService = null;
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		// Set up the action bar to show a dropdown list.
		/*
		 * final ActionBar actionBar = getActionBar();
		 * actionBar.setDisplayShowTitleEnabled(false);
		 * actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		 * 
		 * // Set up the dropdown list navigation in the action bar.
		 * actionBar.setListNavigationCallbacks( // Specify a SpinnerAdapter to
		 * populate the dropdown list. new
		 * ArrayAdapter<String>(actionBar.getThemedContext(),
		 * android.R.layout.simple_list_item_1, android.R.id.text1, new String[]
		 * { getString(R.string.title_section1),
		 * getString(R.string.title_section2),
		 * getString(R.string.title_section3), }), this);
		 */

		// Button b = ((Button) findViewById(R.id.buttonStart));
		// b.setOnClickListener(this);

		settings = getSharedPreferences(PREFS_NAME, 0);
		Button b = ((Button) findViewById(R.id.buttonStart));
		if (b == null)
			Log.d("INFO", "Button start is null");
		else
			Log.d("INFO", "Button start is not null");
		if (settings.getBoolean("serviceRunning", false)) {
			// button should display start
			Log.d("INFO", "Service running");
			b.setText(STOP_TEXT);
		} else {
			// button should display stop
			Log.d("INFO", "Service not running");
			b.setText(START_TEXT);
		}
		b.setOnClickListener(this);
		// CalendarEventManager.init(this);*/

		// Activity Recognition
		// Set the broadcast receiver intent filer
		mBroadcastManager = LocalBroadcastManager.getInstance(this);

		// Create a new Intent filter for the broadcast receiver
		mBroadcastFilter = new IntentFilter(Home.SNOOZE);

		// Get detection requester and remover objects
		mDetectionRequester = new DetectionRequester(this);
		mDetectionRemover = new DetectionRemover(this);
		
		IntentFilter gotSnoozeIntentFiler = new IntentFilter(
				SNOOZE);
		registerReceiver(snoozereceiver, gotSnoozeIntentFiler);

	}

	/*
	 * Handle results returned to this Activity by other Activities started with
	 * startActivityForResult(). In particular, the method onConnectionFailed()
	 * in DetectionRemover and DetectionRequester may call
	 * startResolutionForResult() to start an Activity that handles Google Play
	 * services problems. The result of this call returns here, to
	 * onActivityResult.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {

		// Choose what to do based on the request code
		switch (requestCode) {

		// If the request code matches the code sent in onConnectionFailed
		case ActivityUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST:

			switch (resultCode) {
			// If Google Play services resolved the problem
			case Activity.RESULT_OK:

				// If the request was to start activity recognition updates
				if (ActivityUtils.REQUEST_TYPE.ADD == mRequestType) {

					// Restart the process of requesting activity recognition
					// updates
					mDetectionRequester.requestUpdates();

					// If the request was to remove activity recognition updates
				} else if (ActivityUtils.REQUEST_TYPE.REMOVE == mRequestType) {

					/*
					 * Restart the removal of all activity recognition updates
					 * for the PendingIntent.
					 */
					mDetectionRemover.removeUpdates(mDetectionRequester
							.getRequestPendingIntent());

				}
				break;

			// If any other result was returned by Google Play services
			default:

				// Report that Google Play services was unable to resolve the
				// problem.
				// Log.d(ActivityUtils.APPTAG,
				// getString(R.string.no_resolution));
			}

			// If any other request code was received
		default:
			// Report that this Activity received an unknown requestCode
			// Log.d(ActivityUtils.APPTAG,
			// getString(R.string.unknown_activity_request_code, requestCode));
			break;
		}
	}

	/*
	 * Register the broadcast receiver and update the log of activity updates
	 */
	@Override
	protected void onResume() {
		super.onResume();

		// Register the broadcast receiver
		//mBroadcastManager.registerReceiver(snoozeActivity, mBroadcastFilter);


	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current dropdown position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		// When the given dropdown item is selected, show its contents in the
		// container view.
		getFragmentManager()
				.beginTransaction()
				.replace(R.id.container,
						PlaceholderFragment.newInstance(position + 1)).commit();
		return true;
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_home, container,
					false);
			return rootView;
		}
	}

	/**
	 * Verify that Google Play services is available before making a request.
	 * 
	 * @return true if Google Play services is available, otherwise false
	 */
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
			GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0).show();
			return false;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Button b = (Button) v;
		switch (v.getId()) {

		case R.id.buttonStart:
			String toStart = (String) b.getText();
			if (toStart.compareTo(START_TEXT) == 0) {

				Intent startIntent = new Intent(Home.this,
						StepUpLifeService.class);
				startIntent.putExtra("start_monitoring", true);
				startService(startIntent);

				onStartUpdates(v);

				Log.d("A/Home", "starting service");
				b.setText(STOP_TEXT);

			} else {
				stopService(new Intent(Home.this, StepUpLifeService.class));
				Log.d("A/Home", "stopping service");
				b.setText(START_TEXT);
				onStopUpdates(v);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Respond to "Start" button by requesting activity recognition updates.
	 * 
	 * @param view
	 *            The view that triggered this method.
	 */
	public void onStartUpdates(View view) {

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

	/**
	 * Respond to "Stop" button by canceling updates.
	 * 
	 * @param view
	 *            The view that triggered this method.
	 */
	public void onStopUpdates(View view) {

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

	private BroadcastReceiver snoozereceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			Log.d("A/Home", "snoozeActivity");
			onStopUpdates(null);
		}
	};
}

