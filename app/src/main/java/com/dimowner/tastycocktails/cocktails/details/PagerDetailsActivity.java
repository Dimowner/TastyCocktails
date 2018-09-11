package com.dimowner.tastycocktails.cocktails.details;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.analytics.MixPanel;
import com.dimowner.tastycocktails.dagger.details.DetailsModule;
import com.dimowner.tastycocktails.data.model.Drink;
import com.dimowner.tastycocktails.util.AndroidUtils;

import java.util.ArrayList;
import java.util.Stack;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class PagerDetailsActivity  extends AppCompatActivity {

	public static final int TYPE_SEARCH = 1;
	public static final int TYPE_FILETERS= 2;
	public static final int TYPE_FAVORITES= 3;
	public static final int TYPE_HISTORY= 4;

	private final String EXTRAS_KEY_ADAPTER_DATA = "adapter_data";
	public static final String EXTRAS_KEY_TYPE = "details_type";
	public static final String EXTRAS_KEY_ACTIVE_FILTER = "active_filter";
	public static final String EXTRAS_KEY_FILTER_VALUE = "filter_value";
	public static final String EXTRAS_KEY_IDS = "items_ids";
	public static final String EXTRAS_KEY_ACTIVE_ITEM_POS = "active_item_pos";
	public static final String EXTRAS_KEY_ID = "cocktail_id";

	@Inject
	DetailsViewModel viewModel;

	private Stack<IngredientsAdapter2> adaptersPool;

	private FrameLayout titleBar;
	private ViewPager viewPager;
	private ImageButton btnFav;

	private DetailsPagerAdapter pagerAdapter;

	private CompositeDisposable compositeDisposable = new CompositeDisposable();

	//TODO: move this into presenter
	private int activeItem = 0;
	private ArrayList<Integer> ids;


	public static Intent getStartIntent(Context context, ArrayList<Integer> ids, int activeItemPosition) {
		Intent i = new Intent(context, PagerDetailsActivity.class);
		i.putIntegerArrayListExtra(EXTRAS_KEY_IDS, ids);
		i.putExtra(EXTRAS_KEY_ACTIVE_ITEM_POS, activeItemPosition);
		return i;
	}

	public static Intent getStartIntent(Context context, int type) {
		Intent i = new Intent(context, PagerDetailsActivity.class);
		i.putExtra(EXTRAS_KEY_TYPE, type);
		if (type == TYPE_FILETERS) {
			throw new RuntimeException("Not appropriate method call. Please call another getStartIntent for this type with additional params");
		}
		return i;
	}

	public static Intent getStartIntent(Context context, int type, int activeFilter, String filterValue) {
		Intent i = new Intent(context, PagerDetailsActivity.class);
		i.putExtra(EXTRAS_KEY_TYPE, type);
		if (type == TYPE_FILETERS) {
			i.putExtra(EXTRAS_KEY_ACTIVE_FILTER, activeFilter);
			i.putExtra(EXTRAS_KEY_FILTER_VALUE, filterValue);
		}
		return i;
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pager_details);

		TCApplication.get(getApplicationContext()).applicationComponent()
				.plus(new DetailsModule(this)).injectPagerDetails(this);

		if (getIntent().hasExtra(EXTRAS_KEY_IDS)) {
			ids = getIntent().getIntegerArrayListExtra(EXTRAS_KEY_IDS);
		}
		if (getIntent().hasExtra(EXTRAS_KEY_ACTIVE_ITEM_POS)) {
			activeItem = getIntent().getIntExtra(EXTRAS_KEY_ACTIVE_ITEM_POS, 0);
			if (activeItem == 0) {
				updateHistory(0);
			}
		}

		titleBar = findViewById(R.id.title_bar);
		ImageButton btnBack = findViewById(R.id.btn_back);
		btnBack.setOnClickListener(v -> finish());
		btnFav = findViewById(R.id.btn_favorite);
		btnFav.setOnClickListener(v ->
				compositeDisposable.add(viewModel.reverseFavorite(ids.get(viewPager.getCurrentItem()))
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(() -> updateFavorite(viewModel.getCachedDrink(viewPager.getCurrentItem())), Timber::e))
		);
		viewPager = findViewById(R.id.pager);
		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
			@Override public void onPageSelected(int position) {
				updateFavorite(viewModel.getCachedDrink(position));
				updateHistory(position);
			}
			@Override public void onPageScrollStateChanged(int state) {}
		});

		adaptersPool = new Stack<>();

		pagerAdapter = new DetailsPagerAdapter(getApplicationContext(), R.layout.layout_details, ids.size());
		pagerAdapter.setOnCreatePageListener(new DetailsPagerAdapter.OnCreatePageListener() {
			@Override
			public void onCreatePage(int pos, View view) {
				RecyclerView recyclerView = (RecyclerView) view;
				recyclerView.setHasFixedSize(true);
				recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

				IngredientsAdapter2 adapter;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					if (adaptersPool.empty()) {
						adapter = new IngredientsAdapter2(isInMultiWindowMode());
					} else {
						adapter = adaptersPool.pop();
					}
				} else {
					if (adaptersPool.empty()) {
						adapter = new IngredientsAdapter2();
					} else {
						adapter = adaptersPool.pop();
					}
				}
				adapter.setItemClickListener((v1, pos1) -> startIngredientDetailsActivity(adapter.getItem(pos1).getImageUrl(), v1));
				adapter.setOnImageClickListener((v2, path) -> startIngredientDetailsActivity(path, v2));

				recyclerView.setAdapter(adapter);

				compositeDisposable.add(viewModel.getDrink(ids.get(pos), pos)
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(drink -> {
							adapter.setDrink(drink);
							if (viewPager.getCurrentItem() == pos) {
								updateFavorite(drink);
							}
						}, Timber::e));
			}

			@Override
			public void onDestroyPage(int pos, View view) {
				RecyclerView recyclerView = (RecyclerView) view;
				adaptersPool.push((IngredientsAdapter2) recyclerView.getAdapter());
				recyclerView.setAdapter(null);

				viewModel.removeFromCache(pos);
			}
		});
		viewPager.setAdapter(pagerAdapter);
		viewPager.setCurrentItem(activeItem, false);

		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// Set the padding to match the Status Bar height
			titleBar.setPadding(0, AndroidUtils.getStatusBarHeight(getApplicationContext()), 0, 0);
		}

		if (savedInstanceState == null) {
			AndroidUtils.handleNavigationBarColor(this);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		compositeDisposable.clear();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		compositeDisposable.dispose();
	}

	private void startIngredientDetailsActivity(String path, View view) {
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			startActivity(ImagePreviewActivity.getStartIntent(getApplicationContext(), path),
					ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
		} else {
			startActivity(ImagePreviewActivity.getStartIntent(getApplicationContext(), path));
		}
	}

