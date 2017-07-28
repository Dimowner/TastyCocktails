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

package task.softermii.tastycocktails.dagger.random;

import dagger.Module;
import dagger.Provides;
import task.softermii.tastycocktails.data.Repository;
import task.softermii.tastycocktails.random.RandomCocktailContract;
import task.softermii.tastycocktails.random.RandomCocktailPresenter;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
@Module
public class RandomCocktailModule {

	@Provides
	@RandomCocktailScope
	RandomCocktailContract.UserActionsListener provideDetailsPresenter(Repository repository) {
		return new RandomCocktailPresenter(repository);
	}
}
