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

	private static final String PREF_KEY_FILTER_CATEGORY = "filter_category";
	private static final String PREF_KEY_FILTER_INGREDIENT = "filter_ingredient";
	private static final String PREF_KEY_FILTER_GLASS = "filter_glass";
	private static final String PREF_KEY_FILTER_ALCOHOLIC = "filter_alcoholic";

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

	public void setFirstRunDefaultValues(int filterType, int position, String value) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(PREF_KEY_FILTER_TYPE, filterType);
		editor.putInt(PREF_KEY_FILTER_VALUE_POS, position);
		editor.putString(PREF_KEY_FILTER_VALUE, value);
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

	public void saveFilterCategory(String category) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PREF_KEY_FILTER_CATEGORY, category);
		editor.apply();
	}

	public void saveFilterIngredient(String ingredient) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PREF_KEY_FILTER_INGREDIENT, ingredient);
		editor.apply();
	}

	public void saveFilterGlass(String glass) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PREF_KEY_FILTER_GLASS, glass);
		editor.apply();
	}

	public void saveFilterAlcoholic(String alcoholic) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PREF_KEY_FILTER_ALCOHOLIC, alcoholic);
		editor.apply();
	}

	public String getFilterCategory() {
		return sharedPreferences.getString(PREF_KEY_FILTER_CATEGORY, "");
	}

	public String getFilterIngredient() {
		return sharedPreferences.getString(PREF_KEY_FILTER_INGREDIENT, "");
	}

	public String getFilterGlass() {
		return sharedPreferences.getString(PREF_KEY_FILTER_GLASS, "");
	}

	public String getFilterAlcoholic() {
		return sharedPreferences.getString(PREF_KEY_FILTER_ALCOHOLIC, "");
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

	public void clearFilters() {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PREF_KEY_FILTER_CATEGORY, null);
		editor.putString(PREF_KEY_FILTER_INGREDIENT, null);
		editor.putString(PREF_KEY_FILTER_GLASS, null);
		editor.putString(PREF_KEY_FILTER_ALCOHOLIC, null);
		editor.apply();
	}
}
