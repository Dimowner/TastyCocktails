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

import android.arch.persistence.db.SimpleSQLiteQuery;
import android.arch.persistence.db.SupportSQLiteQuery;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.data.model.Drink;
import com.dimowner.tastycocktails.data.model.Drinks;
import com.dimowner.tastycocktails.data.room.AppDatabase;
import com.dimowner.tastycocktails.data.room.CocktailsDao;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import timber.log.Timber;
import static com.dimowner.tastycocktails.util.LogUtil.log2file;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
public class LocalRepository implements RepositoryContract {

	@Inject
	AppDatabase appDatabase;

	public LocalRepository(Context context) {
		TCApplication.get(context).applicationComponent().inject(this);
	}

	private CocktailsDao getRepositoriesDao() {
		return appDatabase.cocktailsDao();
	}

	@Override
	public Flowable<List<Drink>> searchCocktailsByName(@NonNull String search) {
		search = "%" + search + "%";
		return getRepositoriesDao().searchDrinksRx(search);
	}

	@Override
	public Flowable<List<Drink>> searchCocktailsByNameLocal(@NonNull String search) {
		throw new UnsupportedOperationException("This method is supported only in Repository Please use searchCocktailsByName");
	}

//	@Override
//	public Single<List<Drink>> searchCocktailsByIngredient(@NonNull String ingredient) {
//		throw new RuntimeException("This method is supported only in RemoteRepository");
//	}

	@Override
	public Flowable<List<Drink>> getDrinksHistory(int page) {
		return getRepositoriesDao().getDrinksHistory();
	}

//	@Override
//	public Flowable<List<Drink>> loadDrinksWithFilter(int filterType, String value) {
//		if (filterType == Prefs.FILTER_TYPE_CATEGORY) {
//			return getRepositoriesDao().searchDrinksByCategory(value);
//		} else if (filterType == Prefs.FILTER_TYPE_INGREDIENT) {
//			return getRepositoriesDao().searchDrinksByIngredient(value);
//		} else if (filterType == Prefs.FILTER_TYPE_GLASS) {
//			return getRepositoriesDao().searchDrinksByGlass(value);
//		} else if (filterType == Prefs.FILTER_TYPE_ALCOHOLIC_NON_ALCOHOLIC) {
//			return getRepositoriesDao().searchDrinksByAlcoholic(value);
//		} else {
//			throw new UnsupportedOperationException("This is not implemented yet");
//		}
//	}

