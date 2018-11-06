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

package com.dimowner.tastycocktails.dagger.details;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import dagger.Module;
import dagger.Provides;

import com.dimowner.tastycocktails.FirebaseHandler;
import com.dimowner.tastycocktails.cocktails.details.DetailsViewModel;
import com.dimowner.tastycocktails.cocktails.details.DetailsViewModelImpl;
import com.dimowner.tastycocktails.data.Repository;

/**
 * Created on 15.08.2017.
 * @author Dimowner
 */
@Module
public class DetailsModule {

	private FragmentActivity activity;

	private Fragment fragment;

	public DetailsModule(FragmentActivity activity) {
		this.activity = activity;
		this.fragment = null;
	}

	@Provides
	@DetailsScoupe
	DetailsViewModel provideDetailsViewModel(Repository repository, FirebaseHandler firebaseHandler) {
		DetailsViewModelImpl viewModel;
		if (activity != null) {
			viewModel = ViewModelProviders.of(activity).get(DetailsViewModelImpl.class);
		} else {
			viewModel = ViewModelProviders.of(fragment).get(DetailsViewModelImpl.class);
		}
		viewModel.setRepository(repository);
		viewModel.setFirebaseHandler(firebaseHandler);
		return viewModel;
	}
}
