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

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import javax.inject.Inject;

import task.softermii.tastycocktails.R;
import task.softermii.tastycocktails.TCApplication;
import task.softermii.tastycocktails.dagger.details.DetailsModule;
import task.softermii.tastycocktails.util.AndroidUtils;
import task.softermii.tastycocktails.util.AnimationUtil;
import timber.log.Timber;

/**
 * Created on 26.07.2017.
 * @author Dimowner
 */
public class DetailsActivity extends AppCompatActivity implements DetailsContract.View {

	private final String EXTRAS_KEY_ADAPTER_DATA = "adapter_data";

	public static final String EXTRAS_KEY_ID = "cocktail_id";

	@Inject
	DetailsContract.UserActionsListener mPresenter;

	private Toolbar toolbar;
	private TextView txtError;
	private ProgressBar progress;

	private TextView txtName;
	private TextView txtDescription;
	private ImageView ivImage;

	private RecyclerView mRecyclerView;
	private IngredientsAdapter mAdapter;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_container_scroll_view);

		TCApplication.get(getApplicationContext()).applicationComponent()
				.plus(new DetailsModule()).injectDetails(this);

		// Inflate content and bind views.
		LayoutInflater.from(this).inflate(R.layout.content_cocktail, (ViewGroup) findViewById(R.id.scroll_view));

		supportPostponeEnterTransition();

		txtError = (TextView) findViewById(R.id.details_error);
		progress = (ProgressBar) findViewById(R.id.progress);
		toolbar = (Toolbar) findViewById(R.id.toolbar);

		txtName = (TextView) findViewById(R.id.details_name);
		txtDescription = (TextView) findViewById(R.id.details_description);
		ivImage = (ImageView) findViewById(R.id.details_image);

		mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
		mRecyclerView.setHasFixedSize(true);

		mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

		mPresenter.bindView(this);

		//TODO:fix save state
//		if (savedInstanceState == null) {
			mAdapter = new IngredientsAdapter();
			mAdapter.setItemClickListener((view1, position) ->
					startIngredientDetailsActivity(mAdapter.getItem(position), view1));
			mRecyclerView.setAdapter(mAdapter);
			long id = getIntent().getLongExtra(EXTRAS_KEY_ID, -1);
			if (id >= 0) {
				mPresenter.loadDrinkById(id);
			}
//		}

		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setTitle("");
		}

		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// Set the padding to match the Status Bar height
			toolbar.setPadding(0, AndroidUtils.getStatusBarHeight(getApplicationContext()), 0, 0);
		}

		if (AndroidUtils.isAndroid5()) {
			txtName.setTransitionName(getResources().getString(R.string.list_item_label_transition));
			txtDescription.setTransitionName(getResources().getString(R.string.list_item_content_transition));
			ivImage.setTransitionName(getResources().getString(R.string.list_item_image_transition));
		}
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

	private void showError() {
		txtError.setVisibility(View.VISIBLE);
	}

	@Override
	public void onEnterAnimationComplete() {
		super.onEnterAnimationComplete();
		AnimationUtil.viewRevealAnimation(toolbar.getChildAt(0));
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
		}
		return super.onOptionsItemSelected(item);
	}
//TODO: fix save statee
//	@Override
//	public void onSaveInstanceState(Bundle outState) {
//		super.onSaveInstanceState(outState);
//		if (mAdapter != null) {
//			outState.putParcelable(EXTRAS_KEY_ADAPTER_DATA, mAdapter.onSaveInstanceState());
//		}
//	}
//
//	@Override
//	protected void onRestoreInstanceState(Bundle savedInstanceState) {
//		super.onRestoreInstanceState(savedInstanceState);
//		if (savedInstanceState != null && savedInstanceState.containsKey(EXTRAS_KEY_ADAPTER_DATA)) {
//			if (mAdapter == null) {
//				mAdapter = new IngredientsAdapter();
//				mRecyclerView.setAdapter(mAdapter);
//			}
//			mAdapter.setItemClickListener((view1, position) ->
//					startIngredientDetailsActivity(mAdapter.getItem(position), view1));
//			mAdapter.onRestoreInstanceState(savedInstanceState.getParcelable(EXTRAS_KEY_ADAPTER_DATA));
//		}
//	}

	@Override
	public void showProgress() {
		progress.setVisibility(View.VISIBLE);
	}

	@Override
	public void hideProgress() {
		progress.setVisibility(View.GONE);
	}

	@Override
	public void showQueryError() {
		Toast.makeText(this, R.string.query_error, Toast.LENGTH_LONG).show();
	}

	@Override
	public void showNetworkError() {
		Toast.makeText(this, R.string.network_error, Toast.LENGTH_LONG).show();
	}

	@Override
	public void displayData(String name, String description) {
		txtName.setText(name);
		txtDescription.setText(description);
	}

	@Override
	public void displayImage(String url) {
		if (url != null && !url.isEmpty()) {
			showProgress();
			ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
			Glide.with(getApplicationContext())
					.load(url)
					.listener(new RequestListener<Drawable>() {
						@Override
						public boolean onLoadFailed(@Nullable GlideException e, Object model,
															 Target<Drawable> target, boolean isFirstResource) {
							supportStartPostponedEnterTransition();
							hideProgress();
							showError();
							return false;
						}

						@Override
						public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
																 DataSource dataSource, boolean isFirstResource) {
							supportStartPostponedEnterTransition();
							hideProgress();
							return false;
						}
					})
					.into(ivImage);
		} else {
			supportStartPostponedEnterTransition();
			ivImage.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
			ivImage.setImageResource(R.drawable.no_image);
			hideProgress();
		}
	}

	@Override
	public void displayIngredientsList(List<IngredientItem> items) {
		mAdapter.setData(items);
	}
}
