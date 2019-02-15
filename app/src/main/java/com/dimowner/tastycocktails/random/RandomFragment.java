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

package com.dimowner.tastycocktails.random;

import android.app.ActivityOptions;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import javax.inject.Inject;

import com.dimowner.tastycocktails.AdvHandler;
import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.analytics.MixPanel;
import com.dimowner.tastycocktails.cocktails.details.ImagePreviewActivity;
import com.dimowner.tastycocktails.cocktails.details.IngredientsAdapter;
import com.dimowner.tastycocktails.dagger.random.RandomCocktailModule;
import com.dimowner.tastycocktails.data.Prefs;
import com.dimowner.tastycocktails.util.AndroidUtils;
import com.dimowner.tastycocktails.util.AnimationUtil;
import com.dimowner.tastycocktails.util.UIUtil;
import com.dimowner.tastycocktails.widget.ThresholdListener;
import com.dimowner.tastycocktails.widget.TouchLayout;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
public class RandomFragment extends Fragment implements View.OnClickListener {

//	private final String EXTRAS_KEY_ADAPTER_DATA = "adapter_data";
	public final static String TAG = "RandomFragment";

	@Inject
	RandomContract.UserActionsListener mPresenter;

	@Inject
	Prefs prefs;

	private RecyclerView mRecyclerView;
	private IngredientsAdapter mAdapter;

	private boolean isFavorite;
	private boolean isCreated;
	private int defaultBtnUpY = 0;

	private String ing1 = null;
	private String ing2 = null;

	private ImageButton btnMenu;
	private ImageButton btnFavorite;
	private FloatingActionButton fab;
	private CoordinatorLayout mRoot;
	private AdvHandler advHandler;
	private TouchLayout touchLayout;
	private Button btnFilters;
	private Button btnClear;
	private Button btnClose;
	private TextView txtFiltersSelected;

	private Spinner ingredientSpinner;
	private Spinner ingredientSpinner2;

