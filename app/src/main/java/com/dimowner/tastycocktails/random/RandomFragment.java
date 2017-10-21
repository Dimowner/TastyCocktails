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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.cocktails.details.ImagePreviewActivity;
import com.dimowner.tastycocktails.cocktails.details.IngredientItem;
import com.dimowner.tastycocktails.cocktails.details.IngredientsAdapter;
import com.dimowner.tastycocktails.dagger.random.RandomCocktailModule;

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

	private MenuItem itemFavorite;
	private boolean isFavorite = false;
	private boolean isImageDark = true;
	private Toolbar activityToolbar;

	private IngredientsAdapter.OnSnackBarListener onSnackBarListener;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		TCApplication.get(getContext()).applicationComponent()
				.plus(new RandomCocktailModule(this)).injectDetailsFragment(this);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		mRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_details, container, false);
		return mRecyclerView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		if (savedInstanceState == null) {
			initAdapter();

			mPresenter.bindView(mAdapter);
			mPresenter.loadRandomDrink();
		}
	}

	public void loadRandomDrink() {
		mPresenter.loadRandomDrink();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_details, menu);
		itemFavorite = menu.findItem(R.id.action_add_to_favorites);
		updateFavorite(isFavorite);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_add_to_favorites) {
			mPresenter.reverseFavorite();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("is_favorite", isFavorite);
		outState.putBoolean("is_image_dark", isImageDark);
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
				activityToolbar.setNavigationIcon(R.drawable.menu);
			} else {
				activityToolbar.setNavigationIcon(R.drawable.menu_black);
			}
		});

		if (onSnackBarListener != null) {
			mAdapter.setOnSnackBarListener(onSnackBarListener);
		}
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
		if (itemFavorite != null) {
			if (isImageDark) {
				itemFavorite.setIcon(fav ? R.drawable.heart : R.drawable.heart_outline);
			} else {
				itemFavorite.setIcon(fav ? R.drawable.heart_black : R.drawable.heart_outline_black);
			}
		}
	}

	public void setActivityToolbar(Toolbar activityToolbar) {
		this.activityToolbar = activityToolbar;
	}

	public void setOnSnackBarListener(IngredientsAdapter.OnSnackBarListener onSnackBarListener) {
		if (mAdapter != null) {
			mAdapter.setOnSnackBarListener(onSnackBarListener);
		} else {
			this.onSnackBarListener = onSnackBarListener;
		}
	}
}