	@Override
	public Flowable<List<Drink>> loadFilteredDrinks(String category, String ingredient, String glass, String alcoholic) {

		int argsCount = 0;
		StringBuilder sb = new StringBuilder();
		String[] args;
		if (!TextUtils.isEmpty(ingredient)) {
			args = new String[] {
					ingredient, ingredient, ingredient, ingredient, ingredient,
					ingredient, ingredient, ingredient, ingredient, ingredient
			};
			argsCount = 10;

			sb.append("SELECT * FROM drinks WHERE UPPER(strIngredient1) LIKE UPPER(?)");
			sb.append(" OR UPPER(strIngredient2) LIKE UPPER(?)");
			sb.append(" OR UPPER(strIngredient3) LIKE UPPER(?)");
			sb.append(" OR UPPER(strIngredient4) LIKE UPPER(?)");
			sb.append(" OR UPPER(strIngredient5) LIKE UPPER(?)");
			sb.append(" OR UPPER(strIngredient6) LIKE UPPER(?)");
			sb.append(" OR UPPER(strIngredient7) LIKE UPPER(?)");
			sb.append(" OR UPPER(strIngredient8) LIKE UPPER(?)");
			sb.append(" OR UPPER(strIngredient9) LIKE UPPER(?)");
			sb.append(" OR UPPER(strIngredient10) LIKE UPPER(?)");
			sb.append(" ORDER BY strDrink");
		} else {
			sb.append("SELECT * FROM drinks WHERE");

			if (!TextUtils.isEmpty(category)) {
				sb.append(" UPPER(strCategory) LIKE UPPER(?)");
				argsCount++;
			}
			if (!TextUtils.isEmpty(glass)) {
				if (!TextUtils.isEmpty(category)) {
					sb.append(" AND");
				}
				sb.append(" UPPER(strGlass) LIKE UPPER(?)");
				argsCount++;
			}

			if (!TextUtils.isEmpty(alcoholic)) {
				if (!TextUtils.isEmpty(category) || !TextUtils.isEmpty(glass)) {
					sb.append(" AND");
				}
				argsCount++;
				sb.append(" UPPER(strAlcoholic) LIKE UPPER(?)");
			}

			sb.append(" ORDER BY strDrink");

			args = new String[argsCount];
			if (!TextUtils.isEmpty(category)) {
				args[0] = category;
				if (!TextUtils.isEmpty(glass)) {
					args[1] = glass;
					if (!TextUtils.isEmpty(alcoholic)) {
						args[2] = alcoholic;
					}
				} else {
					if (!TextUtils.isEmpty(alcoholic)) {
						args[1] = alcoholic;
					}
				}
			} else {
				if (!TextUtils.isEmpty(glass)) {
					args[0] = glass;
					if (!TextUtils.isEmpty(alcoholic)) {
						args[1] = alcoholic;
					}
				} else {
					if (!TextUtils.isEmpty(alcoholic)) {
						args[0] = alcoholic;
					}
				}
			}
		}

		Timber.v("RAW QUERY : %s, args %s", sb.toString(), Arrays.toString(args));

		SupportSQLiteQuery query;
		if (argsCount > 0) {
			 query = new SimpleSQLiteQuery(sb.toString(), args);
		} else {
			query = new SimpleSQLiteQuery("SELECT * FROM drinks ORDER BY strDrink");
		}

		return getRepositoriesDao().getFiltered(query)
				.map(drinks -> {
					if (!TextUtils.isEmpty(ingredient) && (!TextUtils.isEmpty(category) || !TextUtils.isEmpty(glass) || !TextUtils.isEmpty(alcoholic))) {
						for (int i = drinks.size() - 1; i >= 0; i--) {
							Drink drink = drinks.get(i);
							//TODO: FIX FILTER
							if (!((!TextUtils.isEmpty(category) && drink.getStrCategory().equalsIgnoreCase(category))
									|| (!TextUtils.isEmpty(glass) && drink.getStrGlass().equalsIgnoreCase(glass))
									|| (!TextUtils.isEmpty(alcoholic) && drink.getStrAlcoholic().equalsIgnoreCase(alcoholic)))) {
								Timber.v("Remove dring i = " + i);
								drinks.remove(i);
							}
						}
					}
					return drinks;
				});
	}

