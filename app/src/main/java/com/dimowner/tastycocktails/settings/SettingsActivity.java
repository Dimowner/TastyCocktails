package com.dimowner.tastycocktails.settings;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.widget.ImageButton;

import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.analytics.MixPanel;
import com.dimowner.tastycocktails.data.Prefs;

import javax.inject.Inject;

public class SettingsActivity extends AppCompatActivity {

	@Inject Prefs prefs;

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
		showAdsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.setShowAds(isChecked));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
			getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
		}
		TCApplication.event(getApplicationContext(), MixPanel.EVENT_SETTINGS);
	}
}
