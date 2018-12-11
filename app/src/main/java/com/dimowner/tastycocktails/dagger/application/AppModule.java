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

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import com.dimowner.tastycocktails.FirebaseHandler;
import com.dimowner.tastycocktails.analytics.MixPanel;
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
//		remoteRepository.setOnLoadListener(localRepository::cacheIntoLocalDatabase);
		return new Repository(localRepository, remoteRepository);
	}

	@Provides
	@Singleton
	MixPanel provideMixPanel() {
		return new MixPanel();
	}

	@Provides
	@Singleton
	AppDatabase provideAppDatabase(Context context) {
		return Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "cocktails_db")
//				.fallbackToDestructiveMigration()
				.addMigrations(MIGRATION_4_6)
				.addMigrations(MIGRATION_6_7)
				.build();
	}

	@Provides
	@Singleton
	Context provideContext() {
		return appContext;
	}

	@Provides
	@Singleton
	FirebaseHandler provideFirebaseHandler() {
		return new FirebaseHandler();
	}

	/**
	 * Migrate from:
	 * version 4 - using the SQLiteDatabase API
	 * to
	 * version 6 - using Room
	 */
	@VisibleForTesting
	static final Migration MIGRATION_4_6 = new Migration(4, 6) {
		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
			//Migration code here
			database.execSQL("ALTER TABLE 'drinks' ADD COLUMN 'cached' INTEGER NOT NULL DEFAULT 0");
		}
	};

	/**
	 * Migrate from:
	 * version 6 - using the SQLiteDatabase API
	 * to
	 * version 7 - using Room
	 */
	@VisibleForTesting
	static final Migration MIGRATION_6_7 = new Migration(6, 7) {
		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
			database.execSQL("CREATE TABLE `rating` (`idDrink` INTEGER NOT NULL, "
					+ " `likeCount` INTEGER NOT NULL DEFAULT 0,"
					+ " `strDrink` TEXT,"
					+ " `strCategory` TEXT,"
					+ " `strAlcoholic` TEXT,"
					+ " `strGlass` TEXT,"
					+ " `strInstructions` TEXT,"
					+ " `strDrinkThumb` TEXT,"
					+ " `strIngredient1` TEXT,"
					+ " `strIngredient2` TEXT,"
					+ " `strIngredient3` TEXT,"
					+ " `strIngredient4` TEXT,"
					+ " `strIngredient5` TEXT,"
					+ " `strIngredient6` TEXT,"
					+ " `strIngredient7` TEXT,"
					+ " `strIngredient8` TEXT,"
					+ " `strIngredient9` TEXT,"
					+ " `strIngredient10` TEXT,"
					+ " `strIngredient11` TEXT,"
					+ " `strIngredient12` TEXT,"
					+ " `strIngredient13` TEXT,"
					+ " `strIngredient14` TEXT,"
					+ " `strIngredient15` TEXT,"
					+ " `strMeasure1` TEXT,"
					+ " `strMeasure2` TEXT,"
					+ " `strMeasure3` TEXT,"
					+ " `strMeasure4` TEXT,"
					+ " `strMeasure5` TEXT,"
					+ " `strMeasure6` TEXT,"
					+ " `strMeasure7` TEXT,"
					+ " `strMeasure8` TEXT,"
					+ " `strMeasure9` TEXT,"
					+ " `strMeasure10` TEXT,"
					+ " `strMeasure11` TEXT,"
					+ " `strMeasure12` TEXT,"
					+ " `strMeasure13` TEXT,"
					+ " `strMeasure14` TEXT,"
					+ " `strMeasure15` TEXT,"
					+ " PRIMARY KEY(`idDrink`))");
		}
	};
}
