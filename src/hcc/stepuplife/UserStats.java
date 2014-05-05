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

	public enum ExerciseType {
		PUSHUPS, LUNGES, UNDEFINED;

		public static int getExerciseImageId(ExerciseType ex) {
			switch (ex) {
			case PUSHUPS:
				return R.drawable.exercise1;
			case LUNGES:
				return R.drawable.exercise2;
			default:
				return R.drawable.exercise3;
			}
		}

		public static ExerciseType getExerciseTypeFromImageId(int imageId) {
			switch (imageId) {
			case R.drawable.exercise1:
				return PUSHUPS;
			case R.drawable.exercise2:
				return LUNGES;
			default:
				return UNDEFINED;
			}
		}
	}

	private int mpushupsCount = 0;
	private int mlungesCount = 0;

	private static final int CALORIES_BURNT_PER_PUSHUPS_INSTANCE = 100;
	private static final int CALORIES_BURNT_PER_LUNGES_INSTANCE = 100;

	private Date date;
	private int mStepCount;
	private int mCaloriesBurnt;
	private int mCancelCount;
	private ProgressTree mProgressTree;

	public static final String PREFS_NAME = "stepuplifePrefs";
	private static final String STATS_DATE_KEY = "todayStatsDate";
	private static final String STATS_LUNGES_COUNT_KEY = "todayStatsLunges";
	private static final String STATS_PUSHUPS_COUNT_KEY = "todayStatsPushups";
	private static final String STATS_CALORIES_KEY = "todayStatsCalories";
	private static final String STATS_STEPCOUNT_KEY = "todayStatsStepCount";
	private static final String STATS_PROGRESS_TREE_KEY = "todayStatsTreeOrdinal";
	private static final String STATS_CANCELCOUNT_KEY = "todayStatsStepCount";

	private static final int TARGET_CALORIES_BURNT = 100;

	private static final String TODAY_STATS_AVAILABLE = "areTodayStatsAvailable";

	private static SharedPreferences mSettings;

	public UserStats() {
		mCancelCount = 0;
		mCaloriesBurnt = 0;
		mProgressTree = ProgressTree.SAPLING;
		date = new Date(System.currentTimeMillis());
	}

	// public int getStepCount() {
	// return mStepCount;
	// }

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

	// public void setStepCount(int stepCount) {
	// mStepCount = stepCount;
	// switch (isBetween(mStepCount * 1.0 / TARGET_CALORIES_BURNT)) {
	// case 0:
	// mProgressTree = ProgressTree.SAPLING;
	// break;
	// case 1:
	// mProgressTree = ProgressTree.BUSH;
	// break;
	// case 2:
	// mProgressTree = ProgressTree.PLANT;
	// break;
	// case 3:
	// mProgressTree = ProgressTree.TREE;
	// break;
	// case 4:
	// mProgressTree = ProgressTree.BIGTREE;
	// break;
	// }
	// updateCaloriesBurnt();
	// }

	private void updateCaloriesBurnt() {
		// TODO Auto-generated method stub
		mCaloriesBurnt = mpushupsCount * CALORIES_BURNT_PER_PUSHUPS_INSTANCE
				+ mlungesCount * CALORIES_BURNT_PER_LUNGES_INSTANCE;
		int targetPerCent = getPercentageGoalReached();
		switch (isBetween(targetPerCent)) {
		case 0:
			mProgressTree = ProgressTree.SAPLING;
			break;
		case 1:
			mProgressTree = ProgressTree.BUSH;
			break;
		case 2:
			mProgressTree = ProgressTree.PLANT;
			break;
		case 3:
			mProgressTree = ProgressTree.TREE;
			break;
		case 4:
			mProgressTree = ProgressTree.BIGTREE;
			break;
		}

	}

	public int getPercentageGoalReached() {
		// TODO Auto-generated method stub
		return mCaloriesBurnt / TARGET_CALORIES_BURNT;
	}

	public int getCaloriesBurnt() {
		return mCaloriesBurnt;
	}

	public int getcancelCount() {
		return mCancelCount;
	}

	public void setcancelCount(int mcancelCount) {
		this.mCancelCount = mcancelCount;
	}

	public ProgressTree getProgressTree() {
		return mProgressTree;
	}

	public void setProgressTree(ProgressTree progressTree) {
		this.mProgressTree = progressTree;
	}

	public void incrementCancelCount() {
		this.mCancelCount++;
	}

	public void incrementPushupsCount() {
		this.mlungesCount++;
		updateCaloriesBurnt();
	}

	public void incrementLungesCount() {
		this.mpushupsCount++;
		updateCaloriesBurnt();
	}

	private static UserStats todayStats;

	public static UserStats loadActivityStats(Context context) {
		mSettings = context.getSharedPreferences(PREFS_NAME, 0);
		if (!mSettings.getBoolean(TODAY_STATS_AVAILABLE, false))
			return null;
		if (todayStats == null) {
			todayStats = new UserStats();
			todayStats.date = new Date(mSettings.getLong(STATS_DATE_KEY, 0L));
			todayStats.mCaloriesBurnt = mSettings.getInt(STATS_CALORIES_KEY, 0);
			// todayStats.mStepCount = mSettings.getInt(STATS_STEPCOUNT_KEY, 0);
			todayStats.mCancelCount = mSettings
					.getInt(STATS_CANCELCOUNT_KEY, 0);
			todayStats.mProgressTree = ProgressTree.values()[(mSettings.getInt(
					STATS_PROGRESS_TREE_KEY, ProgressTree.SAPLING.ordinal()))];
			todayStats.mlungesCount = mSettings.getInt(STATS_LUNGES_COUNT_KEY,
					0);
			todayStats.mpushupsCount = mSettings.getInt(
					STATS_PUSHUPS_COUNT_KEY, 0);
		}
		return todayStats;
	}

	public static boolean saveActivityStats(Context context, UserStats stats) {
		mSettings = context.getSharedPreferences(PREFS_NAME, 0);

		mSettings.edit().putBoolean(TODAY_STATS_AVAILABLE, true);

		mSettings.edit().putLong(STATS_DATE_KEY, stats.date.getTime());
		mSettings.edit().putInt(STATS_CALORIES_KEY, stats.getCaloriesBurnt());
		// mSettings.edit().putInt(STATS_STEPCOUNT_KEY, stats.getStepCount());
		mSettings.edit().putInt(STATS_CANCELCOUNT_KEY, stats.getcancelCount());
		mSettings.edit().putInt(STATS_PROGRESS_TREE_KEY,
				stats.getProgressTree().ordinal());
		mSettings.edit().putInt(STATS_LUNGES_COUNT_KEY, stats.mlungesCount);
		mSettings.edit().putInt(STATS_PUSHUPS_COUNT_KEY, stats.mpushupsCount);
		mSettings.edit().apply();

		return true;
	}

}
