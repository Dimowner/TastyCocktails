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

package com.dimowner.tastycocktails.widget;

import android.content.Context;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.dimowner.tastycocktails.util.AndroidUtils;

import timber.log.Timber;

public class TouchLayout extends FrameLayout {

	private static final int ACTION_NONE = -1;
	private static final int ACTION_DRAG = 1;
	private static final int ACTION_ZOOM = 2;

	private static final int MAX_MOVE = (int) AndroidUtils.dpToPx(250); //dip
	private static final int TOP_THRESHOLD = (int)(MAX_MOVE * 0.25); //dip
	private static final int BOTTOM_THRESHOLD = (int)(MAX_MOVE * 0.40); //dip

	private SpringAnimation moveAnimationY;

	private int action = ACTION_NONE;

	private float realDy = 0;
	private float cumulatedDy = 0;

	private float startY = 0f;

	private float returnPositionY = 0;

	//Converted value from pixels to coefficient used in function which describes move.
	private final float k = (float) (MAX_MOVE / (Math.PI/2));

	private ThresholdListener onThresholdListener;


	public TouchLayout(Context context) {
		super(context);
		init();
	}

	public TouchLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TouchLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		setOnTouchListener((view, motionEvent) -> {
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
					if (onThresholdListener != null) {
						onThresholdListener.onTouchDown();
					}
					break;
				case MotionEvent.ACTION_MOVE:
					if (action == ACTION_DRAG) {
						realDy = motionEvent.getY() - startY;
						cumulatedDy += realDy;
						cumulatedDy = (float) (k * Math.atan(cumulatedDy /k));
						this.setTranslationY(cumulatedDy + returnPositionY);
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
					performClick();
					moveAnimationY = new SpringAnimation(this, DynamicAnimation.TRANSLATION_Y, returnPositionY);
					moveAnimationY.getSpring().setStiffness(SpringForce.STIFFNESS_LOW)
							.setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
					moveAnimationY.start();

					if (cumulatedDy < -TOP_THRESHOLD) {
						if (onThresholdListener != null) {
							moveAnimationY.cancel();
							onThresholdListener.onTopThreshold();
						}
					} else if (cumulatedDy > BOTTOM_THRESHOLD) {
						if (onThresholdListener != null) {
							moveAnimationY.cancel();
							onThresholdListener.onBottomThreshold();
						}
					}
					if (onThresholdListener != null) {
						onThresholdListener.onTouchUp();
					}
					break;
			}
			return true;
		});
	}

	public void setReturnPositionY(float returnPositionY) {
		this.returnPositionY = returnPositionY;
	}

	public float getReturnPositionY() {
		return returnPositionY;
	}

	public void setOnThresholdListener(ThresholdListener onThresholdListener) {
		this.onThresholdListener = onThresholdListener;
	}
}
