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

package com.dimowner.tastycocktails.data;

import android.support.annotation.NonNull;

import java.util.Date;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.data.model.Drink;
import com.dimowner.tastycocktails.data.model.Drinks;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
public class Repository implements RepositoryContract {

	private LocalRepository localRepository;
	private RemoteRepository remoteRepository;

	public Repository(@NonNull LocalRepository localRepository,
							@NonNull RemoteRepository remoteRepository) {
		this.localRepository = localRepository;
		this.remoteRepository = remoteRepository;
	}

	@Override
	public Flowable<List<Drink>> searchCocktailsByName(@NonNull String search) {
		remoteRepository.searchCocktailsByName(search)
				.subscribeOn(Schedulers.io())
				.doOnNext(drinks -> localRepository.cacheDrinks(drinks))
				.subscribe(drinks -> {}, Timber::e);
		return localRepository.searchCocktailsByName(search);
	}

	@Override
	public Single<List<Drink>> getDrinksHistory(int page) {
		return localRepository.getDrinksHistory(page);
	}

	@Override
	public Flowable<List<Drink>> loadDrinksWithFilter(int filterType, String value) {
		remoteRepository.loadDrinksWithFilter(filterType, value)
				.subscribeOn(Schedulers.io())
				.doOnNext(drinks -> localRepository.cacheDrinks(drinks))
				.subscribe(drinks -> {}, Timber::e);
		return localRepository.loadDrinksWithFilter(filterType, value);
	}

	@Override
	public Flowable<List<Drink>> loadFilteredDrinks(String category, String ingredient, String glass, String alcoholic) {
		return localRepository.loadFilteredDrinks(category, ingredient, glass, alcoholic);
	}

	@Override
	public Single<Drink> getRandomCocktail() {
		if (TCApplication.isConnected()) {
			return remoteRepository.getRandomCocktail()
					.doOnSuccess(drink -> {
						drink.setHistory(new Date().getTime());
						localRepository.cacheIntoLocalDatabase(drink);
					});
		} else {
			return localRepository.getRandomCocktail();
		}
	}

	@Override
	public Flowable<Drink> getCocktailRx(long id) {
		return localRepository.getCocktailRx(id).doOnNext(drink -> {
			if (!drink.isCached()) {
				remoteRepository.getCocktailRx(id)
						.subscribe(d -> localRepository.cacheIntoLocalDatabase(d), Timber::e);
			}
		});
	}

	@Override
	public Single<Drink> getLocalCocktailRx(long id) {
		return localRepository.getLocalCocktailRx(id);
	}

	@Override
	public Flowable<List<Drink>> getLastSearch(String query) {
		return localRepository.getLastSearch(query);
	}

	@Override
	public Flowable<List<Drink>> getFavorites() {
		return localRepository.getFavorites();
	}

	@Override
	public Flowable<List<Drink>> getIngredients() {
		return remoteRepository.getIngredients();
	}

	@Override
	public Completable addToFavorites(Drink drink) {
		return localRepository.addToFavorites(drink);
	}

	@Override
	public Completable removeFromFavorites(long id) {
		return localRepository.removeFromFavorites(id);
	}

	@Override
	public Completable reverseFavorite(long id) {
		return localRepository.reverseFavorite(id);
	}

	@Override
	public Completable updateDrinkHistory(long id, long time) {
		return localRepository.updateDrinkHistory(id, time);
	}

	@Override
	public Completable clearHistory() {
		return localRepository.clearHistory();
	}

	@Override
	public Completable removeFromHistory(long id) {
		return localRepository.removeFromHistory(id);
	}

	@Override
	public Single<Drink[]> cacheIntoLocalDatabase(Drinks drinks) {
		return localRepository.cacheIntoLocalDatabase(drinks);
	}
}
