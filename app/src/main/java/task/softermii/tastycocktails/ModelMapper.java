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

package task.softermii.tastycocktails;

import java.util.ArrayList;
import java.util.List;

import task.softermii.tastycocktails.cocktails.details.IngredientItem;
import task.softermii.tastycocktails.data.model.Drink;
import timber.log.Timber;

/**
 * Created on 15.08.2017.
 * @author Dimowner
 */
public class ModelMapper {

	private ModelMapper() {
	}

	public static List<IngredientItem> getIngredientsFromDrink(Drink drink) {
		List<IngredientItem> list = new ArrayList<>();

		if (drink.getStrIngredient1() != null && !drink.getStrIngredient1().isEmpty()) {
			IngredientItem item1 = new IngredientItem(drink.getStrIngredient1(), drink.getStrMeasure1());
			list.add(item1);
		}

		if (drink.getStrIngredient2() != null && !drink.getStrIngredient2().isEmpty()) {
			IngredientItem item2 = new IngredientItem(drink.getStrIngredient2(), drink.getStrMeasure2());
			list.add(item2);
		}

		if (drink.getStrIngredient3() != null && !drink.getStrIngredient3().isEmpty()) {
			IngredientItem item3 = new IngredientItem(drink.getStrIngredient3(), drink.getStrMeasure3());
			list.add(item3);
		}
		if (drink.getStrIngredient4() != null && !drink.getStrIngredient4().isEmpty()) {
			IngredientItem item4 = new IngredientItem(drink.getStrIngredient4(), drink.getStrMeasure4());
			list.add(item4);
		}
		if (drink.getStrIngredient5() != null && !drink.getStrIngredient5().isEmpty()) {
			IngredientItem item5 = new IngredientItem(drink.getStrIngredient5(), drink.getStrMeasure5());
			list.add(item5);
		}
		if (drink.getStrIngredient6() != null && !drink.getStrIngredient6().isEmpty()) {
			IngredientItem item6 = new IngredientItem(drink.getStrIngredient6(), drink.getStrMeasure6());
			list.add(item6);
		}
		if (drink.getStrIngredient7() != null && !drink.getStrIngredient7().isEmpty()) {
			IngredientItem item7 = new IngredientItem(drink.getStrIngredient7(), drink.getStrMeasure7());
			list.add(item7);
		}
		if (drink.getStrIngredient8() != null && !drink.getStrIngredient8().isEmpty()) {
			IngredientItem item8 = new IngredientItem(drink.getStrIngredient8(), drink.getStrMeasure8());
			list.add(item8);
		}
		if (drink.getStrIngredient9() != null && !drink.getStrIngredient9().isEmpty()) {
			IngredientItem item9 = new IngredientItem(drink.getStrIngredient9(), drink.getStrMeasure9());
			list.add(item9);
		}
		if (drink.getStrIngredient10() != null && !drink.getStrIngredient10().isEmpty()) {
			IngredientItem item10 = new IngredientItem(drink.getStrIngredient10(), drink.getStrMeasure10());
			list.add(item10);
		}
		if (drink.getStrIngredient11() != null && !drink.getStrIngredient11().isEmpty()) {
			IngredientItem item11 = new IngredientItem(drink.getStrIngredient11(), drink.getStrMeasure11());
			list.add(item11);
		}
		if (drink.getStrIngredient12() != null && !drink.getStrIngredient12().isEmpty()) {
			IngredientItem item12 = new IngredientItem(drink.getStrIngredient12(), drink.getStrMeasure12());
			list.add(item12);
		}
		if (drink.getStrIngredient13() != null && !drink.getStrIngredient13().isEmpty()) {
			IngredientItem item13 = new IngredientItem(drink.getStrIngredient13(), drink.getStrMeasure13());
			list.add(item13);
		}
		if (drink.getStrIngredient14() != null && !drink.getStrIngredient14().isEmpty()) {
			IngredientItem item14 = new IngredientItem(drink.getStrIngredient14(), drink.getStrMeasure14());
			list.add(item14);
		}
		if (drink.getStrIngredient15() != null && !drink.getStrIngredient15().isEmpty()) {
			IngredientItem item15 = new IngredientItem(drink.getStrIngredient15(), drink.getStrMeasure15());
			list.add(item15);
		}
		return list;
	}
}
