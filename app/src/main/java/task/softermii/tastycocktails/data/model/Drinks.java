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

package task.softermii.tastycocktails.data.model;

import java.util.Arrays;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
public class Drinks {
	private final Drink[] drinks;

	public Drinks(Drink[] drinks) {
		this.drinks = drinks;
	}

	public Drink[] getDrinks() {
		return drinks;
	}

	@Override
	public String toString() {
		return "Drinks{" +
				"drinks=" + Arrays.toString(drinks) +
				'}';
	}
}
