package task.softermii.tastycocktails.data;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {

	private static final String PREF_NAME = "task.softermii.tastycocktails.data.Prefs";

	private static final String PREF_KEY_IS_FIRST_RUN = "is_first_run";
	private static final String PREF_KEY_LAST_SEARCH_STR = "last_search_str";

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
}
