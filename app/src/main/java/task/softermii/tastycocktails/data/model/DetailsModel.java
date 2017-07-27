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

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
public class DetailsModel {
	private final String name;
	private final String description;
	private final String imageUrl;

	public DetailsModel(String name, String description, String imageUrl) {
		this.name = name;
		this.description = description;
		this.imageUrl = imageUrl;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	@Override
	public String toString() {
		return "DetailsModel{" +
				"name='" + name + '\'' +
				", description='" + description + '\'' +
				", imageUrl='" + imageUrl + '\'' +
				'}';
	}
}
