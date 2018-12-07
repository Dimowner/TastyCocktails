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

package com.dimowner.tastycocktails;

import android.support.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposables;
import timber.log.Timber;

public class FirebaseHandler {

	private static final String TABLE_TOP_DRINKS = "top_drinks";
	private static final String LIKE_COUNT = "likeCount";
	private static final int TOP_DINKS_COUNT = 30;

	private FirebaseDatabase database;
	private DatabaseReference topDrinksRef;

	public FirebaseHandler() {
		this.database = FirebaseDatabase.getInstance();
		this.topDrinksRef = database.getReference(TABLE_TOP_DRINKS);
	}

	/**
	 * Increase global favorites counter for Drink
	 * @param id Drink id
	 */
	public void likeDrink(long id) {
		if (!BuildConfig.DEBUG) {
			Timber.d("likeDrink id: " + id);
			try {
				final DatabaseReference ref = topDrinksRef.child(String.valueOf(id)).child(LIKE_COUNT);
				ref.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						Long d = dataSnapshot.getValue(Long.class);
						if (d != null) {
							ref.setValue(d + 1);
						}
					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {
						Timber.e(databaseError.toString());
					}
				});
			} catch (Exception e) {
				Timber.e(e);
			}
		}
	}

	/**
	 * Decrease global favorites counter for Drink
	 * @param id Drink id
	 */
	public void unlikeDrink(long id) {
		if (!BuildConfig.DEBUG) {
			Timber.d("unlikeDrink id: " + id);
			try {
				final DatabaseReference ref = topDrinksRef.child(String.valueOf(id)).child(LIKE_COUNT);
				ref.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						Long d = dataSnapshot.getValue(Long.class);
						if (d != null) {
							ref.setValue(d - 1);
						}
					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {
						Timber.e(databaseError.toString());
					}
				});
			} catch (Exception e) {
				Timber.e(e);
			}
		}
	}

	public Single<List<FirebaseDrink>> getTopDrinks() {
		try {
			return Single.create(new SingleValueOnSubscribe(topDrinksRef.orderByChild(LIKE_COUNT).limitToLast(TOP_DINKS_COUNT)))
					.map(dataSnapshot -> {
						List<FirebaseDrink> list = new LinkedList<>();
						for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
							FirebaseDrink d = postSnapshot.getValue(FirebaseDrink.class);
							if (d != null) {
//								Timber.v("dirk: " + d.getStrDrink() + " count: " + d.getLikeCount());
								list.add(0, d);
							}
						}
						return list;
			});
		} catch (Exception e) {
			Timber.e(e);
			return Single.just(new ArrayList<>());
		}
	}


	public static class SingleValueOnSubscribe implements SingleOnSubscribe<DataSnapshot> {

		private Query query;

		public SingleValueOnSubscribe(Query query) {
			this.query = query;
		}

		@Override
		public void subscribe(@io.reactivex.annotations.NonNull SingleEmitter<DataSnapshot> e) throws Exception {
			ValueEventListener listener = new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					if (!e.isDisposed()) e.onSuccess(dataSnapshot);
				}

				@Override
				public void onCancelled(DatabaseError databaseError) {
					if (!e.isDisposed()) e.onError(databaseError.toException());
				}
			};

			e.setDisposable(Disposables.fromRunnable(() -> {
				if (query != null) {
					query.removeEventListener(listener);
					query = null;
				}
			}));

			if (query != null) {
				query.addListenerForSingleValueEvent(listener);
			}
		}
	}
}
