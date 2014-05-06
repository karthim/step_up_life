package hcc.stepuplife;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.os.Build;

public class Settings extends Activity {

	private SharedPreferences settings;
	public static final String PREFS_NAME = "stepuplifePrefs";
	public static final String IDLE_TIME = "idletime";
	public static final String SNOOZE_TIME = "snoozetime";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.container);
		layout.setBackgroundResource(R.drawable.blue);
		
		settings = getSharedPreferences(PREFS_NAME, 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
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
    	int snoozetime = Integer.parseInt(editTextsnoozetime.getText().toString());   	
    	
    	settings.edit().putInt(IDLE_TIME, idletime).commit();
    	settings.edit().putInt(SNOOZE_TIME, snoozetime).commit();
    	
    }

}
