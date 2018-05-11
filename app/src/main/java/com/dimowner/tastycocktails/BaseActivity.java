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

package com.dimowner.tastycocktails;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.dimowner.tastycocktails.cocktails.SearchFragment;

import com.dimowner.tastycocktails.data.Prefs;
import com.dimowner.tastycocktails.random.RandomFragment;
import com.dimowner.tastycocktails.util.AndroidUtils;
import com.dimowner.tastycocktails.util.AppStartTracker;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Base activity with base functionality and drawer layout.
 * @author Dimowner
 */
public class BaseActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {

	// symbols for navdrawer items (indices must correspond to array below). This is
	// not a list of items that are necessarily *present* in the Nav Drawer; rather,
	// it's a list of all possible items.
	protected static final int NAVDRAWER_ITEM_FAVORITES   = R.id.nav_favorites;
	protected static final int NAVDRAWER_ITEM_COCKTAILS	= R.id.nav_cocktails;
	protected static final int NAVDRAWER_ITEM_RANDOM 		= R.id.nav_random;
	protected static final int NAVDRAWER_ITEM_HISTORY     = R.id.nav_history;
	protected static final int NAVDRAWER_ITEM_ABOUT			= R.id.nav_about;
	protected static final int NAVDRAWER_ITEM_INVALID		= -1;

	// Primary toolbar and drawer toggle
	protected Toolbar mActionBarToolbar;

	// Navigation drawer:
	protected DrawerLayout mDrawerLayout;
	protected NavigationView mNavigationView;
	protected ActionBarDrawerToggle mDrawerToggle;
	protected LinearLayout mRoot;

	private int curActiveItem = NAVDRAWER_ITEM_COCKTAILS;

	private AppStartTracker tracker;

	@Inject Prefs prefs;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		tracker = TCApplication.getAppStartTracker(getApplicationContext());
		tracker.activityOnCreate();
		setTheme(R.style.AppTheme_TransparentStatusBar);
		super.onCreate(savedInstanceState);
		tracker.activityContentViewBefore();
		setContentView(R.layout.base_nav_activity);
		tracker.activityContentViewAfter();

		TCApplication.get(getApplicationContext()).applicationComponent().inject(this);

