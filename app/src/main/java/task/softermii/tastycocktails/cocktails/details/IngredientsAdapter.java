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

package task.softermii.tastycocktails.cocktails.details;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import task.softermii.tastycocktails.R;
import timber.log.Timber;

/**
 * Created on 14.08.2017.
 * @author Dimowner
 */
public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.IngredientViewHolder> {


	private List<IngredientItem> mShowingData;

	private ItemClickListener itemClickListener;

	class IngredientViewHolder extends RecyclerView.ViewHolder {
		TextView name;
		TextView measure;
		View view;

		IngredientViewHolder(View itemView) {
			super(itemView);
			this.view = itemView;
			this.name = (TextView) itemView.findViewById(R.id.list_item_ingredient_name);
			this.measure = (TextView) itemView.findViewById(R.id.list_item_ingredient_measure);
		}
	}

	public IngredientsAdapter() {
		this.mShowingData = Collections.emptyList();
	}

	@Override
	public IngredientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.list_item_ingredient, parent, false);
		return new IngredientViewHolder(v);
	}

	@Override
	public void onBindViewHolder(IngredientViewHolder holder, int position) {
		Timber.v("onBindViewHolder");
		int pos = holder.getAdapterPosition();
		holder.name.setText(mShowingData.get(pos).getName());
		if (mShowingData.get(pos).getMeasure() != null && !mShowingData.get(pos).getMeasure().trim().isEmpty()) {
			holder.measure.setText(mShowingData.get(pos).getMeasure());
		} else {
			holder.measure.setText("");
		}

		holder.view.setOnClickListener(v -> {
			if (itemClickListener != null) {
				itemClickListener.onItemClick(v, holder.getAdapterPosition());
			}
		});

		//Set transition names
		Resources res = holder.view.getResources();
		ViewCompat.setTransitionName(holder.name, res.getString(R.string.list_item_label_transition));
		ViewCompat.setTransitionName(holder.measure, res.getString(R.string.list_item_content_transition));
	}

	@Override
	public int getItemCount() {
		return mShowingData.size();
	}


	public IngredientItem getItem(int pos) {
		return mShowingData.get(pos);
	}

	public void setData(List<IngredientItem> data) {
		if (data != null) {
			this.mShowingData = data;
		} else {
			this.mShowingData = Collections.emptyList();
		}
		notifyDataSetChanged();
	}

	public void setItemClickListener(ItemClickListener itemClickListener) {
		this.itemClickListener = itemClickListener;
	}

	/**
	 * Save adapters state
	 * @return adapter state.
	 */
	public Parcelable onSaveInstanceState() {
		SavedState ss = new SavedState(AbsSavedState.EMPTY_STATE);
		ss.items = mShowingData.toArray(new IngredientItem[0]);
		return ss;
	}

	/**
	 * Restore adapters state
	 * @param state Adapter state.
	 */
	public void onRestoreInstanceState(Parcelable state) {
		SavedState ss = (SavedState) state;
		mShowingData = new ArrayList<>();
		Collections.addAll(mShowingData, ss.items);
		notifyDataSetChanged();
	}


	/**
	 * Object state
	 */
	private static class SavedState extends View.BaseSavedState {
		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			items = (IngredientItem[]) in.readParcelableArray(getClass().getClassLoader());
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeParcelableArray(items, flags);
		}

		IngredientItem[] items;

		public static final Parcelable.Creator<SavedState> CREATOR =
				new Parcelable.Creator<SavedState>() {
					@Override
					public SavedState createFromParcel(Parcel in) {
						return new SavedState(in);
					}

					@Override
					public SavedState[] newArray(int size) {
						return new SavedState[size];
					}
				};
	}

	public interface ItemClickListener{
		void onItemClick(View view, int position);
	}
}
