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

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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

import java.util.List;

import javax.inject.Inject;

import task.softermii.tastycocktails.R;
import task.softermii.tastycocktails.TCApplication;
import task.softermii.tastycocktails.cocktails.details.IngredientItem;
import task.softermii.tastycocktails.cocktails.details.IngredientsAdapter;
import task.softermii.tastycocktails.dagger.random.RandomCocktailModule;
import task.softermii.tastycocktails.data.model.DetailsModel;
import timber.log.Timber;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
public class RandomFragment extends Fragment implements RandomContract.View {

	public static final String EXTRAS_KEY_ID = "cocktail_id";

	@Inject
	RandomContract.UserActionsListener mPresenter;

	private ProgressBar mProgressBar;
	private ImageView ivImage;
	private TextView txtName;
	private TextView txtDescription;
	private TextView txtError;

	private RecyclerView mRecyclerView;
	private IngredientsAdapter mAdapter;

	//TODO: this field shouldn't be in view
	private long mId = -1;

//	public static RandomFragment newInstance(long id, Bundle transitionBundle) {
//		RandomFragment fragment = new RandomFragment();
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
		return inflater.inflate(R.layout.content_cocktail, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
		ivImage = (ImageView) view.findViewById(R.id.details_image);
		txtName = (TextView) view.findViewById(R.id.details_name);
		txtDescription = (TextView) view.findViewById(R.id.details_description);
		txtError = (TextView) view.findViewById(R.id.details_error);

		mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
		mRecyclerView.setHasFixedSize(true);

		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		mPresenter.bindView(this);

		mAdapter = new IngredientsAdapter();
		mAdapter.setItemClickListener((view1, position) ->
				startIngredientDetailsActivity(mAdapter.getItem(position), view1));
		mRecyclerView.setAdapter(mAdapter);

//		if (savedInstanceState == null) {
			mPresenter.loadRandomDrink();
//		} else if (savedInstanceState.containsKey(EXTRAS_KEY_ID)) {
////			TODO fix to restore from cache
//			mId = savedInstanceState.getLong(EXTRAS_KEY_ID);
//			mPresenter.loadDrinkById(mId);
//		}
	}

	private void startIngredientDetailsActivity(IngredientItem item, View view1) {
		Timber.v("start ingredient details activity here");
		//TODO: start ingredient details activity here
	}

	public void loadRandomDrink() {
		mPresenter.loadRandomDrink();
		ivImage.setVisibility(View.VISIBLE);
		txtError.setVisibility(View.GONE);
	}

//	@Override
//	public void onSaveInstanceState(Bundle outState) {
//		super.onSaveInstanceState(outState);
//		if (mId != -1) {
//			outState.putLong(EXTRAS_KEY_ID, mId);
//		}
//	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mPresenter.unbindView();
		mPresenter = null;
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
	public void showQueryError() {
		ivImage.setVisibility(View.INVISIBLE);
		txtError.setVisibility(View.VISIBLE);
		txtError.setText(R.string.msg_error_on_query);
		txtName.setText(null);
		txtDescription.setText(null);
	}

	@Override
	public void showNetworkError() {
		ivImage.setVisibility(View.INVISIBLE);
		txtError.setVisibility(View.VISIBLE);
		txtError.setText(R.string.msg_error_no_internet);
		txtName.setText(null);
		txtDescription.setText(null);
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
			Glide.with(getContext())
					.load(url)
					.listener(new RequestListener<Drawable>() {
						@Override
						public boolean onLoadFailed(@Nullable GlideException e, Object model,
															 Target<Drawable> target, boolean isFirstResource) {
//							supportStartPostponedEnterTransition();
							hideProgress();
//							showError();
							return false;
						}

						@Override
						public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
																 DataSource dataSource, boolean isFirstResource) {
//							supportStartPostponedEnterTransition();
							hideProgress();
							return false;
						}
					})
					.into(ivImage);
		} else {
//			supportStartPostponedEnterTransition();
			ivImage.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
			ivImage.setImageResource(R.drawable.no_image);
			hideProgress();
		}
	}

	@Override
	public void displayIngredientsList(List<IngredientItem> items) {
		mAdapter.setData(items);
	}

//	@Override
//	public void displayData(DetailsModel model) {
//		if (model.getImageUrl() != null) {
//			mId = model.getId();
//			ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
//			Glide.with(getContext())
//					.load(model.getImageUrl())
//					.listener(new RequestListener<Drawable>() {
//						@Override
//						public boolean onLoadFailed(@Nullable GlideException e, Object model,
//															 Target<Drawable> target, boolean isFirstResource) {
//
//							hideProgress();
//							return false;
//						}
//
//						@Override
//						public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
//																 DataSource dataSource, boolean isFirstResource) {
//							hideProgress();
//							return false;
//						}
//					})
//					.into(ivImage);
//		}
//		txtName.setText(model.getName());
//		txtDescription.setText(model.getDescription());
//	}
}
