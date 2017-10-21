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

package com.dimowner.tastycocktails.cocktails.details;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import com.dimowner.tastycocktails.ModelMapper;
import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.cocktails.details.DetailsContract.UserActionsListener;
import com.dimowner.tastycocktails.data.RepositoryContract;
import com.dimowner.tastycocktails.data.model.Drink;
import timber.log.Timber;

/**
 * Created on 15.08.2017.
 * @author Dimowner
 */
public class DetailsPresenter extends AndroidViewModel implements UserActionsListener {

	private RepositoryContract repository;

	private DetailsContract.View view;

	private Drink drink;

	private CompositeDisposable compositeDisposable = new CompositeDisposable();

	public DetailsPresenter(Application application) {
		super(application);
	}

	public void setRepository(RepositoryContract repository) {
		this.repository = repository;
	}

	@Override
	public void bindView(@NonNull DetailsContract.View view) {
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

	public DetailsContract.View getView() {
		return view;
	}

	@Override
	public void loadDrinkById(long id) {
		view.showProgress();
		compositeDisposable.add(
				repository.getCocktail(id)
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(this::displayData, this::handleError));
	}

	@Override
	public void reverseFavorite() {
		if (drink.isFavorite()) {
			repository.removeFromFavorites(drink.getIdDrink())
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(() -> {
							drink.inverseFavorite();
							view.showSnackBar(getApplication().getResources().getString(R.string.removed_from_favorites, drink.getStrDrink()));
							view.displayData(drink.getStrDrink(), drink.getStrInstructions(), drink.getStrCategory(),
									drink.getStrAlcoholic(), drink.getStrGlass(), drink.isFavorite());
						}, Timber::e);
		} else {
			repository.addToFavorites(drink)
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(() -> {
//							drink.inverseFavorite(); //Removed intentionally
							view.showSnackBar(getApplication().getResources().getString(R.string.added_to_favorites, drink.getStrDrink()));
						view.displayData(drink.getStrDrink(), drink.getStrInstructions(), drink.getStrCategory(),
								drink.getStrAlcoholic(), drink.getStrGlass(), drink.isFavorite());
						}, Timber::e);
		}
	}

	private void displayData(Drink model) {
		if (model != null) {
			drink = model;
			view.displayData(model.getStrDrink(), model.getStrInstructions(), model.getStrCategory(),
					model.getStrAlcoholic(), model.getStrGlass(), model.isFavorite());
			view.displayImage(model.getStrDrinkThumb());
			if (model.getStrDrinkThumb() == null || model.getStrDrinkThumb().isEmpty()) {
				view.hideProgress();
			}
			List<IngredientItem> ingredientItems = ModelMapper.getIngredientsFromDrink(model);
			if (ingredientItems.size() > 0) {
				view.displayIngredientsList(ingredientItems);
			}
			repository.updateDrinkHistory(model.getIdDrink(), new Date().getTime())
					.subscribeOn(Schedulers.io())
					.subscribe(() -> {}, Timber::e);
		}
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
}
