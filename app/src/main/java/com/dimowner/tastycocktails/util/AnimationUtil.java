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

package com.dimowner.tastycocktails.util;
import android.support.animation.DynamicAnimation;
import android.support.animation.FloatPropertyCompat;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.view.View;
import android.view.animation.AnimationUtils;

/**
 * Created on 30.07.2017.
 * @author Dimowner
 */
public class AnimationUtil {

	private AnimationUtil() {}

	public static void viewRevealAnimation(View view) {
		view.setAlpha(0f);
		view.setScaleX(0f);
		view.setScaleY(0f);
		view.animate()
				.alpha(1f)
				.scaleX(1f)
				.scaleY(1f)
				.translationY(0f)
				.setDuration(500L)
				.setInterpolator(AnimationUtils.loadInterpolator(view.getContext(),
						android.R.interpolator.accelerate_decelerate))
				.start();
	}

	public static void physBasedRevealAnimation(View fab) {
		fab.setScaleX(0f);
		fab.setScaleY(0f);

		FloatPropertyCompat<View> scale = new FloatPropertyCompat<View>("") {
			@Override
			public float getValue(View object) {
				return object.getScaleX();
			}

			@Override
			public void setValue(View object, float value) {
				object.setScaleX(value);
				object.setScaleY(value);
			}
		};

		SpringAnimation animation = new SpringAnimation(fab, scale, 1f);
		animation.getSpring().setStiffness(SpringForce.STIFFNESS_LOW)
				.setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
		animation.setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_ALPHA)
				.setStartVelocity(2f);

		animation.start();
	}

	public static void verticalSpringAnimation(View view, int positionY) {
		SpringAnimation animY = new SpringAnimation(view, SpringAnimation.TRANSLATION_Y, positionY);
		animY.getSpring().setStiffness(1000)
				.setDampingRatio(0.6f);
		animY.start();
	}

	public static void verticalSpringAnimation(View view, int positionY, DynamicAnimation.OnAnimationEndListener listener) {
		SpringAnimation animY = new SpringAnimation(view, SpringAnimation.TRANSLATION_Y, positionY);
		animY.addEndListener(listener);
		animY.getSpring().setStiffness(900)
				.setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY);
		animY.start();
	}

	public static void viewRotationAnimation(View view, long duration) {
		view.animate()
				.rotation(180)
				.setDuration(duration)
				.setInterpolator(AnimationUtils.loadInterpolator(view.getContext(),
						android.R.interpolator.accelerate_decelerate))
				.start();
	}

	public static void viewBackRotationAnimation(View view, long duration) {
		view.animate()
				.rotation(0)
				.setDuration(duration)
				.setInterpolator(AnimationUtils.loadInterpolator(view.getContext(),
						android.R.interpolator.accelerate_decelerate))
				.start();
	}
}
