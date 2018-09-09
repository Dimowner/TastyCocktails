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

package com.dimowner.tastycocktails.random;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import javax.inject.Inject;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.dimowner.tastycocktails.ModelMapper;
import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.cocktails.details.DetailsViewModel;
import com.dimowner.tastycocktails.cocktails.details.ImagePreviewActivity;
import com.dimowner.tastycocktails.cocktails.details.IngredientItem;
import com.dimowner.tastycocktails.dagger.details.DetailsModule;
import com.dimowner.tastycocktails.data.model.Drink;
import com.dimowner.tastycocktails.util.AndroidUtils;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class RandomFragment2 extends Fragment {

	private final String EXTRAS_KEY_ADAPTER_DATA = "adapter_data";

//	@Inject
//	RandomContract.UserActionsListener mPresenter;

	@Inject
	DetailsViewModel viewModel;

	private CompositeDisposable compositeDisposable = new CompositeDisposable();


//	private RecyclerView mRecyclerView;
//	private IngredientsAdapter mAdapter;

	private boolean isFavorite = false;
	private boolean isCreated = false;

	private ImageButton btnMenu;
	private ImageButton btnFav;
	//	private FloatingActionButton fab;
//	private CoordinatorLayout mRoot;
	private ConstraintLayout mRoot;

	private ImageView imageView;
	private TextView txtName;
	private TextView txtDescription;
	private TextView txtCategory;
	private TextView txtAlcoholic;
	private TextView txtGlass;
	private TextView txtError;
	private ProgressBar progress;

	private View.OnClickListener openMenuListener;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		TCApplication.get(getContext()).applicationComponent()
				.plus(new DetailsModule(this)).injectRandomFragment2(this);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
//		View view = inflater.inflate(R.layout.fragment_random, container, false);
		View view = inflater.inflate(R.layout.fragment_details, container, false);
//		mRecyclerView = view.findViewById(R.id.recycler_view);
		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		FrameLayout titleBar = view.findViewById(R.id.title_bar);
		titleBar.setVisibility(View.VISIBLE);

		btnMenu = view.findViewById(R.id.btn_menu);
		btnFav = view.findViewById(R.id.btn_favorite);
		btnFav.setOnClickListener(v -> {}
//				compositeDisposable.add(viewModel.reverseFavorite(ids.get(viewPager.getCurrentItem()))
//						.subscribeOn(Schedulers.io())
//						.observeOn(AndroidSchedulers.mainThread())
//						.subscribe(() -> {
//							Timber.v("REVERSE FAV");
//							updateFavorite(viewModel.getCachedDrink(viewPager.getCurrentItem()));
//						}, Timber::e))
		);
//		mRecyclerView.setHasFixedSize(true);
//		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//
//		mRoot = view.findViewById(R.id.coordinator);
//		btnFavorite = view.findViewById(R.id.btn_favorite);
//		btnMenu = view.findViewById(R.id.btn_menu);
//		fab = view.findViewById(R.id.fab);
//		fab.setOnClickListener(v -> mPresenter.loadRandomDrink());
//
//		btnFavorite.setOnClickListener(v -> mPresenter.reverseFavorite());

		imageView = view.findViewById(R.id.details_image);
		txtName = view.findViewById(R.id.details_name);
		txtDescription = view.findViewById(R.id.details_description);
		txtCategory = view.findViewById(R.id.details_category_content);
		txtAlcoholic = view.findViewById(R.id.details_alcoholic_content);
		txtGlass = view.findViewById(R.id.details_glass_content);
		txtError = view.findViewById(R.id.details_error);
		progress = view.findViewById(R.id.progress);

		mRoot = view.findViewById(R.id.constraint_layout);

//		ConstraintSet set = new ConstraintSet();
//		View child = getLayoutInflater().inflate(R.layout.list_item_ingredient, null);
//		((TextView)child.findViewById(R.id.list_item_ingredient_name)).setText("Vodka");
//		((TextView)child.findViewById(R.id.list_item_ingredient_measure)).setText("Measure");
//		mRoot.addView(child);
//		set.clone(mRoot);
//		set.connect(R.id.ingredient, ConstraintSet.TOP, R.id.ingredients_label, ConstraintSet.BOTTOM, 60);
//		set.applyTo(mRoot);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// Set the padding to match the Status Bar height
			view.findViewById(R.id.title_bar).setPadding(0, AndroidUtils.getStatusBarHeight(getContext()), 0, 0);
		}


		if (savedInstanceState == null) {
//			initAdapter();
//
//			mPresenter.bindView(mAdapter);
			compositeDisposable.add(viewModel.getRandomDrink()
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(this::showDrink));
		}
	}

	private void showDrink(Drink drink) {
		Timber.v("Drink = " + drink.toString());
//							adapter.setDrink(drink);
		txtName.setText(drink.getStrDrink());
		txtDescription.setText(drink.getStrInstructions());
		txtCategory.setText(drink.getStrCategory());
		txtAlcoholic.setText(drink.getStrAlcoholic());
		txtGlass.setText(drink.getStrGlass());

		List<IngredientItem> ingredientItems = ModelMapper.getIngredientsFromDrink(drink);

		ConstraintSet set = new ConstraintSet();
		View child = getLayoutInflater().inflate(R.layout.list_item_ingredient, null);
		((TextView)child.findViewById(R.id.list_item_ingredient_name)).setText(drink.getStrIngredient1());
		((TextView)child.findViewById(R.id.list_item_ingredient_measure)).setText(drink.getStrMeasure1());
		if (ingredientItems.size() > 0) {
			Glide.with(getContext())
					.load(ingredientItems.get(0).getImageUrl())
					.apply(RequestOptions.circleCropTransform())
					.listener(new RequestListener<Drawable>() {
						@Override
						public boolean onLoadFailed(@Nullable GlideException e, Object model,
															 Target<Drawable> target, boolean isFirstResource) {
							return false;
						}

						@Override
						public boolean onResourceReady(Drawable resource, Object model,
																 Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
							child.findViewById(R.id.list_item_ingredient_image).setVisibility(View.VISIBLE);
							return false;
						}
					})
					.into((ImageView) child.findViewById(R.id.list_item_ingredient_image));
		}

		mRoot.addView(child);
		set.clone(mRoot);
		set.connect(R.id.ingredient, ConstraintSet.TOP, R.id.ingredients_label, ConstraintSet.BOTTOM, 60);
		set.applyTo(mRoot);

		Glide.with(getContext())
				.load(drink.getStrDrinkThumb())
				.listener(new RequestListener<Drawable>() {
					@Override
					public boolean onLoadFailed(@Nullable GlideException e, Object model,
														 Target<Drawable> target, boolean isFirstResource) {
						imageView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
						imageView.setImageResource(R.drawable.no_image);
						txtError.setVisibility(View.VISIBLE);
						progress.setVisibility(View.GONE);
						return false;
					}

					@Override
					public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
															 DataSource dataSource, boolean isFirstResource) {
						imageView.setVisibility(View.VISIBLE);
						if (txtError.getVisibility() == View.VISIBLE) {
							txtError.setVisibility(View.GONE);
						}
						progress.setVisibility(View.GONE);
						return false;
					}
				})
				.into(imageView);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (openMenuListener != null) {
			btnMenu.setOnClickListener(openMenuListener);
		}
	}
