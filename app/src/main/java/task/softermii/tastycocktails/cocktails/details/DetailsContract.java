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

package task.softermii.tastycocktails.cocktails.details;

import android.support.annotation.NonNull;

import java.util.List;

import task.softermii.tastycocktails.data.model.DetailsModel;

/**
 * Created on 15.08.2017.
 *
 * @author Dimowner
 */
public interface DetailsContract {

	interface View {
		void showProgress();

		void hideProgress();

		void showQueryError();

		void showNetworkError();

		void displayData(String name, String description);

		void displayImage(String url);

		void displayIngredientsList(List<IngredientItem> items);
	}

	interface UserActionsListener {

		void bindView(@NonNull View view);

		void unbindView();

		void loadDrinkById(long id);
	}
}
