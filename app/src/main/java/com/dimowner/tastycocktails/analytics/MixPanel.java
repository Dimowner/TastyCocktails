package com.dimowner.tastycocktails.analytics;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import com.dimowner.tastycocktails.R;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;


public class MixPanel implements Application.ActivityLifecycleCallbacks {

	public static final String EVENT_HOME = "home_sceen";
	public static final String EVENT_COCKTAIL_DETAILS = "cocktails_details";
	public static final String EVENT_FAVORITES = "favorites_screen";
	public static final String EVENT_RANDOM = "random_screen";
	public static final String EVENT_HISTORY = "history_screen";
	public static final String EVENT_ABOUT = "about_screen";
	public static final String EVENT_LICENCES = "licences_screen";
	public static final String EVENT_LICENCE_DETAILS = "licence_details";
	public static final String EVENT_RATE_APP = "rate_app";
	public static final String EVENT_ADD_TO_FAVORITES = "add_to_favorites";
	public static final String EVENT_REMOVE_FROM_FAVORITES = "remove_from_favorites";
	public static final String EVENT_REMOVE_FROM_HISTORY = "remove_from_history";
	public static final String EVENT_DRINK_IMAGE_PREVIEW = "drink_image_preview";
	public static final String EVENT_INGREDIENT_PREVIEW = "ingredient_image_preview";
	public static final String EVENT_SEARCH = "search";
	public static final String EVENT_NEW_RANDOM_DRINK = "new_random_drink";
	public static final String EVENT_SHOW_MENU = "show_menu";
	public static final String EVENT_HIDE = "hide_menu";

	private MixpanelAPI mixpanel;


	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
		mixpanel = MixpanelAPI.getInstance(activity.getApplicationContext(), activity.getApplicationContext().getString(R.string.mixpanel_token));
	}

	@Override public void onActivityStarted(Activity activity) {}
	@Override public void onActivityResumed(Activity activity) {}
	@Override public void onActivityPaused(Activity activity) {}
	@Override public void onActivityStopped(Activity activity) {}
	@Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

	@Override
	public void onActivityDestroyed(Activity activity) {
		mixpanel.flush();
	}

	public void trackData(String event, Bundle params) {
		JSONObject props = new JSONObject();

		for (String key : params.keySet()) {
			try {
				props.put(key, params.get(key));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		mixpanel.track(event, props);
	}

	public void trackData(String event) {
		trackData(event, Bundle.EMPTY);
	}
}