	private int panelPadding = 0;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_random, container, false);
		mRecyclerView = view.findViewById(R.id.recycler_view);
		TCApplication.get(getContext()).applicationComponent()
				.plus(new RandomCocktailModule(this)).injectDetailsFragment(this);
		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		panelPadding = (int) getResources().getDimension(R.dimen.panel_padding) + (int) getResources().getDimension(R.dimen.padding_standard);
		mRoot = view.findViewById(R.id.coordinator);
		btnFavorite = view.findViewById(R.id.btn_favorite);
		btnMenu = view.findViewById(R.id.btn_menu);
		txtFiltersSelected = view.findViewById(R.id.txtFilters);
		btnFilters = view.findViewById(R.id.btn_filters);
		btnClear = view.findViewById(R.id.btn_clear);
		btnClose = view.findViewById(R.id.btn_close);
		btnFilters.setOnClickListener(this);
		btnClose.setOnClickListener(this);
		btnClear.setOnClickListener(this);

		touchLayout = view.findViewById(R.id.touch_layout);
		touchLayout.setOnThresholdListener(new ThresholdListener() {
			@Override
			public void onTopThreshold() {
				if (touchLayout.getReturnPositionY() == panelPadding) {
					hideFilters();
				} else {
					showFilters();
				}
			}

			@Override
			public void onBottomThreshold() {
				if (touchLayout.getReturnPositionY() == panelPadding) {
					hideFilters();
				} else {
					showFilters();
				}
			}

			@Override public void onTouchDown() {}
			@Override public void onTouchUp() {}
		});

		//Calculate position by default for btn up
		ViewTreeObserver vto = touchLayout.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				ViewTreeObserver obs = touchLayout.getViewTreeObserver();
				defaultBtnUpY = touchLayout.getHeight() - (int) getResources().getDimension(R.dimen.collapsed_panel_height);
				touchLayout.setTranslationY(defaultBtnUpY);
				touchLayout.setReturnPositionY(defaultBtnUpY);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					obs.removeOnGlobalLayoutListener(this);
				} else {
					obs.removeGlobalOnLayoutListener(this);
				}
			}
		});

		btnMenu.setOnClickListener(this);
		fab = view.findViewById(R.id.fab);
		fab.setOnClickListener(this);

		btnFavorite.setOnClickListener(this);

		initFiltersPanel(view);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// Set the padding to match the Status Bar height
			view.findViewById(R.id.title_bar).setPadding(0, AndroidUtils.getStatusBarHeight(getContext()), 0, 0);
		}

		if (savedInstanceState == null) {
			initAdapter();

			mPresenter.bindView(mAdapter);
		}

		AdView adView = view.findViewById(R.id.adView);
		advHandler = new AdvHandler(adView, prefs);

		TCApplication.event(getActivity().getApplicationContext(), MixPanel.EVENT_RANDOM);
	}

	private void showFilters() {
		touchLayout.setReturnPositionY(panelPadding);
		mAdapter.showBottomPanelMargin(true);
		AnimationUtil.verticalSpringAnimation(touchLayout, panelPadding,
				(animation, canceled, value, velocity) -> {
					btnFilters.setVisibility(View.GONE);
					btnClear.setVisibility(View.VISIBLE);
					btnClose.setVisibility(View.VISIBLE);
					mRecyclerView.smoothScrollBy(0, touchLayout.getHeight());
				});
		AnimationUtil.verticalSpringAnimation(fab, -touchLayout.getHeight() + panelPadding + fab.getHeight()/2 + (int)getResources().getDimension(R.dimen.padding_xdouble));
	}

	private void hideFilters() {
		touchLayout.setReturnPositionY(defaultBtnUpY);
		mAdapter.showBottomPanelMargin(false);
		AnimationUtil.verticalSpringAnimation(touchLayout, defaultBtnUpY,
				(animation, canceled, value, velocity) -> {
					btnFilters.setVisibility(View.VISIBLE);
					btnClear.setVisibility(View.GONE);
					btnClose.setVisibility(View.GONE);
				});
		AnimationUtil.verticalSpringAnimation(fab, 0);
	}

	@Override
	public void onResume() {
		super.onResume();
		advHandler.onResume();
		if (!isCreated) {
			List<String> list = new ArrayList<>();
			if (ing1 != null) {
				list.add(ing1);
			}
			if (ing2 != null) {
				list.add(ing2);
			}

			mPresenter.loadRandomDrink(list);
			fab.show();
			AnimationUtil.physBasedRevealAnimation(fab);
			isCreated = true;
		}
	}

	@Override
	public void onPause() {
		advHandler.onPause();
		super.onPause();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		updateFavorite(isFavorite);
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("is_favorite", isFavorite);
		outState.putBoolean("is_created", isCreated);
		if (mAdapter != null) {
//			outState.putParcelable(EXTRAS_KEY_ADAPTER_DATA, mAdapter.onSaveInstanceState());
			mAdapter.onSaveAdapterState(outState);
		}
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		if (savedInstanceState != null) {
			isFavorite = savedInstanceState.getBoolean("is_favorite");
			isCreated = savedInstanceState.getBoolean("is_created");
//			isCreated = false;
			updateFavorite(isFavorite);
			initAdapter();

			mPresenter.bindView(mAdapter);
//			mAdapter.onRestoreInstanceState(savedInstanceState.getParcelable(EXTRAS_KEY_ADAPTER_DATA));
			mAdapter.onRestoreAdapterSate(savedInstanceState);
			fab.show();
		}
	}

	private void initAdapter() {
		if (mAdapter == null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				mAdapter = new IngredientsAdapter(getActivity().isInMultiWindowMode());
			} else {
				mAdapter = new IngredientsAdapter();
			}

			mRecyclerView.setAdapter(mAdapter);
		}

		mAdapter.setItemClickListener((view1, position) ->
				startIngredientDetailsActivity(mAdapter.getItem(position).getImageUrl()));

		mAdapter.setFavoriteUpdateListener(fav -> {
			isFavorite = fav;
			updateFavorite(isFavorite);
		});

		mAdapter.setOnImageClickListener(this::startIngredientDetailsActivity);

		btnFavorite.setImageResource(isFavorite ? R.drawable.round_heart: R.drawable.round_heart_border);

		mAdapter.setOnSnackBarListener(new IngredientsAdapter.OnSnackBarListener() {
			@Override
			public void showSnackBar(String message) {
				Snackbar.make(mRoot, message, Snackbar.LENGTH_LONG).show();
			}

			@Override
			public void showAlertDialog(int resId) {
				UIUtil.showInfoDialog(getActivity(), resId);
			}
		});
	}

	@Override
	public void onDestroyView() {
		advHandler.onDestroy();
		super.onDestroyView();
		mPresenter.unbindView();
		mPresenter = null;
	}

	private void startIngredientDetailsActivity(String url) {
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			startActivity(ImagePreviewActivity.getStartIntent(getContext(), url),
					ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
		} else {
			startActivity(ImagePreviewActivity.getStartIntent(getContext(), url));
		}
	}

	private void updateFavorite(boolean fav) {
//		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			btnFavorite.setImageResource(fav ? R.drawable.round_heart : R.drawable.round_heart_border);
//		} else {
//			btnFavorite.setImageResource(fav ? R.drawable.heart: R.drawable.heart_outline);
//		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_filters) {
			showFilters();
		} else if (v.getId() == R.id.btn_close) {
			hideFilters();
		} else if (v.getId() == R.id.btn_clear) {
			ingredientSpinner.setSelection(0);
			ingredientSpinner2.setSelection(0);
			prefs.clearFilters();
		} else if (v.getId() == R.id.btn_menu) {
			DrawerLayout dl = getActivity().findViewById(R.id.drawer_layout);
			if (dl != null) {
				dl.openDrawer(Gravity.START);
			}
		} else if (v.getId() == R.id.fab) {
			List<String> list = new ArrayList<>();
			if (ing1 != null) {
				list.add(ing1);
			}
			if (ing2 != null) {
				list.add(ing2);
			}

			mPresenter.loadRandomDrink(list);
			TCApplication.event(getActivity().getApplicationContext(), MixPanel.EVENT_NEW_RANDOM_DRINK);
		} else if (v.getId() == R.id.btn_favorite) {
			mPresenter.reverseFavorite();
		}
	}

	private void initFiltersPanel(View view) {
		//Init INGREDIENTS filter
		ingredientSpinner = view.findViewById(R.id.filter_ingredients);
		ingredientSpinner2 = view.findViewById(R.id.filter_ingredients2);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> ingredientAdapter = ArrayAdapter.createFromResource(getContext(),
				R.array.filter_ingredients_alphabetical, R.layout.spinner_item);
		// Specify the layout to use when the list of choices appears
		ingredientAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> ingredientAdapter2 = ArrayAdapter.createFromResource(getContext(),
				R.array.filter_ingredients_alphabetical, R.layout.spinner_item);
		// Specify the layout to use when the list of choices appears
		ingredientAdapter2.setDropDownViewResource(R.layout.spinner_dropdown_item);

		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> ingredientAdapter3 = ArrayAdapter.createFromResource(getContext(),
				R.array.filter_ingredients_alphabetical, R.layout.spinner_item);
		// Specify the layout to use when the list of choices appears
		ingredientAdapter3.setDropDownViewResource(R.layout.spinner_dropdown_item);

		// Apply the adapter to the spinner
		ingredientSpinner.setAdapter(ingredientAdapter);
		ingredientSpinner2.setAdapter(ingredientAdapter2);

		View.OnTouchListener listener = (v, event) -> {
			v.performClick();
			return false;
		};

		ingredientSpinner.setOnTouchListener(listener);
		ingredientSpinner2.setOnTouchListener(listener);

		ingredientSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
				if (pos == 0) {
					ing1 = null;
				} else {
					CharSequence ing = ingredientAdapter.getItem(pos);
					if (ing != null) {
						ing1 = ing.toString();
					}
				}
				updateSelectedFilters();
			}
			@Override public void onNothingSelected(AdapterView<?> adapterView) { }
		});
		ingredientSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
				if (pos == 0) {
					ing2 = null;
				} else {
					CharSequence ing = ingredientAdapter.getItem(pos);
					if (ing != null) {
						ing2 = ing.toString();
					}
				}
				updateSelectedFilters();
			}
			@Override public void onNothingSelected(AdapterView<?> adapterView) { }
		});
	}

	private void updateSelectedFilters() {
		StringBuilder sb = new StringBuilder();
		if (ing1 != null && !ing1.isEmpty()) {
			sb.append(ing1);
		}
		if (ing2 != null && !ing2.isEmpty()) {
			if (ing1 != null && !ing1.isEmpty()) {
				sb.append(", ");
			}
			sb.append(ing2);
		}
		txtFiltersSelected.setText(sb.toString());
	}
}
