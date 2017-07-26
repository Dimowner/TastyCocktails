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

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;

/**
 * Created on 26.07.2017.
 * @author Dimowner
 */
public class GridAutofitLayoutManager extends GridLayoutManager {
	private int mColumnWidth;
	private boolean mColumnWidthChanged = true;

	public GridAutofitLayoutManager(Context context, int columnWidth) {
        /* Initially set spanCount to 1, will be changed automatically later. */
		super(context, 1);
		setColumnWidth(checkedColumnWidth(context, columnWidth));
	}

	public GridAutofitLayoutManager(Context context, int columnWidth, int orientation, boolean reverseLayout) {
        /* Initially set spanCount to 1, will be changed automatically later. */
		super(context, 1, orientation, reverseLayout);
		setColumnWidth(checkedColumnWidth(context, columnWidth));
	}

	private int checkedColumnWidth(Context context, int columnWidth) {
		if (columnWidth <= 0) {
            /* Set default columnWidth value (48dp here). It is better to move this constant
            to static constant on top, but we need context to convert it to dp, so can't really
            do so. */
			columnWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
					context.getResources().getDisplayMetrics());
		}
		return columnWidth;
	}

	void setColumnWidth(int newColumnWidth) {
		if (newColumnWidth > 0 && newColumnWidth != mColumnWidth) {
			mColumnWidth = newColumnWidth;
			mColumnWidthChanged = true;
		}
	}

	@Override
	public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
		int width = getWidth();
		int height = getHeight();
		if (mColumnWidthChanged && mColumnWidth > 0 && width > 0 && height > 0) {
			int totalSpace;
			if (getOrientation() == VERTICAL) {
				totalSpace = width - getPaddingRight() - getPaddingLeft();
			} else {
				totalSpace = height - getPaddingTop() - getPaddingBottom();
			}
			int spanCount = Math.max(1, totalSpace / mColumnWidth);
			setSpanCount(spanCount);
			mColumnWidthChanged = false;
		}
		super.onLayoutChildren(recycler, state);
	}
}
