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
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Animatable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.cocktails.details.PagerDetailsActivity;
import com.dimowner.tastycocktails.cocktails.list.CocktailsRecyclerAdapter;
import com.dimowner.tastycocktails.cocktails.list.EndlessRecyclerViewScrollListener;
import com.dimowner.tastycocktails.cocktails.list.ListItem;
import com.dimowner.tastycocktails.dagger.cocktails.CocktailsModule;
import com.dimowner.tastycocktails.data.Prefs;
import com.dimowner.tastycocktails.util.AndroidUtils;
import com.dimowner.tastycocktails.util.AnimationUtil;
import com.dimowner.tastycocktails.util.UIUtil;

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
	public static final String EXTRAS_KEY_TYPE = "search_fragment_type";

	public static final int ADD_TO_FAVORITES_ANIMATION_DURATION = 400;

//	private final String EXTRAS_KEY_ADAPTER_DATA = "adapter_data";

	private RecyclerView mRecyclerView;
	private ProgressBar mProgressBar;
	private ScrollView mWelcomePanel;
	private TextView mTxtEmpty;
	private FrameLayout mRoot;
	private LinearLayout filtersPanel;
	private View filterMenu;
	private SwipeRefreshLayout mRefreshLayout;

	private CocktailsRecyclerAdapter mAdapter;

	private MenuItem searchMenu;

	private int fragmentType = TYPE_UNKNOWN;

	boolean isChangedFilter = false;

	private OnFirstRunExecutedListener onFirstRunExecutedListener;

	@Inject
	CocktailsListContract.UserActionsListener mPresenter;

	private ArrayList<Integer> ids;

	@Inject
	Prefs prefs;

	private int selectedFilter = -1;

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

		TCApplication.get(getContext()).applicationComponent()
				.plus(new CocktailsModule(this)).injectCocktailsSearch(this);

		if (getArguments() != null && getArguments().containsKey(EXTRAS_KEY_TYPE)) {
			fragmentType = getArguments().getInt(EXTRAS_KEY_TYPE);
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

		mRoot = view.findViewById(R.id.coordinator_root);
		mWelcomePanel = view.findViewById(R.id.welcome_panel);
		mTxtEmpty = view.findViewById(R.id.txt_empty);
		mProgressBar = view.findViewById(R.id.progress);
		mRecyclerView = view.findViewById(R.id.recycler_view);
		mRecyclerView.setHasFixedSize(true);

		mRefreshLayout = view.findViewById(R.id.swiperefresh);
		mRefreshLayout.setOnRefreshListener(() -> {
			mRefreshLayout.canChildScrollUp();
			loadData();
		});

		initFiltersPanel(view);

		//Hide show filters panel on scroll list
		mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				if (filtersPanel.getVisibility() == View.VISIBLE) {
					float inset = filtersPanel.getTranslationY() - dy;
					if (filtersPanel.getTranslationY() <= -filtersPanel.getHeight()) {
						filtersPanel.setVisibility(View.GONE);
						AnimationUtil.viewBackRotationAnimation(filterMenu, ANIMATION_DURATION);
					}
					if (filtersPanel.getTranslationY() <= 0 && inset > 0) {
						filtersPanel.setTranslationY(0);
					} else {
						filtersPanel.setTranslationY(inset);
					}
				}
			}
		});

		filtersPanel = view.findViewById(R.id.filters_panel);
		filtersPanel.setOnClickListener(v -> {});
		Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
		toolbarMenuItemAnimation(toolbar);

		// use a linear layout manager
		RecyclerView.LayoutManager mLayoutManager = new AppLinearLayoutManager(getContext());
		mRecyclerView.setLayoutManager(mLayoutManager);
		mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), ((AppLinearLayoutManager) mLayoutManager).getOrientation()));
		mRecyclerView.addOnScrollListener(new MyScrollListener(mLayoutManager));

		mPresenter.bindView(this);

		if (prefs.isFirstRun() && fragmentType == TYPE_NORMAL) {
			mWelcomePanel.setVisibility(View.VISIBLE);
			mTxtEmpty.setVisibility(View.GONE);
			mRecyclerView.setVisibility(View.GONE);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
				getActivity().getWindow().setNavigationBarColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
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
							if (viewHolder != null) {
								final View foregroundView = ((CocktailsRecyclerAdapter.ItemViewHolder)viewHolder).getContainer();
								getDefaultUIUtil().onSelected(foregroundView);
							}
						}

						@Override
						public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
															 RecyclerView.ViewHolder viewHolder, float dX, float dY,
															 int actionState, boolean isCurrentlyActive) {
							final View foregroundView = ((CocktailsRecyclerAdapter.ItemViewHolder)viewHolder).getContainer();
							getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
									actionState, isCurrentlyActive);
						}

						@Override
						public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
							final View foregroundView = ((CocktailsRecyclerAdapter.ItemViewHolder)viewHolder).getContainer();
							getDefaultUIUtil().clearView(foregroundView);
						}

						@Override
						public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
							int pos = viewHolder.getAdapterPosition();
							ListItem item = mAdapter.getItem(pos);
							mPresenter.removeFromHistory(item.getId());
							showSnackBarRemoveFromHistory(item, pos);
							mAdapter.removeItem(pos);
						}

						@Override
						public void onChildDraw(Canvas c, RecyclerView recyclerView,
														RecyclerView.ViewHolder viewHolder, float dX, float dY,
														int actionState, boolean isCurrentlyActive) {
							getDefaultUIUtil().onDraw(c, recyclerView, ((CocktailsRecyclerAdapter.ItemViewHolder)viewHolder).getContainer(), dX, dY, actionState, isCurrentlyActive);
						}
					};
			new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
		}

