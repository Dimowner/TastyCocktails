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

package task.softermii.tastycocktails.data;

import android.support.annotation.NonNull;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import task.softermii.tastycocktails.TCApplication;
import task.softermii.tastycocktails.data.model.Drink;

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
	public Single<List<Drink>> searchCocktailsByName(@NonNull String search) {
		return remoteRepository.searchCocktailsByName(search);
	}

	@Override
	public Single<List<Drink>> searchCocktailsByIngredient(@NonNull String ingredient) {
		return remoteRepository.searchCocktailsByIngredient(ingredient);
	}

	@Override
	public Single<Drink> getRandomCocktail() {
		if (TCApplication.isConnected()) {
			return remoteRepository.getRandomCocktail();
		} else {
			return localRepository.getRandomCocktail();
		}
	}

	@Override
	public Single<Drink> getCocktail(long id) {
		return localRepository.getCocktail(id);
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
}