//
//	@Override
//	public void onResume() {
//		super.onResume();
//		if (!isCreated) {
//			mPresenter.loadRandomDrink();
//			fab.setVisibility(View.VISIBLE);
//			AnimationUtil.physBasedRevealAnimation(fab);
//			isCreated = true;
//		}
//	}

	@Override
	public void onStop() {
		super.onStop();
		compositeDisposable.clear();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		compositeDisposable.dispose();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
//		updateFavorite(isFavorite);
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("is_favorite", isFavorite);
		outState.putBoolean("is_created", isCreated);
//		if (mAdapter != null) {
//			outState.putParcelable(EXTRAS_KEY_ADAPTER_DATA, mAdapter.onSaveInstanceState());
//		}
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		if (savedInstanceState != null && savedInstanceState.containsKey(EXTRAS_KEY_ADAPTER_DATA)) {
			isFavorite = savedInstanceState.getBoolean("is_favorite");
			isCreated = savedInstanceState.getBoolean("is_created");
//			updateFavorite(isFavorite);
//			initAdapter();
//
//			mPresenter.bindView(mAdapter);
//			mAdapter.onRestoreInstanceState(savedInstanceState.getParcelable(EXTRAS_KEY_ADAPTER_DATA));
		}
	}

//	private void initAdapter() {
//		if (mAdapter == null) {
//			mAdapter = new IngredientsAdapter();
//			mRecyclerView.setAdapter(mAdapter);
//		}
//
//		mAdapter.setItemClickListener((view1, position) ->
//				startIngredientDetailsActivity(mAdapter.getItem(position)));
//
//		mAdapter.setFavoriteUpdateListener(fav -> {
//			isFavorite = fav;
//			updateFavorite(isFavorite);
//		});
//
//		mAdapter.setOnImageClickListener(path -> {
//			Intent intent = new Intent(getContext(), ImagePreviewActivity.class);
//			intent.putExtra(ImagePreviewActivity.EXTRAS_KEY_IMAGE_PATH, path);
//			startActivity(intent);
//		});
//
//		btnMenu.setImageResource(R.drawable.circle_drawable_menu);
//		btnFavorite.setImageResource(isFavorite ? R.drawable.circle_drawable_heart : R.drawable.circle_drawable_heart_outline);
//
//		mAdapter.setOnSnackBarListener(message -> Snackbar.make(mRoot, message, Snackbar.LENGTH_LONG).show());
//	}
//
//	@Override
//	public void onDestroyView() {
//		super.onDestroyView();
//		mPresenter.unbindView();
//		mPresenter = null;
//	}

	private void startIngredientDetailsActivity(IngredientItem item) {
		Intent intent = new Intent(getContext(), ImagePreviewActivity.class);
		intent.putExtra(ImagePreviewActivity.EXTRAS_KEY_IMAGE_PATH, item.getImageUrl());
		startActivity(intent);
	}
//
//	private void updateFavorite(boolean fav) {
//		btnFavorite.setImageResource(fav ? R.drawable.circle_drawable_heart : R.drawable.circle_drawable_heart_outline);
//	}

	public void setOpenMenuListener(View.OnClickListener openMenuListener) {
		this.openMenuListener = openMenuListener;
	}

	private void updateFavorite(Drink d) {
		if (d != null && d.isFavorite()) {
			btnFav.setImageResource(R.drawable.round_heart);
		} else {
			btnFav.setImageResource(R.drawable.round_heart_border);
		}
	}

}
