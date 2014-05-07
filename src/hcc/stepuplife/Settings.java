package hcc.stepuplife;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Settings extends Activity {

	private SharedPreferences settings;
	public static final String PREFS_NAME = "stepuplifePrefs";
	public static final String IDLE_TIME = "idletime";
	public static final String SNOOZE_TIME = "snoozetime";
	public static final String TARGET_CALORIES_KEY = "targetcalorieskey";

	private boolean mFromNotifScreen = false;
	UserStats mStats;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		getActionBar().setTitle(
				getResources().getString(R.string.app_name) + " Settings");

		LinearLayout layout = (LinearLayout) findViewById(R.id.settingRootLayout);
		layout.setBackgroundResource(StepUpLifeUtils.getBgImage());
		settings = getSharedPreferences(PREFS_NAME, 0);

		EditText editTextTargetCal = (EditText) findViewById(R.id.editTextTargetCal);
		editTextTargetCal
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					public boolean onEditorAction(TextView exampleView,
							int actionId, KeyEvent event) {
						if (actionId == EditorInfo.IME_NULL
								&& event.getAction() == KeyEvent.FLAG_EDITOR_ACTION) {
							ImageButton b = ((ImageButton) findViewById(R.id.buttonSetting));
							b.performClick();
						}
						return true;
					}
				});
	}

	protected void onResume() {

		int idleTime = 0;
		int snoozeTime = 0;

		mStats = UserStats.loadActivityStats(this);
		if (mStats == null) {
			mStats = new UserStats();
		}
		mStats.refresh();

		EditText editTextidletime = (EditText) findViewById(R.id.editTextidle);
		idleTime = settings.getInt(IDLE_TIME,
				StepUpLifeService.IDLE_TIMEOUT_MIN);
		editTextidletime.setText(String.valueOf(idleTime));

		EditText editTextsnooze = (EditText) findViewById(R.id.editTextsnooze);
		snoozeTime = settings.getInt(SNOOZE_TIME, StepUpLifeService.SNOOZE_MIN);
		editTextsnooze.setText(String.valueOf(snoozeTime));

		EditText targetCalories = (EditText) findViewById(R.id.editTextTargetCal);
		int targetCal = settings.getInt(TARGET_CALORIES_KEY,
				UserStats.TARGET_CALORIES_BURNT);
		targetCalories.setText(String.valueOf(targetCal));

		mFromNotifScreen = getIntent().getBooleanExtra(
				"fromNotificationScreen", false);
		Log.d("INFO", "mFromNotifScreen is " + mFromNotifScreen);
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.settings, menu);
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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_settings,
					container, false);
			return rootView;
		}
	}

	public void updateSharedPref(View view) {

		EditText editTextidletime = (EditText) findViewById(R.id.editTextidle);
		int idletime = Integer.parseInt(editTextidletime.getText().toString());

		EditText editTextsnoozetime = (EditText) findViewById(R.id.editTextsnooze);
		int snoozetime = Integer.parseInt(editTextsnoozetime.getText()
				.toString());

		EditText targetCalories = (EditText) findViewById(R.id.editTextTargetCal);
		int targetCal = Integer.parseInt(targetCalories.getText().toString());

		settings.edit().putInt(IDLE_TIME, idletime).commit();
		settings.edit().putInt(SNOOZE_TIME, snoozetime).commit();
		settings.edit().putInt(TARGET_CALORIES_KEY, targetCal).commit();
		mStats.refresh();

		Intent intentSettingsUpdate = new Intent(
				StepUpLifeService.UPDATE_SETTINGS);
		if (mFromNotifScreen)
			intentSettingsUpdate.putExtra("doNotRestart", true);
		sendBroadcast(intentSettingsUpdate);
		StepUpLifeUtils.showToast(this, "Settings updated...");
		finish();

	}

}
