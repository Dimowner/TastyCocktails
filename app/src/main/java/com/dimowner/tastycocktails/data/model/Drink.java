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

package com.dimowner.tastycocktails.data.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
@Entity(tableName = "drinks")
public class Drink {

	@PrimaryKey
	private final long idDrink;

	@ColumnInfo(name = "cached")
	private boolean cached;

	@ColumnInfo(name = "strDrink")
	private final String strDrink;

	@ColumnInfo(name = "strCategory")
	private String strCategory;

	@ColumnInfo(name = "strAlcoholic")
	private final String strAlcoholic;

	@ColumnInfo(name = "strGlass")
	private final String strGlass;

	@ColumnInfo(name = "strInstructions")
	private final String strInstructions;

	@ColumnInfo(name = "strDrinkThumb")
	private final String strDrinkThumb;

	@ColumnInfo(name = "isFavorite")
	private boolean isFavorite = false;

	@ColumnInfo(name = "history")
	private long history = 0;

	@ColumnInfo(name = "strIngredient1")
	private final String strIngredient1;
	@ColumnInfo(name = "strIngredient2")
	private final String strIngredient2;
	@ColumnInfo(name = "strIngredient3")
	private final String strIngredient3;
	@ColumnInfo(name = "strIngredient4")
	private final String strIngredient4;
	@ColumnInfo(name = "strIngredient5")
	private final String strIngredient5;
	@ColumnInfo(name = "strIngredient6")
	private final String strIngredient6;
	@ColumnInfo(name = "strIngredient7")
	private final String strIngredient7;
	@ColumnInfo(name = "strIngredient8")
	private final String strIngredient8;
	@ColumnInfo(name = "strIngredient9")
	private final String strIngredient9;
	@ColumnInfo(name = "strIngredient10")
	private final String strIngredient10;
	@ColumnInfo(name = "strIngredient11")
	private final String strIngredient11;
	@ColumnInfo(name = "strIngredient12")
	private final String strIngredient12;
	@ColumnInfo(name = "strIngredient13")
	private final String strIngredient13;
	@ColumnInfo(name = "strIngredient14")
	private final String strIngredient14;
	@ColumnInfo(name = "strIngredient15")
	private final String strIngredient15;

	@ColumnInfo(name = "strMeasure1")
	private final String strMeasure1;
	@ColumnInfo(name = "strMeasure2")
	private final String strMeasure2;
	@ColumnInfo(name = "strMeasure3")
	private final String strMeasure3;
	@ColumnInfo(name = "strMeasure4")
	private final String strMeasure4;
	@ColumnInfo(name = "strMeasure5")
	private final String strMeasure5;
	@ColumnInfo(name = "strMeasure6")
	private final String strMeasure6;
	@ColumnInfo(name = "strMeasure7")
	private final String strMeasure7;
	@ColumnInfo(name = "strMeasure8")
	private final String strMeasure8;
	@ColumnInfo(name = "strMeasure9")
	private final String strMeasure9;
	@ColumnInfo(name = "strMeasure10")
	private final String strMeasure10;
	@ColumnInfo(name = "strMeasure11")
	private final String strMeasure11;
	@ColumnInfo(name = "strMeasure12")
	private final String strMeasure12;
	@ColumnInfo(name = "strMeasure13")
	private final String strMeasure13;
	@ColumnInfo(name = "strMeasure14")
	private final String strMeasure14;
	@ColumnInfo(name = "strMeasure15")
	private final String strMeasure15;

