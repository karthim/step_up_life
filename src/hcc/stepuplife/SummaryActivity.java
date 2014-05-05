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
import android.widget.Button;
import android.widget.ImageView;

public class SummaryActivity extends Activity implements OnClickListener {

	private static final String LOGTAG = "hcc.stepuplife.notificationactivity";
	private UserStats mStats;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification);

//		Intent startIntent = new Intent(SummaryActivity.this,
//				StepUpLifeService.class);
//		bindService(startIntent, mConnection, Context.BIND_AUTO_CREATE);

//		for (int buttonId : buttonIds) {
//			Button b = ((Button) findViewById(buttonId));
//			b.setOnClickListener(this);
//		}
		

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mStats = UserStats.loadActivityStats(this);
		if(mStats == null){
			mStats = new UserStats();
		}
	}

	int buttonIds[] = { };

//	private StepUpLifeService stepUpLifeService;
//	private ServiceConnection mConnection = new ServiceConnection() {
//
//		@Override
//		public void onServiceConnected(ComponentName name, IBinder service) {
//			stepUpLifeService = ((StepUpLifeService.StepUpLifeServiceBinder) service)
//					.getService();
//			updateViews();
//		}
//
//		private void updateViews() {
//			//Take all summary values from service and update views.
//			
//		}
//
//		@Override
//		public void onServiceDisconnected(ComponentName name) {
//			stepUpLifeService = null;
//			
//		}
//
//	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.reminder, menu);
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
			/*
			 * View rootView = inflater.inflate(R.layout.fragment_reminder,
			 * container, false); return rootView;
			 */
			return null;
		}
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		// Consider using startActivityWithResult() from service

		switch (v.getId()) {
		case R.id.btn_reminder_snooze:
			break;
		case R.id.btn_reminder_cancel:
			break;
		case R.id.btn_reminder_doit: // @TODO: What to do ?
			break;
		default:
			break;
		}
		finish();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
//		unbindService(mConnection);
	}
}