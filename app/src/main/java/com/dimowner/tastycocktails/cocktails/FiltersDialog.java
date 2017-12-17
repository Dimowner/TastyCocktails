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
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.data.Prefs;

/**
 * Dialog shows cocktails search settings.
 * @author Dimowner
 */
public class FiltersDialog extends DialogFragment {

	private DialogInterface.OnClickListener onClickListener;

	private int selectedFilter = -1;

	private Prefs prefs;

	public FiltersDialog() {
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(R.layout.dialog_filters, null);

		//Init CATEGORY filter
		Spinner categorySpinner = view.findViewById(R.id.filter_categories);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(getContext(),
				R.array.filter_categories, R.layout.spinner_item);
		// Specify the layout to use when the list of choices appears
		categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		// Apply the adapter to the spinner
		categorySpinner.setAdapter(categoryAdapter);

		//Init INGREDIENTS filter
		Spinner ingredientSpinner = view.findViewById(R.id.filter_ingredients);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> ingredientAdapter = ArrayAdapter.createFromResource(getContext(),
				R.array.filter_ingredients_alphabetical, R.layout.spinner_item);

		// Specify the layout to use when the list of choices appears
		ingredientAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		// Apply the adapter to the spinner
		ingredientSpinner.setAdapter(ingredientAdapter);

		//Init GLASS filter
		Spinner glassSpinner = view.findViewById(R.id.filter_glass);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> glassAdapter = ArrayAdapter.createFromResource(getContext(),
				R.array.filter_glass, R.layout.spinner_item);
		// Specify the layout to use when the list of choices appears
		glassAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		// Apply the adapter to the spinner
		glassSpinner.setAdapter(glassAdapter);

		//Init ALCOHOLIC filter
		Spinner alcoholicSpinner = view.findViewById(R.id.filter_alcoholic);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> alcoholicAdapter = ArrayAdapter.createFromResource(getContext(),
				R.array.filter_alcoholic, R.layout.spinner_item);
		// Specify the layout to use when the list of choices appears
		glassAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		// Apply the adapter to the spinner
		alcoholicSpinner.setAdapter(alcoholicAdapter);

		prefs = new Prefs(getContext());
		int activeFilter = prefs.getCurrentActiveFilter();
		if (activeFilter == Prefs.FILTER_TYPE_CATEGORY) {
			categorySpinner.setSelection(prefs.getSelectedFilterValuePos());
		} else if (activeFilter == Prefs.FILTER_TYPE_INGREDIENT) {
			ingredientSpinner.setSelection(prefs.getSelectedFilterValuePos());
		} else if (activeFilter == Prefs.FILTER_TYPE_GLASS) {
			glassSpinner.setSelection(prefs.getSelectedFilterValuePos());
		} else if (activeFilter == Prefs.FILTER_TYPE_ALCOHOLIC_NON_ALCOHOLIC) {
			alcoholicSpinner.setSelection(prefs.getSelectedFilterValuePos());
		}

		int prevFilter = prefs.getCurrentActiveFilter();
		int prevPos = prefs.getSelectedFilterValuePos();
		String prevVal = prefs.getSelectedFilterValue();

		categorySpinner.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				selectedFilter = Prefs.FILTER_TYPE_CATEGORY;
				return false;
			}
		});

		ingredientSpinner.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				selectedFilter = Prefs.FILTER_TYPE_INGREDIENT;
				return false;
			}
		});

		glassSpinner.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				selectedFilter = Prefs.FILTER_TYPE_GLASS;
				return false;
			}
		});

		alcoholicSpinner.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				selectedFilter = Prefs.FILTER_TYPE_ALCOHOLIC_NON_ALCOHOLIC;
				return false;
			}
		});

		categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
				if (selectedFilter == Prefs.FILTER_TYPE_CATEGORY) {
					if (pos == 0) {
						prefs.saveCurrentActiveFilter(Prefs.FILTER_TYPE_SEARCH);
					} else {
						prefs.saveCurrentActiveFilter(Prefs.FILTER_TYPE_CATEGORY);
						prefs.saveSelectedFilterValuePos(pos);
						prefs.saveSelectedFilterValue(categoryAdapter.getItem(pos).toString());
					}
					ingredientSpinner.setSelection(0);
					alcoholicSpinner.setSelection(0);
					glassSpinner.setSelection(0);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});


		ingredientSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
				if (selectedFilter == Prefs.FILTER_TYPE_INGREDIENT) {
					if (pos == 0) {
						prefs.saveCurrentActiveFilter(Prefs.FILTER_TYPE_SEARCH);
					} else {
						prefs.saveCurrentActiveFilter(Prefs.FILTER_TYPE_INGREDIENT);
						prefs.saveSelectedFilterValuePos(pos);
						prefs.saveSelectedFilterValue(ingredientAdapter.getItem(pos).toString());
						categorySpinner.setSelection(0);
						alcoholicSpinner.setSelection(0);
						glassSpinner.setSelection(0);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});


		glassSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
				if (selectedFilter == Prefs.FILTER_TYPE_GLASS) {
					if (pos == 0) {
						prefs.saveCurrentActiveFilter(Prefs.FILTER_TYPE_SEARCH);
					} else {
						prefs.saveCurrentActiveFilter(Prefs.FILTER_TYPE_GLASS);
						prefs.saveSelectedFilterValuePos(pos);
						prefs.saveSelectedFilterValue(glassAdapter.getItem(pos).toString());
						categorySpinner.setSelection(0);
						alcoholicSpinner.setSelection(0);
						ingredientSpinner.setSelection(0);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});


		alcoholicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
				if (selectedFilter == Prefs.FILTER_TYPE_ALCOHOLIC_NON_ALCOHOLIC) {
					if (pos == 0) {
						prefs.saveCurrentActiveFilter(Prefs.FILTER_TYPE_SEARCH);
					} else {
						prefs.saveCurrentActiveFilter(Prefs.FILTER_TYPE_ALCOHOLIC_NON_ALCOHOLIC);
						prefs.saveSelectedFilterValuePos(pos);
						prefs.saveSelectedFilterValue(alcoholicAdapter.getItem(pos).toString());
						categorySpinner.setSelection(0);
						ingredientSpinner.setSelection(0);
						glassSpinner.setSelection(0);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});

		AlertDialog.Builder builder= new AlertDialog.Builder(getActivity())
				.setTitle(R.string.dialog_filters)
				.setView(view)
				.setNegativeButton(R.string.btn_cancel, (dialog, whichButton) -> {
					prefs.saveCurrentActiveFilter(prevFilter);
					prefs.saveSelectedFilterValuePos(prevPos);
					prefs.saveSelectedFilterValue(prevVal);
				});
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
