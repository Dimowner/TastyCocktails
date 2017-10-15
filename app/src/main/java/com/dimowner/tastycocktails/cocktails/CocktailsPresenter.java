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
import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import com.dimowner.tastycocktails.ModelMapper;
import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.cocktails.list.ListItem;
import com.dimowner.tastycocktails.data.RepositoryContract;
import timber.log.Timber;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
public class CocktailsPresenter extends AndroidViewModel implements SearchContract.UserActionsListener {

	private RepositoryContract repository;

	private SearchContract.View view;

	private CompositeDisposable compositeDisposable = new CompositeDisposable();

	public CocktailsPresenter(Application application) {
		super(application);
	}

	public void setRepository(RepositoryContract repository) {
		this.repository = repository;
	}

	@Override
	public void bindView(@NonNull SearchContract.View view) {
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
		compositeDisposable.add(
				repository.getFavorites()
						.map(ModelMapper::drinksToListItems)
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(this::displayData, this::handleError));
	}

	@Override
	public void loadHistory() {
		view.showProgress();
		compositeDisposable.add(
				repository.getDrinksHistory()
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
						.subscribe(() -> {}, this::handleError));
	}

	@Override
	public Completable reverseFavorite(long id) {
		return repository.reverseFavorite(id);
	}
}
