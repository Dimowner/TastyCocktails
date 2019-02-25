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
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;

import com.dimowner.tastycocktails.cocktails.CocktailsListFragment;

import com.dimowner.tastycocktails.data.Prefs;
import com.dimowner.tastycocktails.data.Repository;
import com.dimowner.tastycocktails.data.model.Drink;
import com.dimowner.tastycocktails.data.model.Drinks;
import com.dimowner.tastycocktails.random.RandomFragment;
import com.dimowner.tastycocktails.rating.RatingFragment;
import com.dimowner.tastycocktails.settings.SettingsActivity;
import com.dimowner.tastycocktails.util.AndroidUtils;
import com.dimowner.tastycocktails.welcome.WelcomeFragment;
//import com.google.android.gms.ads.MobileAds;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Base activity with base functionality and drawer layout.
 * @author Dimowner
 */
public class NavigationActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {

	// symbols for navdrawer items (indices must correspond to array below). This is
	// not a list of items that are necessarily *present* in the Nav Drawer; rather,
	// it's a list of all possible items.
	protected static final int NAVDRAWER_ITEM_RATING      = R.id.nav_rating;
	protected static final int NAVDRAWER_ITEM_FAVORITES   = R.id.nav_favorites;
	protected static final int NAVDRAWER_ITEM_COCKTAILS	= R.id.nav_cocktails;
	protected static final int NAVDRAWER_ITEM_RANDOM 		= R.id.nav_random;
	protected static final int NAVDRAWER_ITEM_HISTORY     = R.id.nav_history;
//	protected static final int NAVDRAWER_ITEM_ABOUT			= R.id.nav_about;
	protected static final int NAVDRAWER_ITEM_SETTINGS 	= R.id.nav_settings;
//	protected static final int NAVDRAWER_ITEM_RATE			= R.id.nav_rate;
//	protected static final int NAVDRAWER_ITEM_FEEDBACK		= R.id.nav_feedback;
	protected static final int NAVDRAWER_ITEM_INVALID		= -1;

	// Primary toolbar and drawer toggle
	protected Toolbar mActionBarToolbar;

	// Navigation drawer:
	protected DrawerLayout mDrawerLayout;
	protected NavigationView mNavigationView;
	protected ActionBarDrawerToggle mDrawerToggle;

	private int curActiveItem = NAVDRAWER_ITEM_COCKTAILS;

	private Disposable disposable = null;

	@Inject Repository repository;

	@Inject Prefs prefs;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		setTheme(R.style.AppTheme_TransparentStatusBar);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.base_nav_activity);

		TCApplication.get(getApplicationContext()).applicationComponent().inject(this);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// Set the padding to match the Status Bar height
			mActionBarToolbar.setPadding(0, AndroidUtils.getStatusBarHeight(getApplicationContext()), 0, 0);
		}

		if (prefs.isFirstRun()) {
			FragmentManager manager = getSupportFragmentManager();
			WelcomeFragment fragment = WelcomeFragment.newInstance();
			fragment.setOnFirstRunExecutedListener(() -> {
				enableMenu();
				startCocktails();
			});
			FragmentTransaction ft = manager.beginTransaction();
			ft.add(R.id.fragment, fragment, "welcome_fragment");
			ft.commit();
			AndroidUtils.primaryColorNavigationBar(this);
		} else {
			if (savedInstanceState == null) {
				FragmentManager manager = getSupportFragmentManager();
				CocktailsListFragment fragment = CocktailsListFragment.newInstance(CocktailsListFragment.TYPE_NORMAL);
				if (prefs.isFirstRun()) {
					fragment.setOnFirstRunExecutedListener(this::enableMenu);
				}
				manager
						.beginTransaction()
						.add(R.id.fragment, fragment, "cocktails_fragment")
						.commit();

				AndroidUtils.handleNavigationBarColor(this);
			}
		}
		setupNavDrawer();
		if (prefs.isFirstRun()) {
			disableMenu();
		}

		if (!prefs.isDrinksCached() && !prefs.isCacheFailed()) {
			disposable = firstRunInitialization()
					.subscribe(drinks1 -> {
						Timber.d("Succeed to cache %d drinks!", drinks1.length);
						if (drinks1.length > 0) {
							prefs.setDrinksCached();
						} else {
							prefs.setCacheFailed();
						}
					}, Timber::e);
		}

