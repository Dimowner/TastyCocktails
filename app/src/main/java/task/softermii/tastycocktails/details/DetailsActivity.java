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

package task.softermii.tastycocktails.details;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import task.softermii.tastycocktails.R;

/**
 * Created on 26.07.2017.
 * @author Dimowner
 */
public class DetailsActivity extends AppCompatActivity {

	public static final String EXTRAS_KEY_COCKTAIL_ID = "cocktail_id";
	public static final String EXTRAS_KEY_NAME = "cocktail_name";
	public static final String EXTRAS_KEY_DESCRIPTION = "cocktail_description";
	public static final String EXTRAS_KEY_IMAGE_URL = "cocktail_image";

	private long mId;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);

		if (savedInstanceState == null) {
			FragmentManager manager = getSupportFragmentManager();

			Bundle extras = getIntent().getExtras();
			if (extras.containsKey(EXTRAS_KEY_COCKTAIL_ID)) {
				mId = extras.getLong(EXTRAS_KEY_COCKTAIL_ID);
			}
			DetailsFragment fragment = DetailsFragment.newInstance(mId, getIntent().getExtras());
			manager
					.beginTransaction()
					.add(R.id.fragment, fragment, "details_fragment")
					.commit();
		}
	}
}
