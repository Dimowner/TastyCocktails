package com.dimowner.tastycocktails.cocktails.details;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.dimowner.tastycocktails.data.RepositoryContract;
import com.dimowner.tastycocktails.data.model.Drink;

import io.reactivex.Completable;
import io.reactivex.Single;
import timber.log.Timber;

public class DetailsViewModelImpl extends AndroidViewModel implements DetailsViewModel {

	private RepositoryContract repository;

	private SparseArray<Drink> drinks;

	public DetailsViewModelImpl(@NonNull Application application) {
		super(application);
		drinks = new SparseArray<>();
	}

	public void setRepository(RepositoryContract repository) {
		this.repository = repository;
	}

	@Override
	public Single<Drink> getDrink(long id, int position) {
		return repository.getLocalCocktailRx(id)
				.doOnSuccess(drink -> cacheDrink(drink, position));
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
	public Completable reverseFavorite(long id) {
		Drink drink = findDrinkById(id);
		if (drink != null) {
			if (drink.isFavorite()) {
				return repository.removeFromFavorites(drink.getIdDrink());
			} else {
				return repository.addToFavorites(drink);
			}

		} else {
			return Completable.complete();
		}
	}

	private Drink findDrinkById(long id) {
		for (int i = 0; i < drinks.size(); i++) {
			int key = drinks.keyAt(i);
			if (drinks.get(key).getIdDrink() == id) {
				return drinks.get(i);
			}
		}
		return null;
	}
}