		mRoot = findViewById(R.id.root);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// Set the padding to match the Status Bar height
			mActionBarToolbar.setPadding(0, AndroidUtils.getStatusBarHeight(getApplicationContext()), 0, 0);
		}

		if (savedInstanceState == null) {
			FragmentManager manager = getSupportFragmentManager();
			SearchFragment fragment = SearchFragment.newInstance(SearchFragment.TYPE_NORMAL);
			if (prefs.isFirstRun()) {
				fragment.setOnFirstRunExecutedListener(this::enableMenu);
			}
			manager
					.beginTransaction()
					.add(R.id.fragment, fragment, "cocktails_fragment")
					.commit();
		}
		setupNavDrawer();
		if (prefs.isFirstRun()) {
			disableMenu();
		}

		tracker.activityOnCreateEnd();
	}

	@Override
	protected void onStart() {
		super.onStart();
		tracker.activityOnStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		tracker.activityOnResume();
//		Timber.v(tracker.getResults());
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		setupActionBarToolbar();
	}

	/**
	 * Returns the navigation drawer item that corresponds to this Activity. Subclasses
	 * of BaseActivity override this to indicate what nav drawer item corresponds to them
	 * Return NAVDRAWER_ITEM_INVALID to mean that this Activity should not have a Nav Drawer.
	 */
	protected int getSelfNavDrawerItem() {
		return curActiveItem;
	}

	/**
	 * Sets up the navigation drawer as appropriate. Note that the nav drawer will be
	 * different depending on whether the attendee indicated that they are attending the
	 * event on-site vs. attending remotely.
	 */
	protected void setupNavDrawer() {

		mDrawerLayout = findViewById(R.id.drawer_layout);
		if (mDrawerLayout == null) {
			return;
		}
		mNavigationView = findViewById(R.id.nav_view);
		if (mNavigationView != null) {
			mNavigationView.setNavigationItemSelectedListener(
					menuItem -> {
						mDrawerLayout.closeDrawers();
						if (getSelfNavDrawerItem() != menuItem.getItemId()) {
							goToNavDrawerItem(menuItem.getItemId());
						}
						return true;
					});
			mNavigationView.setCheckedItem(R.id.nav_cocktails);
		}

		if (mActionBarToolbar != null) {
			mActionBarToolbar.setNavigationOnClickListener(
					view -> mDrawerLayout.openDrawer(Gravity.START));

			mDrawerToggle = new ActionBarDrawerToggle(
					this,                  /* host Activity */
					mDrawerLayout,         /* DrawerLayout object */
					mActionBarToolbar,
					R.string.drawer_open,  /* "open drawer" description for accessibility */
					R.string.drawer_closed  /* "close drawer" description for accessibility */
			);

			mDrawerToggle.syncState();
			mDrawerLayout.addDrawerListener(mDrawerToggle);
		}
	}

	protected boolean isNavDrawerOpen() {
		return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(Gravity.START);
	}

	protected void closeNavDrawer() {
		if (mDrawerLayout != null) {
			mDrawerLayout.closeDrawer(Gravity.START);
		}
	}

	private void disableMenu() {
		if (mNavigationView != null) {
			mNavigationView.getMenu().findItem(R.id.nav_favorites).setEnabled(false);
			mNavigationView.getMenu().findItem(R.id.nav_history).setEnabled(false);
			mNavigationView.getMenu().findItem(R.id.nav_random).setEnabled(false);
		}
	}

	private void enableMenu() {
		if (mNavigationView != null) {
			mNavigationView.getMenu().findItem(R.id.nav_favorites).setEnabled(true);
			mNavigationView.getMenu().findItem(R.id.nav_history).setEnabled(true);
			mNavigationView.getMenu().findItem(R.id.nav_random).setEnabled(true);
		}
	}

	@Override
	public void onBackPressed() {
		if (isNavDrawerOpen()) {
			closeNavDrawer();
		} else {
			super.onBackPressed();
		}
	}

	private void goToNavDrawerItem(int itemID) {
		switch (itemID) {
			case NAVDRAWER_ITEM_FAVORITES:
				if (mActionBarToolbar.getVisibility() == View.GONE) {
					mActionBarToolbar.setVisibility(View.VISIBLE);
				}
				startFavorites();
				curActiveItem = NAVDRAWER_ITEM_FAVORITES;
				break;
			case NAVDRAWER_ITEM_COCKTAILS:
				if (mActionBarToolbar.getVisibility() == View.GONE) {
					mActionBarToolbar.setVisibility(View.VISIBLE);
				}
				startCocktails();
				curActiveItem = NAVDRAWER_ITEM_COCKTAILS;
				break;
			case NAVDRAWER_ITEM_RANDOM:
				mActionBarToolbar.setVisibility(View.GONE);
				startRandom();
				curActiveItem = NAVDRAWER_ITEM_RANDOM;
				break;
			case NAVDRAWER_ITEM_HISTORY:
				if (mActionBarToolbar.getVisibility() == View.GONE) {
					mActionBarToolbar.setVisibility(View.VISIBLE);
				}
				startHistory();
				break;
			case NAVDRAWER_ITEM_ABOUT:
				showAboutDialog();
				break;
		}
	}

	protected void startFavorites() {
		Timber.d("startFavorites");
		FragmentManager manager = getSupportFragmentManager();
		SearchFragment fragment = SearchFragment.newInstance(SearchFragment.TYPE_FAVORITES);
		manager
				.beginTransaction()
				.replace(R.id.fragment, fragment, "favorites_fragment")
				.commit();
	}

	protected void startHistory() {
		Timber.d("startHistory");
		FragmentManager manager = getSupportFragmentManager();
		SearchFragment fragment = SearchFragment.newInstance(SearchFragment.TYPE_HISTORY);
		manager
				.beginTransaction()
				.replace(R.id.fragment, fragment, "history_fragment")
				.commit();
	}

	protected void startCocktails() {
		Timber.d("startCocktails");
		FragmentManager manager = getSupportFragmentManager();
		SearchFragment fragment = SearchFragment.newInstance(SearchFragment.TYPE_NORMAL);
		manager
				.beginTransaction()
				.replace(R.id.fragment, fragment, "cocktails_fragment")
				.commit();
	}

	protected void startRandom() {
		Timber.d("startRandom");
		FragmentManager manager = getSupportFragmentManager();
		RandomFragment fragment = new RandomFragment();
		fragment.setOpenMenuListener(v -> mDrawerLayout.openDrawer(Gravity.START));
		fragment.setOnSnackBarListener(message -> Snackbar.make(mRoot, message, Snackbar.LENGTH_LONG).show());
		manager
				.beginTransaction()
				.add(R.id.fragment, fragment, "random_fragment")
				.commit();
	}

	private void showAboutDialog() {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment prev = fm.findFragmentByTag("dialog_about");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);
		AboutDialog dialog = new AboutDialog();
		dialog.show(ft, "dialog_about");
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	/**
	 * Get toolbar and init if need.
	 */
	protected void setupActionBarToolbar() {
		if (mActionBarToolbar == null) {
			mActionBarToolbar = (findViewById(R.id.toolbar));
			if (mActionBarToolbar != null) {
				setSupportActionBar(mActionBarToolbar);
			}
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		//Update checked item in NavigationView after AboutDialog dismiss.
		if (getSelfNavDrawerItem() > NAVDRAWER_ITEM_INVALID) {
			mNavigationView.getMenu().findItem(getSelfNavDrawerItem()).setChecked(true);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("cur_active_item", curActiveItem);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null) {
			curActiveItem = savedInstanceState.getInt("cur_active_item");
		}
	}
}
