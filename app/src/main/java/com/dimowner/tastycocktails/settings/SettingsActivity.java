package com.dimowner.tastycocktails.settings;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dimowner.tastycocktails.AboutDialog;
import com.dimowner.tastycocktails.AdvHandler;
import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.analytics.MixPanel;
import com.dimowner.tastycocktails.data.Prefs;
import com.dimowner.tastycocktails.licences.LicenceActivity;
import com.google.android.gms.ads.AdView;

import javax.inject.Inject;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

	@Inject Prefs prefs;

	private AdvHandler advHandler;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		TCApplication.get(getApplicationContext()).applicationComponent().inject(this);

		ImageButton btnBack = findViewById(R.id.btn_back);
		btnBack.setOnClickListener(v -> finish());

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
			} else {
				TCApplication.event(getApplicationContext(), MixPanel.EVENT_DISABLE_ADS);
			}
		});

		TextView btnLicences = findViewById(R.id.btnLicences);
		TextView btnRate = findViewById(R.id.btnRate);
		TextView btnAbout = findViewById(R.id.btnAbout);
		btnLicences.setOnClickListener(this);
		btnRate.setOnClickListener(this);
		btnAbout.setOnClickListener(this);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
			getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
		}

		AdView adView = findViewById(R.id.adView);
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

	private void showAboutDialog() {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment prev = fm.findFragmentByTag("dialog_about");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);
		AboutDialog dialog = new AboutDialog();
		dialog.show(ft, "dialog_about");
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
			case R.id.btnAbout:
				showAboutDialog();
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
}
