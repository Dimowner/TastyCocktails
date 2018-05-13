package com.dimowner.tastycocktails.cocktails.details;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DetailsPagerAdapter extends PagerAdapter {

	private Context context;
	private int layoutId;
	private int size;
	private OnCreatePageListener onCreatePageListener;

	public DetailsPagerAdapter(Context context, int layoutId, int size) {
		this.size = size;
		this.context = context;
		this.layoutId = layoutId;
	}

	@NonNull
	@Override
	public Object instantiateItem(@NonNull ViewGroup collection, int position) {
		LayoutInflater inflater = LayoutInflater.from(context);
		ViewGroup layout = (ViewGroup) inflater.inflate(layoutId, collection, false);
		collection.addView(layout);
		if (onCreatePageListener != null) {
			onCreatePageListener.onCreatePage(position, layout);
		}
		return layout;
	}

	@Override
	public void destroyItem(@NonNull ViewGroup collection, int position, @NonNull Object view) {
		collection.removeView((View) view);
		if (onCreatePageListener != null) {
			onCreatePageListener.onDestroyPage(position, (View) view);
		}
	}

	@Override
	public int getCount() {
		return size;
	}

	@Override
	public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
		return view == object;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return String.valueOf(position);
	}

	public void setOnCreatePageListener(OnCreatePageListener onCreatePageListener) {
		this.onCreatePageListener = onCreatePageListener;
	}

	public interface OnCreatePageListener {
		void onCreatePage(int pos, View view);
		void onDestroyPage(int pos, View view);
	}
}
