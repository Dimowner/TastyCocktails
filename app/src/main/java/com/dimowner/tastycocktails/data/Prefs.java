package com.dimowner.tastycocktails.data;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {

	private static final String PREF_NAME = "task.softermii.tastycocktails.data.Prefs";

	private static final String PREF_KEY_IS_FIRST_RUN = "is_first_run";
	private static final String PREF_KEY_LAST_SEARCH_STR = "last_search_str";
	private static final String PREF_KEY_SEARCH_TYPE = "search_type";

	private static final String PREF_KEY_FILTER_CATEGORY = "filter_category";
	private static final String PREF_KEY_FILTER_CATEGORY_POS = "filter_category_pos";
	private static final String PREF_KEY_FILTER_INGREDIENT = "filter_ingredient";
	private static final String PREF_KEY_FILTER_INGREDIENT_POS = "filter_ingredient_pos";
	private static final String PREF_KEY_FILTER_GLASS = "filter_glass";
	private static final String PREF_KEY_FILTER_GLASS_POS = "filter_glass_pos";
	private static final String PREF_KEY_FILTER_ALCOHOLIC = "filter_alcoholic";
	private static final String PREF_KEY_FILTER_ALCOHOLIC_POS = "filter_alcoholic_pos";

	public static final int SEARCH_TYPE_SEARCH = 0;
	public static final int SEARCH_TYPE_FILTER = 1;


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

	public void setFirstRunDefaultValues(int searchType, int position, String value) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(PREF_KEY_SEARCH_TYPE, searchType);
		editor.putString(PREF_KEY_FILTER_CATEGORY, value);
		editor.putInt(PREF_KEY_FILTER_CATEGORY_POS, position);
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

	public void saveCurrentSearchType(int searchType) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(PREF_KEY_SEARCH_TYPE, searchType);
		editor.apply();
	}

	public void saveSelectedCategoryPos(int pos) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(PREF_KEY_FILTER_CATEGORY_POS, pos);
		editor.apply();
	}

	public void saveSelectedIngredientPos(int pos) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(PREF_KEY_FILTER_INGREDIENT_POS, pos);
		editor.apply();
	}

	public void saveSelectedGlassPos(int pos) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(PREF_KEY_FILTER_GLASS_POS, pos);
		editor.apply();
	}

	public void saveSelectedAlcoholicPos(int pos) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(PREF_KEY_FILTER_ALCOHOLIC_POS, pos);
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

	public int getCurrentSearchType() {
		return sharedPreferences.getInt(PREF_KEY_SEARCH_TYPE, FILTER_TYPE_CATEGORY);
	}

	public int getSelectedCategoryPos() {
		return sharedPreferences.getInt(PREF_KEY_FILTER_CATEGORY_POS, 0);
	}

	public int getSelectedIngredientPos() {
		return sharedPreferences.getInt(PREF_KEY_FILTER_INGREDIENT_POS, 0);
	}

	public int getSelectedGlassPos() {
		return sharedPreferences.getInt(PREF_KEY_FILTER_GLASS_POS, 0);
	}

	public int getSelectedAlcoholicPos() {
		return sharedPreferences.getInt(PREF_KEY_FILTER_ALCOHOLIC_POS, 0);
	}

	public void clearFilters() {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PREF_KEY_FILTER_CATEGORY, null);
		editor.putString(PREF_KEY_FILTER_INGREDIENT, null);
		editor.putString(PREF_KEY_FILTER_GLASS, null);
		editor.putString(PREF_KEY_FILTER_ALCOHOLIC, null);
		editor.putInt(PREF_KEY_FILTER_CATEGORY_POS, 0);
		editor.putInt(PREF_KEY_FILTER_INGREDIENT_POS, 0);
		editor.putInt(PREF_KEY_FILTER_GLASS_POS, 0);
		editor.putInt(PREF_KEY_FILTER_ALCOHOLIC_POS, 0);
		editor.apply();
	}
}
