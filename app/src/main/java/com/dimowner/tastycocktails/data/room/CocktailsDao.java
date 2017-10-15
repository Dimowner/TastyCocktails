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

package com.dimowner.tastycocktails.data.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import com.dimowner.tastycocktails.data.model.Drink;

/**
 * Created on 27.07.2017.
 * @author Dimowner
 */
@Dao
public interface CocktailsDao {

	@Query("SELECT * FROM drinks")
	Single<List<Drink>> getAll();

	@Query("SELECT * FROM drinks WHERE isFavorite <> 1")
	Flowable<List<Drink>> getLastSearch();

	@Query("SELECT * FROM drinks WHERE UPPER(strDrink) LIKE UPPER(:search)")
	Flowable<List<Drink>> searchDrinksRx(String search);

	@Query("SELECT * FROM drinks WHERE UPPER(strDrink) LIKE UPPER(:search)")
	List<Drink> searchDrinks(String search);

	@Query("SELECT * FROM drinks WHERE isFavorite = 1 ORDER BY strDrink")
	Flowable<List<Drink>> getFavorites();

	@Query("SELECT * FROM drinks WHERE history > 0 ORDER BY history DESC")
	Flowable<List<Drink>> getDrinksHistory();

	@Query("UPDATE drinks SET history = :time WHERE idDrink = :id")
	void updateDrinkHistory(long id, long time);

	@Query("UPDATE drinks SET history = 0")
	void clearHistory();

	@Query("SELECT * FROM drinks WHERE idDrink = :id")
	Single<Drink> getDrinkRx(long id);

	@Query("SELECT * FROM drinks WHERE idDrink = :id")
	Drink getDrink(long id);

	@Query("UPDATE drinks SET isFavorite = 1 WHERE idDrink = :id")
	void addToFavorites(long id);

	@Query("UPDATE drinks SET isFavorite = 0 WHERE idDrink = :id")
	void removeFromFavorites(long id);

	@Query("SELECT * FROM drinks ORDER BY RANDOM() LIMIT 1;")
	Single<Drink> getRandom();

	@Query("SELECT Count(*) FROM drinks WHERE isFavorite <> 1")
	Single<Integer> getLastSearchRowCount();

	@Query("SELECT Count(*) FROM drinks WHERE isFavorite = 1")
	Single<Integer> getFavoritesRowCount();

	@Query("UPDATE drinks SET isFavorite = NOT (SELECT isFavorite FROM drinks WHERE idDrink = :id) WHERE idDrink = :id")
	void reverseFavorite(long id);

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	void insertAll(Drink... items);

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	void insertDrink(Drink item);

	@Update
	void updateDrink(Drink item);

	@Query("DELETE FROM drinks")
	void deleteAll();

	@Query("DELETE FROM drinks WHERE isFavorite <> 1")
	void deleteLastSearch();
}
