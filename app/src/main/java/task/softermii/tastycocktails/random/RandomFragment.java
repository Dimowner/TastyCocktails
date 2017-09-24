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

package task.softermii.tastycocktails.random;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import task.softermii.tastycocktails.R;
import task.softermii.tastycocktails.TCApplication;
import task.softermii.tastycocktails.cocktails.details.IngredientsAdapter;
import task.softermii.tastycocktails.dagger.random.RandomCocktailModule;

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
			mAdapter = new IngredientsAdapter();
			mAdapter.setFavoriteUpdateListener(fav -> {
				isFavorite = fav;
				updateFavorite(isFavorite);
			});
			mRecyclerView.setAdapter(mAdapter);

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
			mPresenter.addToFavorites();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("is_favorite", isFavorite);
		if (mAdapter != null) {
			outState.putParcelable(EXTRAS_KEY_ADAPTER_DATA, mAdapter.onSaveInstanceState());
		}
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		if (savedInstanceState != null && savedInstanceState.containsKey(EXTRAS_KEY_ADAPTER_DATA)) {
			isFavorite = savedInstanceState.getBoolean("is_favorite");
			updateFavorite(isFavorite);
			if (mAdapter == null) {
				mAdapter = new IngredientsAdapter();
				mRecyclerView.setAdapter(mAdapter);
			}
			mAdapter.setFavoriteUpdateListener(fav -> {
				isFavorite = fav;
				updateFavorite(isFavorite);
			});
			mPresenter.bindView(mAdapter);
			mAdapter.onRestoreInstanceState(savedInstanceState.getParcelable(EXTRAS_KEY_ADAPTER_DATA));
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mPresenter.unbindView();
		mPresenter = null;
	}

	private void updateFavorite(boolean fav) {
		if (itemFavorite != null) {
			itemFavorite.setIcon(fav ? R.drawable.heart : R.drawable.heart_outline);
		}
	}
}
