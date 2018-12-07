/*
 * Copyright 2018 Dmitriy Ponomarenko
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

package com.dimowner.tastycocktails.rating;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dimowner.tastycocktails.AdvHandler;
import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.analytics.MixPanel;
import com.dimowner.tastycocktails.cocktails.details.PagerDetailsActivity;
import com.dimowner.tastycocktails.cocktails.list.ListItem;
import com.dimowner.tastycocktails.dagger.rating.RatingModule;
import com.dimowner.tastycocktails.data.Prefs;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;

public class RatingFragment extends Fragment implements RatingContract.View {

	@Inject
	RatingContract.UserActionsListener mPresenter;

	@Inject Prefs prefs;

	private RecyclerView mRecyclerView;
	private RatingListAdapter mAdapter;

	private TextView mTxtEmpty;
	private ProgressBar progressBar;
	private AdvHandler advHandler;

	private ArrayList<Integer> ids;



	public static RatingFragment newInstance() {
		return new RatingFragment();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_rating, container, false);
		mRecyclerView = view.findViewById(R.id.recycler_view);
		mTxtEmpty = view.findViewById(R.id.txt_empty);
		progressBar = view.findViewById(R.id.progress);
		TCApplication.get(getContext()).applicationComponent()
				.plus(new RatingModule(this)).injectRatingFragment(this);
		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
		if (actionBar != null) {
			actionBar.setTitle(R.string.title_rating);
		}

		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		initAdapter();

		mPresenter.bindView(this);
		mPresenter.loadRating();

		AdView adView = view.findViewById(R.id.adView);
		advHandler = new AdvHandler(adView, prefs);

		TCApplication.event(getActivity().getApplicationContext(), MixPanel.EVENT_RATING);
	}

	private void initAdapter() {
		if (mAdapter == null) {
			mAdapter = new RatingListAdapter();
			mRecyclerView.setAdapter(mAdapter);
		}
		mAdapter.setItemClickListener((view1, position) -> {
			if (ids != null && ids.size() > 0) {
				startActivity(PagerDetailsActivity.getStartIntent(getContext(), ids, position));
			} else {
				Timber.e("Can't open preview! ids is NULL or empty");
			}
		});
	}

	@Override
	public void displayData(List<ListItem> data) {
		extractIds(data);
		mAdapter.setData(data);
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

	@Override
	public void showProgress() {
		mTxtEmpty.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.VISIBLE);
	}

	@Override
	public void hideProgress() {
		mTxtEmpty.setVisibility(View.GONE);
		progressBar.setVisibility(View.INVISIBLE);
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
}
