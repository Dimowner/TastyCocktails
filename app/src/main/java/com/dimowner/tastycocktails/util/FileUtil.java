/*
 * Copyright 2017 Dmitriy Ponomarenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dimowner.tastycocktails.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created on 01.10.2017.
 * @author Dimowner
 */
public class FileUtil {

	private FileUtil() {}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap bitmap;
		if (drawable instanceof BitmapDrawable) {
			BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			if(bitmapDrawable.getBitmap() != null) {
				return bitmapDrawable.getBitmap();
			}
		}

		if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
			bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
		} else {
			bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		}

		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	/**
	 * Get public directory (external storage).
	 * @param dirName directory name.
	 */
	public static File getStorageDir(String dirName) {
		if (dirName != null && !dirName.isEmpty()) {
			File file = new File(Environment.getExternalStorageDirectory(), dirName);
			if (isExternalStorageReadable() && isExternalStorageWritable()) {
				if (!file.exists() && !file.mkdirs()) {
					Log.e("FileUtils", "Directory " + file.getAbsolutePath() + " was not created");
				}
			}
			return file;
		} else {
			return null;
		}
	}

	/**
	 * Checks if external storage is available for read and write.
	 */
	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state);
	}

	/**
	 * Checks if external storage is available to at least read.
	 */
	public static boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		return (Environment.MEDIA_MOUNTED.equals(state) ||
				Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
	}
}
