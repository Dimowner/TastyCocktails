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
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import task.softermii.tastycocktails.R;
import task.softermii.tastycocktails.util.AndroidUtils;

/**
 * Created on 14.08.2017.
 * @author Dimowner
 */
public class IngredientsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements DetailsContract.View  {

	private static final int VIEW_TYPE_HEADER = 1;
	private static final int VIEW_TYPE_NORMAL = 2;
	private static final int VIEW_TYPE_FOOTER = 3;

	private List<IngredientItem> mShowingData;

	private String name;
	private String description;
	private String imageUrl;

	private HeaderViewHolder headerViewHolder;

	private ItemClickListener itemClickListener;

	private AnimationListener animationListener;

	public static class HeaderViewHolder extends RecyclerView.ViewHolder {
		final View view;
		final ImageView ivImage;
		final TextView txtName;
		final TextView txtDescription;
		final TextView ingredientsLabel;
		final TextView txtError;
		final ProgressBar progress;

		HeaderViewHolder(View itemView){
			super(itemView);
			view = itemView;
			ivImage = itemView.findViewById(R.id.details_image);
			txtName = itemView.findViewById(R.id.details_name);
			txtDescription = itemView.findViewById(R.id.details_description);
			ingredientsLabel = itemView.findViewById(R.id.ingredients_label);

			txtError = itemView.findViewById(R.id.details_error);
			progress = itemView.findViewById(R.id.progress);

			if (AndroidUtils.isAndroid5()) {
				Resources res = txtName.getContext().getResources();
				txtName.setTransitionName(res.getString(R.string.list_item_label_transition));
				txtDescription.setTransitionName(res.getString(R.string.list_item_content_transition));
				ivImage.setTransitionName(res.getString(R.string.list_item_image_transition));
			}
		}
	}

	class IngredientViewHolder extends RecyclerView.ViewHolder {
		TextView txtName;
		TextView txtMeasure;
		View view;

		IngredientViewHolder(View itemView) {
			super(itemView);
			this.view = itemView;
			this.txtName = itemView.findViewById(R.id.list_item_ingredient_name);
			this.txtMeasure = itemView.findViewById(R.id.list_item_ingredient_measure);
		}
	}

	public static class FooterViewHolder extends RecyclerView.ViewHolder {
		final View view;

		FooterViewHolder(View itemView){
			super(itemView);
			view = itemView;
		}
	}

	public IngredientsAdapter() {
		this.mShowingData = Collections.emptyList();
	}

