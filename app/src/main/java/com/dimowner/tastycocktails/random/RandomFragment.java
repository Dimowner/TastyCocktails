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

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import javax.inject.Inject;

import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.cocktails.details.ImagePreviewActivity;
import com.dimowner.tastycocktails.cocktails.details.IngredientItem;
import com.dimowner.tastycocktails.cocktails.details.IngredientsAdapter;
import com.dimowner.tastycocktails.dagger.random.RandomCocktailModule;
import com.dimowner.tastycocktails.util.AndroidUtils;
import com.dimowner.tastycocktails.util.AnimationUtil;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
public class RandomFragment extends Fragment {

	private final String EXTRAS_KEY_ADAPTER_DATA = "adapter_data";

	@Inject
	RandomContract.UserActionsListener mPresenter;

	private RecyclerView mRecyclerView;
	private IngredientsAdapter mAdapter;

	private boolean isFavorite = false;
	private boolean isImageDark = true;
	private boolean isCreated = false;

	private ImageButton btnMenu;
	private ImageButton btnFavorite;
	private FloatingActionButton fab;
	private CoordinatorLayout mRoot;
	private View.OnClickListener openMenuListener;

//	private IngredientsAdapter.OnSnackBarListener onSnackBarListener;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		TCApplication.get(getContext()).applicationComponent()
				.plus(new RandomCocktailModule(this)).injectDetailsFragment(this);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_random, container, false);
		mRecyclerView = view.findViewById(R.id.recycler_view);
		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		mRoot = view.findViewById(R.id.coordinator);
		btnFavorite = view.findViewById(R.id.btn_favorite);
		btnMenu = view.findViewById(R.id.btn_menu);
		fab = view.findViewById(R.id.fab);
		fab.setOnClickListener(v -> mPresenter.loadRandomDrink());

		btnFavorite.setOnClickListener(v -> mPresenter.reverseFavorite());

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// Set the padding to match the Status Bar height
			view.findViewById(R.id.title_bar).setPadding(0, AndroidUtils.getStatusBarHeight(getContext()), 0, 0);
		}

		if (savedInstanceState == null) {
			initAdapter();

			mPresenter.bindView(mAdapter);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (openMenuListener != null) {
			btnMenu.setOnClickListener(openMenuListener);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!isCreated) {
			mPresenter.loadRandomDrink();
//			isCreated = true;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		updateFavorite(isFavorite);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("is_favorite", isFavorite);
		outState.putBoolean("is_image_dark", isImageDark);
		outState.putBoolean("is_created", isCreated);
		if (mAdapter != null) {
			outState.putParcelable(EXTRAS_KEY_ADAPTER_DATA, mAdapter.onSaveInstanceState());
		}
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		if (savedInstanceState != null && savedInstanceState.containsKey(EXTRAS_KEY_ADAPTER_DATA)) {
			isFavorite = savedInstanceState.getBoolean("is_favorite");
			isImageDark = savedInstanceState.getBoolean("is_image_dark");
			isCreated = savedInstanceState.getBoolean("is_created");
			updateFavorite(isFavorite);
			initAdapter();

			mPresenter.bindView(mAdapter);
			mAdapter.onRestoreInstanceState(savedInstanceState.getParcelable(EXTRAS_KEY_ADAPTER_DATA));
		}
	}

	private void initAdapter() {
		if (mAdapter == null) {
			mAdapter = new IngredientsAdapter();
			mRecyclerView.setAdapter(mAdapter);
		}

		mAdapter.setItemClickListener((view1, position) ->
				startIngredientDetailsActivity(mAdapter.getItem(position), view1));

		mAdapter.setFavoriteUpdateListener(fav -> {
			isFavorite = fav;
			updateFavorite(isFavorite);
		});

		mAdapter.setOnImageClickListener(path -> {
			Intent intent = new Intent(getContext(), ImagePreviewActivity.class);
			intent.putExtra(ImagePreviewActivity.EXTRAS_KEY_IMAGE_PATH, path);
			startActivity(intent);
		});

		mAdapter.setOnCheckImageColorListener(isDark -> {
			isImageDark = isDark;
			updateFavorite(isFavorite);
			if (isDark) {
				btnMenu.setImageResource(R.drawable.menu);
			} else {
				btnMenu.setImageResource(R.drawable.menu_black);
			}
			fab.setVisibility(View.VISIBLE);
			if (!isCreated) {
				AnimationUtil.physBasedRevealAnimation(fab);
				isCreated = true;
			}
		});
		mAdapter.setOnSnackBarListener(message -> Snackbar.make(mRoot, message, Snackbar.LENGTH_LONG).show());
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mPresenter.unbindView();
		mPresenter = null;
	}

	private void startIngredientDetailsActivity(IngredientItem item, View view1) {
		Intent intent = new Intent(getContext(), ImagePreviewActivity.class);
		intent.putExtra(ImagePreviewActivity.EXTRAS_KEY_IMAGE_PATH, item.getImageUrl());
		startActivity(intent);
	}

	private void updateFavorite(boolean fav) {
		if (isImageDark) {
			btnFavorite.setImageResource(fav ? R.drawable.heart : R.drawable.heart_outline);
		} else {
			btnFavorite.setImageResource(fav ? R.drawable.heart_black : R.drawable.heart_outline_black);
		}
	}

	public void setOpenMenuListener(View.OnClickListener openMenuListener) {
		this.openMenuListener = openMenuListener;
	}

}
