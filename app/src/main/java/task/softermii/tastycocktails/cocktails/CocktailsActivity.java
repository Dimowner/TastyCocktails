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

package task.softermii.tastycocktails.cocktails;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import task.softermii.tastycocktails.BaseActivity;
import task.softermii.tastycocktails.R;

/**
 * Created on 26.07.2017.
 * @author Dimowner
 */
public class CocktailsActivity extends BaseActivity {

	//TODO: Add dark circle mask around buttons in details to make them visible
	//TODO: Restyle lists
	//TODO: Add dialog that recommends like app in playstore
	//TODO: Add cocktails wall where every one can share photo with cocktail (with moderation)
	//TODO: Add button favorite in lists
	//TODO: Instantly update lists when add/remove from favorites.
	//TODO: Combine Cocktail Details and RandomCocktail in one Fragment
	//TODO: Add mapper and replace Drink model by more readable POJO for using presenters and UI
	//TODO: Use LiveData with Local database
	//TODO: Store images in local cache storage
	//TODO: Add Tests: Unit tests, Instrumentation tests, UI tests
	//TODO: Add Ingredient details

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		setTheme(R.style.AppTheme);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.base_nav_activity);

		if (savedInstanceState == null) {
			FragmentManager manager = getSupportFragmentManager();
			SearchFragment fragment = SearchFragment.newInstance(SearchFragment.TYPE_NORMAL);
			manager
					.beginTransaction()
					.add(R.id.fragment, fragment, "cocktails_fragment")
					.commit();
		}
	}

	@Override
	protected int getSelfNavDrawerItem() {
		return NAVDRAWER_ITEM_COCKTAILS;
	}
}