//		if (savedInstanceState == null) {
			initAdapter();

			if (prefs.isFirstRun()) {
				prefs.saveCurrentActiveFilter(Prefs.FILTER_TYPE_CATEGORY);
				prefs.saveSelectedFilterValuePos(0);
				String[] categories = getResources().getStringArray(R.array.filter_categories);
				prefs.saveSelectedFilterValue(categories[0]);
				Button btnGetStarted = view.findViewById(R.id.get_started);
				btnGetStarted.setOnClickListener(view1 -> {
					prefs.firstRunExecuted();
					if (onFirstRunExecutedListener != null) {
						onFirstRunExecutedListener.onFirstRunExecuted();
					}
					mWelcomePanel.setVisibility(View.GONE);
					mRecyclerView.setVisibility(View.VISIBLE);
					mTxtEmpty.setVisibility(View.GONE);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
						getActivity().getWindow().setNavigationBarColor(ContextCompat.getColor(getContext(), R.color.white));
					}
				});
				String vals[] = getResources().getStringArray(R.array.filter_categories);
				prefs.setFirstRunDefaultValues(Prefs.FILTER_TYPE_CATEGORY, 1, vals[1]);
				mPresenter.loadBuildList(prefs.getCurrentActiveFilter(), prefs.getSelectedFilterValue());
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
					getActivity().getWindow().setNavigationBarColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
				}
			} else {
				loadData();
			}
