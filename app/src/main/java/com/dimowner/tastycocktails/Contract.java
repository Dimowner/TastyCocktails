package com.dimowner.tastycocktails;

import android.support.annotation.NonNull;

public interface Contract {

	interface View {
		void showProgress();

		void hideProgress();

		void showQueryError();

		void showNetworkError();
	}

	interface UserActionsListener<T extends View> {

		void bindView(@NonNull T view);

		void unbindView();
	}
}