//		MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.ad_mob_id));
	}

	public Single<Drink[]> firstRunInitialization() {
		String json;
		try {
			InputStream is = getAssets().open("drinks_json.txt");
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			json = new String(buffer, "UTF-8");

			Gson gson = new Gson();
			Drinks drinks = gson.fromJson(json, Drinks.class);
			List<Drink> cachedFev = new ArrayList<>();
			return repository.getFavoritesCount()
					.subscribeOn(Schedulers.io())
					.flatMap(count -> {
						if (count > 0) {
							cachedFev.addAll(repository.getFavoritesDrinks());
							repository.clearAll();
							return repository.cacheIntoLocalDatabase(drinks)
									.doOnSuccess(v -> {
										for (int i = 0; i < cachedFev.size(); i++) {
											repository.reverseFavorite(cachedFev.get(i).getIdDrink())
													.subscribeOn(Schedulers.io())
													.subscribe();
										}
									});
						} else {
							repository.clearAll();
							return repository.cacheIntoLocalDatabase(drinks);
						}
					});
		} catch (IOException ex) {
			Timber.e(ex);
			return Single.error(ex);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (getSelfNavDrawerItem() > NAVDRAWER_ITEM_INVALID) {
			mNavigationView.getMenu().findItem(getSelfNavDrawerItem()).setChecked(true);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (disposable != null) {
			disposable.dispose();
		}
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		setupActionBarToolbar();
	}

	/**
	 * Returns the navigation drawer item that corresponds to this Activity. Subclasses
	 * of NavigationActivity override this to indicate what nav drawer item corresponds to them
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
			mNavigationView.setCheckedItem(R.id.nav_cocktails);
			mNavigationView.setNavigationItemSelectedListener(
					menuItem -> {
						mDrawerLayout.closeDrawers();
						if (getSelfNavDrawerItem() != menuItem.getItemId()) {
							goToNavDrawerItem(menuItem.getItemId());
						}
						return true;
					});
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

			mDrawerToggle.setDrawerIndicatorEnabled(false);
			mDrawerToggle.setToolbarNavigationClickListener(view -> mDrawerLayout.openDrawer(GravityCompat.START));
			if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				mDrawerToggle.setHomeAsUpIndicator(R.drawable.round_menu);
			} else {
				mDrawerToggle.setHomeAsUpIndicator(R.drawable.round_menu_white_24);
			}

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
			mNavigationView.getMenu().findItem(R.id.nav_rating).setEnabled(false);
			mNavigationView.getMenu().findItem(R.id.nav_cocktails).setEnabled(false);
			mNavigationView.getMenu().findItem(R.id.nav_favorites).setEnabled(false);
			mNavigationView.getMenu().findItem(R.id.nav_history).setEnabled(false);
			mNavigationView.getMenu().findItem(R.id.nav_random).setEnabled(false);
			mNavigationView.getMenu().findItem(R.id.nav_settings).setEnabled(false);
		}
	}

	private void enableMenu() {
		if (mNavigationView != null) {
			mNavigationView.getMenu().findItem(R.id.nav_rating).setEnabled(true);
			mNavigationView.getMenu().findItem(R.id.nav_cocktails).setEnabled(true);
			mNavigationView.getMenu().findItem(R.id.nav_favorites).setEnabled(true);
			mNavigationView.getMenu().findItem(R.id.nav_history).setEnabled(true);
			mNavigationView.getMenu().findItem(R.id.nav_random).setEnabled(true);
			mNavigationView.getMenu().findItem(R.id.nav_settings).setEnabled(true);
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
			case NAVDRAWER_ITEM_RATING:
				if (mActionBarToolbar.getVisibility() == View.GONE) {
					mActionBarToolbar.setVisibility(View.VISIBLE);
				}
				startRating();
				curActiveItem = NAVDRAWER_ITEM_RATING;
				break;
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
				curActiveItem = NAVDRAWER_ITEM_HISTORY;
				break;
			case NAVDRAWER_ITEM_SETTINGS:
				startSettings();
				break;
//			case NAVDRAWER_ITEM_ABOUT:
//				showAboutDialog();
//				break;
//			case NAVDRAWER_ITEM_RATE:
//				rateApp();
//				mNavigationView.getMenu().findItem(getSelfNavDrawerItem()).setChecked(true);
//				break;
//			case NAVDRAWER_ITEM_FEEDBACK:
//				openFeedback();
//				mNavigationView.getMenu().findItem(getSelfNavDrawerItem()).setChecked(true);
//				break;
		}
	}

	public boolean isDirectionToLeft(int id) {
		switch (id) {
			case NAVDRAWER_ITEM_RATING:
				return true;
			case NAVDRAWER_ITEM_COCKTAILS:
				if (curActiveItem == NAVDRAWER_ITEM_RATING) {
					return false;
				} else {
					return true;
				}
			case NAVDRAWER_ITEM_FAVORITES:
				if ((curActiveItem == NAVDRAWER_ITEM_RATING)
						|| (curActiveItem == NAVDRAWER_ITEM_COCKTAILS)) {
					return false;
				} else {
					return true;
				}
			case NAVDRAWER_ITEM_RANDOM:
				if (curActiveItem == NAVDRAWER_ITEM_HISTORY) {
					return true;
				} else {
					return false;
				}
			case NAVDRAWER_ITEM_HISTORY:
				return false;
			default:
				return false;
		}
	}

	protected void startFavorites() {
		Timber.d("startFavorites");
		FragmentManager manager = getSupportFragmentManager();
		CocktailsListFragment fragment = CocktailsListFragment.newInstance(CocktailsListFragment.TYPE_FAVORITES);
		FragmentTransaction ft = manager.beginTransaction();
		if (isDirectionToLeft(NAVDRAWER_ITEM_FAVORITES)) {
			ft.setCustomAnimations(R.anim.enter_left_to_right, R.anim.exit_right_to_left);
		} else {
			ft.setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_left_to_right);
		}
		ft.replace(R.id.fragment, fragment, "favorites_fragment");
		ft.commit();
		AndroidUtils.handleNavigationBarColor(this);
	}

	protected void startHistory() {
		Timber.d("startHistory");
		FragmentManager manager = getSupportFragmentManager();
		CocktailsListFragment fragment = CocktailsListFragment.newInstance(CocktailsListFragment.TYPE_HISTORY);
		FragmentTransaction ft = manager.beginTransaction();
		if (isDirectionToLeft(NAVDRAWER_ITEM_HISTORY)) {
			ft.setCustomAnimations(R.anim.enter_left_to_right, R.anim.exit_right_to_left);
		} else {
			ft.setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_left_to_right);
		}
		ft.replace(R.id.fragment, fragment, "history_fragment");
		ft.commit();
		AndroidUtils.handleNavigationBarColor(this);
	}

	protected void startRating() {
		Timber.d("startRating");
		FragmentManager manager = getSupportFragmentManager();
		RatingFragment fragment = RatingFragment.newInstance();
		FragmentTransaction ft = manager.beginTransaction();
		if (isDirectionToLeft(NAVDRAWER_ITEM_COCKTAILS)) {
			ft.setCustomAnimations(R.anim.enter_left_to_right, R.anim.exit_right_to_left);
		} else {
			ft.setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_left_to_right);
		}
		ft.replace(R.id.fragment, fragment, "rating_fragment");
		ft.commit();
		AndroidUtils.blackNavigationBar(this);
	}

	protected void startCocktails() {
		Timber.d("startCocktails");
		FragmentManager manager = getSupportFragmentManager();
		CocktailsListFragment fragment = CocktailsListFragment.newInstance(CocktailsListFragment.TYPE_NORMAL);
		FragmentTransaction ft = manager.beginTransaction();
		if (isDirectionToLeft(NAVDRAWER_ITEM_COCKTAILS)) {
			ft.setCustomAnimations(R.anim.enter_left_to_right, R.anim.exit_right_to_left);
		} else {
			ft.setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_left_to_right);
		}
		ft.replace(R.id.fragment, fragment, "cocktails_fragment");
		ft.commit();
		AndroidUtils.handleNavigationBarColor(this);
	}

	protected void startRandom() {
		Timber.d("startRandom");
		FragmentManager manager = getSupportFragmentManager();
		RandomFragment fragment = new RandomFragment();
//		fragment.setOnSnackBarListener(message -> Snackbar.make(mRoot, message, Snackbar.LENGTH_LONG).show());
		FragmentTransaction ft = manager.beginTransaction();
		if (isDirectionToLeft(NAVDRAWER_ITEM_RANDOM)) {
			ft.setCustomAnimations(R.anim.enter_left_to_right, R.anim.exit_right_to_left);
		} else {
			ft.setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_left_to_right);
		}
		ft.replace(R.id.fragment, fragment, RandomFragment.TAG);
		ft.commit();
		AndroidUtils.handleNavigationBarColor(this);
	}

	private void startSettings() {
		startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
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
			if (curActiveItem == NAVDRAWER_ITEM_RANDOM) {
				mActionBarToolbar.setVisibility(View.GONE);
			}
		}
		AndroidUtils.handleNavigationBarColor(this);
	}
}
