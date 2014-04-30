package hcc.stepuplife;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class StepUpLifeService extends Service {

	public static final String PREFS_NAME = "MyPrefsFile";
	SharedPreferences settings;

	public enum ServiceState {
		STARTED, STOPPED, RUNNING_MONITORING, RUNNING_NOT_MONITORING, SNOOZE;

		public String toString() {
			switch (this) {
			case STARTED:
				return "StepUpLifeService started";
			case STOPPED:
				return "StepUpLifeService started";
			case RUNNING_MONITORING:
				return "StepUpLifeService started";
			case RUNNING_NOT_MONITORING:
				return "StepUpLifeService started";
			case SNOOZE:
				return "StepUpLifeService started";
			default:
				return "undefined";
			}
		}
	}

	private ServiceState serviceState;

	public boolean isRunning() {
		switch (this.serviceState) {
		case STARTED:
		case RUNNING_MONITORING:
		case RUNNING_NOT_MONITORING:
			return true;
		case SNOOZE:
		case STOPPED:
			return false;
		default:
			return false;
		}
	}

	public StepUpLifeService() {
		serviceState = ServiceState.STOPPED;
	}

	private final IBinder stepUpLifeServiceBinder = new StepUpLifeServiceBinder();

	public class StepUpLifeServiceBinder extends Binder {
		StepUpLifeService getService() {
			return StepUpLifeService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		return stepUpLifeServiceBinder;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		settings = getSharedPreferences(PREFS_NAME, 0);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (intent == null)
			Log.d("INFO", "restarted service Step Up Life");
		else
			Log.d("INFO", "started service Step Up Life");
		settings.edit().putBoolean("serviceRunning", true);
		serviceState = ServiceState.STARTED;
		if (intent.getBooleanExtra("start_monitoring", false))
			startMonitoringActivity();
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		settings.edit().putBoolean("serviceRunning", false);
		stopMonitoringActivity(true);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}

	@Override
	public void onRebind(Intent intent) {
		// TODO Auto-generated method stub
		super.onRebind(intent);
	}

	public void startMonitoringActivity() {
		// TODO Auto-generated method stub
		settings.edit().putBoolean("monitoring", true);
		serviceState = ServiceState.RUNNING_MONITORING;
	}

	public void stopMonitoringActivity(boolean endService) {
		// TODO Auto-generated method stub
		settings.edit().putBoolean("monitoring", false);
		serviceState = ServiceState.RUNNING_NOT_MONITORING;
		if (endService) {
			// save summary
		}
	}

	public void snoozeActivity() {
		// TODO Auto-generated method stub
		serviceState = ServiceState.SNOOZE;
	}

	public void getExerciseRecco() {
		// TODO Auto-generated method stub
	}

	public void startExercise() {
		// TODO Auto-generated method stub
	}

	private void updateCancelCounter() {
		// TODO Auto-generated method stub
	}

	public void cancelRecommendedExercise() {
		// TODO Auto-generated method stub
		updateCancelCounter();
		startMonitoringActivity();
	}
}
