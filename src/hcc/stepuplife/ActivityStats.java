package hcc.stepuplife;

import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;

public class ActivityStats {

	public enum ProgressTree {
		SAPLING, PLANT, BUSH, TREE, BIGTREE;
		public static int getTreeImageId(ProgressTree tree) {
			switch (tree) {
			case SAPLING:
				return R.drawable.stepuplife;
			case PLANT:
				return R.drawable.stepuplife;
			case BUSH:
				return R.drawable.stepuplife;
			case TREE:
				break;
			case BIGTREE:
				return R.drawable.stepuplife;
			default:
				return R.drawable.stepuplife;
			}
			return R.drawable.stepuplife;
		}

	}

	private Date date;
	private int mStepCount;
	private float mCaloriesBurnt;
	private int mCancelCount;
	private ProgressTree progressTree;

	public static final String PREFS_NAME = "stepuplifePrefs";
	private static final String STATS_DATE_KEY = "todayStatsDate";
	private static final String STATS_CALORIES_KEY = "todayStatsCalories";
	private static final String STATS_STEPCOUNT_KEY = "todayStatsStepCount";
	private static final String STATS_PROGRESS_TREE_KEY = "todayStatsTreeOrdinal";
	private static final String STATS_CANCELCOUNT_KEY = "todayStatsStepCount";

	private static final String TODAY_STATS_AVAILABLE = "areTodayStatsAvailable";

	private static SharedPreferences mSettings;

	public ActivityStats(){
		mStepCount = mCancelCount = 0;
		mCaloriesBurnt = 0f;
		progressTree = ProgressTree.SAPLING;
		date = new Date(System.currentTimeMillis());
	}
	public int getStepCount() {
		return mStepCount;
	}

	public void setStepCount(int mStepCount) {
		this.mStepCount = mStepCount;
	}

	public float getCaloriesBurnt() {
		return mCaloriesBurnt;
	}

	public void setCaloriesBurnt(float mCaloriesBurnt) {
		this.mCaloriesBurnt = mCaloriesBurnt;
	}

	public int getcancelCount() {
		return mCancelCount;
	}

	public void setcancelCount(int mcancelCount) {
		this.mCancelCount = mcancelCount;
	}

	public ProgressTree getProgressTree() {
		return progressTree;
	}

	public void setProgressTree(ProgressTree progressTree) {
		this.progressTree = progressTree;
	}
	
	public void incrementCancelCount(){
		this.mCancelCount++;
	}

	public static ActivityStats loadActivityStats(Context context) {
		mSettings = context.getSharedPreferences(PREFS_NAME, 0);
		if (!mSettings.getBoolean(TODAY_STATS_AVAILABLE, false))
			return null;
		ActivityStats todayStats = new ActivityStats();
		todayStats.date = new Date(mSettings.getLong(STATS_DATE_KEY, 0L));
		todayStats.mCaloriesBurnt = mSettings.getFloat(STATS_CALORIES_KEY, 0);
		todayStats.mStepCount = mSettings.getInt(STATS_STEPCOUNT_KEY, 0);
		todayStats.mCancelCount = mSettings.getInt(STATS_CANCELCOUNT_KEY, 0);
		todayStats.progressTree = ProgressTree.values()[(mSettings.getInt(
				STATS_PROGRESS_TREE_KEY, ProgressTree.SAPLING.ordinal()))];

		return todayStats;
	}

	public static boolean saveActivityStats(Context context, ActivityStats stats) {
		mSettings = context.getSharedPreferences(PREFS_NAME, 0);

		mSettings.edit().putBoolean(TODAY_STATS_AVAILABLE, true);
		
		mSettings.edit().putLong(STATS_DATE_KEY, stats.date.getTime());
		mSettings.edit().putFloat(STATS_CALORIES_KEY, stats.getCaloriesBurnt());
		mSettings.edit().putInt(STATS_STEPCOUNT_KEY, stats.getStepCount());
		mSettings.edit().putInt(STATS_CANCELCOUNT_KEY, stats.getcancelCount());
		mSettings.edit().putInt(STATS_PROGRESS_TREE_KEY,
				stats.getProgressTree().ordinal());

		return true;
	}

}
