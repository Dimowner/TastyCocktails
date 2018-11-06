package com.dimowner.tastycocktails.cocktails;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.LinearLayout;

import timber.log.Timber;

/**
 * Horizontal divider without divider in the last item of list.
 * Copied from {@link android.support.v7.widget.DividerItemDecoration}
 */
public class HorizontalDividerItemDecoration extends RecyclerView.ItemDecoration  {

	public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
	public static final int VERTICAL = LinearLayout.VERTICAL;

	private static final int[] ATTRS = new int[]{ android.R.attr.listDivider };

	private Drawable mDivider;

	private final Rect mBounds = new Rect();

	/**
	 * Current orientation. Either {@link #HORIZONTAL} or {@link #VERTICAL}.
	 */
	private int mOrientation;

	private int showDividerForLastItem = 0;

	public HorizontalDividerItemDecoration(Context context, int orientation) {
		final TypedArray a = context.obtainStyledAttributes(ATTRS);
		mDivider = a.getDrawable(0);
		if (mDivider == null) {
			Timber.w("@android:attr/listDivider was not set in the theme used for this "
					+ "DividerItemDecoration. Please set that attribute all call setDrawable()");
		}
		a.recycle();
		setOrientation(orientation);
	}

	/**
	 * Sets the orientation for this divider. This should be called if
	 * {@link RecyclerView.LayoutManager} changes orientation.
	 *
	 * @param orientation {@link #HORIZONTAL} or {@link #VERTICAL}
	 */
	public void setOrientation(int orientation) {
		if (orientation != HORIZONTAL && orientation != VERTICAL) {
			throw new IllegalArgumentException(
					"Invalid orientation. It should be either HORIZONTAL or VERTICAL");
		}
		mOrientation = orientation;
	}

	@Override
	public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
		if (parent.getLayoutManager() == null || mDivider == null) {
			return;
		}
		if (mOrientation == VERTICAL) {
			drawVertical(c, parent);
		} else {
			drawHorizontal(c, parent);
		}
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
		for (int i = 0; i < childCount - showDividerForLastItem; i++) {
			final View child = parent.getChildAt(i);
			parent.getDecoratedBoundsWithMargins(child, mBounds);
			final int bottom = mBounds.bottom + Math.round(child.getTranslationY());
			final int top = bottom - mDivider.getIntrinsicHeight();
			mDivider.setBounds(left, top, right, bottom);
			mDivider.draw(canvas);
		}
		canvas.restore();
	}

	private void drawHorizontal(Canvas canvas, RecyclerView parent) {
		canvas.save();
		final int top;
		final int bottom;
		//noinspection AndroidLintNewApi - NewApi lint fails to handle overrides.
		if (parent.getClipToPadding()) {
			top = parent.getPaddingTop();
			bottom = parent.getHeight() - parent.getPaddingBottom();
			canvas.clipRect(parent.getPaddingLeft(), top,
					parent.getWidth() - parent.getPaddingRight(), bottom);
		} else {
			top = 0;
			bottom = parent.getHeight();
		}

		final int childCount = parent.getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View child = parent.getChildAt(i);
			parent.getLayoutManager().getDecoratedBoundsWithMargins(child, mBounds);
			final int right = mBounds.right + Math.round(child.getTranslationX());
			final int left = right - mDivider.getIntrinsicWidth();
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
		if (mOrientation == VERTICAL) {
			outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
		} else {
			outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
		}
	}

	public void showDividerForLastItem(boolean show) {
		if (show) {
			showDividerForLastItem = 0;
		} else {
			showDividerForLastItem = 1;
		}
	}
}