	@Override
	public Flowable<List<Drink>> loadFilteredDrinks2(String category, List<String> ingredients, String glass, String alcoholic) {
		StringBuilder sb = new StringBuilder();
		int argsCount = 0;
		String[] args;

		sb.append("SELECT * FROM drinks WHERE");

		if (!TextUtils.isEmpty(category)) {
			sb.append(" UPPER(strCategory) LIKE UPPER(?)");
			argsCount++;
		}
		if (!TextUtils.isEmpty(glass)) {
			if (!TextUtils.isEmpty(category)) {
				sb.append(" AND");
			}
			sb.append(" UPPER(strGlass) LIKE UPPER(?)");
			argsCount++;
		}

		if (!TextUtils.isEmpty(alcoholic)) {
			if (!TextUtils.isEmpty(category) || !TextUtils.isEmpty(glass)) {
				sb.append(" AND");
			}
			argsCount++;
			sb.append(" UPPER(strAlcoholic) LIKE UPPER(?)");
		}

		sb.append(" ORDER BY strDrink");

		args = new String[argsCount];
		if (!TextUtils.isEmpty(category)) {
			args[0] = category;
			if (!TextUtils.isEmpty(glass)) {
				args[1] = glass;
				if (!TextUtils.isEmpty(alcoholic)) {
					args[2] = alcoholic;
				}
			} else {
				if (!TextUtils.isEmpty(alcoholic)) {
					args[1] = alcoholic;
				}
			}
		} else {
			if (!TextUtils.isEmpty(glass)) {
				args[0] = glass;
				if (!TextUtils.isEmpty(alcoholic)) {
					args[1] = alcoholic;
				}
			} else {
				if (!TextUtils.isEmpty(alcoholic)) {
					args[0] = alcoholic;
				}
			}
		}

		Timber.v("RAW QUERY : %s, args %s", sb.toString(), Arrays.toString(args));

		SupportSQLiteQuery query;
		if (argsCount > 0) {
			query = new SimpleSQLiteQuery(sb.toString(), args);
		} else {
			query = new SimpleSQLiteQuery("SELECT * FROM drinks ORDER BY strDrink");
		}

		Timber.v("ingredients: " + ingredients.toString());
		return getRepositoriesDao().getFiltered(query)
				.map(drinks -> {
					if (ingredients.size() > 0) {
						for (int i = drinks.size() - 1; i >= 0; i--) {
							Drink drink = drinks.get(i);
							for (int j = 0; j < ingredients.size(); j++) {
								if (!drink.hasIngredient(ingredients.get(j))) {
									drinks.remove(i);
									break;
								}
							}
						}
					}
					return drinks;
				});
	}

	@Override
	public Single<Drink> getRandomCocktail() {
		return getRepositoriesDao().getRandom();
//		return getRepositoriesDao().getLastSearchRowCount().subscribeOn(Schedulers.io()).flatMap(count -> {
//			if (count > 0) {
//				return getRepositoriesDao()
//						.getRandom()
//						.subscribeOn(Schedulers.io());
//			} else {
//				return Single.fromCallable(Drink::emptyDrink);
//			}
//		});
	}

	@Override
	public Flowable<Drink> getCocktailRx(long id) {
//		//This method called here to prevent infinite loop in flowable
//		updateDrinkHistory(id, new Date().getTime())
//				.subscribeOn(Schedulers.io())
//				.subscribe(() -> {}, Timber::e);
		return getRepositoriesDao().getDrinkRx(id).distinct();
//		return getRepositoriesDao().getLastSearchRowCount().subscribeOn(Schedulers.io()).flatMap(count -> {
//			if (count > 0) {
//				return getRepositoriesDao()
//						.getDrinkRx(id)
//						.subscribeOn(Schedulers.io());
//			} else {
//				return Single.fromCallable(Drink::emptyDrink);
//			}
//		});
	}

	@Override
	public Single<Drink> getLocalCocktailRx(long id) {
		//TODO: add history update.
		return getRepositoriesDao().getDrinkSingle(id);
	}

	@Override
	public Flowable<List<Drink>> getLastSearch(String query) {
		if (query == null) {
			//When last query empty return empty list
			query = " ) AND 1=0";
		} else {
			query = "%" + query + "%";
		}
		return getRepositoriesDao().searchDrinksRx(query);
	}

	@Override
	public Flowable<List<Drink>> getFavorites() {
		return getRepositoriesDao().getFavorites();
	}

	@Override
	public List<Drink> getFavoritesDrinks() {
		return getRepositoriesDao().getFavoritesDrinks();
	}

	@Override
	public Single<Integer> getFavoritesCount() {
		return getRepositoriesDao().getFavoritesRowCount();
	}

	@Override
	public Flowable<List<Drink>> getIngredients() {
		throw new UnsupportedOperationException("This method is supported only in RemoteRepository");
	}

