package com.exacttarget.practicefield;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import com.exacttarget.etpushsdk.ETException;
import com.exacttarget.etpushsdk.ETPush;
import org.json.JSONObject;

import java.io.InputStream;

public class PracticeFieldDiscountActivity extends ActionBarActivity {
	private int currentPage = CONSTS.DISCOUNT_ACTIVITY;
	private String payloadStr = "";

	private static final String TAG = PracticeFieldDiscountActivity.class.getName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.discount_layout);

		if (savedInstanceState == null) {
			payloadStr = getIntent().getExtras().getString(CONSTS.KEY_PUSH_RECEIVED_PAYLOAD, "");
		}
		else {
			payloadStr = savedInstanceState.getString(CONSTS.KEY_PUSH_RECEIVED_PAYLOAD, "");
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(CONSTS.KEY_CURRENT_PAGE, currentPage);
		outState.putString(CONSTS.KEY_PUSH_RECEIVED_PAYLOAD, payloadStr);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();

		Utils.setActivityTitle(this, R.string.discount_activity_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		try {
			// Let ExactTarget know when each activity is resumed
			ETPush.pushManager().activityResumed(this);
		}
		catch (ETException e) {
			if (ETPush.getLogLevel() <= Log.ERROR) {
				Log.e(TAG, e.getMessage(), e);
			}
		}

		prepareDisplay();
	}

	@Override
	protected void onPause() {
		super.onPause();

		try {
			// Let ExactTarget know when each activity paused
			ETPush.pushManager().activityPaused(this);
		}
		catch (Exception e) {
			if (ETPush.getLogLevel() <= Log.ERROR) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.global_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Utils.prepareMenu(currentPage, menu);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Boolean result = Utils.selectMenuItem(this, currentPage, item);
		return result != null ? result : super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	private void prepareDisplay() {

		if (!payloadStr.equals("")) {
			// convert JSON String of saved payload back to bundle to display
			JSONObject jo = null;

			try {
				jo = new JSONObject(payloadStr);

				if (jo != null) {

					String discountStr = jo.getString(CONSTS.KEY_PAYLOAD_DISCOUNT);

					if (!discountStr.equals("")) {
						int discount = Integer.valueOf(discountStr);
						String message = jo.getString(CONSTS.KEY_PAYLOAD_ALERT);
						String imageFile;

						switch (discount) {
							case 10:
								imageFile = "10percentoffQR.png";
								break;
							case 15:
								imageFile = "15percentoffQR.png";
								break;
							case 20:
								imageFile = "20percentoffQR.png";
								break;
							default:
								imageFile = "10percentoffQR.png";
								break;
						}

						TextView messageTV = (TextView) findViewById((R.id.messageTV));
						messageTV.setText(message + "\n\n" + "Custom Keys (Discount Code):  " + discount);

						try {
							InputStream ims = getAssets().open(imageFile);
							// load image as Drawable
							Drawable d = Drawable.createFromStream(ims, null);

							ImageView qrIV = (ImageView) findViewById(R.id.QRcodeIV);
							qrIV.setImageDrawable(d);
						}
						catch (Exception e) {
							if (ETPush.getLogLevel() <= Log.ERROR) {
								Log.e(TAG, e.getMessage(), e);
							}
						}
					}
					else {
						showNoDiscounts();
					}
				}
			}
			catch (Exception e) {
				if (ETPush.getLogLevel() <= Log.ERROR) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}
		else {
			showNoDiscounts();
		}

	}

	private void showNoDiscounts() {
		TextView messageTV = (TextView) findViewById((R.id.messageTV));
		messageTV.setText("Problem finding discount information.");
		messageTV.setTypeface(null, Typeface.BOLD);
	}
}