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

package task.softermii.tastycocktails.util;
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
}
