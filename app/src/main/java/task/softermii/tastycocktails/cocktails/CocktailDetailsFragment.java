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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import task.softermii.tastycocktails.R;

/**
 * Created on 26.07.2017.
 * @author Dimowner
 */
public class CocktailDetailsFragment extends Fragment {

	public static final String EXTRAS_KEY_COCKTAIL_ID = "cocktail_id";
	public static final String EXTRAS_KEY_NAME = "cocktail_name";
	public static final String EXTRAS_KEY_DESCRIPTION = "cocktail_description";
	public static final String EXTRAS_KEY_IMAGE_URL = "cocktail_image";

	private long mId;

	public static CocktailDetailsFragment newInstance(long id, String name, String decr, String faceUrl) {
		CocktailDetailsFragment fragment = new CocktailDetailsFragment();
		Bundle data = new Bundle();
		data.putLong(EXTRAS_KEY_COCKTAIL_ID, id);
		data.putString(EXTRAS_KEY_NAME, name);
		data.putString(EXTRAS_KEY_DESCRIPTION, decr);
		data.putString(EXTRAS_KEY_IMAGE_URL, faceUrl);
		fragment.setArguments(data);
		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_cocktail_details, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		TextView txtRepoName = (TextView) view.findViewById(R.id.details_name);
		TextView txtRepoDescription= (TextView) view.findViewById(R.id.details_description);
		ImageView ivFace = (ImageView) view.findViewById(R.id.details_image);

		Bundle args = getArguments();

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
			Glide.with(getContext())
					.load(args.getString(EXTRAS_KEY_IMAGE_URL))
					.into(ivFace);
		}
	}
}
