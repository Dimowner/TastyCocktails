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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import task.softermii.tastycocktails.R;
import task.softermii.tastycocktails.TCApplication;
import task.softermii.tastycocktails.cocktails.details.IngredientsAdapter;
import task.softermii.tastycocktails.dagger.random.RandomCocktailModule;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
public class RandomFragment extends Fragment {

	public static final String EXTRAS_KEY_ID = "cocktail_id";

	@Inject
	RandomContract.UserActionsListener mPresenter;

	private RecyclerView mRecyclerView;
	private IngredientsAdapter mAdapter;

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
		mRecyclerView = (RecyclerView) inflater.inflate(R.layout.content_cocktail, container, false);
		return mRecyclerView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


		mAdapter = new IngredientsAdapter();
		mRecyclerView.setAdapter(mAdapter);

		mPresenter.bindView(mAdapter);

//		if (savedInstanceState == null) {
			mPresenter.loadRandomDrink();
//		} else if (savedInstanceState.containsKey(EXTRAS_KEY_ID)) {
////			TODO fix to restore from cache
//			mId = savedInstanceState.getLong(EXTRAS_KEY_ID);
//			mPresenter.loadDrinkById(mId);
//		}
	}

	public void loadRandomDrink() {
		mPresenter.loadRandomDrink();
//		ivImage.setVisibility(View.VISIBLE);
//		txtError.setVisibility(View.GONE);
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
}
