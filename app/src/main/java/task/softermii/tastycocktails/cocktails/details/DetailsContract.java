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

import java.util.List;

import task.softermii.tastycocktails.Contract;

/**
 * Created on 15.08.2017.
 * @author Dimowner
 */
public interface DetailsContract  {

	interface View extends Contract.View {

		void displayData(String name, String description, boolean isFavorite);

		void displayImage(String url);

		void displayIngredientsList(List<IngredientItem> items);

		void showSnackBar(String message);
	}

	interface UserActionsListener extends Contract.UserActionsListener<DetailsContract.View> {

		void loadDrinkById(long id);

		void reverseFavorite();
	}
}
