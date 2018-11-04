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

package com.dimowner.tastycocktails.cocktails;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Animatable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.analytics.MixPanel;
import com.dimowner.tastycocktails.cocktails.details.PagerDetailsActivity;
import com.dimowner.tastycocktails.cocktails.list.CocktailsRecyclerAdapter;
import com.dimowner.tastycocktails.cocktails.list.ListItem;
import com.dimowner.tastycocktails.dagger.cocktails.CocktailsModule;
import com.dimowner.tastycocktails.data.Prefs;
import com.dimowner.tastycocktails.util.AndroidUtils;
import com.dimowner.tastycocktails.util.AnimationUtil;
import com.dimowner.tastycocktails.util.UIUtil;
import com.dimowner.tastycocktails.widget.ThresholdListener;
import com.dimowner.tastycocktails.widget.TouchLayout;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import timber.log.Timber;

/**
 * Created on 26.07.2017.
 * @author Dimowner
 */
public class CocktailsListFragment extends Fragment implements CocktailsListContract.View {

	public static final long ANIMATION_DURATION = 200;

	public static final int TYPE_UNKNOWN = 0;
	public static final int TYPE_NORMAL = 1;
	public static final int TYPE_FAVORITES = 2;
	public static final int TYPE_HISTORY = 3;

	public static final int FILTER_TYPE_CATEGORY = 1;
	public static final int FILTER_TYPE_INGREDIENT = 2;
	public static final int FILTER_TYPE_GLASS = 3;
	public static final int FILTER_TYPE_ALCOHOLIC_NON_ALCOHOLIC = 4;
	public static final int ITEM_COUNT_WITHOUT_BTN_UP = 15;

	public static final String EXTRAS_KEY_TYPE = "search_fragment_type";

	public static final int ADD_TO_FAVORITES_ANIMATION_DURATION = 600;

//	private final String EXTRAS_KEY_ADAPTER_DATA = "adapter_data";

	private AppCompatActivity parentActivity;

	private RecyclerView mRecyclerView;
	private ScrollView mWelcomePanel;
	private TextView mTxtEmpty;
	private CoordinatorLayout mRoot;
	private TouchLayout touchLayout;
	private View filterMenu;
	private SwipeRefreshLayout mRefreshLayout;
	private FloatingActionButton btnUp;
	private AdView adView;

	private Spinner categorySpinner;
	private Spinner ingredientSpinner;
	private Spinner glassSpinner;
	private Spinner alcoholicSpinner;

	private CocktailsRecyclerAdapter mAdapter;
	private HorizontalDividerItemDecoration dividerItemDecoration;

	private MenuItem searchMenu;

	private int fragmentType = TYPE_UNKNOWN;

	boolean isChangedFilter = false;

	private OnFirstRunExecutedListener onFirstRunExecutedListener;

	private CompositeDisposable compositeDisposable = new CompositeDisposable();

	@Inject
	CocktailsListContract.UserActionsListener mPresenter;

	private ArrayList<Integer> ids;

	@Inject
	Prefs prefs;

	private int selectedFilter = -1;
	private boolean isSearchOpen = false;
	private boolean openSearchClicked = false;
	private boolean onPanelTouch = false;
	private int defaultBtnUpY = 0;


