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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import task.softermii.tastycocktails.R;

/**
 * Created on 26.07.2017.
 * @author Dimowner
 */
class CocktailsRecyclerAdapter extends RecyclerView.Adapter<CocktailsRecyclerAdapter.ItemViewHolder> {

	private List<ListItem> mShowingData;

	private ItemClickListener itemClickListener;

	class ItemViewHolder extends RecyclerView.ViewHolder {
		TextView name;
 		TextView description;
		ImageView image;
 		View view;

		ItemViewHolder(View itemView) {
				super(itemView);
				this.view = itemView;
				this.name = (TextView) itemView.findViewById(R.id.list_item_name);
				this.description = (TextView) itemView.findViewById(R.id.list_item_description);
				this.image = (ImageView) itemView.findViewById(R.id.list_item_image);
			}
 	}

	CocktailsRecyclerAdapter() {
		this.mShowingData = Collections.emptyList();
	}

	@Override
	public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.list_item, parent, false);
		return new ItemViewHolder(v);
	}

	@Override
	public void onBindViewHolder(final ItemViewHolder holder, final int position) {
		holder.name.setText(mShowingData.get(position).getName());
		holder.description.setText(mShowingData.get(position).getDescription());

		if (mShowingData.get(position).getAvatar_url() != null) {
			Glide.with(holder.view.getContext())
					.load(mShowingData.get(position).getAvatar_url())
					.apply(RequestOptions.circleCropTransform())
					.into(holder.image);
		}

		holder.view.setOnClickListener(v -> {
			if (itemClickListener != null) {
				itemClickListener.onItemClick(v, holder.getAdapterPosition());
			}
		});
		//Set transition names
		ViewCompat.setTransitionName(holder.name, mShowingData.get(position).getName());
		ViewCompat.setTransitionName(holder.description, mShowingData.get(position).getName() + "description");
		ViewCompat.setTransitionName(holder.image, mShowingData.get(position).getName() + "image");
	}

	@Override
	public int getItemCount() {
		return mShowingData.size();
	}

	ListItem getItem(int pos) {
		return mShowingData.get(pos);
	}

	public void setData(List<ListItem> data) {
		if (data != null) {
			this.mShowingData = data;
		} else {
			this.mShowingData = Collections.emptyList();
		}
		notifyDataSetChanged();
	}

	void setItemClickListener(ItemClickListener itemClickListener) {
		this.itemClickListener = itemClickListener;
	}

	/**
	 * Save adapters state
	 * @return adapter state.
	 */
	Parcelable onSaveInstanceState() {
		SavedState ss = new SavedState(AbsSavedState.EMPTY_STATE);
		ss.items = mShowingData.toArray(new ListItem[0]);
		return ss;
	}

	/**
	 * Restore adapters state
	 * @param state Adapter state.
	 */
	void onRestoreInstanceState(Parcelable state) {
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
			items = (ListItem[]) in.readParcelableArray(getClass().getClassLoader());
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeParcelableArray(items, flags);
		}

		ListItem[] items;

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

	interface ItemClickListener{
		void onItemClick(View view, int position);
	}
}
