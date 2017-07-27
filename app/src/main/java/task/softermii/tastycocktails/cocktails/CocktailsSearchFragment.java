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
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.List;

import javax.inject.Inject;

import task.softermii.tastycocktails.R;
import task.softermii.tastycocktails.TCApplication;
import task.softermii.tastycocktails.cocktails.list.CocktailsRecyclerAdapter;
import task.softermii.tastycocktails.cocktails.list.ListItem;
import task.softermii.tastycocktails.dagger.cocktails.CocktailsModule;
import task.softermii.tastycocktails.details.DetailsActivity;
import timber.log.Timber;

/**
 * Created on 26.07.2017.
 * @author Dimowner
 */
public class CocktailsSearchFragment extends Fragment implements CocktailsSearchContract.View {

	private final String EXTRAS_KEY_ADAPTER_DATA = "adapter_data";
	public static final String EXTRAS_KEY_NAME_TRANSITION_NAME = "txt_name_transition_name";
	public static final String EXTRAS_KEY_DESCRIPTION_TRANSITION_NAME = "txt_description_transition_name";
	public static final String EXTRAS_KEY_IMAGE_TRANSITION_NAME = "txt_image_transition_name";

	private RecyclerView mRecyclerView;
	private ProgressBar mProgressBar;

	private CocktailsRecyclerAdapter mAdapter;

	@Inject
	CocktailsSearchContract.UserActionsListener mPresenter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		TCApplication.get(getContext()).applicationComponent()
				.plus(new CocktailsModule()).injectCocktailsSearch(this);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_search_cocktails, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
		mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
		mRecyclerView.setHasFixedSize(true);

		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		DividerItemDecoration divider = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
		divider.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.recycler_divider));
		mRecyclerView.addItemDecoration(divider);

		mAdapter = new CocktailsRecyclerAdapter();
		mAdapter.setItemClickListener((view1, position) -> {
			startDetailsActivity(mAdapter.getItem(position), view1);
		});

		mRecyclerView.setAdapter(mAdapter);

		mPresenter.bindView(this);
		mPresenter.loadLastSearch();
	}

	private void startDetailsActivity(ListItem item, View view1) {
		Intent intent = new Intent(getContext(), DetailsActivity.class);
		intent.putExtra(DetailsActivity.EXTRAS_KEY_COCKTAIL_ID, item.getId());
		intent.putExtra(DetailsActivity.EXTRAS_KEY_NAME, item.getName());
		intent.putExtra(DetailsActivity.EXTRAS_KEY_DESCRIPTION, item.getDescription());
		intent.putExtra(DetailsActivity.EXTRAS_KEY_IMAGE_URL, item.getAvatar_url());

		//Transition
		View txtName = view1.findViewById(R.id.list_item_name);
		View txtDescription = view1.findViewById(R.id.list_item_description);
		View ivImage = view1.findViewById(R.id.list_item_image);
		intent.putExtra(EXTRAS_KEY_NAME_TRANSITION_NAME, ViewCompat.getTransitionName(txtName));
		intent.putExtra(EXTRAS_KEY_DESCRIPTION_TRANSITION_NAME, ViewCompat.getTransitionName(txtDescription));
		intent.putExtra(EXTRAS_KEY_IMAGE_TRANSITION_NAME, ViewCompat.getTransitionName(ivImage));

		ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
				CocktailsSearchFragment.this.getActivity(),
				Pair.create(txtName, ViewCompat.getTransitionName(txtName)),
				Pair.create(txtDescription, ViewCompat.getTransitionName(txtDescription)),
				Pair.create(ivImage, ViewCompat.getTransitionName(ivImage)));

		startActivity(intent, options.toBundle());
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mPresenter.unbindView();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.search_menu, menu);
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
	public void showError(Throwable throwable) {
		Timber.e(throwable);
		Snackbar.make(mRecyclerView, R.string.msg_error_on_query, Snackbar.LENGTH_LONG).show();
	}

	@Override
	public void displayData(List<ListItem> data) {
		mAdapter.setData(data);
	}
}
