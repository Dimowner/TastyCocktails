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

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import task.softermii.tastycocktails.R;
import task.softermii.tastycocktails.util.AndroidUtils;
import task.softermii.tastycocktails.util.AnimationUtil;

/**
 * Created on 26.07.2017.
 * @author Dimowner
 */
public class DetailsActivity extends AppCompatActivity {

	public static final String EXTRAS_KEY_NAME = "cocktail_name";
	public static final String EXTRAS_KEY_DESCRIPTION = "cocktail_description";
	public static final String EXTRAS_KEY_IMAGE_URL = "cocktail_image";

	private Toolbar toolbar;
	private TextView txtError;
	private ProgressBar progress;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_container_scroll_view);

		// Inflate content and bind views.
		LayoutInflater.from(this).inflate(R.layout.content_cocktail, (ViewGroup) findViewById(R.id.scroll_view));

		supportPostponeEnterTransition();

		txtError = (TextView) findViewById(R.id.details_error);
		progress = (ProgressBar) findViewById(R.id.progress);
		toolbar = (Toolbar) findViewById(R.id.toolbar);

		TextView txtName = (TextView) findViewById(R.id.details_name);
		TextView txtDescription = (TextView) findViewById(R.id.details_description);
		ImageView ivImage = (ImageView) findViewById(R.id.details_image);

		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setTitle("");
		}

		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// Set the padding to match the Status Bar height
			toolbar.setPadding(0, AndroidUtils.getStatusBarHeight(getApplicationContext()), 0, 0);
		}

		Bundle extras = getIntent().getExtras();

		if (extras.containsKey(EXTRAS_KEY_NAME)) {
			txtName.setText(extras.getString(EXTRAS_KEY_NAME));
		}
		if (extras.containsKey(EXTRAS_KEY_DESCRIPTION)) {
			txtDescription.setText(extras.getString(EXTRAS_KEY_DESCRIPTION));
		}

		if (extras.containsKey(EXTRAS_KEY_IMAGE_URL)) {
			String url = extras.getString(EXTRAS_KEY_IMAGE_URL);
			if (url != null) {
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
			}
		}

		if (AndroidUtils.isAndroid5()) {
			txtName.setTransitionName(getResources().getString(R.string.list_item_label_transition));
			txtDescription.setTransitionName(getResources().getString(R.string.list_item_content_transition));
			ivImage.setTransitionName(getResources().getString(R.string.list_item_image_transition));
		}
	}

	private void showProgress() {
		progress.setVisibility(View.VISIBLE);
	}

	private void hideProgress() {
		progress.setVisibility(View.GONE);
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
}