//	@Override
//	public void onSaveInstanceState(Bundle outState) {
//		super.onSaveInstanceState(outState);
//		if (mAdapter != null) {
//			outState.putParcelable(EXTRAS_KEY_ADAPTER_DATA, mAdapter.onSaveInstanceState());
//		}
//		outState.putBoolean("is_favorite", isFavorite);
//		outState.putBoolean("is_image_dark", isImageDark);
//	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		AndroidUtils.handleNavigationBarColor(this);
//		if (savedInstanceState != null && savedInstanceState.containsKey(EXTRAS_KEY_ADAPTER_DATA)) {
//			isFavorite = savedInstanceState.getBoolean("is_favorite");
//			isImageDark = savedInstanceState.getBoolean("is_image_dark");
//			updateFavorite(isFavorite);
//
//			initAdapter();
//			mPresenter.bindView(mAdapter);
//			mAdapter.onRestoreInstanceState(savedInstanceState.getParcelable(EXTRAS_KEY_ADAPTER_DATA));
//		}
	}

	private void updateFavorite(Drink d) {
//		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			if (d != null && d.isFavorite()) {
				btnFav.setImageResource(R.drawable.round_heart);
			} else {
				btnFav.setImageResource(R.drawable.round_heart_border);
			}
//		} else {
//			if (d != null && d.isFavorite()) {
//				btnFav.setImageResource(R.drawable.heart);
//			} else {
//				btnFav.setImageResource(R.drawable.heart_outline);
//			}
//		}
	}

	private void updateHistory(int pos) {
		compositeDisposable.add(viewModel.updateDrinkHistory(ids.get(pos))
				.subscribeOn(Schedulers.io())
				.subscribe(() -> {}, Timber::e));
	}
}
