package com.dimowner.tastycocktails.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

//import com.facebook.AccessToken;
//import com.facebook.CallbackManager;
//import com.facebook.FacebookCallback;
//import com.facebook.FacebookException;
//import com.facebook.Profile;
//import com.facebook.login.LoginResult;
//import com.facebook.login.widget.LoginButton;

import java.util.Arrays;

import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.cocktails.CocktailsActivity;
import com.dimowner.tastycocktails.util.UIUtil;
import timber.log.Timber;

/**
 * Created on 26.07.2017.
 * @author Dimowner
 */
public class LoginActivity extends AppCompatActivity {

//	CallbackManager callbackManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

//		callbackManager = CallbackManager.Factory.create();
//		LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
//		loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday"));
//		loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//
//			@Override
//			public void onSuccess(LoginResult loginResult) {
//				startCocktailsActivity();
//			}
//
//			@Override
//			public void onCancel() {
//			}
//
//			@Override
//			public void onError(FacebookException e) {
//				Timber.e(e);
//				UIUtil.showWarningDialog(LoginActivity.this, R.string.msg_error_on_login);
//			}
//		});
//
//		if (isLogged()) {
//			startCocktailsActivity();
//		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
//		callbackManager.onActivityResult(requestCode, resultCode, data);
	}

	public void startCocktailsActivity() {
		Intent intent = new Intent(getApplicationContext(), CocktailsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

//	private boolean isLogged() {
//		return Profile.getCurrentProfile() != null && AccessToken.getCurrentAccessToken() != null;
//	}
}
