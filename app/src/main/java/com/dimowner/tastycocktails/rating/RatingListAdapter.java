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

package com.dimowner.tastycocktails.rating;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.cocktails.list.ListItem;

public class RatingListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private List<ListItem> mShowingData;

	private ItemClickListener itemClickListener;

	public class ItemViewHolder extends RecyclerView.ViewHolder {
		TextView name;
		TextView description;
		TextView number;
		ImageView image;
		View view;

		public ItemViewHolder(View itemView) {
			super(itemView);
			this.view = itemView;
			this.name = itemView.findViewById(R.id.list_item_name);
			this.description = itemView.findViewById(R.id.list_item_description);
			this.image = itemView.findViewById(R.id.list_item_image);
			this.number = itemView.findViewById(R.id.list_item_number);
		}
	}

	public RatingListAdapter() {
		this.mShowingData = new ArrayList<>();
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_rating, parent, false);
		return new ItemViewHolder(v);
	}

	@Override
	public void onBindViewHolder(final RecyclerView.ViewHolder h, final int position1) {
		final int pos = position1;
		ItemViewHolder holder = (ItemViewHolder) h;
		holder.name.setText(mShowingData.get(pos).getName());
		holder.description.setText(mShowingData.get(pos).getCategory());
		holder.number.setText(String.valueOf(pos+1));

		if (mShowingData.get(pos).getAvatar_url() != null) {
			Glide.with(holder.view.getContext())
					.load(mShowingData.get(pos).getAvatar_url())
//						.apply(RequestOptions.circleCropTransform())
					.listener(new RequestListener<Drawable>() {
						@Override
						public boolean onLoadFailed(@Nullable GlideException e, Object model,
															 Target<Drawable> target, boolean isFirstResource) {
							holder.image.setImageResource(R.drawable.no_image);
							return false;
						}

						@Override
						public boolean onResourceReady(Drawable resource, Object model,
																 Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
							return false;
						}
					})
					.into(holder.image);
		} else {
			holder.image.setImageResource(R.drawable.no_image);
		}

		final int id = (int) mShowingData.get(pos).getId();
		holder.view.setOnClickListener(v -> {
			if (itemClickListener != null) {
				itemClickListener.onItemClick(v, findPositionForId(id));
			}
		});
	}

	private int findPositionForId(int id) {
		for (int i = 0; i < mShowingData.size(); i++) {
			if (id == mShowingData.get(i).getId()) {
				return i;
			}
		}
		return 0;
	}

	@Override
	public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
		super.onDetachedFromRecyclerView(recyclerView);
	}

	@Override
	public int getItemCount() {
		return mShowingData.size();
	}

	public ListItem getItem(int pos) {
		if (pos >= 0 && pos < mShowingData.size()) {
			return mShowingData.get(pos);
		} else {
			return null;
		}
	}

	public void setData(List<ListItem> data) {
		mShowingData = data;
		notifyDataSetChanged();
	}

	public List<ListItem> getData() {
		return mShowingData;
	}

	public void setItemClickListener(ItemClickListener itemClickListener) {
		this.itemClickListener = itemClickListener;
	}


	public interface ItemClickListener{
		void onItemClick(View view, int position);
	}
}
