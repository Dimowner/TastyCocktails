package com.dimowner.tastycocktails.cocktails;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Horizontal divider without divider in the last item of list
 */
public class HorizontalDividerItemDecoration extends RecyclerView.ItemDecoration  {

	private static final int[] ATTRS = new int[]{ android.R.attr.listDivider };

	private Drawable mDivider;

	private final Rect mBounds = new Rect();

	private int showDividerForLastItem = 0;


	public HorizontalDividerItemDecoration(Context context) {
		final TypedArray a = context.obtainStyledAttributes(ATTRS);
		mDivider = a.getDrawable(0);
		a.recycle();
	}

	@Override
	public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
		if (parent.getLayoutManager() == null) {
			return;
		}
		drawVertical(c, parent);
	}

	private void drawVertical(Canvas canvas, RecyclerView parent) {
		canvas.save();
		final int left;
		final int right;
		//noinspection AndroidLintNewApi - NewApi lint fails to handle overrides.
		if (parent.getClipToPadding()) {
			left = parent.getPaddingLeft();
			right = parent.getWidth() - parent.getPaddingRight();
			canvas.clipRect(left, parent.getPaddingTop(), right,
					parent.getHeight() - parent.getPaddingBottom());
		} else {
			left = 0;
			right = parent.getWidth();
		}

		final int childCount = parent.getChildCount();
		for (int i = 0; i < childCount- showDividerForLastItem; i++) {
			final View child = parent.getChildAt(i);
			parent.getDecoratedBoundsWithMargins(child, mBounds);
			final int bottom = mBounds.bottom + Math.round(child.getTranslationY());
			final int top = bottom - mDivider.getIntrinsicHeight();
			mDivider.setBounds(left, top, right, bottom);
			mDivider.draw(canvas);
		}
		canvas.restore();
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
										RecyclerView.State state) {
		if (mDivider == null) {
			outRect.set(0, 0, 0, 0);
			return;
		}
		outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
	}

	public void showDividerForLastItem(boolean show) {
		if (show) {
			showDividerForLastItem = 0;
		} else {
			showDividerForLastItem = 1;
		}
	}
}
