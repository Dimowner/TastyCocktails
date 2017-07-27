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

import java.util.ArrayList;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import task.softermii.tastycocktails.R;
import task.softermii.tastycocktails.data.LocalRepository;
import task.softermii.tastycocktails.data.RemoteRepository;
import task.softermii.tastycocktails.data.Repository;
import task.softermii.tastycocktails.data.model.Drink;
import timber.log.Timber;

/**
 * Created on 26.07.2017.
 * @author Dimowner
 */
public class CocktailsSearchFragment extends Fragment {

	private final String EXTRAS_KEY_ADAPTER_DATA = "adapter_data";
	public static final String EXTRAS_KEY_NAME_TRANSITION_NAME = "txt_name_transition_name";
	public static final String EXTRAS_KEY_DESCRIPTION_TRANSITION_NAME = "txt_description_transition_name";
	public static final String EXTRAS_KEY_IMAGE_TRANSITION_NAME = "txt_image_transition_name";

	public static final int DEFAULT_ITEM_WIDTH = 120;//px

	private RecyclerView mRecyclerView;

	private CocktailsRecyclerAdapter mAdapter;


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setRetainInstance(true);
		setHasOptionsMenu(true);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_search_cocktails, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
		mRecyclerView.setHasFixedSize(true);

		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		DividerItemDecoration divider = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
		divider.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.recycler_divider));
		mRecyclerView.addItemDecoration(divider);

		mAdapter = new CocktailsRecyclerAdapter();
		mAdapter.setItemClickListener((view1, position) -> {
			ListItem item = mAdapter.getItem(position);
			Intent intent = new Intent(getContext(), CocktailDetailsActivity.class);
			intent.putExtra(CocktailDetailsActivity.EXTRAS_KEY_COCKTAIL_ID, item.getId());
			intent.putExtra(CocktailDetailsActivity.EXTRAS_KEY_NAME, item.getName());
			intent.putExtra(CocktailDetailsActivity.EXTRAS_KEY_DESCRIPTION, item.getDescription());
			intent.putExtra(CocktailDetailsActivity.EXTRAS_KEY_IMAGE_URL, item.getAvatar_url());

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

		});
		searchCocktails();

		mRecyclerView.setAdapter(mAdapter);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.search_menu, menu);
		final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				Timber.v("onQueryTextSubmit q = " + query);
				LocalRepository localRepository = new LocalRepository(getContext());
				RemoteRepository remoteRepository = new RemoteRepository();
				remoteRepository.setOnLoadListener(localRepository::rewriteRepositories);

				Repository repository = new Repository(getContext(), localRepository, remoteRepository);
				repository.searchCocktailsByName(query)
						.map(CocktailsSearchFragment.this::convertModel)
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(CocktailsSearchFragment.this::displayData, CocktailsSearchFragment.this::handleError);
				return false;
			}

			@Override
			public boolean onQueryTextChange(final String newText) {
				Timber.v("onQueryTextChange t = " + newText);
				return false;
			}
		});
	}

	private void searchCocktails() {
		LocalRepository localRepository = new LocalRepository(getContext());
		RemoteRepository remoteRepository = new RemoteRepository();
		remoteRepository.setOnLoadListener(localRepository::rewriteRepositories);

		Repository repository = new Repository(getContext(), localRepository, remoteRepository);
		repository.getLastSearch()
				.map(this::convertModel)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::displayData, this::handleError);

	}

	private void handleError(Throwable throwable) {
		Timber.e(throwable);
		//TODO: make retry
		Snackbar
				.make(mRecyclerView, R.string.error_on_query, Snackbar.LENGTH_LONG)
				.setAction(R.string.retry, view -> {})
				.show();
	}

	private List<ListItem> convertModel(List<Drink> drinks) {
		List<ListItem> list = new ArrayList<>(drinks.size());
		for (int i = 0; i < drinks.size(); i++) {
			Drink drink = drinks.get(i);
			list.add(new ListItem(drink.getIdDrink(), drink.getStrDrink(), drink.getStrInstructions(), drink.getStrDrinkThumb()));
		}
		return list;
	}

	private ListItem convertModel(Drink drink) {
		if (drink.getIdDrink() != Drink.NO_ID) {
			return new ListItem(drink.getIdDrink(), drink.getStrDrink(), drink.getStrInstructions(), drink.getStrDrinkThumb());
		} else {
			return null;
		}
	}

	private void displayData(List<ListItem> data) {
		if (data.size() > 0) {
			mAdapter.setData(data);
		} else {
			Snackbar.make(mRecyclerView, R.string.did_not_find_anything, Snackbar.LENGTH_LONG).show();
		}
	}

	private void displayData(ListItem data) {
		List<ListItem> list = new ArrayList<>(1);
		list.add(data);
		mAdapter.setData(list);
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
}
