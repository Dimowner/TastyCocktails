package com.dimowner.tastycocktails;

import android.view.View;

import com.dimowner.tastycocktails.data.Prefs;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class AdvHandler {

	private AdView adView;
	private Prefs prefs;

	public AdvHandler(AdView adView, Prefs prefs) {
		this.adView = adView;
		this.prefs = prefs;

		if (adView != null) {
			if (prefs.isShowAds()) {
				adView.setVisibility(View.VISIBLE);
				AdRequest adRequest = new AdRequest.Builder()
						.addTestDevice("3CDE42B77B78065EF7879C6A83E0AF4B")
						.addTestDevice("849A8D331C1E0F2AE74C7330D0BEF9D8")
						.addTestDevice("53ECB11D7A7CCB1BCC9B40BAF5F5DAE7")
						.addTestDevice("F42EF6DB3B5A04F29CA55B366777AE69")
						.build();
				adView.loadAd(adRequest);
			} else {
				adView.setVisibility(View.GONE);
			}
		}
	}

	public void onResume() {
		if (adView != null) {
			if (prefs.isShowAds()) {
				adView.resume();
			} else {
				adView.setVisibility(View.GONE);
			}
		}
	}

	public void onPause() {
		if (adView != null) {
			adView.pause();
		}
	}

	public void onDestroy() {
		if (adView != null) {
			adView.destroy();
		}
	}
}
