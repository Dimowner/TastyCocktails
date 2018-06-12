package com.dimowner.tastycocktails.util;

import android.util.Log;
import com.dimowner.tastycocktails.AppConstants;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LogUtil {

	private LogUtil() {}

	/**
	 * Write log into file.
	 * @param content Useful information to save in log file.
	 */
	public static void log2file(String content) {
		Log.i("LogUtil", "logToFile");
		File file = createLogFileFromPath();
		PrintWriter out = null;
		if (file != null) {
			try {
				out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
				out.println(content + "\n");
				out.flush();
			} catch (IOException e) {
				Log.e("LogUtils", "", e);
			} finally {
				if (out != null) {
					out.close();
				}
			}
		} else {
			Log.v("LogsUtil", "Cant write logs file is null");
		}
	}

	/**
	 * Get File form the file path.<BR>
	 * if the file does not exist, create it and return it.
	 * @return the file
	 */
	private static File createLogFileFromPath() {
		File dirPath = FileUtil.getStorageDir(AppConstants.APP_DIRECTORY);
		//Create directory if need.
		if (dirPath != null) {
			File file = new File(dirPath.getAbsolutePath() + "/" + AppConstants.LOG_FILE_NAME);
			//Create file if need.
			if (!file.exists()) {
				try {
					if (file.createNewFile()) {
						Log.i("LogUtils", "The Log file was successfully created! -" + file.getAbsolutePath());
					} else {
						Log.i("LogUtils", "The Log file exist! -" + file.getAbsolutePath());
					}
				} catch (IOException e) {
					Log.e("LogUtils", "Failed to create The Log file.", e);
					return null;
				}
			}
			if (!file.canWrite()) {
				Log.e("LogUtils", "The Log file can not be written.");
			}
			return file;
		} else {
			return null;
		}
	}
}
