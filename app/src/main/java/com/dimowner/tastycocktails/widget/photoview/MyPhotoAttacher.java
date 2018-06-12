/*
 * Copyright 2018 Dmitriy Ponomarenko
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership. The ASF licenses this
 * file to you under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.dimowner.tastycocktails.widget.photoview;

import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.dimowner.tastycocktails.util.AndroidUtils;
import com.dimowner.tastycocktails.widget.ThresholdListener;
import com.github.chrisbanes.photoview.PhotoViewAttacher;

import timber.log.Timber;

public class MyPhotoAttacher extends PhotoViewAttacher implements View.OnLongClickListener,View.OnTouchListener {

	private static final int ACTION_NONE = -1;
	private static final int ACTION_DRAG = 1;
	private static final int ACTION_ZOOM = 2;

	private static final int MAX_MOVE = (int) AndroidUtils.dpToPx(200); //dip
	private static final int TOP_THRESHOLD = (int)(MAX_MOVE * 0.5); //dip
	private static final int BOTTOM_THRESHOLD = (int)(MAX_MOVE * 0.5); //dip

	private SpringAnimation moveAnimationY;

	private int action = ACTION_NONE;

	private float realDy = 0;
	private float cumulatedDy = 0;

	private float startY = 0f;

	private boolean isScaled = false;
	private float cumulatedScale = 1f;

	private ImageView imageView;

	//Converted value from pixels to coefficient used in function which describes move.
	private final float k = (float) (MAX_MOVE / (Math.PI/2));

	private ThresholdListener onThresholdListener;

	public MyPhotoAttacher(ImageView imageView) {
		super(imageView);
		this.imageView = imageView;
		setOnScaleChangeListener((scaleFactor, focusX, focusY) -> {
			cumulatedScale += scaleFactor - 1;
			if (cumulatedScale > 1.6) {
				isScaled = true;
			} else {
				isScaled = false;
			}
		});
	}

	@Override
	public boolean onLongClick(View v) {
		return false;
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		if (!isScaled) {
			switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					Timber.v("DOWN");
					action = ACTION_DRAG;
					startY = motionEvent.getY();
					cumulatedDy = 0;
					realDy = 0;

					if (moveAnimationY != null) {
						if (moveAnimationY.canSkipToEnd()) {
							moveAnimationY.skipToEnd();
						} else {
							moveAnimationY.cancel();
						}
					}
					break;
				case MotionEvent.ACTION_MOVE:
					if (action == ACTION_DRAG) {
						realDy = motionEvent.getY() - startY;
						cumulatedDy += realDy;
						cumulatedDy = (float) (k * Math.atan(cumulatedDy / k));
						imageView.setTranslationY(cumulatedDy);
					}
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					Timber.v("ZOOM");
					action = ACTION_ZOOM;
					break;
				case MotionEvent.ACTION_POINTER_UP:
					Timber.v("DRAG");
					action = ACTION_NONE;
					break;
				case MotionEvent.ACTION_UP:
					Timber.v("UP");
					moveAnimationY = new SpringAnimation(imageView, DynamicAnimation.TRANSLATION_Y, 0);
					moveAnimationY.getSpring().setStiffness(SpringForce.STIFFNESS_LOW)
							.setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
					moveAnimationY.start();

					if (cumulatedDy < -TOP_THRESHOLD) {
						if (onThresholdListener != null) {
							onThresholdListener.onTopThreshold();
						}
					} else if (cumulatedDy > BOTTOM_THRESHOLD) {
						if (onThresholdListener != null) {
							onThresholdListener.onBottomThreshold();
						}
					}
					break;
			}
		}
		return super.onTouch(view, motionEvent);
	}

	public void setOnThresholdListener(ThresholdListener onThresholdListener) {
		this.onThresholdListener = onThresholdListener;
	}
}
