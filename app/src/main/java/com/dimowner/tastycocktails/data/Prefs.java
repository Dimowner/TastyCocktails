package com.dimowner.tastycocktails.data;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {

	private static final String PREF_NAME = "task.softermii.tastycocktails.data.Prefs";

	private static final String PREF_KEY_IS_FIRST_RUN = "is_first_run";
	private static final String PREF_KEY_LAST_SEARCH_STR = "last_search_str";
	private static final String PREF_KEY_FILTER_TYPE = "filter_type";
	private static final String PREF_KEY_FILTER_VALUE_POS = "filter_value_pos";
	private static final String PREF_KEY_FILTER_VALUE = "filter_value";

	public static final int FILTER_TYPE_SEARCH = 0;
	public static final int FILTER_TYPE_CATEGORY = 1;
	public static final int FILTER_TYPE_INGREDIENT = 2;
	public static final int FILTER_TYPE_GLASS = 3;
	public static final int FILTER_TYPE_ALCOHOLIC_NON_ALCOHOLIC = 4;

	private SharedPreferences sharedPreferences;

	public Prefs(Context context) {
		sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
	}

	public boolean isFirstRun() {
		return !sharedPreferences.contains(PREF_KEY_IS_FIRST_RUN) || sharedPreferences.getBoolean(PREF_KEY_IS_FIRST_RUN, false);
	}

	public void firstRunExecuted() {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(PREF_KEY_IS_FIRST_RUN, false);
		editor.apply();
	}

	public String getLastSearchString() {
		return sharedPreferences.getString(PREF_KEY_LAST_SEARCH_STR, null);
	}

	public void setLastSearchString(String str) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PREF_KEY_LAST_SEARCH_STR, str);
		editor.apply();
	}

	public void saveCurrentActiveFilter(int filterType) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(PREF_KEY_FILTER_TYPE, filterType);
		editor.apply();
	}

	public void saveSelectedFilterValuePos(int pos) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(PREF_KEY_FILTER_VALUE_POS, pos);
		editor.apply();
	}

	public void saveSelectedFilterValue(String value) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PREF_KEY_FILTER_VALUE, value);
		editor.apply();
	}

	public int getSelectedFilterValuePos() {
		return sharedPreferences.getInt(PREF_KEY_FILTER_VALUE_POS, 0);
	}

	public String getSelectedFilterValue() {
		return sharedPreferences.getString(PREF_KEY_FILTER_VALUE, "");
	}

	public int getCurrentActiveFilter() {
		return sharedPreferences.getInt(PREF_KEY_FILTER_TYPE, FILTER_TYPE_CATEGORY);
	}
}