	public Drink(long idDrink, boolean cached, String strDrink, String strCategory, String strAlcoholic, String strGlass,
					 String strInstructions, String strDrinkThumb, boolean isFavorite, long history, String strIngredient1,
					 String strIngredient2, String strIngredient3, String strIngredient4, String strIngredient5,
					 String strIngredient6, String strIngredient7, String strIngredient8, String strIngredient9,
					 String strIngredient10, String strIngredient11, String strIngredient12, String strIngredient13,
					 String strIngredient14, String strIngredient15, String strMeasure1, String strMeasure2,
					 String strMeasure3, String strMeasure4, String strMeasure5, String strMeasure6,
					 String strMeasure7, String strMeasure8, String strMeasure9, String strMeasure10,
					 String strMeasure11, String strMeasure12, String strMeasure13, String strMeasure14, String strMeasure15) {
		this.idDrink = idDrink;
		this.cached = cached;
		this.strDrink = strDrink;
		this.strCategory = strCategory;
		this.strAlcoholic = strAlcoholic;
		this.strGlass = strGlass;
		this.strInstructions = strInstructions;
		this.strDrinkThumb = strDrinkThumb;
		this.isFavorite = isFavorite;
		this.history = history;
		this.strIngredient1 = strIngredient1;
		this.strIngredient2 = strIngredient2;
		this.strIngredient3 = strIngredient3;
		this.strIngredient4 = strIngredient4;
		this.strIngredient5 = strIngredient5;
		this.strIngredient6 = strIngredient6;
		this.strIngredient7 = strIngredient7;
		this.strIngredient8 = strIngredient8;
		this.strIngredient9 = strIngredient9;
		this.strIngredient10 = strIngredient10;
		this.strIngredient11 = strIngredient11;
		this.strIngredient12 = strIngredient12;
		this.strIngredient13 = strIngredient13;
		this.strIngredient14 = strIngredient14;
		this.strIngredient15 = strIngredient15;
		this.strMeasure1 = strMeasure1;
		this.strMeasure2 = strMeasure2;
		this.strMeasure3 = strMeasure3;
		this.strMeasure4 = strMeasure4;
		this.strMeasure5 = strMeasure5;
		this.strMeasure6 = strMeasure6;
		this.strMeasure7 = strMeasure7;
		this.strMeasure8 = strMeasure8;
		this.strMeasure9 = strMeasure9;
		this.strMeasure10 = strMeasure10;
		this.strMeasure11 = strMeasure11;
		this.strMeasure12 = strMeasure12;
		this.strMeasure13 = strMeasure13;
		this.strMeasure14 = strMeasure14;
		this.strMeasure15 = strMeasure15;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Drink)) {
			return false;
		}
		Drink d = (Drink) obj;
		return   d.idDrink == idDrink
				&& d.cached == cached
				&& d.strDrink != null && d.strDrink.equals(strDrink)
				&& d.isFavorite == isFavorite
				&& d.history == history;
	}

	@Override
	public int hashCode() {
		return (int) idDrink;
	}

	public long getIdDrink() {
		return idDrink;
	}

	public boolean isCached() {
		return cached;
	}

	public void setCached(boolean cached) {
		this.cached = cached;
	}

	public String getStrDrink() {
		return strDrink;
	}

	public String getStrCategory() {
		return strCategory;
	}

	public void setStrCategory(String strCategory) {
		this.strCategory = strCategory;
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

	public boolean isFavorite() {
		return isFavorite;
	}

	public void inverseFavorite() {
		this.isFavorite = !this.isFavorite;
	}

	public long getHistory() {
		return history;
	}

	public void setHistory(long history) {
		this.history = history;
	}

	public String getStrIngredient1() {
		return strIngredient1;
	}

	public String getStrIngredient2() {
		return strIngredient2;
	}

	public String getStrIngredient3() {
		return strIngredient3;
	}

	public String getStrIngredient4() {
		return strIngredient4;
	}

	public String getStrIngredient5() {
		return strIngredient5;
	}

	public String getStrIngredient6() {
		return strIngredient6;
	}

	public String getStrIngredient7() {
		return strIngredient7;
	}

	public String getStrIngredient8() {
		return strIngredient8;
	}

	public String getStrIngredient9() {
		return strIngredient9;
	}

	public String getStrIngredient10() {
		return strIngredient10;
	}

	public String getStrIngredient11() {
		return strIngredient11;
	}

	public String getStrIngredient12() {
		return strIngredient12;
	}

	public String getStrIngredient13() {
		return strIngredient13;
	}

	public String getStrIngredient14() {
		return strIngredient14;
	}

	public String getStrIngredient15() {
		return strIngredient15;
	}

	public String getStrMeasure1() {
		return strMeasure1;
	}

	public String getStrMeasure2() {
		return strMeasure2;
	}

	public String getStrMeasure3() {
		return strMeasure3;
	}

	public String getStrMeasure4() {
		return strMeasure4;
	}

	public String getStrMeasure5() {
		return strMeasure5;
	}

	public String getStrMeasure6() {
		return strMeasure6;
	}

	public String getStrMeasure7() {
		return strMeasure7;
	}

	public String getStrMeasure8() {
		return strMeasure8;
	}

	public String getStrMeasure9() {
		return strMeasure9;
	}

	public String getStrMeasure10() {
		return strMeasure10;
	}

	public String getStrMeasure11() {
		return strMeasure11;
	}

	public String getStrMeasure12() {
		return strMeasure12;
	}

	public String getStrMeasure13() {
		return strMeasure13;
	}

	public String getStrMeasure14() {
		return strMeasure14;
	}

	public String getStrMeasure15() {
		return strMeasure15;
	}

	@Override
	public String toString() {
		return "Drink{" +
				"idDrink=" + idDrink +
				", cached='" + cached + '\'' +
				", strDrink='" + strDrink + '\'' +
				", strCategory='" + strCategory + '\'' +
				", strAlcoholic='" + strAlcoholic + '\'' +
				", strGlass='" + strGlass + '\'' +
				", strInstructions='" + strInstructions + '\'' +
				", strDrinkThumb='" + strDrinkThumb + '\'' +
				", isFavorite=" + isFavorite +
				", history=" + history +
				", strIngredient1='" + strIngredient1 + '\'' +
				", strIngredient2='" + strIngredient2 + '\'' +
				", strIngredient3='" + strIngredient3 + '\'' +
				", strIngredient4='" + strIngredient4 + '\'' +
				", strIngredient5='" + strIngredient5 + '\'' +
				", strIngredient6='" + strIngredient6 + '\'' +
				", strIngredient7='" + strIngredient7 + '\'' +
				", strIngredient8='" + strIngredient8 + '\'' +
				", strIngredient9='" + strIngredient9 + '\'' +
				", strIngredient10='" + strIngredient10 + '\'' +
				", strIngredient11='" + strIngredient11 + '\'' +
				", strIngredient12='" + strIngredient12 + '\'' +
				", strIngredient13='" + strIngredient13 + '\'' +
				", strIngredient14='" + strIngredient14 + '\'' +
				", strIngredient15='" + strIngredient15 + '\'' +
				", strMeasure1='" + strMeasure1 + '\'' +
				", strMeasure2='" + strMeasure2 + '\'' +
				", strMeasure3='" + strMeasure3 + '\'' +
				", strMeasure4='" + strMeasure4 + '\'' +
				", strMeasure5='" + strMeasure5 + '\'' +
				", strMeasure6='" + strMeasure6 + '\'' +
				", strMeasure7='" + strMeasure7 + '\'' +
				", strMeasure8='" + strMeasure8 + '\'' +
				", strMeasure9='" + strMeasure9 + '\'' +
				", strMeasure10='" + strMeasure10 + '\'' +
				", strMeasure11='" + strMeasure11 + '\'' +
				", strMeasure12='" + strMeasure12 + '\'' +
				", strMeasure13='" + strMeasure13 + '\'' +
				", strMeasure14='" + strMeasure14 + '\'' +
				", strMeasure15='" + strMeasure15 + '\'' +
				'}';
	}
}
