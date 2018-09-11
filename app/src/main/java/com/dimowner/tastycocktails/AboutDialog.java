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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.dimowner.tastycocktails.analytics.MixPanel;
import com.dimowner.tastycocktails.licences.LicenceActivity;
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

//		LinearLayout container = new LinearLayout(getContext());
//		container.setOrientation(LinearLayout.VERTICAL);
//		LinearLayout.LayoutParams containerLp = new LinearLayout.LayoutParams(
//				LinearLayout.LayoutParams.MATCH_PARENT,
//				LinearLayout.LayoutParams.WRAP_CONTENT);
//		container.setLayoutParams(containerLp);
//
//		TextView aboutBodyView = new TextView(getContext());
//		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
//				ViewGroup.LayoutParams.MATCH_PARENT,
//				ViewGroup.LayoutParams.WRAP_CONTENT);
//		aboutBodyView.setLayoutParams(lp);
//
//		aboutBodyView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_medium));
//		aboutBodyView.setText(aboutBody);
//		int pad = (int) getResources().getDimension(R.dimen.padding_double);
//		aboutBodyView.setPadding(pad, pad, pad, pad);
//		aboutBodyView.setMovementMethod(new LinkMovementMethod());
//
//		Button btnLicences = new Button(getContext());
//		btnLicences.setText(R.string.open_source_licences);
//		btnLicences.setOnClickListener(view -> {
//			dismiss();
//			startActivity(new Intent(getContext(), LicenceActivity.class));
//		});
//
//		ViewGroup.MarginLayoutParams btnLp = new ViewGroup.MarginLayoutParams(
//				ViewGroup.LayoutParams.MATCH_PARENT,
//				ViewGroup.LayoutParams.WRAP_CONTENT);
//		int margin = (int) getResources().getDimension(R.dimen.padding_standard);
//		btnLp.setMargins(margin, 0, margin, 0);
//		btnLicences.setLayoutParams(btnLp);
//
//		container.addView(aboutBodyView);
//		container.addView(btnLicences);

		LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(R.layout.dialog_about, null);

		TextView content = view.findViewById(R.id.about_txt_content);
		content.setText(aboutBody);
		TextView btnLicences = view.findViewById(R.id.about_btn_licences);
		TextView btnRate = view.findViewById(R.id.about_btn_rate);
		if (AndroidUtils.isAndroid5()) {
			btnLicences.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dna, 0, 0, 0);
			btnRate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_round_thumbs_up_down_24px, 0, 0, 0);
			btnRate.setCompoundDrawablePadding((int)getContext().getResources().getDimension(R.dimen.padding_small));
			btnLicences.setCompoundDrawablePadding((int)getContext().getResources().getDimension(R.dimen.padding_small));
		}
		btnLicences.setOnClickListener(v -> {
			dismiss();
			startActivity(new Intent(getContext(), LicenceActivity.class));
		});
		btnRate.setOnClickListener(v -> {
			dismiss();
			rateApp();
		});

		AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
				.setTitle(R.string.nav_about)
				.setView(view)
				.setPositiveButton(R.string.btn_ok, (dialog, whichButton) -> dismiss())
				.create();

//		if (AndroidUtils.isAndroid5() && alertDialog.getWindow() != null) {
//			View decorView = alertDialog.getWindow().getDecorView();
//			alertDialog.setOnShowListener(dialogInterface -> revealShow(decorView));
//		}

		TCApplication.event(getActivity().getApplicationContext(), MixPanel.EVENT_ABOUT);
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

	public void rateApp() {
		TCApplication.event(getActivity().getApplicationContext(), MixPanel.EVENT_RATE_APP);
		try {
			Intent rateIntent = rateIntentForUrl("market://details");
			startActivity(rateIntent);
		} catch (ActivityNotFoundException e) {
			Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details");
			startActivity(rateIntent);
		}
	}

	private Intent rateIntentForUrl(String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, getActivity().getApplicationContext().getPackageName())));
		int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
		if (Build.VERSION.SDK_INT >= 21) {
			flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
		} else {
			flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
		}
		intent.addFlags(flags);
		return intent;
	}

//	//Reveal animation for dialog
//	@TargetApi(21)
//	private void revealShow(View view) {
//		int w = view.getWidth();
//		int h = view.getHeight();
//
//		int endRadius = (int) Math.hypot(w, h);
//
//		int cx = (int) view.getX() + w / 2;
//		int cy = (int) view.getY() + h / 2;
//
//		Animator revealAnimator = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, endRadius);
//
//		view.setVisibility(View.VISIBLE);
//		revealAnimator.setDuration(REVEAL_DURATION);
//		revealAnimator.start();
//	}
}
