package task.softermii.tastycocktails.data;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {

	private static final String PREF_NAME = "task.softermii.tastycocktails.data.Prefs";

	private static final String PREF_KEY_IS_FIRST_RUN = "is_first_run";

	private static Prefs prefs;
	private SharedPreferences sharedPreferences;

	private Prefs(Context context) {
		sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
	}

	public static Prefs getInstance(Context context) {
		if (prefs == null) {
			prefs = new Prefs(context);
		}
		return prefs;
	}

	public boolean isFirstRun() {
		return !sharedPreferences.contains(PREF_KEY_IS_FIRST_RUN) || sharedPreferences.getBoolean(PREF_KEY_IS_FIRST_RUN, false);
	}

	public void firstRunExecuted() {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(PREF_KEY_IS_FIRST_RUN, false);
		editor.apply();
	}
}
