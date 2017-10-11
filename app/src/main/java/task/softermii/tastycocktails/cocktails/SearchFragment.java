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

package task.softermii.tastycocktails.cocktails;

import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import task.softermii.tastycocktails.R;
import task.softermii.tastycocktails.TCApplication;
import task.softermii.tastycocktails.cocktails.details.DetailsActivity;
import task.softermii.tastycocktails.cocktails.list.CocktailsRecyclerAdapter;
import task.softermii.tastycocktails.cocktails.list.ListItem;
import task.softermii.tastycocktails.dagger.cocktails.CocktailsModule;
import task.softermii.tastycocktails.data.Prefs;
import task.softermii.tastycocktails.util.AndroidUtils;
import timber.log.Timber;

/**
 * Created on 26.07.2017.
 * @author Dimowner
 */
public class SearchFragment extends Fragment implements SearchContract.View {

	public static final int TYPE_NORMAL = 1;
	public static final int TYPE_FAVORITES = 2;
	public static final String EXTRAS_KEY_TYPE = "search_fragment_type";

	public static final int ADD_TO_FAVORITES_ANIMATION_DURATION = 400;

	private final String EXTRAS_KEY_ADAPTER_DATA = "adapter_data";

	private RecyclerView mRecyclerView;
	private ProgressBar mProgressBar;
	private LinearLayout mWelcomePanel;

	private CocktailsRecyclerAdapter mAdapter;

	private int fragmentType = TYPE_NORMAL;

	@Inject
	SearchContract.UserActionsListener mPresenter;

	@Inject
	Prefs prefs;

	public static SearchFragment newInstance(int fragmentType) {
		SearchFragment fragment = new SearchFragment();
		Bundle args = new Bundle();
		args.putInt(EXTRAS_KEY_TYPE, fragmentType);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		TCApplication.get(getContext()).applicationComponent()
				.plus(new CocktailsModule(this)).injectCocktailsSearch(this);

		if (getArguments().containsKey(EXTRAS_KEY_TYPE)) {
			fragmentType = getArguments().getInt(EXTRAS_KEY_TYPE);
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_search, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mWelcomePanel = view.findViewById(R.id.welcome_panel);
		mProgressBar = view.findViewById(R.id.progress);
		mRecyclerView = view.findViewById(R.id.recycler_view);
		mRecyclerView.setHasFixedSize(true);

		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		mPresenter.bindView(this);

		if (prefs.isFirstRun()) {
			mWelcomePanel.setVisibility(View.VISIBLE);
			mRecyclerView.setVisibility(View.GONE);
		}

		if (savedInstanceState == null) {
			mAdapter = new CocktailsRecyclerAdapter();
			mAdapter.setItemClickListener((view1, position) ->
					startDetailsActivity(mAdapter.getItem(position), view1));
			mAdapter.setOnFavoriteClickListener((view12, position, id, action) -> {
				if (AndroidUtils.isAndroid5()) {
					//Add or remove from favorites with animation
					view12.setImageResource(R.drawable.avd_favorite_progress);
					Animatable animatable = ((Animatable) view12.getDrawable());
					animatable.start();
					mPresenter.reverseFavorite(id)
							.subscribeOn(Schedulers.io())
							.delay(ADD_TO_FAVORITES_ANIMATION_DURATION, TimeUnit.MILLISECONDS)
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(animatable::stop,
									throwable -> {
										animatable.stop();
										Timber.e("", throwable);
									});
				} else {
					//Add or remove from favorites without animation
					mPresenter.reverseFavorite(id)
							.subscribeOn(Schedulers.io())
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(() -> {}, throwable -> Timber.e("", throwable));
				}
			});

			mRecyclerView.setAdapter(mAdapter);
			if (fragmentType == TYPE_NORMAL) {
				mPresenter.loadLastSearch(prefs.getLastSearchString());
			} else if (fragmentType == TYPE_FAVORITES) {
				mPresenter.loadFavorites();
			} else {
				Timber.e("Con't load data not correct fragment type!");
			}
		}
	}

	private void startDetailsActivity(ListItem item, View view1) {
		Intent intent = new Intent(getContext(), DetailsActivity.class);
		intent.putExtra(DetailsActivity.EXTRAS_KEY_ID, item.getId());

		//Transition
//		View txtName = view1.findViewById(R.id.list_item_name);
//		View txtDescription = view1.findViewById(R.id.list_item_description);
//		View ivImage = view1.findViewById(R.id.list_item_image);
//
//		ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(SearchFragment.this.getActivity(),
//				Pair.create(txtName, getResources().getString(R.string.list_item_label_transition)),
//				Pair.create(txtDescription, getResources().getString(R.string.list_item_content_transition)),
//				Pair.create(ivImage, getResources().getString(R.string.list_item_image_transition)));
//
//		startActivity(intent, options.toBundle());
		startActivity(intent);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mPresenter.unbindView();
		mPresenter = null;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_search, menu);
		final MenuItem searchMenu = menu.findItem(R.id.action_search);
		final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				if (fragmentType == TYPE_NORMAL) {
					//Save search query string
					prefs.setLastSearchString(query);

					mPresenter.startSearch(query);
				}
				return false;
			}

			@Override
			public boolean onQueryTextChange(final String newText) {
				if (fragmentType == TYPE_FAVORITES && mAdapter != null) {
					mAdapter.applyFilter(newText);
				}
				return false;
			}
		});

		// Get the search close button image view
		ImageView closeButton = searchView.findViewById(R.id.search_close_btn);

		// Set on click listener
		closeButton.setOnClickListener(v -> {
			mPresenter.cancelSearch();
			//Clear query
			searchView.setQuery("", false);
			//Collapse the action view
			searchView.onActionViewCollapsed();
			//Collapse the search widget
			searchMenu.collapseActionView();
		});
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mAdapter != null) {
			outState.putParcelable(EXTRAS_KEY_ADAPTER_DATA, mAdapter.onSaveInstanceState());
		}
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		if (savedInstanceState != null && savedInstanceState.containsKey(EXTRAS_KEY_ADAPTER_DATA)) {
			if (mAdapter == null) {
				mAdapter = new CocktailsRecyclerAdapter();
				mRecyclerView.setAdapter(mAdapter);
			}
			mAdapter.setItemClickListener((view1, position) ->
					startDetailsActivity(mAdapter.getItem(position), view1));
			mAdapter.onRestoreInstanceState(savedInstanceState.getParcelable(EXTRAS_KEY_ADAPTER_DATA));
		}
	}

	@Override
	public void showProgress() {
		mProgressBar.setVisibility(View.VISIBLE);
		mRecyclerView.setVisibility(View.GONE);
	}

	@Override
	public void hideProgress() {
		mProgressBar.setVisibility(View.GONE);
		mRecyclerView.setVisibility(View.VISIBLE);
	}

	@Override
	public void showQueryError() {
		Snackbar.make(mRecyclerView, R.string.msg_error_on_query, Snackbar.LENGTH_LONG).show();
	}

	@Override
	public void showNetworkError() {
		Snackbar.make(mRecyclerView, R.string.msg_error_no_internet, Snackbar.LENGTH_LONG).show();
	}

	@Override
	public void displayData(List<ListItem> data) {
		if (data.size() > 0 && prefs.isFirstRun()) {
			prefs.firstRunExecuted();
			mRecyclerView.setVisibility(View.VISIBLE);
			mWelcomePanel.setVisibility(View.GONE);
		}

		mAdapter.setData(data);
	}
}
