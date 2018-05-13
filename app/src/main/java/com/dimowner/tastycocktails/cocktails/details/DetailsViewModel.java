package com.dimowner.tastycocktails.cocktails.details;

import com.dimowner.tastycocktails.data.model.Drink;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface DetailsViewModel {

	Single<Drink> getDrink(long id, int position);

	Completable reverseFavorite(long id);

	Drink getCachedDrink(int pos);

	void removeFromCache(int pos);

}