	public static CocktailsListFragment newInstance(int fragmentType) {
		CocktailsListFragment fragment = new CocktailsListFragment();
		Bundle args = new Bundle();
		args.putInt(EXTRAS_KEY_TYPE, fragmentType);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		if (getContext() != null) {
			TCApplication.get(getContext()).applicationComponent()
					.plus(new CocktailsModule(this)).injectCocktailsSearch(this);
		}

		if (getArguments() != null && getArguments().containsKey(EXTRAS_KEY_TYPE)) {
			fragmentType = getArguments().getInt(EXTRAS_KEY_TYPE);
		}

		switch (fragmentType) {
			case TYPE_NORMAL:
				TCApplication.event(getActivity().getApplicationContext(), MixPanel.EVENT_HOME);
				break;
			case TYPE_FAVORITES:
				TCApplication.event(getActivity().getApplicationContext(), MixPanel.EVENT_FAVORITES);
				break;
			case TYPE_HISTORY:
				TCApplication.event(getActivity().getApplicationContext(), MixPanel.EVENT_HISTORY);
				break;
		}
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_search, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		parentActivity = (AppCompatActivity) getActivity();
		mRoot = view.findViewById(R.id.coordinator_root);
		touchLayout = view.findViewById(R.id.touch_layout);
		mWelcomePanel = view.findViewById(R.id.welcome_panel);
		mTxtEmpty = view.findViewById(R.id.txt_empty);
		btnUp = view.findViewById(R.id.fab);
		mRecyclerView = view.findViewById(R.id.recycler_view);
		mRecyclerView.setHasFixedSize(true);

		mRefreshLayout = view.findViewById(R.id.swiperefresh);
		mRefreshLayout.setOnRefreshListener(() -> {
			mRefreshLayout.canChildScrollUp();
			loadData();
		});
		btnUp.setOnClickListener(v -> mRecyclerView.smoothScrollToPosition(0));

		//Calculate position by default for btn up
		ViewTreeObserver vto = btnUp.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				ViewTreeObserver obs = btnUp.getViewTreeObserver();
				defaultBtnUpY = btnUp.getHeight() + (int) getResources().getDimension(R.dimen.padding_huge);
				btnUp.setTranslationY(defaultBtnUpY);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					obs.removeOnGlobalLayoutListener(this);
				} else {
					obs.removeGlobalOnLayoutListener(this);
				}
			}
		});

		//Hide show filters panel on scroll list
		mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				handleFiltersPanelScroll(dy);
				handleBtnUpScroll(dy);
			}
		});

		// use a linear layout manager
		RecyclerView.LayoutManager mLayoutManager = new AppLinearLayoutManager(getContext());
		mRecyclerView.setLayoutManager(mLayoutManager);
		if (getContext() != null) {
			dividerItemDecoration = new HorizontalDividerItemDecoration(getContext(), ((AppLinearLayoutManager) mLayoutManager).getOrientation());
			mRecyclerView.addItemDecoration(dividerItemDecoration);
		}

		mPresenter.bindView(this);

		if (prefs.isFirstRun() && fragmentType == TYPE_NORMAL) {
			mWelcomePanel.setVisibility(View.VISIBLE);
			mTxtEmpty.setVisibility(View.GONE);
			mRecyclerView.setVisibility(View.GONE);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
				parentActivity.getWindow().setNavigationBarColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
			}
		}

		if (!prefs.isDrinksCached() && !prefs.isCacheFailed()) {
			compositeDisposable.add(mPresenter.firstRunInitialization(getContext())
					.subscribe(drinks1 -> {
						Timber.d("Succeed to cache %d drinks!", drinks1.length);
						if (drinks1.length > 0) {
							prefs.setDrinksCached();
						} else {
							prefs.setCacheFailed();
						}
					}, Timber::e));
		}

		if (!prefs.isFirstRun()) {
			adView = view.findViewById(R.id.publisherAdView);
			adView.setVisibility(View.VISIBLE);
			if (adView != null ) {
				if (prefs.isShowAds()) {
					AdRequest adRequest = new AdRequest.Builder()
							.addTestDevice("3CDE42B77B78065EF7879C6A83E0AF4B")
							.addTestDevice("849A8D331C1E0F2AE74C7330D0BEF9D8")
							.addTestDevice("53ECB11D7A7CCB1BCC9B40BAF5F5DAE7")
							.build();
					adView.loadAd(adRequest);
				} else {
					adView.setVisibility(View.GONE);
				}
			}
		}

		if (fragmentType == TYPE_HISTORY) {
			ItemTouchHelper.SimpleCallback itemTouchHelperCallback =
					new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
						@Override
						public boolean onMove(RecyclerView recyclerView,
													 RecyclerView.ViewHolder viewHolder,
													 RecyclerView.ViewHolder target) {
							return false;
						}

						@Override
						public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
							if (viewHolder instanceof CocktailsRecyclerAdapter.ItemViewHolder) {
								final View foregroundView = ((CocktailsRecyclerAdapter.ItemViewHolder)viewHolder).getContainer();
								getDefaultUIUtil().onSelected(foregroundView);
							}
						}

						@Override
						public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
															 RecyclerView.ViewHolder viewHolder, float dX, float dY,
															 int actionState, boolean isCurrentlyActive) {
							if (viewHolder instanceof CocktailsRecyclerAdapter.ItemViewHolder) {
								final View foregroundView = ((CocktailsRecyclerAdapter.ItemViewHolder) viewHolder).getContainer();
								getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
										actionState, isCurrentlyActive);
							}
						}

						@Override
						public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
							if (viewHolder instanceof CocktailsRecyclerAdapter.ItemViewHolder) {
								final View foregroundView = ((CocktailsRecyclerAdapter.ItemViewHolder) viewHolder).getContainer();
								getDefaultUIUtil().clearView(foregroundView);
							}
						}

						@Override
						public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
							int pos = viewHolder.getAdapterPosition();
							ListItem item = mAdapter.getItem(pos);
							mPresenter.removeFromHistory(item.getId());
							showSnackBarRemoveFromHistory(item, pos);
						}

						@Override
						public void onChildDraw(Canvas c, RecyclerView recyclerView,
														RecyclerView.ViewHolder viewHolder, float dX, float dY,
														int actionState, boolean isCurrentlyActive) {
							if (viewHolder instanceof CocktailsRecyclerAdapter.ItemViewHolder) {
								getDefaultUIUtil().onDraw(c, recyclerView, ((CocktailsRecyclerAdapter.ItemViewHolder) viewHolder).getContainer(), dX, dY, actionState, isCurrentlyActive);
							}
						}
					};
			new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
		} else if (fragmentType == TYPE_NORMAL) {
			initFiltersPanel(view);
			touchLayout.setOnThresholdListener(new ThresholdListener() {
				@Override
				public void onTopThreshold() {
					invertMenuButton();
					showMenu();
				}

				@Override
				public void onBottomThreshold() {
					invertMenuButton();
					showMenu();
				}

				@Override
				public void onTouchDown() {
					onPanelTouch = true;
				}

				@Override
				public void onTouchUp() {
					onPanelTouch = false;
				}
			});
		}
		Toolbar toolbar = parentActivity.findViewById(R.id.toolbar);
		toolbarMenuItemAnimation(toolbar);

		initAdapter();
		if (prefs.isFirstRun()) {
			Button btnGetStarted = view.findViewById(R.id.get_started);
			btnGetStarted.setOnClickListener(view1 -> executeFirsRun());
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		mPresenter.bindView(this);
		if (prefs.isFirstRun()) {
			String values[] = getResources().getStringArray(R.array.filter_categories);
			prefs.setFirstRunDefaultValues(Prefs.SEARCH_TYPE_FILTER, 1, values[1]);
			categorySpinner.setSelection(1);

			mPresenter.loadFilteredList(
					prefs.getFilterCategory(),
					prefs.getFilterIngredient(),
					prefs.getFilterGlass(),
					prefs.getFilterAlcoholic());
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
				parentActivity.getWindow().setNavigationBarColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
			}
		} else {
			loadData();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (adView != null) {
			if (prefs.isShowAds()) {
				adView.resume();
			} else {
				adView.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onPause() {
		if (adView != null) {
			adView.pause();
		}
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		mPresenter.unbindView();
	}

	@Override
	public void onDestroyView() {
		if (adView != null) {
			adView.destroy();
		}
		super.onDestroyView();
		mPresenter = null;
		compositeDisposable.dispose();
	}

	private void loadData() {
		if (fragmentType == TYPE_NORMAL) {
			String lastSearch = prefs.getLastSearchString();
			if (prefs.getCurrentSearchType() == Prefs.SEARCH_TYPE_SEARCH) {
				mPresenter.loadLastSearch(lastSearch);
				updateToolbarTitle(getString(R.string.search, lastSearch));
			} else {
				mPresenter.loadFilteredList(
						prefs.getFilterCategory(),
						prefs.getFilterIngredient(),
						prefs.getFilterGlass(),
						prefs.getFilterAlcoholic());
				updateToolbarTitle(getString(R.string.app_name));
			}
		} else if (fragmentType == TYPE_FAVORITES) {
			mPresenter.loadFavorites();
			updateToolbarTitle(getString(R.string.nav_favorites));
		} else if (fragmentType == TYPE_HISTORY) {
			mPresenter.loadHistory(1);
			updateToolbarTitle(getString(R.string.nav_history));
		} else {
			Timber.e("Con't load data not correct fragment type!");
		}
	}

	private void initFiltersPanel(View view) {
		//Init CATEGORY filter
		categorySpinner = view.findViewById(R.id.filter_categories);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(getContext(),
				R.array.filter_categories, R.layout.spinner_item);
		// Specify the layout to use when the list of choices appears
		categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		// Apply the adapter to the spinner
		categorySpinner.setAdapter(categoryAdapter);

		//Init INGREDIENTS filter
		ingredientSpinner = view.findViewById(R.id.filter_ingredients);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> ingredientAdapter = ArrayAdapter.createFromResource(getContext(),
				R.array.filter_ingredients_alphabetical, R.layout.spinner_item);

		// Specify the layout to use when the list of choices appears
		ingredientAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		// Apply the adapter to the spinner
		ingredientSpinner.setAdapter(ingredientAdapter);

		//Init GLASS filter
		glassSpinner = view.findViewById(R.id.filter_glass);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> glassAdapter = ArrayAdapter.createFromResource(getContext(),
				R.array.filter_glass, R.layout.spinner_item);
		// Specify the layout to use when the list of choices appears
		glassAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		// Apply the adapter to the spinner
		glassSpinner.setAdapter(glassAdapter);

		//Init ALCOHOLIC filter
		alcoholicSpinner = view.findViewById(R.id.filter_alcoholic);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> alcoholicAdapter = ArrayAdapter.createFromResource(getContext(),
				R.array.filter_alcoholic, R.layout.spinner_item);
		// Specify the layout to use when the list of choices appears
		alcoholicAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		// Apply the adapter to the spinner
		alcoholicSpinner.setAdapter(alcoholicAdapter);

		prefs = new Prefs(getContext());
		categorySpinner.setSelection(prefs.getSelectedCategoryPos());
		ingredientSpinner.setSelection(prefs.getSelectedIngredientPos());
		glassSpinner.setSelection(prefs.getSelectedGlassPos());
		alcoholicSpinner.setSelection(prefs.getSelectedAlcoholicPos());

		categorySpinner.setOnTouchListener((view14, motionEvent) -> {
			selectedFilter = FILTER_TYPE_CATEGORY;
			view14.performClick();
			return false;
		});

		ingredientSpinner.setOnTouchListener((view13, motionEvent) -> {
			selectedFilter = FILTER_TYPE_INGREDIENT;
			view13.performClick();
			return false;
		});

		glassSpinner.setOnTouchListener((view12, motionEvent) -> {
			selectedFilter = FILTER_TYPE_GLASS;
			view12.performClick();
			return false;
		});

		alcoholicSpinner.setOnTouchListener((view1, motionEvent) -> {
			selectedFilter = FILTER_TYPE_ALCOHOLIC_NON_ALCOHOLIC;
			view1.performClick();
			return false;
		});

		categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
				if (selectedFilter == FILTER_TYPE_CATEGORY) {
					if (pos == 0) {
						prefs.saveFilterCategory(null);
						prefs.saveSelectedCategoryPos(0);
					} else {
						CharSequence category = categoryAdapter.getItem(pos);
						if (category != null) {
							prefs.saveFilterCategory(category.toString());
							prefs.saveSelectedCategoryPos(pos);
						}
					}
					prefs.setLastSearchString(null);
					prefs.saveCurrentSearchType(Prefs.SEARCH_TYPE_FILTER);
					isChangedFilter = true;
					onFilterSelected();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});

		ingredientSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
				if (selectedFilter == FILTER_TYPE_INGREDIENT) {
					if (pos == 0) {
						prefs.saveFilterIngredient(null);
						prefs.saveSelectedIngredientPos(0);
					} else {
						CharSequence ing = ingredientAdapter.getItem(pos);
						if (ing != null) {
							prefs.saveFilterIngredient(ing.toString());
							prefs.saveSelectedIngredientPos(pos);
						}
					}
					prefs.setLastSearchString(null);
					prefs.saveCurrentSearchType(Prefs.SEARCH_TYPE_FILTER);
					isChangedFilter = true;
					onFilterSelected();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});


		glassSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
				if (selectedFilter == FILTER_TYPE_GLASS) {
					if (pos == 0) {
						prefs.saveFilterGlass(null);
						prefs.saveSelectedGlassPos(0);
					} else {
						CharSequence glass = glassAdapter.getItem(pos);
						if (glass != null) {
							prefs.saveFilterGlass(glass.toString());
							prefs.saveSelectedGlassPos(pos);
						}
					}
					prefs.setLastSearchString(null);
					prefs.saveCurrentSearchType(Prefs.SEARCH_TYPE_FILTER);
					isChangedFilter = true;
					onFilterSelected();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});

		alcoholicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
				if (selectedFilter == FILTER_TYPE_ALCOHOLIC_NON_ALCOHOLIC) {
					if (pos == 0) {
						prefs.saveFilterAlcoholic(null);
						prefs.saveSelectedAlcoholicPos(0);
					} else {
						CharSequence alc = alcoholicAdapter.getItem(pos);
						if (alc != null) {
							prefs.saveFilterAlcoholic(alc.toString());
							prefs.saveSelectedAlcoholicPos(pos);
						}
					}
					prefs.setLastSearchString(null);
					prefs.saveCurrentSearchType(Prefs.SEARCH_TYPE_FILTER);
					isChangedFilter = true;
					onFilterSelected();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});

		Button btnClear = view.findViewById(R.id.btn_clear);
		Button btnClose = view.findViewById(R.id.btn_close);
		btnClear.setOnClickListener(v -> {
			categorySpinner.setSelection(0);
			ingredientSpinner.setSelection(0);
			glassSpinner.setSelection(0);
			alcoholicSpinner.setSelection(0);
			prefs.clearFilters();
			applyFilters();
		});

		btnClose.setOnClickListener(v -> {
			invertMenuButton();
			showMenu();
		});
	}

	private void onFilterSelected() {
		if (isChangedFilter) {
			applyFilters();
			isChangedFilter = false;
		}
	}

	private void invertMenuButton() {
		if (filterMenu != null) {
			if (touchLayout.getVisibility() == View.VISIBLE) {
				AnimationUtil.viewBackRotationAnimation(filterMenu, ANIMATION_DURATION);
			} else {
				AnimationUtil.viewRotationAnimation(filterMenu, ANIMATION_DURATION);
			}
		}
	}

	private void initAdapter() {
		if (mAdapter == null) {
			if (fragmentType == TYPE_HISTORY) {
				mAdapter = new CocktailsRecyclerAdapter(TYPE_HISTORY, R.layout.list_item_history);
				boolean show = prefs.isShowHistoryInstructions();
				mAdapter.showInstructions(show);
				if (show) {
					mAdapter.setInstructionsInteractionListener(() -> prefs.setShowHistoryInstructions(false));
				}
			} else {
				mAdapter = new CocktailsRecyclerAdapter(R.layout.list_item2);
			}
			mRecyclerView.setAdapter(mAdapter);
		}
		mAdapter.setItemClickListener((view1, position) -> {
			hideKeyboard();
			if (ids != null && ids.size() > 0) {
				startActivity(PagerDetailsActivity.getStartIntent(getContext(), ids, position));
			} else {
				Timber.e("Can't open preview! ids is NULL or empty");
			}
		});
//				startDetailsActivity(mAdapter.getItem(position), view1));
		mAdapter.setOnFavoriteClickListener((view12, position, id, action) -> {
			final boolean fev = mAdapter.getItem(position).isFavorite();
			final String name = mAdapter.getItem(position).getName();
			if (AndroidUtils.isAndroid5()) {
				//Add or remove from favorites with animation
				view12.setImageResource(R.drawable.avd_favorite_progress);
				Animatable animatable = ((Animatable) view12.getDrawable());
				animatable.start();
				//TODO: refactor this into presenter
				compositeDisposable.add(
						Single.just("")
								.delay(ADD_TO_FAVORITES_ANIMATION_DURATION, TimeUnit.MILLISECONDS)
								.flatMapCompletable(d -> mPresenter.reverseFavorite(id))
								.subscribeOn(Schedulers.io())
								.observeOn(AndroidSchedulers.mainThread())
								.subscribe(() -> {
										animatable.stop();
										showSnackBar(id, !fev, name);
									},
									throwable -> {
										animatable.stop();
										Timber.e(throwable);
									})
				);
			} else {
				//Add or remove from favorites without animation
				compositeDisposable.add(mPresenter.reverseFavorite(id)
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(() -> showSnackBar(id, !fev, name), Timber::e));
			}
		});
		if (fragmentType == TYPE_HISTORY) {
			mAdapter.setItemLongClickListener((view, id, position) ->
					UIUtil.showWarningDialog(
							getActivity(),
							R.drawable.round_delete_forever_black, //Dialog title icon
							getString(R.string.remove_from_history, mAdapter.getItem(position).getName()),  //Dialog title text
							(dialogInterface, i) -> mPresenter.removeFromHistory(id), //Callback for positive button
							(dialogInterface, i) -> dialogInterface.dismiss() //Callback for negative button
					)
			);
		}
	}

	private void hideKeyboard() {
		// Check if no view has focus:
		View view = getActivity().getCurrentFocus();
		if (view != null) {
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

//	private void startDetailsActivity(ListItem item, View view1) {
//		Intent intent = new Intent(getContext(), PagerDetailsActivity.class);
//		intent.putExtra(PagerDetailsActivity.EXTRAS_KEY_ID, item.getId());
//
//		//Transition
//		View txtName = view1.findViewById(R.id.list_item_name);
//		View txtDescription = view1.findViewById(R.id.list_item_description);
//		View ivImage = view1.findViewById(R.id.list_item_image);
//
//		ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(CocktailsListFragment.this.getActivity(),
//				Pair.create(txtName, getResources().getString(R.string.list_item_label_transition)),
//				Pair.create(txtDescription, getResources().getString(R.string.list_item_content_transition)),
//				Pair.create(ivImage, getResources().getString(R.string.list_item_image_transition)));
//
//		startActivity(intent, options.toBundle());
//		startActivity(intent);
//	}

	private void showSnackBar(long id, boolean isFavorite, String drinkName) {
		if (isFavorite) {
			Snackbar.make(mRoot, getString(R.string.added_to_favorites, drinkName), Snackbar.LENGTH_LONG).show();
		} else {
			if (fragmentType == TYPE_NORMAL || fragmentType == TYPE_HISTORY) {
				Snackbar.make(mRoot, getString(R.string.removed_from_favorites, drinkName), Snackbar.LENGTH_LONG).show();
			} else {
				Snackbar snackbar = Snackbar
						.make(mRoot, getString(R.string.removed_from_favorites, drinkName) , Snackbar.LENGTH_LONG)
						.setAction(R.string.undo, view ->
								compositeDisposable.add(mPresenter.reverseFavorite(id)
									.subscribeOn(Schedulers.io())
									.delay(ADD_TO_FAVORITES_ANIMATION_DURATION, TimeUnit.MILLISECONDS)
									.observeOn(AndroidSchedulers.mainThread())
									.subscribe(() -> Snackbar.make(mRoot,
											getString(R.string.added_to_favorites, drinkName), Snackbar.LENGTH_LONG).show(), Timber::e)));

				snackbar.show();
			}
		}
	}

	private void showSnackBarRemoveFromHistory(ListItem item, int pos) {
		Snackbar snackbar = Snackbar
				.make(mRoot, getString(R.string.removed_from_history, item.getName()) , Snackbar.LENGTH_LONG)
				.setAction(R.string.undo, view -> {
					mPresenter.returnToHistory(item.getId(), item.getHistory());
				});
		snackbar.show();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_search, menu);

		if (fragmentType != TYPE_HISTORY) {
			MenuItem clearHistory = menu.findItem(R.id.action_clear_history);
			clearHistory.setVisible(false);
		}
		MenuItem filters = menu.findItem(R.id.action_filter);
		if (fragmentType != TYPE_NORMAL) {
			filters.setVisible(false);
		}

		searchMenu = menu.findItem(R.id.action_search);
		final SearchView searchView = (SearchView) searchMenu.getActionView();
		searchMenu.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				Timber.v("onMenuItemActionExpand");
				isSearchOpen = true;
				openSearchClicked = true;
				searchView.setQuery(prefs.getLastSearchString(), false);
				if (touchLayout.getVisibility() == View.VISIBLE) {
					showMenu();
				}
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				Timber.v("onMenuItemActionCollapse");
				isSearchOpen = false;
				return true;
			}
		});

		searchView.setOnCloseListener(() -> {
			Timber.v("onClose");
			return false;
		});
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				if (fragmentType == TYPE_NORMAL) {
//					TODO: should move this logic into presenter
					//Save search query string
//					if (TCApplication.isConnected()) {
					applySearch(query, false);

				}
				return false;
			}

			@Override
			public boolean onQueryTextChange(final String newText) {
				Timber.v("onQueryTextChange text = %s", newText);
				if ((fragmentType == TYPE_FAVORITES || fragmentType == TYPE_HISTORY) && mAdapter != null) {
					mAdapter.applyFilter(newText);
					extractIds(mAdapter.getData());
				} else if (fragmentType == TYPE_NORMAL && isSearchOpen) {
					if (openSearchClicked && newText.isEmpty()) {
						openSearchClicked = false;
						searchView.setQuery(prefs.getLastSearchString(), false);
					} else {
						applySearch(newText, true);
					}
				}
				return false;
			}
		});

		// Get the search close button image view
		ImageView closeButton = searchView.findViewById(R.id.search_close_btn);

		// Set on click listener
		closeButton.setOnClickListener(v -> {
			Timber.v("closeBtnClick");
			isSearchOpen = false;
			mPresenter.cancelSearch();
			//Clear query
//			searchView.setQuery("", false);
			//Collapse the action view
			searchView.onActionViewCollapsed();
			//Collapse the search widget
			searchMenu.collapseActionView();
		});
	}

	private void applySearch(String query, boolean localSearch) {
		Timber.v("startSearch local = " + localSearch);
		prefs.setLastSearchString(query);
		if (!query.isEmpty()) {
			prefs.saveCurrentSearchType(Prefs.SEARCH_TYPE_SEARCH);
		} else {
			prefs.saveCurrentSearchType(Prefs.SEARCH_TYPE_FILTER);
		}
		prefs.clearFilters();

		categorySpinner.setSelection(0);
		ingredientSpinner.setSelection(0);
		glassSpinner.setSelection(0);
		alcoholicSpinner.setSelection(0);

		if (localSearch) {
			mPresenter.startSearchLocal(query);
		} else {
			mPresenter.startSearch(query);
		}
		String lastSearch = prefs.getLastSearchString();
		if (lastSearch != null && !lastSearch.isEmpty()) {
			updateToolbarTitle(getString(R.string.search, prefs.getLastSearchString()));
		} else {
			updateToolbarTitle(getString(R.string.app_name));
		}
		if (prefs.isFirstRun()) {
			executeFirsRun();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_clear_history) {
			if (mAdapter.getItemCount() > 0) {
				UIUtil.showWarningDialog(
						getActivity(),
						R.drawable.round_delete_forever_black,
						R.string.do_you_really_want_clear_history,
						(dialogInterface, i) -> mPresenter.clearHistory(),
						(dialogInterface, i) -> dialogInterface.dismiss()
				);
			} else {
				Toast.makeText(getContext(), R.string.history_already_empty, Toast.LENGTH_LONG).show();
			}
		} else if (item.getItemId() == R.id.action_search) {
			Timber.v("ActionSearch click");

		} else if (item.getItemId() == android.R.id.home) {
			Timber.v("onHomeBtnClick");
		}
		return super.onOptionsItemSelected(item);
	}

	private void applyFilters() {
		if (prefs.isFirstRun()) {
			executeFirsRun();
		}
		loadData();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("fragment_type", fragmentType);
//		if (mAdapter != null) {
//			outState.putParcelable(EXTRAS_KEY_ADAPTER_DATA, mAdapter.onSaveInstanceState());
//		}
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		if (savedInstanceState != null) {
//				&& savedInstanceState.containsKey(EXTRAS_KEY_ADAPTER_DATA)) {
			fragmentType = savedInstanceState.getInt("fragment_type");
//			initAdapter();
//			mAdapter.onRestoreInstanceState(savedInstanceState.getParcelable(EXTRAS_KEY_ADAPTER_DATA));
		}
	}

	@Override
	public void showProgress() {
		mRefreshLayout.setRefreshing(true);
	}

	@Override
	public void hideProgress() {
		mRefreshLayout.setRefreshing(false);
	}

	@Override
	public void showQueryError() {
		mTxtEmpty.setVisibility(View.VISIBLE);
		Snackbar.make(mRecyclerView, R.string.msg_error_on_query, Snackbar.LENGTH_LONG).show();
	}

	@Override
	public void showNetworkError() {
		Snackbar.make(mRecyclerView, R.string.msg_error_no_internet, Snackbar.LENGTH_LONG).show();
	}

	@Override
	public void displayData(List<ListItem> data) {
		extractIds(data);
		//Handle btnUp show logic.
		if (data.size() > ITEM_COUNT_WITHOUT_BTN_UP) {
			btnUp.setVisibility(View.VISIBLE);
			mAdapter.showFooter(true);
			dividerItemDecoration.showDividerForLastItem(false);
		} else {
			btnUp.setVisibility(View.GONE);
			mAdapter.showFooter(false);
			dividerItemDecoration.showDividerForLastItem(true);
		}
		if (prefs.isFirstRun() && fragmentType == TYPE_NORMAL) {
			mRecyclerView.setVisibility(View.GONE);
			mWelcomePanel.setVisibility(View.VISIBLE);
			mTxtEmpty.setVisibility(View.GONE);
			mAdapter.setData(data);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && getContext() != null){
				parentActivity.getWindow().setNavigationBarColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
			}
		} else if (data.size() == 0) {
			mRecyclerView.setVisibility(View.GONE);
			mWelcomePanel.setVisibility(View.GONE);
			mTxtEmpty.setVisibility(View.VISIBLE);
			if (fragmentType == TYPE_FAVORITES) {
				mTxtEmpty.setText(R.string.no_favorite_drinks);
			} else if (fragmentType == TYPE_HISTORY) {
				mTxtEmpty.setText(R.string.history_is_empty);
			} else {
				mTxtEmpty.setText(R.string.empty);
			}
		} else {
			if (mRecyclerView.getVisibility() != View.VISIBLE) {
				mRecyclerView.setVisibility(View.VISIBLE);
			}
			if (mTxtEmpty.getVisibility() == View.VISIBLE) {
				mTxtEmpty.setVisibility(View.GONE);
			}
			mAdapter.setData(data);
		}
	}

	public void updateToolbarTitle(String title) {
		if (parentActivity.getSupportActionBar() != null) {
			parentActivity.getSupportActionBar().setTitle(title);
		}
	}

	private void extractIds(List<ListItem> data) {
		if (ids == null) {
			ids = new ArrayList<>();
		} else {
			ids.clear();
		}
		for (int i = 0; i < data.size(); i++) {
			//TODO: Cast long to int, potential errors here.
			ids.add((int) data.get(i).getId());
		}
	}

	public void setOnFirstRunExecutedListener(OnFirstRunExecutedListener onFirstRunExecutedListener) {
		this.onFirstRunExecutedListener = onFirstRunExecutedListener;
	}

	public void executeFirsRun() {
		prefs.firstRunExecuted();
//		mPresenter.firstRunInitialization(getContext());
		if (onFirstRunExecutedListener != null) {
			onFirstRunExecutedListener.onFirstRunExecuted();
		}
		mWelcomePanel.setVisibility(View.GONE);
		mRecyclerView.setVisibility(View.VISIBLE);
		mTxtEmpty.setVisibility(View.GONE);
		AndroidUtils.handleNavigationBarColor(getActivity());
	}

	private void toolbarMenuItemAnimation(final Toolbar toolbar) {
		toolbar.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom,
												int oldLeft, int oldTop, int oldRight, int oldBottom) {
					if (fragmentType == TYPE_NORMAL) {
						filterMenu = toolbar.findViewById(R.id.action_filter);
						if (filterMenu != null) {
							toolbar.removeOnLayoutChangeListener(this);
							filterMenu.setOnClickListener(v1 -> {
								if (!prefs.isFirstRun()) {
									if (touchLayout.getVisibility() == View.VISIBLE) {
										AnimationUtil.viewBackRotationAnimation(v1, ANIMATION_DURATION);
									} else {
										AnimationUtil.viewRotationAnimation(v1, ANIMATION_DURATION);
									}
									showMenu();
								}
							});
						}
					} else if (fragmentType == TYPE_HISTORY) {
						filterMenu = toolbar.findViewById(R.id.action_clear_history);
						if (filterMenu != null) {
							filterMenu.setOnClickListener(v1 -> {
								if (mAdapter.getItemCount() > 0) {
									UIUtil.showWarningDialog(
											getActivity(),
											R.drawable.round_delete_forever_black,
											R.string.do_you_really_want_clear_history,
											(dialogInterface, i) -> mPresenter.clearHistory(),
											(dialogInterface, i) -> dialogInterface.dismiss()
									);
								} else {
									Toast.makeText(getContext(), R.string.history_already_empty, Toast.LENGTH_LONG).show();
								}
							});
						}
					}
				}
		});
	}

	private void showMenu() {
		if (touchLayout.getVisibility() == View.VISIBLE) {
			selectedFilter = -1;
//			touchLayout.setElevation(getResources().getDimension(R.dimen.under_toolbar_elevation));
			AnimationUtil.verticalSpringAnimation(
					touchLayout,
					-touchLayout.getHeight(),
					(animation, canceled, value, velocity) -> touchLayout.setVisibility(View.INVISIBLE)
			);
		} else {
			SearchView searchView = ((SearchView)searchMenu.getActionView());
			if (!searchView.isIconified()) {
				//Collapse the action view
//				searchView.onActionViewCollapsed();
				//Collapse the search widget
				searchMenu.collapseActionView();
//				applySearch(searchView.getQuery().toString(), true);
			}
			touchLayout.setVisibility(View.VISIBLE);
			if (touchLayout.getHeight() == 0) {
				touchLayout.setTranslationY(-AndroidUtils.dpToPx(800));
			} else {
				touchLayout.setTranslationY(-touchLayout.getHeight());
			}

			AnimationUtil.verticalSpringAnimation(touchLayout, 0);
//					(animation, canceled, value, velocity) ->
//							touchLayout.setElevation(getResources().getDimension(R.dimen.toolbar_elevation)));
		}
		touchLayout.setReturnPositionY(0);
	}

	private void handleBtnUpScroll(int dy) {
		if (btnUp != null && ((dy < 0 && btnUp.getTranslationY() < defaultBtnUpY)
				|| (dy > 0 && btnUp.getTranslationY() > 0))) {

			float inset = btnUp.getTranslationY() - dy;
			if (inset < 0) { inset = 0; }
			if (inset > defaultBtnUpY) { inset = defaultBtnUpY; }
			btnUp.setTranslationY(inset);
		}
	}

	private void handleFiltersPanelScroll(int dy) {
		if (touchLayout.getVisibility() == View.VISIBLE && !onPanelTouch) {
			float inset = touchLayout.getTranslationY() - dy;
			touchLayout.setReturnPositionY(inset);
			if (touchLayout.getTranslationY() <= -touchLayout.getHeight()) {
				touchLayout.setVisibility(View.GONE);
				AnimationUtil.viewBackRotationAnimation(filterMenu, ANIMATION_DURATION);
			}
			if (touchLayout.getTranslationY() <= 0 && inset > 0) {
				touchLayout.setTranslationY(0);
			} else {
				touchLayout.setTranslationY(inset);
			}
		}
	}

	/**
	 * Layout to ensure Predictive animation is disabled to prevent app crash when it is ON.
	 */
	public class AppLinearLayoutManager extends LinearLayoutManager {
		AppLinearLayoutManager(Context context) {
			super(context);
		}

		@Override
		public boolean supportsPredictiveItemAnimations() {
			return false;
		}
	}

	public interface OnFirstRunExecutedListener {
		void onFirstRunExecuted();
	}
}
