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

package task.softermii.tastycocktails.details;

import android.support.annotation.NonNull;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import task.softermii.tastycocktails.data.RepositoryContract;
import task.softermii.tastycocktails.data.model.DetailsModel;
import task.softermii.tastycocktails.data.model.Drink;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
public class DetailsPresenter implements DetailsContract.UserActionsListener {

	private RepositoryContract repository;

	private DetailsContract.View view;

	private CompositeDisposable compositeDisposable = new CompositeDisposable();

	public DetailsPresenter(RepositoryContract repository) {
		this.repository = repository;
	}

	@Override
	public void bindView(@NonNull DetailsContract.View view) {
		this.view = view;
	}

	@Override
	public void unbindView() {
		compositeDisposable.dispose();
		this.view = null;
	}

	@Override
	public void loadDrinkById(long id) {
		view.showProgress();
		compositeDisposable.add(
				repository.getCocktail(id)
						.map(this::convertModel)
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(this::displayData, this::handleError));
	}

	@Override
	public void loadRandomDrink() {
		view.showProgress();
		compositeDisposable.add(
				repository.getRandomCocktail()
						.map(this::convertModel)
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(this::displayData, this::handleError));
	}

	private void displayData(DetailsModel model) {
		view.hideProgress();
		if (model != null) {
			view.displayData(model);
		}
	}

	private void handleError(Throwable throwable) {
		view.hideProgress();
		view.showError(throwable);
	}

	private DetailsModel convertModel(Drink drink) {
		if (drink.getIdDrink() != Drink.NO_ID) {
			return new DetailsModel(drink.getStrDrink(), drink.getStrInstructions(), drink.getStrDrinkThumb());
		} else {
			return null;
		}
	}
}
