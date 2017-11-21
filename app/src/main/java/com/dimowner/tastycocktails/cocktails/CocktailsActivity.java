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

package com.dimowner.tastycocktails.cocktails;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import com.dimowner.tastycocktails.BaseActivity;
import com.dimowner.tastycocktails.R;

/**
 * Created on 26.07.2017.
 * @author Dimowner
 */
public class CocktailsActivity extends BaseActivity {

	//TODO: do some refactoring and optimization
	//TODO: fix load by category from local db
	//TODO: Full test app and publish
	//TODO: Write database migration
	//TODO: Fix double query when open cocktails details
	//TODO: Fix rebuild drinks list when open details

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
