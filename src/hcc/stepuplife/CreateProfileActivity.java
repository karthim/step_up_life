package hcc.stepuplife;

import hcc.stepuplife.UserProfile.Gender;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class CreateProfileActivity extends Activity implements OnClickListener {

	private static final String LOGTAG = "CreateProfileActivity";
	private boolean mUpdate;
	private boolean mProfileCreated = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_profile);
		
		LinearLayout layout =(LinearLayout)findViewById(R.id.LinearLytUserProfile);
		layout.setBackgroundResource(StepUpLifeUtils.getBgImage());
		
		mUpdate = getIntent().getBooleanExtra("update", false);

	}
	
	public void finish() {
		  // Prepare data intent 
		  Intent data = new Intent();
		  if(mProfileCreated = true)
			  setResult(RESULT_OK, data);
		  else
			  setResult(RESULT_CANCELED, data);
		  super.finish();
		} 

	protected void onResume() {
		super.onResume();

		Button b = ((Button) findViewById(R.id.buttonCreate));
		if (b == null)
			Log.d("INFO", "Button Create is null");
		else
			Log.d("INFO", "Button Create is not null");

		if (mUpdate) {
			TextView nameText = ((TextView) findViewById(R.id.nameText));
			TextView gmailText = ((TextView) findViewById(R.id.gmailText));
			TextView ageText = ((TextView) findViewById(R.id.ageText));
			RadioGroup radioGender = ((RadioGroup) findViewById(R.id.genderRadio));
			b.setText("Update");
			
			try {
				nameText.setText(UserProfile.getUserName());
				ageText.setText(UserProfile.getAge());
				gmailText.setText(UserProfile.getGmailID());
				if (UserProfile.getGender() == UserProfile.Gender.MALE)
					radioGender.check(R.id.male);
				else
					radioGender.check(R.id.female);

			} catch (UserProfileNotFoundException e) {
				Log.d(LOGTAG, "Did not find user profile");
			}

		}
		b.setOnClickListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_profile, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_create_profile,
					container, false);
			return rootView;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Button b = (Button) v;
		switch (v.getId()) {
		case R.id.buttonCreate:
			String userName = ((TextView) findViewById(R.id.nameText))
					.getText().toString();
			if (userName == null || userName == "") {
				StepUpLifeUtils.showToast(this, "Please enter name");
				return;
			}
			Log.d(LOGTAG, "userName is " + userName);
			int age;
			try {
				age = Integer.parseInt(((TextView) findViewById(R.id.ageText))
						.getText().toString());
				if (age < 18 && age > 90) {
					StepUpLifeUtils.showToast(this, "Please enter valid age");
					return;
				}
			} catch (NumberFormatException e) {
				StepUpLifeUtils.showToast(this, "Please enter age");
				return;
			}
			String gmailid = ((TextView) findViewById(R.id.gmailText))
					.getText().toString();
			if (gmailid == null || gmailid == "") {
				StepUpLifeUtils.showToast(this, "Please enter gmail ID");
				return;
			}
			int checkedRadioButtonid = ((RadioGroup) findViewById(R.id.genderRadio))
					.getCheckedRadioButtonId();
			Gender gender;
			if (checkedRadioButtonid == R.id.male)
				gender = Gender.MALE;
			else
				gender = Gender.FEMALE;

			if (mUpdate) {
				try {
					UserProfile.updateProfile(userName, age, gmailid, gender);
					StepUpLifeUtils.showToast(this,
							"Your profile was updated !!!");
				} catch (UserProfileNotFoundException e) {
					UserProfile.init(this);
					if (UserProfile.isUserProfileCreated(this)) {
						try {
							UserProfile.updateProfile(userName, age, gmailid,
									gender);
							StepUpLifeUtils.showToast(this,
									"Your profile was updated !!!");
						} catch (Exception another) {
							Log.d(LOGTAG, "Failed to update");
							StepUpLifeUtils.showToast(this,
									"Profile update failed !!!");
						}
					}
				}
			} else {
				if (UserProfile.createProfile(userName, age, gmailid, gender)) {
					Log.d(LOGTAG, "User Profile created");
					StepUpLifeUtils
							.showToast(this, "Your profile is saved !!!");
					mProfileCreated  = true;
				} else
					Log.d(LOGTAG, "User Profile exists?");
			}
			finish();
			break;
		default:
			break;
		}
	}
}