//		}
	}

	private void loadData() {
		if (fragmentType == TYPE_NORMAL) {
			int filter = prefs.getCurrentActiveFilter();
			if (filter == Prefs.FILTER_TYPE_SEARCH) {
				mPresenter.loadLastSearch(prefs.getLastSearchString());
				if (prefs.getLastSearchString() != null && getActivity() != null && ((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
					((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.search, prefs.getLastSearchString()));
				}
			} else {
				mPresenter.loadBuildList(prefs.getCurrentActiveFilter(), prefs.getSelectedFilterValue());
				if (getActivity() != null && ((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
					((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
				}
			}
		} else if (fragmentType == TYPE_FAVORITES) {
			mPresenter.loadFavorites();
		} else if (fragmentType == TYPE_HISTORY) {
			mPresenter.loadHistory(1);
		} else {
			Timber.e("Con't load data not correct fragment type!");
		}
	}

	private void initFiltersPanel(View view) {
		//Init CATEGORY filter
		Spinner categorySpinner = view.findViewById(R.id.filter_categories);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(getContext(),
				R.array.filter_categories, R.layout.spinner_item);
		// Specify the layout to use when the list of choices appears
		categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		// Apply the adapter to the spinner
		categorySpinner.setAdapter(categoryAdapter);

		//Init INGREDIENTS filter
		Spinner ingredientSpinner = view.findViewById(R.id.filter_ingredients);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> ingredientAdapter = ArrayAdapter.createFromResource(getContext(),
				R.array.filter_ingredients_alphabetical, R.layout.spinner_item);

		// Specify the layout to use when the list of choices appears
		ingredientAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		// Apply the adapter to the spinner
		ingredientSpinner.setAdapter(ingredientAdapter);

		//Init GLASS filter
		Spinner glassSpinner = view.findViewById(R.id.filter_glass);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> glassAdapter = ArrayAdapter.createFromResource(getContext(),
				R.array.filter_glass, R.layout.spinner_item);
		// Specify the layout to use when the list of choices appears
		glassAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		// Apply the adapter to the spinner
		glassSpinner.setAdapter(glassAdapter);

		//Init ALCOHOLIC filter
		Spinner alcoholicSpinner = view.findViewById(R.id.filter_alcoholic);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> alcoholicAdapter = ArrayAdapter.createFromResource(getContext(),
				R.array.filter_alcoholic, R.layout.spinner_item);
		// Specify the layout to use when the list of choices appears
		alcoholicAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		// Apply the adapter to the spinner
		alcoholicSpinner.setAdapter(alcoholicAdapter);

		prefs = new Prefs(getContext());
		int activeFilter = prefs.getCurrentActiveFilter();
		if (activeFilter == Prefs.FILTER_TYPE_CATEGORY) {
			categorySpinner.setSelection(prefs.getSelectedFilterValuePos());
		} else if (activeFilter == Prefs.FILTER_TYPE_INGREDIENT) {
			ingredientSpinner.setSelection(prefs.getSelectedFilterValuePos());
		} else if (activeFilter == Prefs.FILTER_TYPE_GLASS) {
			glassSpinner.setSelection(prefs.getSelectedFilterValuePos());
		} else if (activeFilter == Prefs.FILTER_TYPE_ALCOHOLIC_NON_ALCOHOLIC) {
			alcoholicSpinner.setSelection(prefs.getSelectedFilterValuePos());
		}

		int prevFilter = prefs.getCurrentActiveFilter();
		int prevPos = prefs.getSelectedFilterValuePos();
		String prevVal = prefs.getSelectedFilterValue();

		categorySpinner.setOnTouchListener((view14, motionEvent) -> {
			selectedFilter = Prefs.FILTER_TYPE_CATEGORY;
			view14.performClick();
			return false;
		});

		ingredientSpinner.setOnTouchListener((view13, motionEvent) -> {
			selectedFilter = Prefs.FILTER_TYPE_INGREDIENT;
			view13.performClick();
			return false;
		});

		glassSpinner.setOnTouchListener((view12, motionEvent) -> {
			selectedFilter = Prefs.FILTER_TYPE_GLASS;
			view12.performClick();
			return false;
		});

		alcoholicSpinner.setOnTouchListener((view1, motionEvent) -> {
			selectedFilter = Prefs.FILTER_TYPE_ALCOHOLIC_NON_ALCOHOLIC;
			view1.performClick();
			return false;
		});

		categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
				if (selectedFilter == Prefs.FILTER_TYPE_CATEGORY) {
					if (pos == 0) {
						prefs.saveCurrentActiveFilter(Prefs.FILTER_TYPE_SEARCH);
					} else {
						prefs.saveCurrentActiveFilter(Prefs.FILTER_TYPE_CATEGORY);
						prefs.saveSelectedFilterValuePos(pos);
						prefs.saveSelectedFilterValue(categoryAdapter.getItem(pos).toString());
					}
					ingredientSpinner.setSelection(0);
					alcoholicSpinner.setSelection(0);
					glassSpinner.setSelection(0);
					isChangedFilter = true;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});


		ingredientSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
				if (selectedFilter == Prefs.FILTER_TYPE_INGREDIENT) {
					if (pos == 0) {
						prefs.saveCurrentActiveFilter(Prefs.FILTER_TYPE_SEARCH);
					} else {
						prefs.saveCurrentActiveFilter(Prefs.FILTER_TYPE_INGREDIENT);
						prefs.saveSelectedFilterValuePos(pos);
						prefs.saveSelectedFilterValue(ingredientAdapter.getItem(pos).toString());
						categorySpinner.setSelection(0);
						alcoholicSpinner.setSelection(0);
						glassSpinner.setSelection(0);
					}
					isChangedFilter = true;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});


		glassSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
				if (selectedFilter == Prefs.FILTER_TYPE_GLASS) {
					if (pos == 0) {
						prefs.saveCurrentActiveFilter(Prefs.FILTER_TYPE_SEARCH);
					} else {
						prefs.saveCurrentActiveFilter(Prefs.FILTER_TYPE_GLASS);
						prefs.saveSelectedFilterValuePos(pos);
						prefs.saveSelectedFilterValue(glassAdapter.getItem(pos).toString());
						categorySpinner.setSelection(0);
						alcoholicSpinner.setSelection(0);
						ingredientSpinner.setSelection(0);
					}
					isChangedFilter = true;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});


		alcoholicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
				if (selectedFilter == Prefs.FILTER_TYPE_ALCOHOLIC_NON_ALCOHOLIC) {
					if (pos == 0) {
						prefs.saveCurrentActiveFilter(Prefs.FILTER_TYPE_SEARCH);
					} else {
						prefs.saveCurrentActiveFilter(Prefs.FILTER_TYPE_ALCOHOLIC_NON_ALCOHOLIC);
						prefs.saveSelectedFilterValuePos(pos);
						prefs.saveSelectedFilterValue(alcoholicAdapter.getItem(pos).toString());
						categorySpinner.setSelection(0);
						ingredientSpinner.setSelection(0);
						glassSpinner.setSelection(0);
					}
					isChangedFilter = true;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});

		Button btnOk = view.findViewById(R.id.btn_ok);
		Button btnCancel = view.findViewById(R.id.btn_cancel);
		btnOk.setOnClickListener(v -> {
			if (isChangedFilter) {
				applyFilters();
				isChangedFilter = false;
			}

			if (filterMenu != null) {
				if (filtersPanel.getVisibility() == View.VISIBLE) {
					AnimationUtil.viewBackRotationAnimation(filterMenu, ANIMATION_DURATION);
				} else {
					AnimationUtil.viewRotationAnimation(filterMenu, ANIMATION_DURATION);
				}
			}
			showMenu();
		});
		btnCancel.setOnClickListener(v -> {
			if (filterMenu != null) {
				if (filtersPanel.getVisibility() == View.VISIBLE) {
					AnimationUtil.viewBackRotationAnimation(filterMenu, ANIMATION_DURATION);
				} else {
					AnimationUtil.viewRotationAnimation(filterMenu, ANIMATION_DURATION);
				}
			}
			prefs.saveCurrentActiveFilter(prevFilter);
			prefs.saveSelectedFilterValuePos(prevPos);
			prefs.saveSelectedFilterValue(prevVal);
			showMenu();
		});
	}

	private void initAdapter() {
		if (mAdapter == null) {
			if (fragmentType == TYPE_HISTORY) {
				mAdapter = new CocktailsRecyclerAdapter(fragmentType, R.layout.list_item_history, prefs);
			} else {
				mAdapter = new CocktailsRecyclerAdapter(fragmentType, R.layout.list_item2, prefs);
			}
			mRecyclerView.setAdapter(mAdapter);
		}
		mAdapter.setItemClickListener((view1, position) ->
				startActivity(PagerDetailsActivity.getStartIntent(getContext(), ids, position)));
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
				mPresenter.reverseFavorite(id)
						.subscribeOn(Schedulers.io())
						.delay(ADD_TO_FAVORITES_ANIMATION_DURATION, TimeUnit.MILLISECONDS)
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(() -> {
									animatable.stop();
									showSnackBar(id, !fev, name);
								},
								throwable -> {
									animatable.stop();
									Timber.e(throwable);
								});
			} else {
				//Add or remove from favorites without animation
				mPresenter.reverseFavorite(id)
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(() -> showSnackBar(id, !fev, name), Timber::e);
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

	private void startDetailsActivity(ListItem item, View view1) {
		Intent intent = new Intent(getContext(), PagerDetailsActivity.class);
		intent.putExtra(PagerDetailsActivity.EXTRAS_KEY_ID, item.getId());

		//Transition
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
		startActivity(intent);
	}

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
								mPresenter.reverseFavorite(id)
									.subscribeOn(Schedulers.io())
									.delay(ADD_TO_FAVORITES_ANIMATION_DURATION, TimeUnit.MILLISECONDS)
									.observeOn(AndroidSchedulers.mainThread())
									.subscribe(() -> Snackbar.make(mRoot,
											getString(R.string.added_to_favorites, drinkName), Snackbar.LENGTH_LONG).show(), Timber::e));

				snackbar.show();
			}
		}
	}

	private void showSnackBarRemoveFromHistory(ListItem item, int pos) {
		Snackbar snackbar = Snackbar
				.make(mRoot, getString(R.string.removed_from_history, item.getName()) , Snackbar.LENGTH_LONG)
				.setAction(R.string.undo, view -> {
					mPresenter.returnToHistory(item.getId(), item.getHistory());
					mAdapter.addItem(item, pos);
				});
		snackbar.show();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mPresenter.unbindView();
		mPresenter = null;
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
		final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				if (fragmentType == TYPE_NORMAL) {
//					TODO: should move this logic into presenter
					//Save search query string
					if (TCApplication.isConnected()) {
						prefs.setLastSearchString(query);
						prefs.saveCurrentActiveFilter(Prefs.FILTER_TYPE_SEARCH);

						mPresenter.startSearch(query);
						if (prefs.getLastSearchString() != null && getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
							((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.search, prefs.getLastSearchString()));
						}
						if (prefs.isFirstRun()) {
							prefs.firstRunExecuted();
							if (onFirstRunExecutedListener != null) {
								onFirstRunExecutedListener.onFirstRunExecuted();
							}
							mWelcomePanel.setVisibility(View.GONE);
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
								getActivity().getWindow().setNavigationBarColor(ContextCompat.getColor(getContext(), R.color.white));
							}
						}
					} else {
						showNetworkError();
					}
				}
				return false;
			}

			@Override
			public boolean onQueryTextChange(final String newText) {
				if ((fragmentType == TYPE_FAVORITES || fragmentType == TYPE_HISTORY) && mAdapter != null) {
					mAdapter.applyFilter(newText);
				}
				return false;
			}
		});

		// Get the search close button image view
		ImageView closeButton = searchView.findViewById(R.id.search_close_btn);

		// Set on click listener
		closeButton.setOnClickListener(v -> {
			mPresenter.cancelSearch();
			//Clear query
			searchView.setQuery("", false);
			//Collapse the action view
			searchView.onActionViewCollapsed();
			//Collapse the search widget
			searchMenu.collapseActionView();
		});
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
		} else if (item.getItemId() == R.id.action_filter) {
			showFilterDialog();
			if (searchMenu.isActionViewExpanded()) {
				searchMenu.collapseActionView();
			}
		}
		return super.onOptionsItemSelected(item);
	}

	private void showFilterDialog() {
		if (getActivity() != null) {
			FragmentManager fm = getActivity().getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			Fragment prev = fm.findFragmentByTag("dialog_filters");
			if (prev != null) {
				ft.remove(prev);
			}
			ft.addToBackStack(null);
			FiltersDialog dialog = new FiltersDialog();
			dialog.setPositiveButtonListener((dialogInterface, i) -> {
				applyFilters();
			});
			dialog.show(ft, "dialog_filters");
		}
	}

	private void applyFilters() {
		if (prefs.isFirstRun()) {
			prefs.firstRunExecuted();
			if (onFirstRunExecutedListener != null) {
				onFirstRunExecutedListener.onFirstRunExecuted();
			}
			mWelcomePanel.setVisibility(View.GONE);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
				getActivity().getWindow().setNavigationBarColor(ContextCompat.getColor(getContext(), R.color.white));
			}
		}
		if (prefs.getCurrentActiveFilter() == Prefs.FILTER_TYPE_SEARCH) {
			mPresenter.loadLastSearch(prefs.getLastSearchString());
			if (prefs.getLastSearchString() != null && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
				((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.search, prefs.getLastSearchString()));
			}
		} else {
			mPresenter.loadBuildList(prefs.getCurrentActiveFilter(), prefs.getSelectedFilterValue());
			if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
				((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
			}
		}
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
		mProgressBar.setVisibility(View.VISIBLE);
		mRecyclerView.setVisibility(View.GONE);
	}

	@Override
	public void hideProgress() {
		mProgressBar.setVisibility(View.GONE);
		mRecyclerView.setVisibility(View.VISIBLE);
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
		if (prefs.isFirstRun() && fragmentType == TYPE_NORMAL) {
			mRecyclerView.setVisibility(View.GONE);
			mWelcomePanel.setVisibility(View.VISIBLE);
			mTxtEmpty.setVisibility(View.GONE);
			mAdapter.setData(data);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
				getActivity().getWindow().setNavigationBarColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
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
				mTxtEmpty.setText(R.string.click_search_to_find_some_drink);
			}
		} else {
			mRecyclerView.setVisibility(View.VISIBLE);
			mTxtEmpty.setVisibility(View.GONE);
			mAdapter.setData(data);
//			mRecyclerView.post(() -> mAdapter.addItems(data));
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
		Timber.v("ids = " + ids.toString());
	}

	public void setOnFirstRunExecutedListener(OnFirstRunExecutedListener onFirstRunExecutedListener) {
		this.onFirstRunExecutedListener = onFirstRunExecutedListener;
	}

	private void toolbarMenuItemAnimation(final Toolbar toolbar) {
		toolbar.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom,
												int oldLeft, int oldTop, int oldRight, int oldBottom) {
				filterMenu = toolbar.findViewById(R.id.action_filter);
				if (filterMenu != null) {
					toolbar.removeOnLayoutChangeListener(this);
					filterMenu.setOnClickListener(v1 -> {
						if (filtersPanel.getVisibility() == View.VISIBLE) {
							AnimationUtil.viewBackRotationAnimation(v1, ANIMATION_DURATION);
						} else {
							AnimationUtil.viewRotationAnimation(v1, ANIMATION_DURATION);
						}
						showMenu();
					});
				}
			}
		});
	}

	private void showMenu() {
		if (filtersPanel.getVisibility() == View.VISIBLE) {
//			filtersPanel.setElevation(getResources().getDimension(R.dimen.under_toolbar_elevation));
			AnimationUtil.verticalSpringAnimation(
					filtersPanel,
					-filtersPanel.getHeight(),
					(animation, canceled, value, velocity) -> filtersPanel.setVisibility(View.GONE)
			);
		} else {
			filtersPanel.setVisibility(View.VISIBLE);
			if (filtersPanel.getHeight() == 0) {
//				TODO: fix this 1000 px
				filtersPanel.setTranslationY(-1000);
			} else {
				filtersPanel.setTranslationY(-filtersPanel.getHeight());
			}

			AnimationUtil.verticalSpringAnimation(filtersPanel, 0);
//					(animation, canceled, value, velocity) -> filtersPanel.setElevation(getResources().getDimension(R.dimen.toolbar_elevation)));
		}
	}

	public class MyScrollListener extends EndlessRecyclerViewScrollListener {

		<L extends RecyclerView.LayoutManager> MyScrollListener(L layoutManager) {
			super(layoutManager);
		}

		@Override
		public void onLoadMore(int page, int totalItemsCount) {
			Timber.d("onLoadMore page = " + page + " count = " + totalItemsCount);
//			if (fragmentType == TYPE_HISTORY) {
//				mPresenter.loadHistory(page);
//			}
		}
	}

	/**
	 * Simple extension of LinearLayoutManager for the sole purpose of showing what happens
	 * when predictive animations (which are enabled by default in LinearLayoutManager) are
	 * not enabled. This behavior is toggled via a checkbox in the UI.
	 */
	public class AppLinearLayoutManager extends LinearLayoutManager {
		AppLinearLayoutManager(Context context) {
			super(context);
		}

		@Override
		public boolean supportsPredictiveItemAnimations() {
			return true;
		}
	}

	public interface OnFirstRunExecutedListener {
		void onFirstRunExecuted();
	}
}
