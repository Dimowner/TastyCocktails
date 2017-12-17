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

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.cocktails.details.DetailsActivity;
import com.dimowner.tastycocktails.cocktails.list.CocktailsRecyclerAdapter;
import com.dimowner.tastycocktails.cocktails.list.EndlessRecyclerViewScrollListener;
import com.dimowner.tastycocktails.cocktails.list.ListItem;
import com.dimowner.tastycocktails.dagger.cocktails.CocktailsModule;
import com.dimowner.tastycocktails.data.Prefs;
import com.dimowner.tastycocktails.util.AndroidUtils;
import com.dimowner.tastycocktails.util.UIUtil;

import timber.log.Timber;

/**
 * Created on 26.07.2017.
 * @author Dimowner
 */
public class SearchFragment extends Fragment implements SearchContract.View {

	public static final int TYPE_UNKNOWN = 0;
	public static final int TYPE_NORMAL = 1;
	public static final int TYPE_FAVORITES = 2;
	public static final int TYPE_HISTORY = 3;
	public static final String EXTRAS_KEY_TYPE = "search_fragment_type";

	public static final int ADD_TO_FAVORITES_ANIMATION_DURATION = 400;

//	private final String EXTRAS_KEY_ADAPTER_DATA = "adapter_data";

	private RecyclerView mRecyclerView;
	private ProgressBar mProgressBar;
	private LinearLayout mWelcomePanel;
	private TextView mTxtEmpty;
	private FrameLayout mRoot;

	private CocktailsRecyclerAdapter mAdapter;

	private MenuItem searchMenu;

	private int fragmentType = TYPE_UNKNOWN;

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
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_search, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mRoot = view.findViewById(R.id.coordinator_root);
		mWelcomePanel = view.findViewById(R.id.welcome_panel);
		mTxtEmpty = view.findViewById(R.id.txt_empty);
		mProgressBar = view.findViewById(R.id.progress);
		mRecyclerView = view.findViewById(R.id.recycler_view);
		mRecyclerView.setHasFixedSize(true);

		// use a linear layout manager
		RecyclerView.LayoutManager mLayoutManager = new AppLinearLayoutManager(getContext());
		mRecyclerView.setLayoutManager(mLayoutManager);
		mRecyclerView.addOnScrollListener(new MyScrollListener(mLayoutManager));

		mPresenter.bindView(this);

		if (prefs.isFirstRun() && fragmentType == TYPE_NORMAL) {
			mWelcomePanel.setVisibility(View.VISIBLE);
			mTxtEmpty.setVisibility(View.GONE);
			mRecyclerView.setVisibility(View.GONE);
		}

//		if (savedInstanceState == null) {
			initAdapter();

