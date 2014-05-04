package hcc.stepuplife;

import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;

public class UserStats {

	public enum ProgressTree {
		SAPLING, PLANT, BUSH, TREE, BIGTREE;
		public static int getTreeImageId(ProgressTree tree) {
			switch (tree) {
			case SAPLING:
				return R.drawable.oak_tree_1;
			case BUSH:
				return R.drawable.oak_tree_2;
			case PLANT:
				return R.drawable.oak_tree_3;
			case TREE:
				return R.drawable.oak_tree_4;
			case BIGTREE:
				return R.drawable.oak_tree_6;
			default:
				return R.drawable.stepuplife;
			}

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

	private static final int TARGET_STEPCOUNT = 100;

	private static final String TODAY_STATS_AVAILABLE = "areTodayStatsAvailable";

	private static SharedPreferences mSettings;

	public UserStats() {
		mStepCount = mCancelCount = 0;
		mCaloriesBurnt = 0f;
		progressTree = ProgressTree.SAPLING;
		date = new Date(System.currentTimeMillis());
	}

	public int getStepCount() {
		return mStepCount;
	}

	private int isBetween(double percentage) {
		if (percentage >= 0 && percentage <= 20)
			return 0;
		if (percentage > 20 && percentage <= 40)
			return 1;
		if (percentage > 40 && percentage <= 60)
			return 2;
		if (percentage > 60 && percentage <= 80)
			return 3;
		if (percentage >= 80)
			return 4;
		return 0;
	}

	public void setStepCount(int stepCount) {
		mStepCount = stepCount;
		switch (isBetween(mStepCount * 1.0 / TARGET_STEPCOUNT)) {
		case 0:
			progressTree = ProgressTree.SAPLING;
			break;
		case 1:
			progressTree = ProgressTree.BUSH;
			break;
		case 2:
			progressTree = ProgressTree.PLANT;
			break;
		case 3:
			progressTree = ProgressTree.TREE;
			break;
		case 4:
			progressTree = ProgressTree.BIGTREE;
			break;
		}
		updateCaloriesBurnt();
	}

	private void updateCaloriesBurnt() {
		// TODO Auto-generated method stub

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

	public void incrementCancelCount() {
		this.mCancelCount++;
	}

	public static UserStats loadActivityStats(Context context) {
		mSettings = context.getSharedPreferences(PREFS_NAME, 0);
		if (!mSettings.getBoolean(TODAY_STATS_AVAILABLE, false))
			return null;
		UserStats todayStats = new UserStats();
		todayStats.date = new Date(mSettings.getLong(STATS_DATE_KEY, 0L));
		todayStats.mCaloriesBurnt = mSettings.getFloat(STATS_CALORIES_KEY, 0);
		todayStats.mStepCount = mSettings.getInt(STATS_STEPCOUNT_KEY, 0);
		todayStats.mCancelCount = mSettings.getInt(STATS_CANCELCOUNT_KEY, 0);
		todayStats.progressTree = ProgressTree.values()[(mSettings.getInt(
				STATS_PROGRESS_TREE_KEY, ProgressTree.SAPLING.ordinal()))];

		return todayStats;
	}

	public static boolean saveActivityStats(Context context, UserStats stats) {
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
