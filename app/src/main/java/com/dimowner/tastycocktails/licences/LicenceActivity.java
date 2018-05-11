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

package com.dimowner.tastycocktails.licences;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.util.AndroidUtils;

/**
 * Activity shows list which contains all licences used in app.
 * @author Dimowner
 */
public class LicenceActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ativity_licences);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// Set the padding to match the Status Bar height
			toolbar.setPadding(0, AndroidUtils.getStatusBarHeight(getApplicationContext()), 0, 0);
		}

		String[] licenceNames = getResources().getStringArray(R.array.licences_names);
		ListView list = findViewById(R.id.licence_list);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(
				this, android.R.layout.simple_list_item_1, android.R.id.text1, licenceNames);
		list.setAdapter(adapter);
		list.setOnItemClickListener((adapterView, view, i, l) -> {
			Intent intent = new Intent(getApplicationContext(), LicenceDetail.class);
			intent.putExtra(LicenceDetail.EXTRAS_KEY_LICENCE_ITEM_POS, i);
			startActivity(intent);
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_licences, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_close) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
}