	@Override
	public Completable addToFavorites(Drink drink) {
		drink.inverseFavorite();
		return Completable.fromAction(() -> {
			if (getRepositoriesDao().getDrink(drink.getIdDrink()) != null) {
				Drink d = getRepositoriesDao().getDrink(drink.getIdDrink());
				if (!d.isFavorite()) {
					d.inverseFavorite();
					getRepositoriesDao().updateDrink(d);
				}
			} else {
				getRepositoriesDao().insertDrink(drink);
			}
		});
	}

	@Override
	public Completable removeFromFavorites(long id) {
		return Completable.fromAction(() -> getRepositoriesDao().removeFromFavorites(id));
	}

	@Override
	public Completable reverseFavorite(long id) {
		return Completable.fromAction(() -> getRepositoriesDao().reverseFavorite(id));
	}

	@Override
	public Completable updateDrinkHistory(long id, long time) {
		return Completable.fromAction(() -> getRepositoriesDao().updateDrinkHistory(id, time));
	}

	@Override
	public Completable clearHistory() {
		return Completable.fromAction(() -> getRepositoriesDao().clearHistory());
	}

	@Override
	public void clearAll() {
		getRepositoriesDao().deleteAll();
	}

	@Override
	public Completable removeFromHistory(long id) {
		return Completable.fromAction(() -> getRepositoriesDao().updateDrinkHistory(id, 0));
	}

	void cacheDrinks(List<Drink> drinks) {
		for (int i = 0; i < drinks.size(); i++) {
			if (drinks.get(i).getStrInstructions() != null && !drinks.get(i).getStrInstructions().isEmpty()) {
				drinks.get(i).setCached(true);
			}
		}
		getRepositoriesDao().insertAll(drinks.toArray(new Drink[drinks.size()]));
	}

	/**
	 * Cache list of drinks into local database
	 * @param items new Drinks to save.
	 */
	public void cacheIntoLocalDatabase(List<Drink> items) {
		Single.just(items).map(data -> {
				getRepositoriesDao().insertAll(data.toArray(new Drink[data.size()]));
				return null;
			})
			.subscribeOn(Schedulers.io())
			.subscribe((o, throwable) -> Timber.e(throwable));
	}

	/**
	 * Cache list of drinks into local database
	 * @param items new Drinks to save.
	 */
	public Single<Drink[]> cacheIntoLocalDatabase(Drinks items) {
		return Single.just(items.getDrinks())
				.doOnSuccess(data -> getRepositoriesDao().insertAllWithReplace(data))
				.subscribeOn(Schedulers.io());
	}

	/**
	 * Cache list of drinks into local database
	 * @param item new Drink to save.
	 */
	void cacheIntoLocalDatabase(Drink item) {
		item.setHistory(new Date().getTime());
		item.setCached(true);
		Single.just(item).map(data -> {
			if (getRepositoriesDao().checkDrinkExists(item.getIdDrink()) == 1) {
				Drink d = getRepositoriesDao().getDrink(item.getIdDrink());
				if (d.isFavorite()) {
					item.inverseFavorite();
				}
			}
			item.setHistory(new Date().getTime());

			getRepositoriesDao().insertDrink(item);
			return null;
		})
				.subscribeOn(Schedulers.io())
				.subscribe((o, throwable) -> Timber.e(throwable));
	}

	private void cacheIntoFile() {
		getRepositoriesDao().getAll()
				.subscribeOn(Schedulers.io())
				.subscribe(drinks -> {
					ArrayList<Long> ids = new ArrayList<>(drinks.size());
					for (int i = 0; i < drinks.size(); i++) {
						drinks.get(i).setHistory(0);
						ids.add(drinks.get(i).getIdDrink());
						Gson gson = new GsonBuilder().create();
						String json = gson.toJson(drinks.get(i));
						log2file(json +",\n");
					}
					Timber.v("Drinks count = " + drinks.size() + " ids: " + ids.toString());}, Timber::e);
	}
}
