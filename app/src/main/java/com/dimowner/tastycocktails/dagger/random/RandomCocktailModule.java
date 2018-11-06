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

package com.dimowner.tastycocktails.dagger.random;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v4.app.Fragment;

import dagger.Module;
import dagger.Provides;

import com.dimowner.tastycocktails.FirebaseHandler;
import com.dimowner.tastycocktails.cocktails.details.DetailsViewModel;
import com.dimowner.tastycocktails.cocktails.details.DetailsViewModelImpl;
import com.dimowner.tastycocktails.dagger.details.DetailsScoupe;
import com.dimowner.tastycocktails.data.Repository;
import com.dimowner.tastycocktails.random.RandomContract;
import com.dimowner.tastycocktails.random.RandomPresenter;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
@Module
public class RandomCocktailModule {

	private Fragment fragment;

	public RandomCocktailModule(Fragment fragment) {
		this.fragment = fragment;
	}

	@Provides
	@RandomCocktailScope
	RandomContract.UserActionsListener provideDetailsPresenter(Repository repository, FirebaseHandler firebaseHandler) {
		RandomPresenter presenter = ViewModelProviders.of(fragment).get(RandomPresenter.class);
//		MyViewModel model = ViewModelProviders.of(this).get(MyViewModel.class);
		presenter.setRepository(repository);
		presenter.setFirebaseHandler(firebaseHandler);
		return presenter;
	}

//	@Provides
//	@DetailsScoupe
//	DetailsViewModel provideDetailsViewModel(Repository repository) {
//		DetailsViewModelImpl viewModel = ViewModelProviders.of(fragment).get(DetailsViewModelImpl.class);
//		viewModel.setRepository(repository);
//		return viewModel;
//	}
}
