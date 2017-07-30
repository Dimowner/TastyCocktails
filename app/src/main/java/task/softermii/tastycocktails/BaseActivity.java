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

package task.softermii.tastycocktails;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.Profile;
import com.facebook.ProfileTracker;

import task.softermii.tastycocktails.cocktails.CocktailsActivity;
import task.softermii.tastycocktails.random.RandomActivity;

import static task.softermii.tastycocktails.util.AndroidUtils.dpToPx;

/**
 * Base activity with base functionality and drawer layout.
 * @author Dimowner
 */
public class BaseActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {//implements IMainView {

	// symbols for navdrawer items (indices must correspond to array below). This is
	// not a list of items that are necessarily *present* in the Nav Drawer; rather,
	// it's a list of all possible items.
	protected static final int NAVDRAWER_ITEM_COCKTAILS	= R.id.nav_cocktails;
	protected static final int NAVDRAWER_ITEM_RANDOM 		= R.id.nav_random;
	protected static final int NAVDRAWER_ITEM_ABOUT			= R.id.nav_about;
	protected static final int NAVDRAWER_ITEM_INVALID		= -1;

	// Primary toolbar and drawer toggle
	protected Toolbar mActionBarToolbar;

	// Navigation drawer:
	protected DrawerLayout mDrawerLayout;
	protected NavigationView mNavigationView;
	protected ActionBarDrawerToggle mDrawerToggle;

	private TextView userName;
	private ImageView userIconView;

	private ProfileTracker profileTracker;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setupNavDrawer();

		userName = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.user_name);
		userIconView = (ImageView) mNavigationView.getHeaderView(0).findViewById(R.id.user_icon);

		Profile profile = Profile.getCurrentProfile();
		if (profile != null) {
			loadUserFace(profile);
			setUserDetailsActivity(profile);
		}
	}

	private void loadUserFace(@NonNull Profile profile) {
		userName.setText(profile.getName());
		Glide.with(BaseActivity.this)
				.load(profile.getProfilePictureUri(dpToPx(60), dpToPx(60)).toString())
				.apply(RequestOptions.circleCropTransform())
				.into(userIconView);
	}

	private void setUserDetailsActivity(@NonNull Profile profile) {
		userIconView.setOnClickListener(v -> startUserDetailsActivity(profile.getId()));
		userName.setOnClickListener(v -> startUserDetailsActivity(profile.getId()));
	}

	private void startUserDetailsActivity(@NonNull String id) {
		Intent intent = new Intent(getApplicationContext(), UserDetailsActivity.class);
		intent.putExtra(UserDetailsActivity.EXTRAS_KEY_USER_ID, id);
		ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
				BaseActivity.this,
				Pair.create(userIconView, ViewCompat.getTransitionName(userIconView)),
				Pair.create(userName, ViewCompat.getTransitionName(userName))
			);
		startActivity(intent, options.toBundle());
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		getActionBarToolbar();
	}

	/**
	 * Returns the navigation drawer item that corresponds to this Activity. Subclasses
	 * of BaseActivity override this to indicate what nav drawer item corresponds to them
	 * Return NAVDRAWER_ITEM_INVALID to mean that this Activity should not have a Nav Drawer.
	 */
	protected int getSelfNavDrawerItem() {
		return NAVDRAWER_ITEM_INVALID;
	}

	/**
	 * Sets up the navigation drawer as appropriate. Note that the nav drawer will be
	 * different depending on whether the attendee indicated that they are attending the
	 * event on-site vs. attending remotely.
	 */
	protected void setupNavDrawer() {
		// What nav drawer item should be selected?
		int selfItem = getSelfNavDrawerItem();

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (mDrawerLayout == null) {
			return;
		}
		mNavigationView = (NavigationView) findViewById(R.id.nav_view);
		if (mNavigationView != null) {
			mNavigationView.setNavigationItemSelectedListener(
					menuItem -> {
						mDrawerLayout.closeDrawers();
						if (getSelfNavDrawerItem() != menuItem.getItemId()) {
							goToNavDrawerItem(menuItem.getItemId());
						}
						return true;
					});
			if (selfItem > NAVDRAWER_ITEM_INVALID) {
				mNavigationView.getMenu().findItem(selfItem).setChecked(true);
			}
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

	@Override
	protected void onResume() {
		super.onResume();
		profileTracker = new ProfileTracker() {
			@Override
			protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
				if (currentProfile != null) {
					loadUserFace(currentProfile);
					setUserDetailsActivity(currentProfile);
				}
			}
		};
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		profileTracker.stopTracking();
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
			case NAVDRAWER_ITEM_COCKTAILS:
				startActivity(new Intent(getApplicationContext(), CocktailsActivity.class));
				finish();
				break;
			case NAVDRAWER_ITEM_RANDOM:
				startActivity(new Intent(getApplicationContext(), RandomActivity.class));
				finish();
				break;
			case NAVDRAWER_ITEM_ABOUT:
				showAboutDialog();
				break;
		}
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
	protected Toolbar getActionBarToolbar() {
		if (mActionBarToolbar == null) {
			mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
			if (mActionBarToolbar != null) {
				setSupportActionBar(mActionBarToolbar);
			}
		}
		return mActionBarToolbar;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		//Update checked item in NavigationView after AboutDialog dismiss.
		if (getSelfNavDrawerItem() > NAVDRAWER_ITEM_INVALID) {
			mNavigationView.getMenu().findItem(getSelfNavDrawerItem()).setChecked(true);
		}
	}
}
