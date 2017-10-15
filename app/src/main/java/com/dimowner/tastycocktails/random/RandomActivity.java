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

package com.dimowner.tastycocktails.random;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;

import com.dimowner.tastycocktails.BaseActivity;
import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.util.AndroidUtils;
import com.dimowner.tastycocktails.util.AnimationUtil;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
public class RandomActivity extends BaseActivity {

	private RandomFragment fragment;
	private FloatingActionButton fab;
	private CoordinatorLayout mRoot;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_random);

		fab = findViewById(R.id.fab);
		mRoot = findViewById(R.id.coordinator_root);

		if (getSupportActionBar() != null) getSupportActionBar().setTitle("");

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// Set the padding to match the Status Bar height
			mActionBarToolbar.setPadding(0, AndroidUtils.getStatusBarHeight(getApplicationContext()), 0, 0);
		}
		if (savedInstanceState == null) {
			FragmentManager manager = getSupportFragmentManager();
			fragment = new RandomFragment();
			fragment.setActivityToolbar(mActionBarToolbar);
			fragment.setOnSnackBarListener(message -> Snackbar.make(mRoot, message, Snackbar.LENGTH_LONG).show());
			manager
					.beginTransaction()
					.add(R.id.fragment, fragment, "random_fragment")
					.commit();
		}
		fab.setOnClickListener(v -> fragment.loadRandomDrink());
	}

	@Override
	public void onStart() {
		super.onStart();
		AnimationUtil.physBasedRevealAnimation(fab);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("fragment_tag", fragment.getTag());
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		fragment = (RandomFragment) getSupportFragmentManager()
				.findFragmentByTag(savedInstanceState.getString("fragment_tag"));
		fab.setOnClickListener(v -> fragment.loadRandomDrink());
	}

	@Override
	protected int getSelfNavDrawerItem() {
		return NAVDRAWER_ITEM_RANDOM;
	}
}
