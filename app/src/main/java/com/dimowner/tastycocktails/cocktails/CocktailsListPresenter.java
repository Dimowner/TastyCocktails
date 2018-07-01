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

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Context;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import com.dimowner.tastycocktails.ModelMapper;
import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.cocktails.list.ListItem;
import com.dimowner.tastycocktails.data.RepositoryContract;
import com.dimowner.tastycocktails.data.model.Drink;
import com.dimowner.tastycocktails.data.model.Drinks;
import com.google.gson.Gson;

import timber.log.Timber;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
public class CocktailsListPresenter extends AndroidViewModel implements CocktailsListContract.UserActionsListener {

	private RepositoryContract repository;

	private CocktailsListContract.View view;

	private CompositeDisposable compositeDisposable = new CompositeDisposable();

	public CocktailsListPresenter(Application application) {
		super(application);
	}

	public void setRepository(RepositoryContract repository) {
		this.repository = repository;
	}

	@Override
	public void bindView(@NonNull CocktailsListContract.View view) {
		this.view = view;
	}

	@Override
	public void unbindView() {
		if (compositeDisposable.size() > 0) {
			compositeDisposable.clear();
		}
		this.view = null;
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		compositeDisposable.dispose();
	}

	@Override
	public void startSearch(String search) {
		view.showProgress();
		if (compositeDisposable.size() > 0) {
			compositeDisposable.clear();
		}
		compositeDisposable.add(
				repository.searchCocktailsByName(search)
						.map(ModelMapper::drinksToListItems)
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(this::displayData, this::handleError));
	}

	@Override
	public void cancelSearch() {
		view.hideProgress();
		if (compositeDisposable.size() > 0) {
			compositeDisposable.clear();
		}
	}

	private void displayData(List<ListItem> data) {
		view.hideProgress();
		view.displayData(data);
	}

	private void handleError(Throwable throwable) {
		Timber.e(throwable);
		view.hideProgress();
		if (TCApplication.isConnected()) {
			view.showQueryError();
		} else {
			view.showNetworkError();
		}
	}

	@Override
	public void loadLastSearch(String query) {
		view.showProgress();
		compositeDisposable.clear();
		compositeDisposable.add(
				repository.getLastSearch(query)
						.map(ModelMapper::drinksToListItems)
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(this::displayData, this::handleError));
	}

	@Override
	public void loadFavorites() {
		view.showProgress();
		compositeDisposable.clear();
		compositeDisposable.add(
				repository.getFavorites()
						.map(ModelMapper::drinksToListItems)
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(this::displayData, this::handleError));
	}

	@Override
	public void loadHistory(int page) {
		view.showProgress();
		compositeDisposable.clear();
		compositeDisposable.add(
				repository.getDrinksHistory(page)
						.map(ModelMapper::drinksToListItems)
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(this::displayData, this::handleError));
	}

//	@Override
//	public void loadBuildList(int filterType, String filterVal) {
//		view.showProgress();
//		compositeDisposable.clear();
//		compositeDisposable.add(
//				repository.loadDrinksWithFilter(filterType, filterVal)
//						.map(ModelMapper::drinksToListItems)
//						.subscribeOn(Schedulers.io())
//						.observeOn(AndroidSchedulers.mainThread())
//						.subscribe(this::displayData, this::handleError));
//	}

	@Override
	public void loadFilteredList(String category, String ingredient, String glass, String alcoholic) {
		view.showProgress();
		compositeDisposable.clear();
		compositeDisposable.add(
				repository.loadFilteredDrinks(category, ingredient, glass, alcoholic)
						.map(ModelMapper::drinksToListItems)
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(this::displayData, this::handleError));
	}

	@Override
	public void clearHistory() {
		view.showProgress();
		compositeDisposable.add(
				repository.clearHistory()
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(() -> {displayData(new ArrayList<>());}, this::handleError));
	}

	@Override
	public void returnToHistory(long id, long time) {
		compositeDisposable.add(repository.updateDrinkHistory(id, time)
				.subscribeOn(Schedulers.io())
				.subscribe(() -> {}, Timber::e));
	}

	@Override
	public void removeFromHistory(long id) {
		view.showProgress();
		compositeDisposable.add(
				repository.removeFromHistory(id)
						.subscribeOn(Schedulers.io())
						.subscribe(() -> {}, this::handleError));
	}

	@Override
	public Completable reverseFavorite(long id) {
		return repository.reverseFavorite(id);
	}

	@Override
	public Single<Drink[]> firstRunInitialization(Context context) {
		String json;
		try {
			InputStream is = context.getAssets().open("drinks_json.txt");
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			json = new String(buffer, "UTF-8");

			Gson gson = new Gson();
			Drinks drinks = gson.fromJson(json, Drinks.class);
			List<Drink> cachedFev = new ArrayList<>();
			return repository.getFavoritesCount()
					.subscribeOn(Schedulers.io())
					.flatMap(count -> {
						if (count > 0) {
							cachedFev.addAll(repository.getFavoritesDrinks());
							repository.clearAll();
							return repository.cacheIntoLocalDatabase(drinks)
									.doOnSuccess(v -> {
										for (int i = 0; i < cachedFev.size(); i++) {
											repository.reverseFavorite(cachedFev.get(i).getIdDrink())
													.subscribeOn(Schedulers.io())
													.subscribe();
										}
									});
						} else {
							repository.clearAll();
							return repository.cacheIntoLocalDatabase(drinks);
						}
					});
		} catch (IOException ex) {
			Timber.e(ex);
			return Single.error(ex);
		}
	}
}
