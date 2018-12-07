package com.dimowner.tastycocktails.rating;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.dimowner.tastycocktails.FirebaseHandler;
import com.dimowner.tastycocktails.ModelMapper;
import com.dimowner.tastycocktails.data.RepositoryContract;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class RatingPresenter extends ViewModel implements RatingContract.UserActionsListener {

	private RepositoryContract repository;

	private FirebaseHandler firebaseHandler;

	private RatingContract.View view;

	private CompositeDisposable compositeDisposable = new CompositeDisposable();


	public RatingPresenter(RepositoryContract repository, FirebaseHandler firebaseHandler) {
		this.repository = repository;
		this.firebaseHandler = firebaseHandler;
	}

	@Override
	public void loadRating() {
		view.showProgress();
		if (compositeDisposable.size() > 0) {
			compositeDisposable.clear();
		}
		compositeDisposable.add(firebaseHandler.getTopDrinks()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(list -> {
					view.displayData(ModelMapper.firebaseDrinksToListItems(list));
					view.hideProgress();
				}, throwable -> {
					view.showQueryError();
					view.hideProgress();
				})
		);
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		compositeDisposable.dispose();
	}

	@Override
	public void bindView(@NonNull RatingContract.View view) {
		this.view = view;
	}

	@Override
	public void unbindView() {
		if (compositeDisposable.size() > 0) {
			compositeDisposable.clear();
		}
		this.view = null;
	}
}
