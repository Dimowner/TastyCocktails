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
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

public class FirebaseHandler {

	private static final String TABLE_TOP_DRINKS = "top_drinks";
	private static final String LIKE_COUNT = "likeCount";

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
		}
	}

	/**
	 * Decrease global favorites counter for Drink
	 * @param id Drink id
	 */
	public void unlikeDrink(long id) {
		if (!BuildConfig.DEBUG) {
			Timber.d("unlikeDrink id: " + id);
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
		}
	}

	public void getTop20() {
		topDrinksRef.orderByChild(LIKE_COUNT).limitToLast(20).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				List<FirebaseDrink> list = new LinkedList<>();
				for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
					FirebaseDrink d = postSnapshot.getValue(FirebaseDrink.class);
					if (d != null) {
						Timber.v("dirk: " + d.toString());
						list.add(0, d);
					}
				}
				Timber.v("list size: " + list.size());
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

//	public void putDrink(Drink drink) {
//		topDrinksRef.child(String.valueOf(drink.getIdDrink()))
//				.setValue(ModelMapper.drinkToFirebaseDrink(drink), (databaseError, databaseReference) -> Timber.v("onComplete"));
//	}
//
//	public void putDrinks(List<Drink> drinks) {
//		Timber.v("putDrinks size: " + drinks.size());
//		for (int i = 0; i < drinks.size(); i++) {
//			putDrink(drinks.get(i));
//		}
//	}
}
