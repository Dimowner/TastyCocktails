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

package task.softermii.tastycocktails;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import task.softermii.tastycocktails.login.LoginActivity;

import static task.softermii.tastycocktails.util.AndroidUtils.dpToPx;

public class UserDetailsActivity extends AppCompatActivity {

	public static final String EXTRAS_KEY_USER_ID = "user_id";

	private AccessTokenTracker accessTokenTracker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.AppTheme);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_info);

		Button button = (Button) findViewById(R.id.btn_logout);
		button.setOnClickListener(v -> LoginManager.getInstance().logOut());

		Profile profile = Profile.getCurrentProfile();
		if (profile != null) {
			updateUserInfo(profile);
		}
	}

	private void updateUserInfo(Profile profile) {
		TextView userName = (TextView) findViewById(R.id.txt_user_name);
		ImageView userFace = (ImageView) findViewById(R.id.iv_user_face);

		userName.setText(profile.getName());

		Glide.with(UserDetailsActivity.this)
				.load(profile.getProfilePictureUri(dpToPx(200), dpToPx(200)).toString())
				.apply(RequestOptions.circleCropTransform())
				.into(userFace);
	}

	@Override
	protected void onResume() {
		super.onResume();
		accessTokenTracker = new AccessTokenTracker() {
			@Override
			protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
				if (currentAccessToken == null) {
					startLoginActivity();
				}
			}
		};
		accessTokenTracker.startTracking();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		accessTokenTracker.stopTracking();
	}

	private void startLoginActivity() {
		Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
		finish();
	}
}
