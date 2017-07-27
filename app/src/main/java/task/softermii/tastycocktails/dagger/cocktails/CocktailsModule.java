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

package task.softermii.tastycocktails.dagger.cocktails;

import android.content.Context;
import dagger.Module;
import dagger.Provides;
import task.softermii.tastycocktails.cocktails.CocktailsPresenter;
import task.softermii.tastycocktails.cocktails.CocktailsSearchContract;
import task.softermii.tastycocktails.data.LocalRepository;
import task.softermii.tastycocktails.data.RemoteRepository;
import task.softermii.tastycocktails.data.Repository;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
@Module
public class CocktailsModule {

	@Provides
	@CocktailsScope
	LocalRepository provideLocalRepository(Context context) {
		return new LocalRepository(context);
	}

	@Provides
	@CocktailsScope
	RemoteRepository provideRemoteRepository() {
		return new RemoteRepository();
	}

	@Provides
	@CocktailsScope
	Repository provideRepository(Context context,
												    LocalRepository localRepository,
												    RemoteRepository remoteRepository) {
		//Remote repo passes last query result into Local repo for saving.
		remoteRepository.setOnLoadListener(localRepository::rewriteRepositories);

		return new Repository(context, localRepository, remoteRepository);
	}

	@Provides
	@CocktailsScope
	CocktailsSearchContract.UserActionsListener provideIExercisesPresenter(Repository repository) {
		return new CocktailsPresenter(repository);
	}
}
