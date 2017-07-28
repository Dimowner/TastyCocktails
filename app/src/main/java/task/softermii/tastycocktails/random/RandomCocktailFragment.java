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

package task.softermii.tastycocktails.random;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import javax.inject.Inject;

import task.softermii.tastycocktails.R;
import task.softermii.tastycocktails.TCApplication;
import task.softermii.tastycocktails.dagger.random.RandomCocktailModule;
import task.softermii.tastycocktails.data.model.DetailsModel;
import timber.log.Timber;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
public class RandomCocktailFragment extends Fragment implements RandomCocktailContract.View {

//	public static final String EXTRAS_KEY_ID = "cocktail_id";
//	public static final String EXTRAS_KEY_TRANSITION_BUNDLE = "transition_bundle";

	@Inject
	RandomCocktailContract.UserActionsListener mPresenter;

	private ProgressBar mProgressBar;
	private ImageView ivImage;
	private TextView txtName;
	private TextView txtDescription;
	private TextView txtError;

//	private long mId;
//
//	public static RandomCocktailFragment newInstance(long id, Bundle transitionBundle) {
//		RandomCocktailFragment fragment = new RandomCocktailFragment();
//		Bundle data = new Bundle();
//		data.putBundle(EXTRAS_KEY_TRANSITION_BUNDLE, transitionBundle);
//		data.putLong(EXTRAS_KEY_ID, id);
//		fragment.setArguments(data);
//		return fragment;
//	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TCApplication.get(getContext()).applicationComponent()
				.plus(new RandomCocktailModule()).injectDetailsFragment(this);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_cocktail_details, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
		ivImage = (ImageView) view.findViewById(R.id.details_image);
		txtName = (TextView) view.findViewById(R.id.details_name);
		txtDescription = (TextView) view.findViewById(R.id.details_description);
		txtError = (TextView) view.findViewById(R.id.details_error);

		mPresenter.bindView(this);
		mPresenter.loadRandomDrink();
	}

	public void loadNewRandomDrink() {
		mPresenter.loadRandomDrink();
		hideError();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mPresenter.unbindView();
	}

	@Override
	public void showProgress() {
		mProgressBar.setVisibility(View.VISIBLE);
	}

	@Override
	public void hideProgress() {
		mProgressBar.setVisibility(View.GONE);
	}

	@Override
	public void showError(Throwable throwable) {
		Timber.e(throwable);
		showError();
	}

	private void showError() {
		ivImage.setVisibility(View.GONE);
		txtName.setVisibility(View.GONE);
		txtDescription.setVisibility(View.GONE);
		txtError.setVisibility(View.VISIBLE);
	}

	private void hideError() {
		ivImage.setVisibility(View.VISIBLE);
		txtName.setVisibility(View.VISIBLE);
		txtDescription.setVisibility(View.VISIBLE);
		txtError.setVisibility(View.GONE);
	}

	@Override
	public void displayData(DetailsModel model) {
		if (model.getImageUrl() != null) {
			ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
			Glide.with(getContext())
					.load(model.getImageUrl())
					.into(ivImage);
		}
		txtName.setText(model.getName());
		txtDescription.setText(model.getDescription());
	}
}
