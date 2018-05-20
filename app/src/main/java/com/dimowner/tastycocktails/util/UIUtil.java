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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import com.dimowner.tastycocktails.R;

/**
 * User interface common methods
 * Created on 26.07.2017.
 * @author Dimowner
 */
public class UIUtil {

	/**
	 * Is private to forbid creation of an object.
	 */
	private UIUtil() {
	}

	/**
	 * Show warning dialog with OK and Cancel buttons.
	 *
	 * @param activity                 Application context.
	 * @param mesRes                   Resource id of message to show in dialog.
	 * @param positiveBtnClickListener Listener for positive button click.
	 * @param negativeBtnClickListener Listener for negative button click.
	 */
	public static void showWarningDialog(
			Activity activity, int mesRes,
			DialogInterface.OnClickListener positiveBtnClickListener,
			DialogInterface.OnClickListener negativeBtnClickListener) {

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.title_warning)
				.setMessage(mesRes)
				.setIcon(R.drawable.round_alert)
				.setCancelable(false)
				.setPositiveButton(R.string.btn_ok, positiveBtnClickListener)
				.setNegativeButton(R.string.btn_cancel, negativeBtnClickListener);

		AlertDialog alert = builder.create();
		alert.show();
	}

	/**
	 * Show warning dialog with OK and Cancel buttons.
	 *
	 * @param activity                 Application context.
	 * @param mesRes                   Resource id of message to show in dialog.
	 * @param iconRes                  Resource id of header icon to show in dialog.
	 * @param positiveBtnClickListener Listener for positive button click.
	 * @param negativeBtnClickListener Listener for negative button click.
	 */
	public static void showWarningDialog(
			Activity activity, int iconRes, int mesRes,
			DialogInterface.OnClickListener positiveBtnClickListener,
			DialogInterface.OnClickListener negativeBtnClickListener) {

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.title_warning)
				.setMessage(mesRes)
				.setIcon(iconRes)
				.setCancelable(false)
				.setPositiveButton(R.string.btn_ok, positiveBtnClickListener)
				.setNegativeButton(R.string.btn_cancel, negativeBtnClickListener);

		AlertDialog alert = builder.create();
		alert.show();
	}

	/**
	 * Show warning dialog with OK and Cancel buttons.
	 *
	 * @param activity                 Application context.
	 * @param mesStr                   Message string to show in dialog.
	 * @param iconRes                  Resource id of header icon to show in dialog.
	 * @param positiveBtnClickListener Listener for positive button click.
	 * @param negativeBtnClickListener Listener for negative button click.
	 */
	public static void showWarningDialog(
			Activity activity, int iconRes, String mesStr,
			DialogInterface.OnClickListener positiveBtnClickListener,
			DialogInterface.OnClickListener negativeBtnClickListener) {

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.title_warning)
				.setMessage(mesStr)
				.setIcon(iconRes)
				.setCancelable(false)
				.setPositiveButton(R.string.btn_ok, positiveBtnClickListener)
				.setNegativeButton(R.string.btn_cancel, negativeBtnClickListener);

		AlertDialog alert = builder.create();
		alert.show();
	}

	/**
	 * Show warning dialog with OK and Cancel buttons.
	 *
	 * @param activity Application context.
	 * @param mesRes   Resource id of message to show in dialog.
	 */
	public static void showWarningDialog(Activity activity, int mesRes) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.title_warning)
				.setMessage(mesRes)
				.setIcon(R.drawable.round_alert)
				.setCancelable(true)
				.setPositiveButton(R.string.btn_ok, null);

		AlertDialog alert = builder.create();
		alert.show();
	}
}
