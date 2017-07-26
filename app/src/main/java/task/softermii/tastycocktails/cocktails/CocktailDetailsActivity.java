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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import task.softermii.tastycocktails.R;

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
	@BindView(R.id.details_name) TextView txtRepoName;
	@BindView(R.id.details_description) TextView txtRepoDescription;
	@BindView(R.id.details_image) ImageView ivFace;


	private long mId;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cocktail_details);

		ButterKnife.bind(this);

		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setTitle("");
		}

		Bundle args = getIntent().getExtras();
		if (args.containsKey(EXTRAS_KEY_COCKTAIL_ID)) {
			mId = args.getLong(EXTRAS_KEY_COCKTAIL_ID);
		}

		if (args.containsKey(EXTRAS_KEY_NAME)) {
			txtRepoName.setText(args.getString(EXTRAS_KEY_NAME));
		}
		if (args.containsKey(EXTRAS_KEY_DESCRIPTION)) {
			txtRepoDescription.setText(args.getString(EXTRAS_KEY_DESCRIPTION));
		}

		if (args.containsKey(EXTRAS_KEY_IMAGE_URL)) {
			ivFace.setScaleType(ImageView.ScaleType.CENTER_CROP);
			Glide.with(getApplicationContext())
					.load(args.getString(EXTRAS_KEY_IMAGE_URL))
					.into(ivFace);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
