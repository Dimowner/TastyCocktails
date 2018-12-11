package com.dimowner.tastycocktails;

/**
 * Created on 21.10.2017.
 *
 * @author Dimowner
 */
public class AppConstants {
	private AppConstants() {}

	public static final String BASE_INGREDIENT_PATH = "http://www.thecocktaildb.com/images/ingredients/";

	public static final String FEEDBACK_EMAIL = "dimmony@gmail.com";
	public static final String FEEDBACK_SUBJECT = "Tasty Cocktails feedback";

	public static final String APP_DIRECTORY = "TastyCocktails";
	public static final String LOG_FILE_NAME = "Log.txt";

	public static final int CONNECTION_TIMEOUT = 30; //Seconds
	public static final int READ_TIMEOUT = 45; //Seconds

	public static final int TIME_FORMAT_24H = 11;
	public static final int TIME_FORMAT_12H = 12;

	public static final int SHOW_INSTRUCTIONS_DELAY_MILLS = 800;

	public static final long MIN_RATING_UPDATE_INTERVAL_MILLS = 1000 * 60 * 60; //1h

}
