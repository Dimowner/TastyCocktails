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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import com.dimowner.tastycocktails.data.model.Drink;
import com.dimowner.tastycocktails.data.model.Drinks;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
public class RemoteRepository implements RepositoryContract {

	private static final String API_KEY = "8362";
	private static final String API_URL = "http://www.thecocktaildb.com/api/json/v1/" + API_KEY + "/";

	private Retrofit retrofit;

	private CocktailApi cocktailApi;

	private OnLoadListener onLoadListener;


	@Override
	public Flowable<List<Drink>> searchCocktailsByName(@NonNull String search) {
		return getCocktailApi()
					.searchByName(search)
					.map(this::convertDrinksToListWithCaching)
					.subscribeOn(Schedulers.io());

	}

	@Override
	public Single<List<Drink>> searchCocktailsByIngredient(@NonNull String ingredient) {
		return getCocktailApi()
					.searchByIngredient(ingredient)
					.map(this::convertDrinksToListWithCaching)
					.subscribeOn(Schedulers.io());
	}

	@Override
	public Flowable<List<Drink>> getDrinksHistory(int page) {
		throw new UnsupportedOperationException("This method is supported only in LocalRepository");
	}

	@Override
	public Flowable<List<Drink>> loadDrinksWithFilter(int filterType, String value) {
		if (filterType == Prefs.FILTER_TYPE_CATEGORY) {
			return getCocktailApi().searchByCategory(value)
					.map(this::convertDrinksToListWithCaching)
					.map(drinks -> {
						for (int i = 0; i < drinks.size(); i++) {
							drinks.get(i).setStrCategory(value);
						}
						return drinks;
					});
		} else {
//			TODO: add implementation
			throw new UnsupportedOperationException("This is not implemented yet");
		}
	}

	@Override
	public Single<Drink> getRandomCocktail() {
		return getCocktailApi()
				.getRandom()
				.map(this::convertDrinksToDrink)
				.subscribeOn(Schedulers.io());
	}

	@Override
	public Flowable<Drink> getCocktailRx(long id) {
		return getCocktailApi()
				.getCocktail(id)
				.map(this::convertDrinksToDrink)
				.subscribeOn(Schedulers.io());
	}

	@Override
	public Flowable<List<Drink>> getLastSearch(String query) {
		throw new UnsupportedOperationException("This method is supported only in LocalRepository");
	}

	@Override
	public Flowable<List<Drink>> getFavorites() {
		throw new UnsupportedOperationException("This method is supported only in LocalRepository");
	}

	@Override
	public Completable addToFavorites(Drink drink) {
		throw new UnsupportedOperationException("This method is supported only in LocalRepository");
	}

	@Override
	public Completable removeFromFavorites(long id) {
		throw new UnsupportedOperationException("This method is supported only in LocalRepository");
	}

	@Override
	public Completable reverseFavorite(long id) {
		throw new UnsupportedOperationException("This method is supported only in LocalRepository");
	}

	@Override
	public Completable updateDrinkHistory(long id, long time) {
		throw new UnsupportedOperationException("This method is supported only in LocalRepository");
	}

	@Override
	public Completable clearHistory() {
		throw new UnsupportedOperationException("This method is supported only in LocalRepository");
	}

	@Override
	public Completable removeFromHistory(long id) {
		throw new UnsupportedOperationException("This method is supported only in LocalRepository");
	}

	private CocktailApi getCocktailApi() {
		if (retrofit == null) {
			HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
			interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
			OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

			retrofit = new Retrofit.Builder()
					.baseUrl(API_URL)
					.client(client)
					.addConverterFactory(GsonConverterFactory.create())
					.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
					.build();
		}
		if (cocktailApi == null) {
			cocktailApi = retrofit.create(CocktailApi.class);
		}
		return cocktailApi;
	}

	interface CocktailApi {
		@GET("search.php")
		Flowable<Drinks> searchByName(@Query("s") String search);

		@GET("filter.php")
		Single<Drinks> searchByIngredient(@Query("i") String ingredient);

		@GET("filter.php")
		Flowable<Drinks> searchByCategory(@Query("c") String category);

		@GET("lookup.php")
		Flowable<Drinks> getCocktail(@Query("i") long id);

		@GET("random.php")
		Single<Drinks> getRandom();
	}

	private List<Drink> convertDrinksToListWithCaching(Drinks drinks) {
		if (drinks.getDrinks() != null) {
			List<Drink> list = Arrays.asList(drinks.getDrinks());
			onLoadListener.onDrinksLoad(list);
			return list;
		} else {
			return Collections.emptyList();
		}
	}
//
//	private List<Drink> convertDrinksToList(Drinks drinks) {
//		if (drinks.getDrinks() != null) {
//			List<Drink> list = Arrays.asList(drinks.getDrinks());
////			onLoadListener.onDrinksLoad(list);
//			return list;
//		} else {
//			return Collections.emptyList();
//		}
//	}

	private Drink convertDrinksToDrink(Drinks drinks) {
		return drinks.getDrinks()[0];
	}

	public void setOnLoadListener(OnLoadListener onLoadListener) {
		this.onLoadListener = onLoadListener;
	}

	/**
	 * Interface for transfer query results into {@link LocalRepository} for caching.
	 */
	public interface OnLoadListener {
		void onDrinksLoad(List<Drink> list);
	}
}
