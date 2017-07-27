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

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
@Entity
public class Drink {

	public static long NO_ID = -1;

	@PrimaryKey
	private final long idDrink;

	@ColumnInfo(name = "strDrink")
	private final String strDrink;

	@ColumnInfo(name = "strCategory")
	private final String strCategory;

	@ColumnInfo(name = "strAlcoholic")
	private final String strAlcoholic;

	@ColumnInfo(name = "strGlass")
	private final String strGlass;

	@ColumnInfo(name = "strInstructions")
	private final String strInstructions;

	@ColumnInfo(name = "strDrinkThumb")
	private final String strDrinkThumb;

	public Drink(long idDrink, String strDrink, String strCategory, String strAlcoholic,
					 String strGlass, String strInstructions, String strDrinkThumb) {
		this.idDrink = idDrink;
		this.strDrink = strDrink;
		this.strCategory = strCategory;
		this.strAlcoholic = strAlcoholic;
		this.strGlass = strGlass;
		this.strInstructions = strInstructions;
		this.strDrinkThumb = strDrinkThumb;
	}

	public long getIdDrink() {
		return idDrink;
	}

	public String getStrDrink() {
		return strDrink;
	}

	public String getStrCategory() {
		return strCategory;
	}

	public String getStrAlcoholic() {
		return strAlcoholic;
	}

	public String getStrGlass() {
		return strGlass;
	}

	public String getStrInstructions() {
		return strInstructions;
	}

	public String getStrDrinkThumb() {
		return strDrinkThumb;
	}

	@Override
	public String toString() {
		return "Drink{" +
				"idDrink=" + idDrink +
				", strDrink='" + strDrink + '\'' +
				", strCategory='" + strCategory + '\'' +
				", strAlcoholic='" + strAlcoholic + '\'' +
				", strGlass='" + strGlass + '\'' +
				", strInstructions='" + strInstructions + '\'' +
				", strDrinkThumb='" + strDrinkThumb + '\'' +
				'}';
	}

	public static Drink emptyDrink() {
		return new Drink(NO_ID, null, null, null, null, null, null);
	}
}
