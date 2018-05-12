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

package com.dimowner.tastycocktails.cocktails.list;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created on 26.07.2017.
 * @author Dimowner
 */
public class ListItem implements Parcelable {
	private final long id;
	private final String name;
	private final String description;
	private final String category;
	private final String alcoholic;
	private final String glass;
	private final String avatar_url;
	private final long history;
	private final boolean isFavorite;

	public ListItem(long id, String name, String description, String category, String alcoholic,
						 String glass, String avatar_url, long history, boolean isFavorite) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.category = category;
		this.alcoholic = alcoholic;
		this.glass = glass;
		this.avatar_url = avatar_url;
		this.history = history;
		this.isFavorite = isFavorite;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getCategory() {
		return category;
	}

	public String getAlcoholic() {
		return alcoholic;
	}

	public String getGlass() {
		return glass;
	}

	public String getAvatar_url() {
		return avatar_url;
	}

	public long getHistory() {
		return history;
	}

	public boolean isFavorite() {
		return isFavorite;
	}

	//----- START Parcelable implementation ----------
	private ListItem(Parcel in) {
		long[] longs = new long[2];
		in.readLongArray(longs);
		id = longs[0];
		history = longs[1];
		String[] data = new String[6];
		in.readStringArray(data);
		name = data[0];
		description = data[1];
		category = data[2];
		alcoholic = data[3];
		glass = data[4];
		avatar_url = data[5];
		boolean[] bools = new boolean[1];
		in.readBooleanArray(bools);
		isFavorite = bools[0];
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeLongArray(new long[] {id, history});
		out.writeStringArray(new String[] {name, description, category, alcoholic, glass, avatar_url});
		out.writeBooleanArray(new boolean[] {isFavorite});
	}

	public static final Parcelable.Creator<ListItem> CREATOR
			= new Parcelable.Creator<ListItem>() {
		public ListItem createFromParcel(Parcel in) {
			return new ListItem(in);
		}

		public ListItem[] newArray(int size) {
			return new ListItem[size];
		}
	};
	//----- END Parcelable implementation ----------

	@Override
	public String toString() {
		return "ListItem{" +
				"id=" + id +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", category='" + category + '\'' +
				", alcoholic='" + alcoholic + '\'' +
				", glass='" + glass+ '\'' +
				", avatar_url='" + avatar_url + '\'' +
				", history='" + history + '\'' +
				", isFavorite=" + isFavorite +
				'}';
	}
}
