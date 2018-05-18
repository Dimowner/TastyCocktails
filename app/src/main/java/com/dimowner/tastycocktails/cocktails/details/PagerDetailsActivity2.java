package com.dimowner.tastycocktails.cocktails.details;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.dagger.details.DetailsModule;
import com.dimowner.tastycocktails.data.model.Drink;
import com.dimowner.tastycocktails.util.AndroidUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class PagerDetailsActivity2 extends AppCompatActivity {

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

	private FrameLayout titleBar;
	private ViewPager viewPager;
	private ImageButton btnFav;

	private DetailsPagerAdapter pagerAdapter;

	private CompositeDisposable compositeDisposable = new CompositeDisposable();

	//TODO: move this into presenter
	private int activeItem = 0;
	private ArrayList<Integer> ids;


	public static Intent getStartIntent(Context context, ArrayList<Integer> ids, int activeItemPosition) {
		Intent i = new Intent(context, PagerDetailsActivity2.class);
		i.putIntegerArrayListExtra(EXTRAS_KEY_IDS, ids);
		i.putExtra(EXTRAS_KEY_ACTIVE_ITEM_POS, activeItemPosition);
		return i;
	}

	public static Intent getStartIntent(Context context, int type) {
		Intent i = new Intent(context, PagerDetailsActivity2.class);
		i.putExtra(EXTRAS_KEY_TYPE, type);
		if (type == TYPE_FILETERS) {
			throw new RuntimeException("Not appropriate method call. Please call another getStartIntent for this type with additional params");
		}
		return i;
	}

	public static Intent getStartIntent(Context context, int type, int activeFilter, String filterValue) {
		Intent i = new Intent(context, PagerDetailsActivity2.class);
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
				.plus(new DetailsModule(this)).injectPagerDetails2(this);

		if (getIntent().hasExtra(EXTRAS_KEY_IDS)) {
			ids = getIntent().getIntegerArrayListExtra(EXTRAS_KEY_IDS);
		}
		if (getIntent().hasExtra(EXTRAS_KEY_ACTIVE_ITEM_POS)) {
			activeItem = getIntent().getIntExtra(EXTRAS_KEY_ACTIVE_ITEM_POS, 0);
		}

		titleBar = findViewById(R.id.title_bar);
		ImageButton btnBack = findViewById(R.id.btn_back);
		btnBack.setOnClickListener(v -> finish());
		btnFav = findViewById(R.id.btn_favorite);
		btnFav.setOnClickListener(v ->
				compositeDisposable.add(viewModel.reverseFavorite(ids.get(viewPager.getCurrentItem()))
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(() -> {
							Timber.v("REVERSE FAV");
							updateFavorite(viewModel.getCachedDrink(viewPager.getCurrentItem()));
						}, Timber::e))
		);
		viewPager = findViewById(R.id.pager);
		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
			@Override public void onPageSelected(int position) {
				updateFavorite(viewModel.getCachedDrink(position));
			}
			@Override public void onPageScrollStateChanged(int state) {}
		});

		pagerAdapter = new DetailsPagerAdapter(getApplicationContext(), R.layout.fragment_details, ids.size());
		pagerAdapter.setOnCreatePageListener(new DetailsPagerAdapter.OnCreatePageListener() {
			@Override
			public void onCreatePage(int pos, View view) {

				ImageView imageView = view.findViewById(R.id.details_image);
				TextView txtName = view.findViewById(R.id.details_name);
				TextView txtDescription = view.findViewById(R.id.details_description);
				TextView txtCategory = view.findViewById(R.id.details_category_content);
				TextView txtAlcoholic = view.findViewById(R.id.details_alcoholic_content);
				TextView txtGlass = view.findViewById(R.id.details_glass_content);
				TextView txtError = view.findViewById(R.id.details_error);
				ProgressBar progress = view.findViewById(R.id.progress);

				compositeDisposable.add(viewModel.getDrink(ids.get(pos), pos)
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(drink -> {
							Timber.v("Drink = " + drink.toString());
//							adapter.setDrink(drink);
							txtName.setText(drink.getStrDrink());
							txtDescription.setText(drink.getStrInstructions());
							txtCategory.setText(drink.getStrCategory());
							txtAlcoholic.setText(drink.getStrAlcoholic());
							txtGlass.setText(drink.getStrGlass());

							Glide.with(getApplicationContext())
									.load(drink.getStrDrinkThumb())
									.listener(new RequestListener<Drawable>() {
										@Override
										public boolean onLoadFailed(@Nullable GlideException e, Object model,
																			 Target<Drawable> target, boolean isFirstResource) {
											imageView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
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
							updateFavorite(drink);
						}, Timber::e));
			}

			@Override
			public void onDestroyPage(int pos, View view) {
				Timber.v("onDestroyPage p = " + pos);
//				compositeDisposable.clear();
				viewModel.removeFromCache(pos);
			}
		});
		viewPager.setAdapter(pagerAdapter);
		viewPager.setCurrentItem(activeItem, false);

		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// Set the padding to match the Status Bar height
			titleBar.setPadding(0, AndroidUtils.getStatusBarHeight(getApplicationContext()), 0, 0);
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

	private void startIngredientDetailsActivity(String path) {
		Intent intent = new Intent(getApplicationContext(), ImagePreviewActivity.class);
		intent.putExtra(ImagePreviewActivity.EXTRAS_KEY_IMAGE_PATH, path);
		startActivity(intent);
	}

	private void updateFavorite(Drink d) {
		if (d != null && d.isFavorite()) {
			btnFav.setImageResource(R.drawable.circle_drawable_heart);
		} else {
			btnFav.setImageResource(R.drawable.circle_drawable_heart_outline);
		}
	}
}
