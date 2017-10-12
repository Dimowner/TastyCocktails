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

package task.softermii.tastycocktails.cocktails.details;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import javax.inject.Inject;

import task.softermii.tastycocktails.R;
import task.softermii.tastycocktails.TCApplication;
import task.softermii.tastycocktails.dagger.details.DetailsModule;
import task.softermii.tastycocktails.util.AndroidUtils;
import timber.log.Timber;

/**
 * Created on 26.07.2017.
 * @author Dimowner
 */
public class DetailsActivity extends AppCompatActivity {

	private final String EXTRAS_KEY_ADAPTER_DATA = "adapter_data";

	public static final String EXTRAS_KEY_ID = "cocktail_id";

	@Inject
	DetailsContract.UserActionsListener mPresenter;

	private Toolbar toolbar;

	private FrameLayout mRoot;
	private RecyclerView mRecyclerView;
	private IngredientsAdapter mAdapter;

	private MenuItem itemFavorite;

	//TODO: move this into presenter
	private boolean isFavorite = false;
	private boolean isImageDark = true;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_container_scroll_view);

		TCApplication.get(getApplicationContext()).applicationComponent()
				.plus(new DetailsModule(this)).injectDetails(this);

		// Inflate content and bind views.
		LayoutInflater.from(this).inflate(R.layout.fragment_details, findViewById(R.id.container));

//		supportPostponeEnterTransition();

		mRoot = findViewById(R.id.coordinator_root);
		toolbar = findViewById(R.id.toolbar);

		mRecyclerView = findViewById(R.id.recycler_view);
		mRecyclerView.setHasFixedSize(true);

		mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

		if (savedInstanceState == null) {
			initAdapter();

			mPresenter.bindView(mAdapter);

			long id = getIntent().getLongExtra(EXTRAS_KEY_ID, -1);
			if (id >= 0) {
				mPresenter.loadDrinkById(id);
			}
		}

		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle("");
		}

		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// Set the padding to match the Status Bar height
			toolbar.setPadding(0, AndroidUtils.getStatusBarHeight(getApplicationContext()), 0, 0);
		}

//		AnimationUtil.physBasedRevealAnimation(toolbar.getChildAt(0));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mPresenter.unbindView();
	}

	private void startIngredientDetailsActivity(IngredientItem item, View view1) {
		Timber.v("start ingredient details activity here");
		//TODO: start ingredient details activity here
	}

	private void initAdapter() {
		if (mAdapter == null) {
			mAdapter = new IngredientsAdapter();
			mRecyclerView.setAdapter(mAdapter);
		}

		mAdapter.setItemClickListener((view1, position) ->
				startIngredientDetailsActivity(mAdapter.getItem(position), view1));
		mAdapter.setFavoriteUpdateListener(fav -> {
			isFavorite = fav;
			updateFavorite(fav);
		});

		mAdapter.setOnImageClickListener(path -> {
			Intent intent = new Intent(getApplicationContext(), ImagePreviewActivity.class);
			intent.putExtra(ImagePreviewActivity.EXTRAS_KEY_IMAGE_PATH, path);
			startActivity(intent);
		});

		mAdapter.setOnCheckImageColorListener(isDark -> {
			isImageDark = isDark;
			updateFavorite(isFavorite);
			if (getSupportActionBar() != null) {
				getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			}
			if (isDark) {
				toolbar.setNavigationIcon(R.drawable.arrow_left);
			} else {
				toolbar.setNavigationIcon(R.drawable.arrow_left_black);
			}
		});
		mAdapter.setOnSnackBarListener(message -> Snackbar.make(mRoot, message, Snackbar.LENGTH_LONG).show());
	}

//	@Override
//	public void onEnterAnimationComplete() {
//		super.onEnterAnimationComplete();
//		AnimationUtil.viewRevealAnimation(toolbar.getChildAt(0));
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_details, menu);
		itemFavorite = menu.findItem(R.id.action_add_to_favorites);
		updateFavorite(isFavorite);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (AndroidUtils.isAndroid5()) {
					finishAfterTransition();
				} else {
					finish();
				}
				break;
			case R.id.action_add_to_favorites:
				mPresenter.reverseFavorite();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mAdapter != null) {
			outState.putParcelable(EXTRAS_KEY_ADAPTER_DATA, mAdapter.onSaveInstanceState());
		}
		outState.putBoolean("is_favorite", isFavorite);
		outState.putBoolean("is_image_dark", isImageDark);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null && savedInstanceState.containsKey(EXTRAS_KEY_ADAPTER_DATA)) {
			isFavorite = savedInstanceState.getBoolean("is_favorite");
			isImageDark = savedInstanceState.getBoolean("is_image_dark");
			updateFavorite(isFavorite);

			initAdapter();
			mPresenter.bindView(mAdapter);
			mAdapter.onRestoreInstanceState(savedInstanceState.getParcelable(EXTRAS_KEY_ADAPTER_DATA));
		}
	}

	private void updateFavorite(boolean fav) {
		if (itemFavorite != null) {
			if (isImageDark) {
				itemFavorite.setIcon(fav ? R.drawable.heart : R.drawable.heart_outline);
			} else {
				itemFavorite.setIcon(fav ? R.drawable.heart_black : R.drawable.heart_outline_black);
			}
		}
	}
}
