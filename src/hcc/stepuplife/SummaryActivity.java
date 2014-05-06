package hcc.stepuplife;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SummaryActivity extends Activity implements OnClickListener {

	private static final String LOGTAG = "hcc.stepuplife.notificationactivity";
	private static final String STOP_HOME_ACTIVITY_INTENT = "hcc.stepuplife.homeclose";
	private static final String GOAL_REACHED_CONGRATS_MSG = "You reached your goal !!!";
	private static final String WAY_TO_GO_MSG = "Try harder tomorrow !";
	private static final String DOING_GREAT_MSG = "You are doing great !!!";
	private UserStats mStats;
	private boolean mLaunchedFromService = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_summary);

		// Intent startIntent = new Intent(SummaryActivity.this,
		// StepUpLifeService.class);
		// bindService(startIntent, mConnection, Context.BIND_AUTO_CREATE);

		// for (int buttonId : buttonIds) {
		// Button b = ((Button) findViewById(buttonId));
		// b.setOnClickListener(this);
		// }
		FrameLayout layout = (FrameLayout) findViewById(R.id.summaryFrameLayout);
		layout.setBackgroundResource(StepUpLifeUtils.getBgImage());
		mLaunchedFromService = getIntent()
				.getBooleanExtra("fromService", false);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mStats = UserStats.loadActivityStats(this);
		if (mStats == null) {
			mStats = new UserStats();
		}
		TextView t;

		t = (TextView) findViewById(R.id.calories);
		t.setText(String.valueOf(mStats.getCaloriesBurnt()) + " / "
				+ String.valueOf(UserStats.TARGET_CALORIES_BURNT));

		t = (TextView) findViewById(R.id.cancel);
		t.setText(String.valueOf(mStats.getCancelCount()));

		t = (TextView) findViewById(R.id.pushups);
		t.setText(String.valueOf(mStats.getPushupsCount()));

		t = (TextView) findViewById(R.id.lunges);
		t.setText(String.valueOf(mStats.getlungesCount()));

		Button b = (Button) findViewById(R.id.buttonClose);
		b.setOnClickListener(this);

		ImageView finalTreeImageView = (ImageView) findViewById(R.id.finalTreeImageView);
		finalTreeImageView.setImageResource(UserStats.ProgressTree
				.getTreeImageId(mStats.getProgressTree()));
		t = (TextView) findViewById(R.id.summaryText);
		if (mStats.isGoalReached())
			t.setText(GOAL_REACHED_CONGRATS_MSG);
		else {
			if (mStats.getPercentageGoalReached() <= 50)
				t.setText(WAY_TO_GO_MSG);
			else
				t.setText(DOING_GREAT_MSG);
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)
				|| keyCode == KeyEvent.KEYCODE_HOME) {
			doFinish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void doFinish() {
		if (mLaunchedFromService) {
			stopService(new Intent(SummaryActivity.this,
					StepUpLifeService.class));
			Log.d("A/Home", "stopping service");
			Intent intent = new Intent(STOP_HOME_ACTIVITY_INTENT);
			sendBroadcast(intent);
		}
		finish();
	}

	int buttonIds[] = { R.id.buttonClose };

	// private StepUpLifeService stepUpLifeService;
	// private ServiceConnection mConnection = new ServiceConnection() {
	//
	// @Override
	// public void onServiceConnected(ComponentName name, IBinder service) {
	// stepUpLifeService = ((StepUpLifeService.StepUpLifeServiceBinder) service)
	// .getService();
	// updateViews();
	// }
	//
	// private void updateViews() {
	// //Take all summary values from service and update views.
	//
	// }
	//
	// @Override
	// public void onServiceDisconnected(ComponentName name) {
	// stepUpLifeService = null;
	//
	// }
	//
	// };

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
		case R.id.buttonClose:
			doFinish();
			break;
		default:
			break;
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// unbindService(mConnection);
	}
}
