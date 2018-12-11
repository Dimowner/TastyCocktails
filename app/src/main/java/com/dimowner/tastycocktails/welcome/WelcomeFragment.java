package com.dimowner.tastycocktails.welcome;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.cocktails.CocktailsListFragment;
import com.dimowner.tastycocktails.data.Prefs;

import javax.inject.Inject;

public class WelcomeFragment extends Fragment {

	@Inject
	Prefs prefs;

	private CocktailsListFragment.OnFirstRunExecutedListener onFirstRunExecutedListener;

	public static WelcomeFragment newInstance() {
		return new WelcomeFragment();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_welcome, container, false);
		TCApplication.get(getContext()).applicationComponent().inject(this);
		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (prefs.isFirstRun()) {
			Button btnGetStarted = view.findViewById(R.id.get_started);
			btnGetStarted.setOnClickListener(view1 -> executeFirsRun());
		}
	}

	public void executeFirsRun() {
		prefs.firstRunExecuted();
		if (onFirstRunExecutedListener != null) {
			onFirstRunExecutedListener.onFirstRunExecuted();
		}
	}

	public void setOnFirstRunExecutedListener(CocktailsListFragment.OnFirstRunExecutedListener onFirstRunExecutedListener) {
		this.onFirstRunExecutedListener = onFirstRunExecutedListener;
	}
}
