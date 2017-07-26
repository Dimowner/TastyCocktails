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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import butterknife.BindView;
import butterknife.ButterKnife;
import task.softermii.tastycocktails.R;
import task.softermii.tastycocktails.util.AndroidUtils;

/**
 * Created on 26.07.2017.
 * @author Dimowner
 */
public class CocktailDetailsActivity extends AppCompatActivity {

	public static final String EXTRAS_KEY_COCKTAIL_ID = "cocktail_id";
	public static final String EXTRAS_KEY_NAME = "cocktail_name";
	public static final String EXTRAS_KEY_DESCRIPTION = "cocktail_description";
	public static final String EXTRAS_KEY_IMAGE_URL = "cocktail_image";

	@BindView(R.id.toolbar) Toolbar toolbar;
	@BindView(R.id.details_name) TextView txtName;
	@BindView(R.id.details_description) TextView txtDescription;
	@BindView(R.id.details_image) ImageView ivImage;

	private long mId;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cocktail_details);
		supportPostponeEnterTransition();
		ButterKnife.bind(this);

		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setTitle("");
		}

		Bundle extras = getIntent().getExtras();
		if (extras.containsKey(EXTRAS_KEY_COCKTAIL_ID)) {
			mId = extras.getLong(EXTRAS_KEY_COCKTAIL_ID);
		}

		if (extras.containsKey(EXTRAS_KEY_NAME)) {
			txtName.setText(extras.getString(EXTRAS_KEY_NAME));
		}
		if (extras.containsKey(EXTRAS_KEY_DESCRIPTION)) {
			txtDescription.setText(extras.getString(EXTRAS_KEY_DESCRIPTION));
		}

		if (extras.containsKey(EXTRAS_KEY_IMAGE_URL)) {
			ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
			Glide.with(getApplicationContext())
					.load(extras.getString(EXTRAS_KEY_IMAGE_URL))
					.listener(new RequestListener<Drawable>() {
						@Override
						public boolean onLoadFailed(@Nullable GlideException e, Object model,
															 Target<Drawable> target, boolean isFirstResource) {
							supportStartPostponedEnterTransition();
							return false;
						}

						@Override
						public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
																 DataSource dataSource, boolean isFirstResource) {
							supportStartPostponedEnterTransition();
							return false;
						}
					})
					.into(ivImage);
		}

		if (AndroidUtils.isAndroid5()) {
			String nameTransitionName = extras.getString(CocktailsSearchFragment.EXTRAS_KEY_NAME_TRANSITION_NAME);
			txtName.setTransitionName(nameTransitionName);
			String descrTransitionName = extras.getString(CocktailsSearchFragment.EXTRAS_KEY_DESCRIPTION_TRANSITION_NAME);
			txtDescription.setTransitionName(descrTransitionName);
			String imageTransitionName = extras.getString(CocktailsSearchFragment.EXTRAS_KEY_IMAGE_TRANSITION_NAME);
			ivImage.setTransitionName(imageTransitionName);
		}
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
