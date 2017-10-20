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

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import com.dimowner.tastycocktails.data.LocalRepository;
import com.dimowner.tastycocktails.data.Prefs;
import com.dimowner.tastycocktails.data.RemoteRepository;
import com.dimowner.tastycocktails.data.Repository;
import com.dimowner.tastycocktails.data.room.AppDatabase;

/**
 * Created on 27.0.2017.
 * @author Dimowner
 */
@Module
public class AppModule {

	private final Context appContext;

	public AppModule(@NonNull Context context) {
		appContext = context;
	}

	@Provides
	@Singleton
	Prefs providePrefs(Context context) {
		return new Prefs(context);
	}

	@Provides
	@Singleton
	LocalRepository provideLocalRepository(Context context) {
		return new LocalRepository(context);
	}

	@Provides
	@Singleton
	RemoteRepository provideRemoteRepository() {
		return new RemoteRepository();
	}

	@Provides
	@Singleton
	Repository provideRepository(LocalRepository localRepository,
										  RemoteRepository remoteRepository) {
		//Remote repo passes last query result into Local repo for saving.
		remoteRepository.setOnLoadListener(localRepository::cacheIntoLocalDatabase);

		return new Repository(localRepository, remoteRepository);
	}

	@Provides
	@Singleton
	AppDatabase provideAppDatabase(Context context) {
		return Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "cocktails_db")
				.fallbackToDestructiveMigration()
				.build();
	}

	@Provides
	@Singleton
	Context provideContext() {
		return appContext;
	}
}