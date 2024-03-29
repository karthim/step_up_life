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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NotificationActivity extends Activity implements OnClickListener {

	private static final String LOGTAG = "hcc.stepuplife.notificationactivity";
	private UserStats mStats;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification);
		LinearLayout layout = (LinearLayout) findViewById(R.id.notificationllayout);
		layout.setBackgroundResource(StepUpLifeUtils.getBgImage());
		getActionBar().setTitle(R.string.app_name);

		Intent startIntent = new Intent(NotificationActivity.this,
				StepUpLifeService.class);
		bindService(startIntent, mConnection, Context.BIND_AUTO_CREATE);

		for (int buttonId : buttonIds) {
			ImageButton b = ((ImageButton) findViewById(buttonId));
			b.setOnClickListener(this);
		}

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)
				|| keyCode == KeyEvent.KEYCODE_HOME) {
			stepUpLifeService.cancelRecommendedExercise();
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		mStats = UserStats.loadActivityStats(this);
		if (mStats == null) {
			mStats = new UserStats();
		}
		mStats.refresh();

		int imageId = UserStats.ProgressTree.getTreeStageImageId(mStats
				.getProgressTree());
		ImageView treeStageImage = (ImageView) findViewById(R.id.oakTreeStageImageView);

		if (treeStageImage != null) {
			treeStageImage.setImageResource(imageId);
			treeStageImage.setOnClickListener(NotificationActivity.this);
		}

		super.onResume();
	}

	int buttonIds[] = { R.id.btn_reminder_cancel, R.id.btn_reminder_snooze,
			R.id.btn_reminder_doit };

	private StepUpLifeService stepUpLifeService;

	protected int mExerciseImageId = R.drawable.pushups_anim;
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			stepUpLifeService = ((StepUpLifeService.StepUpLifeServiceBinder) service)
					.getService();
			if (stepUpLifeService == null) {
				Log.d(LOGTAG, "stepUpLifeService is null inside mConnection !");
				// Dunno whats going on here, temporary fix
			}
			// ImageView exerciseImage = (ImageView)
			// findViewById(R.id.imageView1);
			GifMovieView animView = (GifMovieView) findViewById(R.id.animView);

			if (animView == null) {
				Log.d(LOGTAG, "animView and treeStageImage are null");
			} else {

				int imageId = stepUpLifeService.getExerciseImageId();
				NotificationActivity.this.mExerciseImageId = imageId;
				// exerciseImage.setImageResource(imageId);
				animView.setMovieResource(imageId);

				String exerciseString = UserStats.ExerciseType
						.getExerciseTypeFromAnimId(imageId).toString();
				TextView t = (TextView) findViewById(R.id.exerciseTypeTextMsg);
				t.setText("Time for " + exerciseString + " !");

			}
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
		// MenuInflater inflater = getMenuInflater();
		// inflater.inflate(R.menu.reminder, menu);
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
			intent.putExtra("fromNotificationScreen", true);
			startActivity(intent);
			return true;
		} else if (id == R.id.action_profile) {
			Intent intent = new Intent(this, CreateProfileActivity.class);
			intent.putExtra("update", true);
			startActivity(intent);
			return true;
		} else if (id == R.id.action_stats) {
			Intent intent = new Intent(this, SummaryActivity.class);
			// intent.putExtra("update", true);
			startActivity(intent);
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
			if (stepUpLifeService.isRunning())
				stepUpLifeService.snoozeExercise();
			else
				Log.d("INFO", "Tried to snooze, but activity not running !");
			finish();
			break;
		case R.id.btn_reminder_cancel:
			if (stepUpLifeService != null)
				stepUpLifeService.cancelRecommendedExercise();
			else
				Log.d("INFO", "stepUpLifeService is null, cannot snooze !");
			finish();
			break;
		case R.id.btn_reminder_doit: // @TODO: What to do ?
			if (stepUpLifeService != null) {
				stepUpLifeService.doExercise(mExerciseImageId);
			} else
				Log.d("INFO", "stepUpLifeService is null, cannot snooze !");
			finish();
			break;
		case R.id.oakTreeStageImageView:
			Intent intent = new Intent(this, SummaryActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unbindService(mConnection);
	}
}
