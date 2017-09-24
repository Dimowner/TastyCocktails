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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import task.softermii.tastycocktails.data.model.Drink;
import task.softermii.tastycocktails.data.model.Drinks;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
public class RemoteRepository implements RepositoryContract {

	private static final String API_URL = "http://www.thecocktaildb.com/api/json/v1/1/";

	private Retrofit retrofit;

	private CocktailApi cocktailApi;

	private OnLoadListener onLoadListener;


	@Override
	public Single<List<Drink>> searchCocktailsByName(@NonNull String search) {
		return getCocktailApi()
					.searchByName(search)
					.map(this::convertDrinksToList)
					.subscribeOn(Schedulers.io());

	}

	@Override
	public Single<List<Drink>> searchCocktailsByIngredient(@NonNull String ingredient) {
		return getCocktailApi()
					.searchByIngredient(ingredient)
					.map(this::convertDrinksToList)
					.subscribeOn(Schedulers.io());
	}

	@Override
	public Single<Drink> getRandomCocktail() {
		return getCocktailApi()
				.getRandom()
				.map(this::convertDrinksToDrink)
				.subscribeOn(Schedulers.io());
	}

	@Override
	public Single<Drink> getCocktail(long id) {
		return getCocktailApi()
				.getCocktail(id)
				.map(this::convertDrinksToDrink)
				.subscribeOn(Schedulers.io());
	}

	@Override
	public Single<List<Drink>> getLastSearch() {
		throw new UnsupportedOperationException("This method is supported only in LocalRepository");
	}

	@Override
	public Single<List<Drink>> getFavorites() {
		throw new UnsupportedOperationException("This method is supported only in LocalRepository");
	}

	@Override
	public Completable addToFavorites(long id) {
		throw new UnsupportedOperationException("This method is supported only in LocalRepository");
	}

	@Override
	public Completable removeFromFavorites(long id) {
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
		Single<Drinks> searchByName(@Query("s") String search);

		@GET("filter.php")
		Single<Drinks> searchByIngredient(@Query("i") String ingredient);

		@GET("lookup.php")
		Single<Drinks> getCocktail(@Query("i") long id);

		@GET("random.php")
		Single<Drinks> getRandom();
	}

	private List<Drink> convertDrinksToList(Drinks drinks) {
		if (drinks.getDrinks() != null) {
			List<Drink> list = Arrays.asList(drinks.getDrinks());
			onLoadListener.onDrinksLoad(list);
			return list;
		} else {
			return Collections.emptyList();
		}
	}

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
