package com.dimowner.tastycocktails.cocktails.details;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.dimowner.tastycocktails.FirebaseHandler;
import com.dimowner.tastycocktails.data.RepositoryContract;
import com.dimowner.tastycocktails.data.model.Drink;

import java.util.Date;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import timber.log.Timber;

public class DetailsViewModelImpl extends AndroidViewModel implements DetailsViewModel {

	private RepositoryContract repository;

	private SparseArray<Drink> drinks;

	private FirebaseHandler firebaseHandler;

	public DetailsViewModelImpl(@NonNull Application application) {
		super(application);
		drinks = new SparseArray<>();
	}

	public void setRepository(RepositoryContract repository) {
		this.repository = repository;
	}

	public void setFirebaseHandler(FirebaseHandler firebaseHandler) {
		this.firebaseHandler = firebaseHandler;
	}

	@Override
	public Flowable<Drink> getDrink(long id, int position) {
		return repository.getCocktailRx(id)
				.doOnNext(drink -> cacheDrink(drink, position));
	}

	@Override
	public Single<Drink> getRandomDrink() {
		return repository.getRandomCocktail();
	}

	private void cacheDrink(Drink drink, int position) {
		Timber.v("cacheDrink id = " + drink.getIdDrink());
		drinks.put(position, drink);
	}

	public Drink getCachedDrink(int pos) {
		return drinks.get(pos);
	}

	public void removeFromCache(int pos) {
		drinks.delete(pos);
	}

	@Override
	public Completable updateDrinkHistory(long id) {
		return repository.updateDrinkHistory(id, new Date().getTime());
	}

	@Override
	public Completable reverseFavorite(long id) {
		Drink drink = findDrinkById(id);
		if (drink != null) {
			if (drink.isFavorite()) {
				return repository.removeFromFavorites(drink.getIdDrink())
						.doOnComplete(() -> {
							drink.inverseFavorite();
							firebaseHandler.unlikeDrink(drink.getIdDrink());
						});
			} else {
				return repository.addToFavorites(drink)
						.doOnComplete(() -> firebaseHandler.likeDrink(drink.getIdDrink()));
			}

		} else {
			return Completable.complete();
		}
	}

	private Drink findDrinkById(long id) {
		for (int i = 0; i < drinks.size(); i++) {
			//TODO: find why drink is NULL in some case
			Drink drink = drinks.valueAt(i);
			if (drink != null) {
				long key = drink.getIdDrink();
				if (key == id) {
					return drinks.valueAt(i);
				}
			}
		}
		return null;
	}
}
