package com.dimowner.tastycocktails.rating;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.dimowner.tastycocktails.AppConstants;
import com.dimowner.tastycocktails.FirebaseHandler;
import com.dimowner.tastycocktails.ModelMapper;
import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.data.Prefs;
import com.dimowner.tastycocktails.data.RepositoryContract;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class RatingPresenter extends ViewModel implements RatingContract.UserActionsListener {

	private RepositoryContract repository;

	private FirebaseHandler firebaseHandler;

	private Prefs prefs;

	private RatingContract.View view;

	private CompositeDisposable compositeDisposable = new CompositeDisposable();


	public RatingPresenter(RepositoryContract repository, FirebaseHandler firebaseHandler, Prefs prefs) {
		this.repository = repository;
		this.firebaseHandler = firebaseHandler;
		this.prefs = prefs;
	}

	@Override
	public void loadRating() {
		view.showProgress();
		if (compositeDisposable.size() > 0) {
			compositeDisposable.clear();
		}
		compositeDisposable.add(repository.getRatingList()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(list -> {
					if (list.size() > 0) {
						view.displayData(ModelMapper.ratingDrinksToListItems(list));
						view.hideProgress();
					}
				}, throwable -> {
					Timber.e(throwable);
					view.hideProgress();
				})
		);

		if (prefs.getRatingUpdatedTime() < new Date().getTime() - AppConstants.MIN_RATING_UPDATE_INTERVAL_MILLS) {
			compositeDisposable.add(firebaseHandler.getTopDrinks().subscribeOn(Schedulers.io())
					.flatMapCompletable(d -> repository.replaceRating(d).subscribeOn(Schedulers.io()))
					.timeout(10, TimeUnit.SECONDS)
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(() -> {
						prefs.setRatingUpdatedTime(new Date().getTime());
						view.hideProgress();
					}, t -> {
						Timber.e(t);
						view.hideProgress();
						if (TCApplication.isConnected()) {
							view.showQueryError();
						} else {
							view.showNetworkError();
						}
					})
			);
		} else {
			Timber.d("Not able to load rating!");
		}
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
