package hcc.stepuplife;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class UserProfile {

	private static final String LOGTAG = "UserProfile";

	private static final String PREFS_NAME = "stepuplifePrefs";
	private static final String USER_NAME_SETTING = "userName";
	private static final String USER_AGE_SETTING = "userAge";
	private static final String USER_GENDER_SETTING = "userGender";
	private static final String USER_GMAIL_SETTING = "userGmail";

	private static SharedPreferences settings;
	private static boolean isUserProfileCreated;

	public enum Gender {
		MALE, FEMALE
	}

	public static String getUserName(Context context)
			throws UserProfileNotFoundException {
		if (settings == null) {
			Log.d(LOGTAG, "Call init with Context param first");
			init(context);
			// throw new UserProfileNotFoundException();
		}
		return settings.getString(USER_NAME_SETTING, "");
	}

	public static int getAge(Context context)
			throws UserProfileNotFoundException {
		if (settings == null) {
			Log.d(LOGTAG, "Call init with Context param first");
			init(context);
			// ////throw new UserProfileNotFoundException();
		}
		return settings.getInt(USER_AGE_SETTING, 0);
	}

	public static String getGmailID(Context context)
			throws UserProfileNotFoundException {
		if (settings == null) {
			Log.d(LOGTAG, "Call init with Context param first");
			init(context);
			// throw new UserProfileNotFoundException();
		}

		return settings.getString(USER_GMAIL_SETTING, "");
	}

	public static Gender getGender(Context context)
			throws UserProfileNotFoundException {
		if (settings == null) {
			Log.d(LOGTAG, "Call init with Context param first");
			init(context);
			// throw new UserProfileNotFoundException();
		}
		return (settings.getBoolean(USER_GENDER_SETTING, false)) ? Gender.MALE
				: Gender.FEMALE;
	}

	public static void setUserName(String userName, Context context)
			throws UserProfileNotFoundException {
		if (settings == null) {
			Log.d(LOGTAG, "Call init with Context param first");
			init(context);
			// throw new UserProfileNotFoundException();
		}
		settings.edit().putString(USER_NAME_SETTING, userName.trim()).commit();
	}

	public static void setAge(int age, Context context)
			throws UserProfileNotFoundException {
		if (settings == null) {
			Log.d(LOGTAG, "Call init with Context param first");
			init(context);
			// throw new UserProfileNotFoundException();
		}
		settings.edit().putInt(USER_AGE_SETTING, age).commit();
	}

	public static void setGmailID(String gmailId, Context context)
			throws UserProfileNotFoundException {
		if (settings == null) {
			Log.d(LOGTAG, "Call init with Context param first");
			init(context);
			// throw new UserProfileNotFoundException();
		}
		settings.edit().putString(USER_GMAIL_SETTING, gmailId.trim()).commit();
	}

	public static void setGender(Gender gender, Context context)
			throws UserProfileNotFoundException {
		if (settings == null) {
			Log.d(LOGTAG, "Call init with Context param first");
			init(context);
			// throw new UserProfileNotFoundException();
		}
		settings.edit()
				.putBoolean(USER_GENDER_SETTING,
						(gender == Gender.MALE ? true : false)).commit();
	}

	public static boolean createProfile(String name, int age, String gmailid,
			Gender gender) {
		if (isUserProfileCreated) {
			Log.d(LOGTAG, "User profile exists, use getProfile");
			return false;
		}

		if (settings == null) {
			Log.d(LOGTAG, "Call init with Context param first");
			return false;
		}

		settings.edit().putString(USER_NAME_SETTING, name.trim()).commit();
		settings.edit().putInt(USER_AGE_SETTING, age).commit();
		settings.edit()
				.putBoolean(USER_GENDER_SETTING,
						(gender == Gender.MALE ? true : false)).commit();
		settings.edit().putString(USER_GMAIL_SETTING, gmailid.trim()).commit();

		isUserProfileCreated = true;

		return true;
	}

	public static boolean updateProfile(String name, int age, String gmailid,
			Gender gender, Context context) throws UserProfileNotFoundException {
		if (!isUserProfileCreated) {
			Log.d(LOGTAG, "User profile does not exist, use getProfile");
			return false;
		}

		if (name != null) {
			setUserName(name, context);
		}
		if (age != -1)
			setAge(age, context);
		if (gmailid != null)
			setGmailID(gmailid, context);
		if (gender != null)
			setGender(gender, context);

		return true;
	}

	public static void init(Context context) {
		settings = context.getSharedPreferences(PREFS_NAME, 0);
		if (settings.contains(USER_NAME_SETTING))
			isUserProfileCreated = true;
		else
			isUserProfileCreated = false;

	}

	public static boolean isUserProfileCreated(Context context) {
		return context.getSharedPreferences(PREFS_NAME, 0).contains(
				USER_NAME_SETTING);
	}

}
