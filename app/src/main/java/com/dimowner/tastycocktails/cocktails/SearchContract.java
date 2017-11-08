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

import java.util.List;

import io.reactivex.Completable;
import com.dimowner.tastycocktails.Contract;
import com.dimowner.tastycocktails.cocktails.list.ListItem;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
public interface SearchContract {

	interface View extends Contract.View {

		void displayData(List<ListItem> data);
	}

	interface UserActionsListener extends Contract.UserActionsListener<SearchContract.View> {

		void startSearch(String search);

		void cancelSearch();

		void loadLastSearch(String query);

		void loadFavorites();

		void loadHistory(int page);

		void clearHistory();

		void removeFromHistory(long id);

		Completable reverseFavorite(long id);
	}
}
