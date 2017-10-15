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

package com.dimowner.tastycocktails.cocktails.list;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.view.AbsSavedState;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dimowner.tastycocktails.R;

/**
 * Created on 26.07.2017.
 * @author Dimowner
 */
public class CocktailsRecyclerAdapter extends RecyclerView.Adapter<CocktailsRecyclerAdapter.ItemViewHolder> {

	private List<ListItem> mBaseData = new ArrayList<>();

	private String filterStr = "";

	private List<ListItem> mShowingData;

	private ItemClickListener itemClickListener;

	private OnFavoriteClickListener onFavoriteClickListener;

	class ItemViewHolder extends RecyclerView.ViewHolder {
		TextView name;
 		TextView description;
		ImageView image;
		ImageView btnFev;
 		View view;

		ItemViewHolder(View itemView) {
				super(itemView);
				this.view = itemView;
				this.name = itemView.findViewById(R.id.list_item_name);
				this.description = itemView.findViewById(R.id.list_item_description);
				this.image = itemView.findViewById(R.id.list_item_image);
				this.btnFev = itemView.findViewById(R.id.list_item_btn_favorite);
			}
 	}

	public CocktailsRecyclerAdapter() {
		this.mShowingData = new ArrayList<>();
	}

	@Override
	public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.list_item, parent, false);
		return new ItemViewHolder(v);
	}

	@Override
	public void onBindViewHolder(final ItemViewHolder holder, final int position1) {
		int pos = holder.getAdapterPosition();
		holder.name.setText(mShowingData.get(pos).getName());
		holder.description.setText(mShowingData.get(pos).getDescription());

		if (mShowingData.get(pos).getAvatar_url() != null) {
			Glide.with(holder.view.getContext())
					.load(mShowingData.get(pos).getAvatar_url())
					.apply(RequestOptions.circleCropTransform())
					.listener(new RequestListener<Drawable>() {
						@Override public boolean onLoadFailed(@Nullable GlideException e, Object model,
															 Target<Drawable> target, boolean isFirstResource) {
							holder.image.setImageResource(R.drawable.no_image);
							return false;
						}
						@Override public boolean onResourceReady(Drawable resource, Object model,
										Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
							return false;
						}
					})
					.into(holder.image);
		} else {
			holder.image.setImageResource(R.drawable.no_image);
		}

		holder.btnFev.setOnClickListener(v -> {
			if (onFavoriteClickListener != null) {
				onFavoriteClickListener.onFavoriteClick(
						holder.btnFev, pos, (int) mShowingData.get(pos).getId(), -1);
			}
		});

		if (mShowingData.get(holder.getAdapterPosition()).isFavorite()) {
			holder.btnFev.setImageResource(R.drawable.heart_grey_pressed);
		} else {
			holder.btnFev.setImageResource(R.drawable.heart_outline_grey);
		}

		holder.view.setOnClickListener(v -> {
			if (itemClickListener != null) {
				itemClickListener.onItemClick(v, holder.getAdapterPosition());
			}
		});

//		//Set transition names
//		Resources res = holder.view.getResources();
//		ViewCompat.setTransitionName(holder.name, res.getString(R.string.list_item_label_transition));
//		ViewCompat.setTransitionName(holder.description, res.getString(R.string.list_item_content_transition));
//		ViewCompat.setTransitionName(holder.image, res.getString(R.string.list_item_image_transition));
	}

	@Override
	public int getItemCount() {
		return mShowingData.size();
	}

	public ListItem getItem(int pos) {
		return mShowingData.get(pos);
	}

	public void setData(List<ListItem> data) {
		mBaseData = data;
		if (isFiltered()) {
			updateShowingDataWithFilter();
		} else {
			this.mShowingData.clear();
			this.mShowingData.addAll(mBaseData);
			notifyDataSetChanged();
		}
	}

	/**
	 * Update showing data by applying search filter.
	 */
	private void updateShowingDataWithFilter() {
		if (isFiltered()) {
			mShowingData.clear();
			for (int i = 0; i < mBaseData.size(); i++) {
				if (mBaseData.get(i).getName().toLowerCase().contains(filterStr.toLowerCase())) {
					mShowingData.add(mBaseData.get(i));
				}
			}
			notifyDataSetChanged();
		}
	}

	public void applyFilter(String str) {
		if (str == null || str.isEmpty()) {
			filterStr = "";
			this.mShowingData.clear();
			mShowingData.addAll(mBaseData);
			notifyDataSetChanged();
		} else {
			filterStr = str;
			updateShowingDataWithFilter();
		}
	}

	private boolean isFiltered() {
		return (filterStr != null && !filterStr.isEmpty());
	}

	public void setItemClickListener(ItemClickListener itemClickListener) {
		this.itemClickListener = itemClickListener;
	}

	public void setOnFavoriteClickListener(OnFavoriteClickListener onFavoriteClickListener) {
		this.onFavoriteClickListener = onFavoriteClickListener;
	}

	/**
	 * Save adapters state
	 * @return adapter state.
	 */
	public Parcelable onSaveInstanceState() {
		SavedState ss = new SavedState(AbsSavedState.EMPTY_STATE);
		ss.items = mShowingData.toArray(new ListItem[0]);
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

	public interface ItemClickListener{
		void onItemClick(View view, int position);
	}

	public interface OnFavoriteClickListener {
		void onFavoriteClick(ImageView view, int position, int id, int action);
	}
}
