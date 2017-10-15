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

package com.dimowner.tastycocktails;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dimowner.tastycocktails.util.AndroidUtils;

/**
 * Dialog shows information about application.
 * @author Dimowner
 */
public class AboutDialog extends DialogFragment {

	private static final String VERSION_UNAVAILABLE = "N/A";
	private static final int REVEAL_DURATION = 600; //mils

	public AboutDialog() {
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Get app version
		PackageManager pm = getActivity().getPackageManager();
		String packageName = getActivity().getPackageName();
		String versionName;
		try {
			PackageInfo info = pm.getPackageInfo(packageName, 0);
			versionName = info.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			versionName = VERSION_UNAVAILABLE;
		}

		// Build the about body view and append the link to see OSS licenses
		SpannableStringBuilder aboutBody = new SpannableStringBuilder();
		aboutBody.append(Html.fromHtml(getString(R.string.about_body, versionName)));

		TextView aboutBodyView = new TextView(getContext());
		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		aboutBodyView.setLayoutParams(lp);

		aboutBodyView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_medium));
		aboutBodyView.setText(aboutBody);
		int pad = (int) getResources().getDimension(R.dimen.padding_double);
		aboutBodyView.setPadding(pad, pad, pad, pad);
		aboutBodyView.setMovementMethod(new LinkMovementMethod());

		AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
				.setTitle(R.string.nav_about)
				.setView(aboutBodyView)
				.setPositiveButton(R.string.btn_ok, (dialog, whichButton) -> dismiss())
				.create();

		if (AndroidUtils.isAndroid5() && alertDialog.getWindow() != null) {
			View decorView = alertDialog.getWindow().getDecorView();
			alertDialog.setOnShowListener(dialogInterface -> revealShow(decorView));
		}
		return alertDialog;
	}

	@Override
	public void onDismiss(final DialogInterface dialog) {
		super.onDismiss(dialog);
		final Activity activity = getActivity();
		if (activity instanceof DialogInterface.OnDismissListener) {
			((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
		}
	}

	//Reveal animation for dialog
	@TargetApi(21)
	private void revealShow(View view) {
		int w = view.getWidth();
		int h = view.getHeight();

		int endRadius = (int) Math.hypot(w, h);

		int cx = (int) view.getX() + w / 2;
		int cy = (int) view.getY() + h / 2;

		Animator revealAnimator = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, endRadius);

		view.setVisibility(View.VISIBLE);
		revealAnimator.setDuration(REVEAL_DURATION);
		revealAnimator.start();
	}
}
