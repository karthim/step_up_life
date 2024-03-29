package hcc.stepuplife;

import hcc.stepuplife.ActivityUtils.REQUEST_TYPE;

import java.lang.ref.WeakReference;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Home extends Activity implements ActionBar.OnNavigationListener,
		OnClickListener {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	public static final String PREFS_NAME = "stepuplifePrefs";
	private SharedPreferences settings;
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	private static final String GOOD_MORNING_MSG = "Good Morning ! Ready to start ?";
	private static final String GOOD_AFTERNOON_MSG = "How is the afternoon going ?";
	private static final String GOOD_EVENING_MSG = "Having a great evening ?";

	private static final String START_TEXT = "Start !";
	private static final String STOP_TEXT = "Stop !";
	private static final String CREATE_PROFILE_TEXT = "\"Create Profile\"";
	private REQUEST_TYPE mRequestType;

	public static final String SNOOZE = "hcc.stepuplife.snooze";
	private static final CharSequence CREATE_PROFILE_MSG = "Ready to create your profile ?";
	private static final int EVENING_THRESHOLD_PM = 18;
	private static final int NOON_PM = 12;

	public static final String STOP_HOME_ACTIVITY_INTENT = "hcc.stepuplife.homeclose";
	private static final int MORNING_THRESHOLD = 6;
	private static final int REQUEST_CODE = 0;
	private boolean mProfileCreated = false;

	private ImageButton mButton;

	private void updateTextView(boolean profileNotCreated, boolean isStarted) {

		TextView greetText = (TextView) findViewById(R.id.greetUser);
		if (profileNotCreated) {
			greetText.setText(CREATE_PROFILE_MSG);
			return;
		}
		if (isStarted)
			greetText.setText("\"Press Stop to stop monitoring activity\"");
		else
			greetText.setText("\"Press Start to start monitoring activity\"");

	}

	/*
	 * Holds activity recognition data, in the form of strings that can contain
	 * markup
	 */
	// private ArrayAdapter<Spanned> mStatusAdapter;
	/*
	 * Intent filter for incoming broadcasts from the IntentService.
	 */
	// IntentFilter mBroadcastFilter;
	// Instance of a local broadcast manager
	// private LocalBroadcastManager mBroadcastManager;

	// The activity recognition update request object
	// private DetectionRequester mDetectionRequester;

	// The activity recognition update removal object
	// private DetectionRemover mDetectionRemover;
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

	private BroadcastReceiver homeReceiver = new BroadcastReceiver() {
		private final String LOGTAG = "B/stepUpLifeReceiver";

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(STOP_HOME_ACTIVITY_INTENT)) {
				finish();
			} else if (intent.getAction().equals(
					StepUpLifeService.SERVICE_SHUTDOWN)) {
				if (Home.this.mButton != null) {
					Home.this.updateTextView(false, false);
					Home.this.mButton.setBackgroundResource(R.drawable.green);
					Home.this.mButton.setImageResource(R.drawable.start_icon);
				}
			}
		}
	};

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)
				|| keyCode == KeyEvent.KEYCODE_HOME) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		LinearLayout layout = (LinearLayout) findViewById(R.id.LinearLayout1);
		layout.setBackgroundResource(StepUpLifeUtils.getBgImage());

		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

		settings = getSharedPreferences(PREFS_NAME, 0);
		IntentFilter iFilter = new IntentFilter(STOP_HOME_ACTIVITY_INTENT);
		registerReceiver(homeReceiver, iFilter);
		iFilter = new IntentFilter(StepUpLifeService.SERVICE_SHUTDOWN);
		registerReceiver(homeReceiver, iFilter);
		mButton = ((ImageButton) findViewById(R.id.buttonStart));

	}

	/*
	 * Register the broadcast receiver and update the log of activity updates
	 */
	protected void onResume() {
		super.onResume();
		//
		ImageButton b = ((ImageButton) findViewById(R.id.buttonStart));
		if (b == null)
			Log.d("INFO", "Button start is null");
		else
			Log.d("INFO", "Button start is not null");
		if (UserProfile.isUserProfileCreated(this)) {
			mProfileCreated = true;
			Log.d("INFO", "User profile exists");
			// @TODO: Change this; call bindService in onCreate and call
			// isRunning
			if (settings.getBoolean("serviceRunning", false)) {
				updateTextView(false, true);
				// button should display start
				Log.d("INFO", "Service running");
				b.setTag(STOP_TEXT);
				b.setImageResource(R.drawable.stop_icon);
				b.setBackgroundResource(R.drawable.red);
			} else {
				// button should display stop
				Log.d("INFO", "Service not running");
				updateTextView(false, false);
				b.setTag(START_TEXT);
				b.setImageResource(R.drawable.start_icon);
				b.setBackgroundResource(R.drawable.green);
			}
		} else {
			UserProfile.init(this);
			updateTextView(true, true);
			Log.d("INFO", "User profile not created");
			b.setImageResource(R.drawable.create_profile_icon);
			b.setTag(CREATE_PROFILE_TEXT);
		}

		b.setOnClickListener(this);

		// Register the broadcast receiver
		// mBroadcastManager.registerReceiver(snoozeActivity, mBroadcastFilter);

	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current dropdown position.
		// if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
		// getActionBar().setSelectedNavigationItem(
		// savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		// }
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
		// outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
		// .getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, Settings.class);
			startActivity(intent);
			return true;
		} else if (id == R.id.action_profile) {
			if (mProfileCreated) {
				Intent intent = new Intent(this, CreateProfileActivity.class);
				intent.putExtra("update", true);
				startActivity(intent);
			} else {
				Intent intent = new Intent(this, CreateProfileActivity.class);
				// startActivity(intent);
				startActivityForResult(intent, REQUEST_CODE);
			}
			return true;
		} else if (id == R.id.action_stats) {
			Intent intent = new Intent(this, SummaryActivity.class);
			// intent.putExtra("update", true);
			startActivity(intent);
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
				.replace(R.id.summaryFrameLayout,
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
	// private boolean servicesConnected() {
	//
	// // Check that Google Play services is available
	// int resultCode = GooglePlayServicesUtil
	// .isGooglePlayServicesAvailable(this);
	//
	// // If Google Play services is available
	// if (ConnectionResult.SUCCESS == resultCode) {
	//
	// // In debug mode, log the status
	// // Log.d(ActivityUtils.APPTAG,
	// // getString(R.string.play_services_available));
	//
	// // Continue
	// return true;
	//
	// // Google Play services was not available for some reason
	// } else {
	//
	// // Display an error dialog
	// GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0).show();
	// return false;
	// }
	// }

	// class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
	// private final WeakReference<ImageView> imageViewReference;
	// private int data = 0;
	//
	// public BitmapWorkerTask(ImageView imageView) {
	// // Use a WeakReference to ensure the ImageView can be garbage collected
	// imageViewReference = new WeakReference<ImageView>(imageView);
	// }
	//
	// // Decode image in background.
	// @Override
	// protected Bitmap doInBackground(Integer... params) {
	// data = params[0];
	// return decodeSampledBitmapFromResource(getResources(), data, 100, 100));
	// }
	//
	// // Once complete, see if ImageView is still around and set bitmap.
	// @Override
	// protected void onPostExecute(Bitmap bitmap) {
	// if (imageViewReference != null && bitmap != null) {
	// final ImageView imageView = imageViewReference.get();
	// if (imageView != null) {
	// imageView.setImageBitmap(bitmap);
	// }
	// }
	// }
	// }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		ImageButton b = (ImageButton) v;
		switch (v.getId()) {

		case R.id.buttonStart:
			String toStart = (String) b.getTag();
			if (toStart == null) {
				Log.d("INFO", "No tag set for the imagebutton");
				return;
			}
			if (toStart.compareTo(START_TEXT) == 0) {

				Intent startIntent = new Intent(Home.this,
						StepUpLifeService.class);
				startIntent.putExtra("start_monitoring", true);
				startService(startIntent);

				// onStartUpdates(v);

				Log.d("A/Home", "starting service");
				b.setTag(STOP_TEXT);
				b.setBackgroundResource(R.drawable.red);
				b.setImageResource(R.drawable.stop_icon);
				updateTextView(false, true);

			} else if (toStart.compareTo(STOP_TEXT) == 0) {
				stopService(new Intent(Home.this, StepUpLifeService.class));
				Log.d("A/Home", "stopping service");

				updateTextView(false, false);
				b.setTag(START_TEXT);

				b.setBackgroundResource(R.drawable.green);
				b.setImageResource(R.drawable.start_icon);
				// onStopUpdates(v);
			} else if (toStart.compareTo(CREATE_PROFILE_TEXT) == 0) {
				Intent intent = new Intent(this, CreateProfileActivity.class);
				// startActivity(intent);
				startActivityForResult(intent, REQUEST_CODE);
			}
			break;
		default:
			break;
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				updateTextView(true, false);
				mProfileCreated = true;
			} else
				updateTextView(false, false);
		}

	}

	/**
	 * Respond to "Start" button by requesting activity recognition updates.
	 * 
	 * @param view
	 *            The view that triggered this method.
	 */
	// public void onStartUpdates(View view) {
	//
	// // Check for Google Play services
	// if (!servicesConnected()) {
	//
	// return;
	// }
	//
	// /*
	// * Set the request type. If a connection error occurs, and Google Play
	// * services can handle it, then onActivityResult will use the request
	// * type to retry the request
	// */
	// mRequestType = ActivityUtils.REQUEST_TYPE.ADD;
	//
	// // Pass the update request to the requester object
	// mDetectionRequester.requestUpdates();
	// }

	public void onDestroy() {
		unregisterReceiver(homeReceiver);
		super.onDestroy();
	}
	/**
	 * Respond to "Stop" button by canceling updates.
	 * 
	 * @param view
	 *            The view that triggered this method.
	 */
	// public void onStopUpdates(View view) {
	//
	// // Check for Google Play services
	// if (!servicesConnected()) {
	//
	// return;
	// }
	//
	// /*
	// * Set the request type. If a connection error occurs, and Google Play
	// * services can handle it, then onActivityResult will use the request
	// * type to retry the request
	// */
	// mRequestType = ActivityUtils.REQUEST_TYPE.REMOVE;
	//
	// // Pass the remove request to the remover object
	// mDetectionRemover.removeUpdates(mDetectionRequester
	// .getRequestPendingIntent());
	//
	// /*
	// * Cancel the PendingIntent. Even if the removal request fails,
	// * canceling the PendingIntent will stop the updates.
	// */
	// mDetectionRequester.getRequestPendingIntent().cancel();
	// }
	//
	// private BroadcastReceiver snoozereceiver = new BroadcastReceiver() {
	//
	// @Override
	// public void onReceive(Context arg0, Intent arg1) {
	// Log.d("A/Home", "snoozeActivity");
	// onStopUpdates(null);
	// }
	// };
}
