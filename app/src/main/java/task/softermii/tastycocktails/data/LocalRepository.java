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

import android.content.Context;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import task.softermii.tastycocktails.data.model.Drink;
import task.softermii.tastycocktails.data.room.AppDatabase;
import task.softermii.tastycocktails.data.room.CocktailsDao;
import timber.log.Timber;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
public class LocalRepository implements RepositoryContract {

	private WeakReference<Context> weakContext;

	public LocalRepository(Context context) {
		this.weakContext = new WeakReference<>(context);
	}

	@Override
	public Single<List<Drink>> searchCocktailsByName(@NonNull String search) {
		throw new RuntimeException("This method is supported only in RemoteRepository");
	}

	@Override
	public Single<List<Drink>> searchCocktailsByIngredient(@NonNull String ingredient) {
		throw new RuntimeException("This method is supported only in RemoteRepository");
	}

	@Override
	public Single<Drink> getRandomCocktail() {
		return getRepositoriesDao().getRowCount().subscribeOn(Schedulers.io()).flatMap(count -> {
			if (count > 0) {
				return getRepositoriesDao()
						.getRandom()
						.subscribeOn(Schedulers.io());
			} else {
				return Single.fromCallable(Drink::emptyDrink);
			}
		});
	}

	@Override
	public Single<Drink> getCocktail(long id) {
		throw new RuntimeException("This method is supported only in RemoteRepository");
	}

	@Override
	public Single<List<Drink>> getLastSearch() {
		return getRepositoriesDao().getRowCount().subscribeOn(Schedulers.io()).flatMap(count -> {
			if (count > 0) {
				return getRepositoriesDao().getAll().subscribeOn(Schedulers.io());
			} else {
				return Single.fromCallable(() -> {
					List<Drink> list = new ArrayList<>(1);
					list.add(Drink.emptyDrink());
					return list;
				});
			}
		});
	}

	/**
	 * Rewrite local cached Drinks
	 * @param items new Drinks to save.
	 */
	public void rewriteRepositories(List<Drink> items) {
		Single.just(items).map(data -> {
				getRepositoriesDao().deleteAll();
				getRepositoriesDao().insertAll(data.toArray(new Drink[data.size()]));
				return null;
			}).subscribeOn(Schedulers.io()).subscribe((o, throwable) -> Timber.e(throwable));
	}

	private CocktailsDao getRepositoriesDao() {
		return AppDatabase.getInstance(weakContext.get()).repositoriesDao();
	}
}