	@Override
	public void showProgress() {
		if (headerViewHolder !=  null) {
			headerViewHolder.progress.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void hideProgress() {
		if (headerViewHolder !=  null) {
			headerViewHolder.progress.setVisibility(View.GONE);
		}
	}

	@Override
	public void showQueryError() {
		if (headerViewHolder !=  null) {
			headerViewHolder.ivImage.setVisibility(View.INVISIBLE);
			headerViewHolder.txtError.setVisibility(View.VISIBLE);
			headerViewHolder.txtError.setText(R.string.msg_error_on_query);
			headerViewHolder.ingredientsLabel.setVisibility(View.INVISIBLE);
			headerViewHolder.txtName.setText(null);
			headerViewHolder.txtDescription.setText(null);
		}
	}

	@Override
	public void showNetworkError() {
		if (headerViewHolder !=  null) {
			headerViewHolder.ivImage.setVisibility(View.INVISIBLE);
			headerViewHolder.txtError.setVisibility(View.VISIBLE);
			headerViewHolder.txtError.setText(R.string.msg_error_no_internet);
			headerViewHolder.ingredientsLabel.setVisibility(View.INVISIBLE);
			headerViewHolder.txtName.setText(null);
			headerViewHolder.txtDescription.setText(null);
		}
	}

	@Override
	public void displayData(String name, String description) {
		this.name = name;
		this.description = description;
		if (headerViewHolder !=  null) {
			displayData(headerViewHolder);
		}
	}

	private void displayData(HeaderViewHolder holder) {
		holder.txtName.setText(name);
		holder.txtDescription.setText(description);
		holder.ingredientsLabel.setVisibility(View.VISIBLE);
	}

	@Override
	public void displayImage(String url) {
		if (imageUrl == null || !imageUrl.equals(url)) {
			imageUrl = url;
		}
		if (headerViewHolder != null) {
			displayImage(headerViewHolder);
		}
	}

	private void displayImage(HeaderViewHolder header) {
		if (imageUrl != null && !imageUrl.isEmpty()) {
			header.ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
			Glide.with(header.ivImage.getContext())
					.load(imageUrl)
					.listener(new RequestListener<Drawable>() {
						@Override
						public boolean onLoadFailed(@Nullable GlideException e, Object model,
															 Target<Drawable> target, boolean isFirstResource) {
							if (animationListener != null) {
								animationListener.onAnimation();
							}
							hideProgress();
							header.txtError.setVisibility(View.VISIBLE);
							return false;
						}

						@Override
						public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
																 DataSource dataSource, boolean isFirstResource) {
							if (animationListener != null) {
								animationListener.onAnimation();
							}
							hideProgress();
							return false;
						}
					})
					.into(header.ivImage);
		} else {
			if (animationListener != null) {
				animationListener.onAnimation();
			}
			header.ivImage.setBackgroundColor(ContextCompat.getColor(header.ivImage.getContext(), R.color.colorPrimary));
			header.ivImage.setImageResource(R.drawable.no_image);
			hideProgress();
		}
	}

	@Override
	public void displayIngredientsList(List<IngredientItem> items) {
		setData(items);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == VIEW_TYPE_HEADER) {
			View v = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.list_item_details_header, parent, false);
			return new HeaderViewHolder(v);
		} else if (viewType == VIEW_TYPE_NORMAL) {
			View v = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.list_item_ingredient, parent, false);
			return new IngredientViewHolder(v);
		} else if (viewType == VIEW_TYPE_FOOTER) {
			View v = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.list_item_details_footer, parent, false);
			return new FooterViewHolder(v);
		}
		return null;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		if (viewHolder.getItemViewType() == VIEW_TYPE_HEADER) {
			//Do nothing
			displayData((HeaderViewHolder) viewHolder);
			displayImage((HeaderViewHolder) viewHolder);
		} else if (viewHolder.getItemViewType() == VIEW_TYPE_NORMAL) {
			IngredientViewHolder holder = ((IngredientViewHolder) viewHolder);
			int pos = holder.getAdapterPosition()-1;
			holder.txtName.setText(mShowingData.get(pos).getName());
			if (mShowingData.get(pos).getMeasure() != null && !mShowingData.get(pos).getMeasure().trim().isEmpty()) {
				holder.txtMeasure.setText(mShowingData.get(pos).getMeasure());
			} else {
				holder.txtMeasure.setText("");
			}

			holder.view.setOnClickListener(v -> {
				if (itemClickListener != null) {
					itemClickListener.onItemClick(v, holder.getAdapterPosition());
				}
			});

			//Set transition names
			Resources res = holder.view.getResources();
			ViewCompat.setTransitionName(holder.txtName, res.getString(R.string.list_item_label_transition));
			ViewCompat.setTransitionName(holder.txtMeasure, res.getString(R.string.list_item_content_transition));
		} else if (viewHolder.getItemViewType() == VIEW_TYPE_FOOTER) {
			//Do nothing
		}
	}

	@Override
	public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
		super.onViewAttachedToWindow(holder);
		if (holder.getItemViewType() == VIEW_TYPE_HEADER) {
			headerViewHolder = (HeaderViewHolder) holder;
		}
	}

	@Override
	public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
		super.onViewDetachedFromWindow(holder);
		if (holder.getItemViewType() == VIEW_TYPE_HEADER) {
			headerViewHolder = null;
		}
	}

	@Override
	public int getItemCount() {
		return mShowingData.size() + 2;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0) {
			return VIEW_TYPE_HEADER;
		} else if (position == mShowingData.size()+1) {
			return VIEW_TYPE_FOOTER;
		}
		return VIEW_TYPE_NORMAL;
	}

	public IngredientItem getItem(int pos) {
		return mShowingData.get(pos-1);
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

	public void setAnimationListener(AnimationListener animationListener) {
		this.animationListener = animationListener;
	}

	/**
	 * Save adapters state
	 * @return adapter state.
	 */
	public Parcelable onSaveInstanceState() {
		SavedState ss = new SavedState(AbsSavedState.EMPTY_STATE);
		ss.items = mShowingData.toArray(new IngredientItem[0]);
		ss.name = name;
		ss.description = description;
		ss.imageUrl = imageUrl;
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
		name = ss.name;
		description = ss.description;
		imageUrl = ss.imageUrl;
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
			String[] strings = new String[3];
			in.readStringArray(strings);
			name = strings[0];
			description = strings[1];
			imageUrl = strings[2];
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeParcelableArray(items, flags);
			out.writeStringArray(new String[] {name, description, imageUrl});
		}

		IngredientItem[] items;
		String name;
		String description;
		String imageUrl;

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

	public interface AnimationListener {
		void onAnimation();
	}
}
