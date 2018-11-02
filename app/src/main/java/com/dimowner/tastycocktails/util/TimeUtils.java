package com.dimowner.tastycocktails.util;

import com.dimowner.tastycocktails.AppConstants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

	/** Date format: May 16, 03:30 PM */
	private static SimpleDateFormat dateFormat12H = new SimpleDateFormat("MMM dd, hh:mm aa", Locale.US);

	/** Date format: May 16, 15:30 */
	private static SimpleDateFormat dateFormat24H = new SimpleDateFormat("MMM dd, HH:mm", Locale.US);

	private TimeUtils() {}

	public static String formatTime(long timeMills, int timeFormat) {
		if (timeMills <= 0) {
			return "";
		}
		if (timeFormat == AppConstants.TIME_FORMAT_12H) {
			return dateFormat12H.format(new Date(timeMills));
		} else {
			return dateFormat24H.format(new Date(timeMills));
		}
	}

	public static String formatTime(long timeMills) {
		if (timeMills <= 0) {
			return "";
		}
		return dateFormat24H.format(new Date(timeMills));
	}
}
