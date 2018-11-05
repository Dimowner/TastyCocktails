package com.dimowner.tastycocktails.cocktails.details;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dimowner.tastycocktails.AppConstants;
import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.data.Prefs;
import com.dimowner.tastycocktails.util.AnimationUtil;
import com.dimowner.tastycocktails.widget.ThresholdListener;
import com.dimowner.tastycocktails.widget.photoview.MyPhotoAttacher;
import com.github.chrisbanes.photoview.PhotoView;

import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.util.AndroidUtils;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * Created on 27.09.2017.
 * @author Dimowner
 */
public class ImagePreviewActivity extends AppCompatActivity {

	public static final String EXTRAS_KEY_IMAGE_PATH = "image_path";

	private FrameLayout container;
	private TextView txtInstructions;
	private int navigationBarHeight = 0;

	@Inject
	Prefs prefs;

	private Disposable disposable = null;

	public static Intent getStartIntent(Context context, String path) {
		Intent intent = new Intent(context, ImagePreviewActivity.class);
		intent.putExtra(ImagePreviewActivity.EXTRAS_KEY_IMAGE_PATH, path);
		return intent;
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_preview);

		TCApplication.get(getApplicationContext()).applicationComponent().inject(this);

		container = findViewById(R.id.container);
		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setNavigationIcon(R.drawable.round_arrow_back);

		PhotoView photoView = findViewById(R.id.photo_view);
		photoView.setImageResource(R.drawable.loadscreen_new);

		MyPhotoAttacher attacher = new MyPhotoAttacher(photoView);
		attacher.setOnThresholdListener(new ThresholdListener() {
			@Override
			public void onTopThreshold() {
				finishActivity();
				prefs.setShowImagePreviewInstructions(false);
			}
			@Override
			public void onBottomThreshold() {
				finishActivity();
				prefs.setShowImagePreviewInstructions(false);
			}
			@Override public void onTouchDown() { }
			@Override public void onTouchUp() { }
		});

		Glide.with(getApplicationContext())
				.load(getIntent().getStringExtra(EXTRAS_KEY_IMAGE_PATH))
				.into(photoView);

		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setTitle("");
		}

		if (prefs.isShowImagePreviewInstructions()) {
			txtInstructions = findViewById(R.id.txtInstructions);
			disposable = Completable.complete().delay(AppConstants.SHOW_INSTRUCTIONS_DELAY_MILLS, TimeUnit.MILLISECONDS)
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(
							() -> {
								txtInstructions.setVisibility(View.VISIBLE);
								txtInstructions.setTranslationY(500);// Here should be instructions panel height.
								AnimationUtil.verticalSpringAnimation(txtInstructions, -navigationBarHeight);
								txtInstructions.setOnClickListener(v ->
										AnimationUtil.verticalSpringAnimation(
												txtInstructions,
												txtInstructions.getHeight(),
												(animation, canceled, value, velocity) -> {
													txtInstructions.setVisibility(View.GONE);
													prefs.setShowImagePreviewInstructions(false);
												}));
							}
					);
		}

		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// Set the padding to match the Status Bar height
			toolbar.setPadding(0, AndroidUtils.getStatusBarHeight(getApplicationContext()), 0, 0);
			int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
			if (resourceId > 0) {
				navigationBarHeight = getResources().getDimensionPixelSize(resourceId);
			}
		}

//		AnimationUtil.physBasedRevealAnimation(toolbar.getChildAt(0));
		AndroidUtils.transparentNavigationBar(this);
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			setupWindowAnimations();
		}
	}

	@TargetApi(21)
	private void setupWindowAnimations() {
		Transition slide = TransitionInflater.from(this).inflateTransition(R.transition.slide_from_bottom);
		getWindow().setEnterTransition(slide);

	}

	private void finishActivity() {
		container.setBackgroundResource(android.R.color.transparent);
		if (AndroidUtils.isAndroid5()) {
			finishAfterTransition();
		} else {
			finish();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finishActivity();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (disposable != null) {
			disposable.dispose();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		container.setBackgroundResource(android.R.color.transparent);
	}
}
