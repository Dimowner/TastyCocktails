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

package com.dimowner.tastycocktails.cocktails;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.data.Prefs;

import timber.log.Timber;

/**
 * Dialog shows cocktails search settings.
 * @author Dimowner
 */
public class FiltersDialog extends DialogFragment {

	private DialogInterface.OnClickListener onClickListener;

	public FiltersDialog() {
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(R.layout.dialog_filters, null);

		Spinner spinner = view.findViewById(R.id.filter_categories);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
				R.array.filter_categories, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);

		Prefs prefs = new Prefs(getContext());
		spinner.setSelection(prefs.getSelectedFilterValuePos());

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
				prefs.saveCurrentActiveFilter(Prefs.FILTER_TYPE_CATEGORY);
				prefs.saveSelectedFilterValuePos(pos);
				prefs.saveSelectedFilterValue(adapter.getItem(pos).toString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});

		AlertDialog.Builder builder= new AlertDialog.Builder(getActivity())
				.setTitle(R.string.dialog_filters)
				.setView(view)
				.setNegativeButton(R.string.btn_cancel, (dialog, whichButton) -> {});
		if (onClickListener != null) {
			builder.setPositiveButton(R.string.btn_ok, onClickListener);
		} else {
			builder.setPositiveButton(R.string.btn_ok, (dialog, whichButton) -> {});
		}

		return builder.create();
	}

	@Override
	public void onDismiss(final DialogInterface dialog) {
		super.onDismiss(dialog);
		final Activity activity = getActivity();
		if (activity instanceof DialogInterface.OnDismissListener) {
			((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
		}
	}

	public void setPositiveButtonListener(DialogInterface.OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}
}
