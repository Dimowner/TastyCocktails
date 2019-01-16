package com.dimowner.tastycocktails.settings;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dimowner.tastycocktails.AdvHandler;
import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.analytics.MixPanel;
import com.dimowner.tastycocktails.data.Prefs;
import com.dimowner.tastycocktails.licences.LicenceActivity;
import com.dimowner.tastycocktails.util.AndroidUtils;
import com.google.android.gms.ads.AdView;

import javax.inject.Inject;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

	private static final String VERSION_UNAVAILABLE = "N/A";

	@Inject Prefs prefs;

	private AdvHandler advHandler;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		TCApplication.get(getApplicationContext()).applicationComponent().inject(this);

		ImageButton btnBack = findViewById(R.id.btn_back);
		btnBack.setOnClickListener(v -> finish());
		AdView adView = findViewById(R.id.adView);

		SwitchCompat showAdsSwitch = findViewById(R.id.showAdsSwitch);
		if (prefs.isShowAds()) {
			showAdsSwitch.setChecked(true);
		} else {
			showAdsSwitch.setChecked(false);
		}

		showAdsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
			prefs.setShowAds(isChecked);
			if (isChecked) {
				TCApplication.event(getApplicationContext(), MixPanel.EVENT_ENABLE_ADS);
				adView.setVisibility(View.VISIBLE);
			} else {
				TCApplication.event(getApplicationContext(), MixPanel.EVENT_DISABLE_ADS);
				adView.setVisibility(View.INVISIBLE);
			}
		});

		TextView btnLicences = findViewById(R.id.btnLicences);
		TextView btnRate = findViewById(R.id.btnRate);
		TextView btnAbout = findViewById(R.id.txtAbout);
		btnAbout.setText(getAboutContent());
		btnLicences.setOnClickListener(this);
		btnRate.setOnClickListener(this);

		AndroidUtils.primaryColorNavigationBar(this);

		advHandler = new AdvHandler(adView, prefs);

		TCApplication.event(getApplicationContext(), MixPanel.EVENT_SETTINGS);
	}

	@Override
	public void onResume() {
		super.onResume();
		advHandler.onResume();
	}

	@Override
	public void onPause() {
		advHandler.onResume();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		advHandler.onDestroy();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnLicences:
				startActivity(new Intent(getApplicationContext(), LicenceActivity.class));
				break;
			case R.id.btnRate:
				rateApp();
				break;
		}
	}

	public void rateApp() {
		TCApplication.event(getApplicationContext(), MixPanel.EVENT_RATE_APP);
		try {
			Intent rateIntent = rateIntentForUrl("market://details");
			startActivity(rateIntent);
		} catch (ActivityNotFoundException e) {
			Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details");
			startActivity(rateIntent);
		}
	}

	private Intent rateIntentForUrl(String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, getApplicationContext().getPackageName())));
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
