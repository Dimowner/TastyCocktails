package com.dimowner.tastycocktails.settings;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.SwitchCompat;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

//import com.dimowner.tastycocktails.AdvHandler;
import com.dimowner.tastycocktails.AppConstants;
import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.analytics.MixPanel;
import com.dimowner.tastycocktails.data.Prefs;
import com.dimowner.tastycocktails.licences.LicenceActivity;
import com.dimowner.tastycocktails.util.AndroidUtils;

import javax.inject.Inject;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

	private static final String VERSION_UNAVAILABLE = "N/A";

	@Inject Prefs prefs;

//	private AdvHandler advHandler;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		TCApplication.get(getApplicationContext()).applicationComponent().inject(this);

		ImageButton btnBack = findViewById(R.id.btn_back);
		btnBack.setOnClickListener(v -> finish());
//		AdView adView = findViewById(R.id.adView);

//		SwitchCompat showAdsSwitch = findViewById(R.id.showAdsSwitch);
//		if (prefs.isShowAds()) {
//			showAdsSwitch.setChecked(true);
//		} else {
//			showAdsSwitch.setChecked(false);
//		}
//
//		showAdsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
//			prefs.setShowAds(isChecked);
//			if (isChecked) {
//				TCApplication.event(getApplicationContext(), MixPanel.EVENT_ENABLE_ADS);
////				adView.setVisibility(View.VISIBLE);
//			} else {
//				TCApplication.event(getApplicationContext(), MixPanel.EVENT_DISABLE_ADS);
////				adView.setVisibility(View.INVISIBLE);
//			}
//		});

		TextView btnLicences = findViewById(R.id.btnLicences);
		TextView btnRate = findViewById(R.id.btnRate);
		TextView btnAbout = findViewById(R.id.txtAbout);
		TextView btnRequest = findViewById(R.id.btnRequest);
		TextView btnAudioRecorder = findViewById(R.id.btnAudioRecorder);
		TextView btnAiryCompass = findViewById(R.id.btnAiryCompass);
		btnAbout.setText(getAboutContent());
		btnLicences.setOnClickListener(this);
		btnRate.setOnClickListener(this);
		btnRequest.setOnClickListener(this);
		btnAudioRecorder.setOnClickListener(this);
		btnAiryCompass.setOnClickListener(this);

		AndroidUtils.primaryColorNavigationBar(this);

//		advHandler = new AdvHandler(adView, prefs);

		TCApplication.event(getApplicationContext(), MixPanel.EVENT_SETTINGS);
	}

//	@Override
//	public void onResume() {
//		super.onResume();
//		advHandler.onResume();
//	}
//
//	@Override
//	public void onPause() {
//		advHandler.onResume();
//		super.onPause();
//	}
//
//	@Override
//	protected void onDestroy() {
//		advHandler.onDestroy();
//		super.onDestroy();
//	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnLicences:
				startActivity(new Intent(getApplicationContext(), LicenceActivity.class));
				break;
			case R.id.btnRate:
//				rateApp();
				openApp(getApplicationContext().getPackageName());
				break;
			case R.id.btnRequest:
				requestFeature();
				break;
			case R.id.btnAudioRecorder:
				openApp("com.dimowner.audiorecorder");
				break;
			case R.id.btnAiryCompass:
				openApp("com.dimowner.airycompass");
				break;
		}
	}

//	public void rateApp() {
//		TCApplication.event(getApplicationContext(), MixPanel.EVENT_RATE_APP);
//		try {
//			Intent rateIntent = rateIntentForUrl("market://details", getApplicationContext().getPackageName());
//			startActivity(rateIntent);
//		} catch (ActivityNotFoundException e) {
//			Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details", getApplicationContext().getPackageName());
//			startActivity(rateIntent);
//		}
//	}

	public void openApp(String appPackage) {
		TCApplication.event(getApplicationContext(), MixPanel.EVENT_RATE_APP);
//		https://play.google.com/store/apps/details?id=com.dimowner.audiorecorder
		try {
			Intent rateIntent = rateIntentForUrl("market://details", appPackage);
			startActivity(rateIntent);
		} catch (ActivityNotFoundException e) {
			Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details", appPackage);
			startActivity(rateIntent);
		}
	}

	private void requestFeature() {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL, new String[]{AppConstants.REQUESTS_RECEIVER});
		i.putExtra(Intent.EXTRA_SUBJECT,
				"[" + getResources().getString(R.string.app_name) + "] - " + getResources().getString(R.string.request)
		);
		try {
			startActivity(Intent.createChooser(i, getResources().getString(R.string.send_email)));
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(getApplicationContext(), R.string.email_clients_not_found, Toast.LENGTH_LONG).show();
		}
	}

	private Intent rateIntentForUrl(String url, String pack) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, pack)));
		int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
		if (Build.VERSION.SDK_INT >= 21) {
			flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
		} else {
			flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
		}
		intent.addFlags(flags);
		return intent;
	}

	public SpannableStringBuilder getAboutContent() {
		// Get app version;
		String packageName = getPackageName();
		String versionName;
		try {
			PackageInfo info = getPackageManager().getPackageInfo(packageName, 0);
			versionName = info.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			versionName = VERSION_UNAVAILABLE;
		}

		// Build the about body view and append the link to see OSS licenses
		SpannableStringBuilder aboutBody = new SpannableStringBuilder();
		aboutBody.append(Html.fromHtml(getString(R.string.about_body, versionName)));
		return aboutBody;
	}
}
