package hcc.stepuplife;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class ReminderActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reminder);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		Intent startIntent = new Intent(ReminderActivity.this,
				StepUpLifeService.class);
		bindService(startIntent, mConnection, Context.BIND_AUTO_CREATE);
	}

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
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.reminder, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_reminder,
					container, false);
			return rootView;
		}
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_reminder_snooze:
			if (stepUpLifeService.isRunning())
				stepUpLifeService.snoozeActivity();
			else
				Log.d("INFO", "Tried to snooze, but activity not running !");
			break;
		case R.id.btn_reminder_cancel:
			if (stepUpLifeService.isRunning())
				stepUpLifeService.cancelRecommendedExercise();
			else
				Log.d("INFO", "Tried to snooze, but activity not running !");
			break;
		case R.id.btn_reminder_doit:
			//@TODO: What to do ?
			if (stepUpLifeService.isRunning())
				stepUpLifeService.snoozeActivity();
			else
				Log.d("INFO", "Tried to snooze, but activity not running !");
			break;
		default:
			break;
		}
	}
}
