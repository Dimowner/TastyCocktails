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

package com.dimowner.tastycocktails.cocktails.details;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created on 14.08.2017.
 * @author Dimowner
 */
public class IngredientItem implements Parcelable {

	private final String name;
	private final String measure;
	private final String imageUrl;

	public IngredientItem(String name, String measure, String imageUrl) {
		this.name = name;
		this.measure = measure;
		this.imageUrl = imageUrl;
	}

	public String getName() {
		return name;
	}

	public String getMeasure() {
		return measure;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	//----- START Parcelable implementation ----------
	private IngredientItem(Parcel in) {
//		id = in.readLong();
		String[] data = new String[3];
		in.readStringArray(data);
		name = data[0];
		measure = data[1];
		imageUrl = data[2];
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeStringArray(new String[] {name, measure, imageUrl});
	}

	public static final Parcelable.Creator<IngredientItem> CREATOR
			= new Parcelable.Creator<IngredientItem>() {
		public IngredientItem createFromParcel(Parcel in) {
			return new IngredientItem(in);
		}

		public IngredientItem[] newArray(int size) {
			return new IngredientItem[size];
		}
	};
	//----- END Parcelable implementation ----------

	@Override
	public String toString() {
		return "IngredientItem{" +
				"name='" + name + '\'' +
				", measure='" + measure + '\'' +
				", imageUrl='" + imageUrl + '\'' +
				'}';
	}
}
