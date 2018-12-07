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

package com.dimowner.tastycocktails.dagger.application;

import javax.inject.Singleton;

import dagger.Component;

import com.dimowner.tastycocktails.NavigationActivity;
import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.cocktails.details.ImagePreviewActivity;
import com.dimowner.tastycocktails.dagger.cocktails.CocktailsComponent;
import com.dimowner.tastycocktails.dagger.cocktails.CocktailsModule;
import com.dimowner.tastycocktails.dagger.details.DetailsComponent;
import com.dimowner.tastycocktails.dagger.details.DetailsModule;
import com.dimowner.tastycocktails.dagger.random.RandomCocktailComponent;
import com.dimowner.tastycocktails.dagger.random.RandomCocktailModule;
import com.dimowner.tastycocktails.dagger.rating.RatingComponent;
import com.dimowner.tastycocktails.dagger.rating.RatingModule;
import com.dimowner.tastycocktails.data.LocalRepository;
import com.dimowner.tastycocktails.settings.SettingsActivity;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
@Component(modules = {AppModule.class})
@Singleton
public interface AppComponent {

	CocktailsComponent plus(CocktailsModule exerciseModule);
	RandomCocktailComponent plus(RandomCocktailModule detailsModule);
	DetailsComponent plus(DetailsModule detailsModule);
	RatingComponent plus(RatingModule ratingModule);
	void inject(LocalRepository repository);
	void inject(NavigationActivity activity);
	void inject(SettingsActivity activity);
	void inject(ImagePreviewActivity activity);
	void inject(TCApplication application);
}
