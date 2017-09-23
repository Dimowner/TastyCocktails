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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
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
import android.widget.ProgressBar;

import java.util.List;

import javax.inject.Inject;

import task.softermii.tastycocktails.R;
import task.softermii.tastycocktails.TCApplication;
import task.softermii.tastycocktails.cocktails.details.DetailsActivity;
import task.softermii.tastycocktails.cocktails.list.CocktailsRecyclerAdapter;
import task.softermii.tastycocktails.cocktails.list.ListItem;
import task.softermii.tastycocktails.dagger.cocktails.CocktailsModule;

/**
 * Created on 26.07.2017.
 * @author Dimowner
 */
public class SearchFragment extends Fragment implements SearchContract.View {

	private final String EXTRAS_KEY_ADAPTER_DATA = "adapter_data";

	private RecyclerView mRecyclerView;
	private ProgressBar mProgressBar;

	private CocktailsRecyclerAdapter mAdapter;

	@Inject
	SearchContract.UserActionsListener mPresenter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		TCApplication.get(getContext()).applicationComponent()
				.plus(new CocktailsModule(this)).injectCocktailsSearch(this);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_search, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mProgressBar = view.findViewById(R.id.progress);
		mRecyclerView = view.findViewById(R.id.recycler_view);
		mRecyclerView.setHasFixedSize(true);

		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		mPresenter.bindView(this);

		if (savedInstanceState == null) {
			mAdapter = new CocktailsRecyclerAdapter();
			mAdapter.setItemClickListener((view1, position) ->
					startDetailsActivity(mAdapter.getItem(position), view1));
			mRecyclerView.setAdapter(mAdapter);
			mPresenter.loadLastSearch();
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
				mPresenter.startSearch(query);
				return false;
			}

			@Override
			public boolean onQueryTextChange(final String newText) {
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
		mAdapter.setData(data);
	}
}