			if (prefs.isFirstRun()) {
				prefs.saveCurrentActiveFilter(Prefs.FILTER_TYPE_CATEGORY);
				prefs.saveSelectedFilterValuePos(0);
				String[] categories = getResources().getStringArray(R.array.filter_categories);
				prefs.saveSelectedFilterValue(categories[0]);
				Button btnGetStarted = view.findViewById(R.id.get_started);
				btnGetStarted.setOnClickListener(view1 -> {
					prefs.firstRunExecuted();
					mWelcomePanel.setVisibility(View.GONE);
					mPresenter.loadBuildList(prefs.getCurrentActiveFilter(), prefs.getSelectedFilterValue());
				});
			} else {
				if (fragmentType == TYPE_NORMAL) {
					int filter = prefs.getCurrentActiveFilter();
					if (filter == Prefs.FILTER_TYPE_SEARCH) {
						mPresenter.loadLastSearch(prefs.getLastSearchString());
						if (getActivity() != null && ((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
							((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.search, prefs.getLastSearchString()));
						}
					} else {
						mPresenter.loadBuildList(prefs.getCurrentActiveFilter(), prefs.getSelectedFilterValue());
						if (getActivity() != null && ((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
							((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
						}
					}
				} else if (fragmentType == TYPE_FAVORITES) {
					mPresenter.loadFavorites();
				} else if (fragmentType == TYPE_HISTORY) {
					mPresenter.loadHistory(1);
				} else {
					Timber.e("Con't load data not correct fragment type!");
				}
			}
//		}
	}

	private void initAdapter() {
		if (mAdapter == null) {
			mAdapter = new CocktailsRecyclerAdapter(fragmentType, prefs);
			mRecyclerView.setAdapter(mAdapter);
		}
		mAdapter.setItemClickListener((view1, position) ->
				startDetailsActivity(mAdapter.getItem(position), view1));
		mAdapter.setOnFavoriteClickListener((view12, position, id, action) -> {
			final boolean fev = mAdapter.getItem(position).isFavorite();
			final String name = mAdapter.getItem(position).getName();
			if (AndroidUtils.isAndroid5()) {
				//Add or remove from favorites with animation
				view12.setImageResource(R.drawable.avd_favorite_progress);
				Animatable animatable = ((Animatable) view12.getDrawable());
				animatable.start();
				//TODO: refactor this into presenter
				mPresenter.reverseFavorite(id)
						.subscribeOn(Schedulers.io())
						.delay(ADD_TO_FAVORITES_ANIMATION_DURATION, TimeUnit.MILLISECONDS)
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(() -> {
									animatable.stop();
									showSnackBar(id, !fev, name);
								},
								throwable -> {
									animatable.stop();
									Timber.e(throwable);
								});
			} else {
				//Add or remove from favorites without animation
				mPresenter.reverseFavorite(id)
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(() -> showSnackBar(id, !fev, name), Timber::e);
			}
		});
		if (fragmentType == TYPE_HISTORY) {
			mAdapter.setItemLongClickListener((view, id, position) ->
					UIUtil.showWarningDialog(
							getActivity(),
							R.drawable.delete_forever_black, //Dialog title icon
							R.string.remove_from_history,  //Dialog title text
							(dialogInterface, i) -> mPresenter.removeFromHistory(id), //Callback for positive button
							(dialogInterface, i) -> dialogInterface.dismiss() //Callback for negative button
					)
			);
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

	private void showSnackBar(long id, boolean isFavorite, String drinkName) {
		if (isFavorite) {
			Snackbar.make(mRoot, getString(R.string.added_to_favorites, drinkName), Snackbar.LENGTH_LONG).show();
		} else {
			if (fragmentType == TYPE_NORMAL || fragmentType == TYPE_HISTORY) {
				Snackbar.make(mRoot, getString(R.string.removed_from_favorites, drinkName), Snackbar.LENGTH_LONG).show();
			} else {
				Snackbar snackbar = Snackbar
						.make(mRoot, getString(R.string.removed_from_favorites, drinkName) , Snackbar.LENGTH_LONG)
						.setAction(R.string.undo, view ->
								mPresenter.reverseFavorite(id)
									.subscribeOn(Schedulers.io())
									.delay(ADD_TO_FAVORITES_ANIMATION_DURATION, TimeUnit.MILLISECONDS)
									.observeOn(AndroidSchedulers.mainThread())
									.subscribe(() -> Snackbar.make(mRoot,
											getString(R.string.added_to_favorites, drinkName), Snackbar.LENGTH_LONG).show(), Timber::e));

				snackbar.show();
			}
		}
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

		if (fragmentType != TYPE_HISTORY) {
			MenuItem clearHistory = menu.findItem(R.id.action_clear_history);
			clearHistory.setVisible(false);
		}
		MenuItem filters = menu.findItem(R.id.action_filter);
		if (fragmentType != TYPE_NORMAL) {
			filters.setVisible(false);
		}

		searchMenu = menu.findItem(R.id.action_search);
		final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				if (fragmentType == TYPE_NORMAL) {
//					TODO: should move this logic into presenter
					//Save search query string
					if (TCApplication.isConnected()) {
						prefs.setLastSearchString(query);
						prefs.saveCurrentActiveFilter(Prefs.FILTER_TYPE_SEARCH);

						mPresenter.startSearch(query);
						if (getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
							((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.search, prefs.getLastSearchString()));
						}
						if (prefs.isFirstRun()) {
							prefs.firstRunExecuted();
							mWelcomePanel.setVisibility(View.GONE);
						}
					} else {
						showNetworkError();
					}
				}
				return false;
			}

			@Override
			public boolean onQueryTextChange(final String newText) {
				if ((fragmentType == TYPE_FAVORITES || fragmentType == TYPE_HISTORY) && mAdapter != null) {
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
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_clear_history) {
			if (mAdapter.getItemCount() > 0) {
				UIUtil.showWarningDialog(
						getActivity(),
						R.drawable.delete_forever_black,
						R.string.do_you_really_want_clear_history,
						(dialogInterface, i) -> mPresenter.clearHistory(),
						(dialogInterface, i) -> dialogInterface.dismiss()
				);
			} else {
				Toast.makeText(getContext(), R.string.history_already_empty, Toast.LENGTH_LONG).show();
			}
		} else if (item.getItemId() == R.id.action_filter) {
			showFilterDialog();
			if (searchMenu.isActionViewExpanded()) {
				searchMenu.collapseActionView();
			}
		}
		return super.onOptionsItemSelected(item);
	}

	private void showFilterDialog() {
		if (getActivity() != null) {
			FragmentManager fm = getActivity().getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			Fragment prev = fm.findFragmentByTag("dialog_filters");
			if (prev != null) {
				ft.remove(prev);
			}
			ft.addToBackStack(null);
			FiltersDialog dialog = new FiltersDialog();
			dialog.setPositiveButtonListener((dialogInterface, i) -> {
				if (prefs.isFirstRun()) {
					prefs.firstRunExecuted();
					mWelcomePanel.setVisibility(View.GONE);
				}
				if (prefs.getCurrentActiveFilter() == Prefs.FILTER_TYPE_SEARCH) {
					mPresenter.loadLastSearch(prefs.getLastSearchString());
					if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
						((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.search, prefs.getLastSearchString()));
					}
				} else {
					mPresenter.loadBuildList(prefs.getCurrentActiveFilter(), prefs.getSelectedFilterValue());
					if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
						((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
					}
				}
			});
			dialog.show(ft, "dialog_filters");
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("fragment_type", fragmentType);
//		if (mAdapter != null) {
//			outState.putParcelable(EXTRAS_KEY_ADAPTER_DATA, mAdapter.onSaveInstanceState());
//		}
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		if (savedInstanceState != null) {
//				&& savedInstanceState.containsKey(EXTRAS_KEY_ADAPTER_DATA)) {
			fragmentType = savedInstanceState.getInt("fragment_type");
//			initAdapter();
//			mAdapter.onRestoreInstanceState(savedInstanceState.getParcelable(EXTRAS_KEY_ADAPTER_DATA));
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
		mTxtEmpty.setVisibility(View.VISIBLE);
		Snackbar.make(mRecyclerView, R.string.msg_error_on_query, Snackbar.LENGTH_LONG).show();
	}

	@Override
	public void showNetworkError() {
		Snackbar.make(mRecyclerView, R.string.msg_error_no_internet, Snackbar.LENGTH_LONG).show();
	}

	@Override
	public void displayData(List<ListItem> data) {
		if (prefs.isFirstRun() && fragmentType == TYPE_NORMAL) {
			mRecyclerView.setVisibility(View.GONE);
			mWelcomePanel.setVisibility(View.VISIBLE);
			mTxtEmpty.setVisibility(View.GONE);
		} else if (data.size() == 0) {
			mRecyclerView.setVisibility(View.GONE);
			mWelcomePanel.setVisibility(View.GONE);
			mTxtEmpty.setVisibility(View.VISIBLE);
			if (fragmentType == TYPE_FAVORITES) {
				mTxtEmpty.setText(R.string.no_favorite_drinks);
			} else if (fragmentType == TYPE_HISTORY) {
				mTxtEmpty.setText(R.string.history_is_empty);
			} else {
				mTxtEmpty.setText(R.string.click_search_to_find_some_drink);
			}
		} else {
			mRecyclerView.setVisibility(View.VISIBLE);
			mTxtEmpty.setVisibility(View.GONE);
			mAdapter.setData(data);
//			mRecyclerView.post(() -> mAdapter.addItems(data));
		}
	}


	public class MyScrollListener extends EndlessRecyclerViewScrollListener {

		<L extends RecyclerView.LayoutManager> MyScrollListener(L layoutManager) {
			super(layoutManager);
		}

		@Override
		public void onLoadMore(int page, int totalItemsCount) {
			Timber.d("onLoadMore page = " + page + " count = " + totalItemsCount);
//			if (fragmentType == TYPE_HISTORY) {
//				mPresenter.loadHistory(page);
//			}
		}
	}

	/**
	 * Simple extension of LinearLayoutManager for the sole purpose of showing what happens
	 * when predictive animations (which are enabled by default in LinearLayoutManager) are
	 * not enabled. This behavior is toggled via a checkbox in the UI.
	 */
	public class AppLinearLayoutManager extends LinearLayoutManager {
		AppLinearLayoutManager(Context context) {
			super(context);
		}

		@Override
		public boolean supportsPredictiveItemAnimations() {
			return true;
		}
	}
}
