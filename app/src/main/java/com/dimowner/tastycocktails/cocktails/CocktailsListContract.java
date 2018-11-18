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

package com.dimowner.tastycocktails.cocktails;

import android.content.Context;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

import com.dimowner.tastycocktails.Contract;
import com.dimowner.tastycocktails.cocktails.list.ListItem;
import com.dimowner.tastycocktails.data.model.Drink;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
public interface CocktailsListContract {

	interface View extends Contract.View {

		void displayData(List<ListItem> data);
	}

	interface UserActionsListener extends Contract.UserActionsListener<CocktailsListContract.View> {

		void startSearch(String search);

		void startSearchLocal(String search);

		void cancelSearch();

		void loadLastSearch(String query);

		void loadFavorites();

		void loadHistory(int page);

//		void loadBuildList(int filterType, String filterVal);

		void loadFilteredList(String category, List<String> ingredient, String glass, String alcoholic);

		void clearHistory();

		void returnToHistory(long id, long time);

		void removeFromHistory(long id);

		Completable reverseFavorite(long id);

		Single<Drink[]> firstRunInitialization(Context context);
	}
}
