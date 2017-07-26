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


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import java.util.List;

import task.softermii.tastycocktails.R;

/**
 * Created on 26.07.2017.
 * @author Dimowner
 */
public class CocktailsSearchFragment extends Fragment {

	private final String EXTRAS_KEY_ADAPTER_DATA = "adapter_data";

//	private ConstraintLayout constraintLayout;

	private RecyclerView mRecyclerView;

	private CocktailsRecyclerAdapter mAdapter;


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_search_cocktails, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
		mRecyclerView.setHasFixedSize(true);

		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		DividerItemDecoration divider = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
		divider.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.recycler_divider));
		mRecyclerView.addItemDecoration(divider);

		mAdapter = new CocktailsRecyclerAdapter();
		mAdapter.setItemClickListener((view1, position) -> {

			//TODO: fix test data
			ListItem item = mAdapter.getItem(position);
			Intent intent = new Intent(getContext(), CocktailDetailsActivity.class);
			intent.putExtra(CocktailDetailsActivity.EXTRAS_KEY_COCKTAIL_ID, item.getId());
			intent.putExtra(CocktailDetailsActivity.EXTRAS_KEY_NAME, item.getName());
			intent.putExtra(CocktailDetailsActivity.EXTRAS_KEY_DESCRIPTION, item.getDescription());
			intent.putExtra(CocktailDetailsActivity.EXTRAS_KEY_IMAGE_URL, item.getAvatar_url());
			startActivity(intent);
		});
		List<ListItem> data = new ArrayList<>();
		data.add(new ListItem(0, "name0", "description0", "http://www.thecocktaildb.com/images/media/drink/tqpvqp1472668328.jpg"));
		data.add(new ListItem(0, "name1", "description1", "http:\\/\\/www.thecocktaildb.com\\/images\\/media\\/drink\\/tqpvqp1472668328.jpg"));
		data.add(new ListItem(0, "name2", "description2", "https://github.com/Dimowner/WorkoutLogger/blob/master/app/src/main/res/drawable/default_cover.png"));
		data.add(new ListItem(0, "name3", "description3", "https://github.com/Dimowner/WorkoutLogger/blob/master/app/src/main/res/drawable/default_cover.png"));
		data.add(new ListItem(0, "name4", "description4", "https://github.com/Dimowner/WorkoutLogger/blob/master/app/src/main/res/drawable/default_cover.png"));
		data.add(new ListItem(0, "name5", "description5", "https://github.com/Dimowner/WorkoutLogger/blob/master/app/src/main/res/drawable/default_cover.png"));
		data.add(new ListItem(0, "name6", "description6", "https://github.com/Dimowner/WorkoutLogger/blob/master/app/src/main/res/drawable/default_cover.png"));
		mAdapter.setData(data);

		mRecyclerView.setAdapter(mAdapter);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mAdapter != null) {
			outState.putParcelable(EXTRAS_KEY_ADAPTER_DATA, mAdapter.onSaveInstanceState());
		}
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		if (savedInstanceState != null && savedInstanceState.containsKey(EXTRAS_KEY_ADAPTER_DATA)) {
			if (mAdapter == null) {
				mAdapter = new CocktailsRecyclerAdapter();
				mRecyclerView.setAdapter(mAdapter);
			}
			mAdapter.onRestoreInstanceState(savedInstanceState.getParcelable(EXTRAS_KEY_ADAPTER_DATA));
		}
	}
}
