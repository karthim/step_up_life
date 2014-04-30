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

				Log.d("A/Home", "starting service");
				b.setText(STOP_TEXT);
			} else {
				stopService(new Intent(Home.this, StepUpLifeService.class));
				Log.d("A/Home", "stopping service");
				b.setText(START_TEXT);
			}
			break;
		default:
			break;
		}
	}

}
