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
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;
import com.dimowner.tastycocktails.TCApplication;
import com.dimowner.tastycocktails.analytics.MixPanel;
import com.dimowner.tastycocktails.widget.ThresholdListener;
import com.dimowner.tastycocktails.widget.photoview.MyPhotoAttacher;
import com.github.chrisbanes.photoview.PhotoView;

import com.dimowner.tastycocktails.R;
import com.dimowner.tastycocktails.util.AndroidUtils;
import com.github.chrisbanes.photoview.PhotoViewAttacher;

/**
 * Created on 27.09.2017.
 * @author Dimowner
 */
public class ImagePreviewActivity extends AppCompatActivity {

	public static final String EXTRAS_KEY_IMAGE_PATH = "image_path";

	private FrameLayout container;

	public static Intent getStartIntent(Context context, String path) {
		Intent intent = new Intent(context, ImagePreviewActivity.class);
		intent.putExtra(ImagePreviewActivity.EXTRAS_KEY_IMAGE_PATH, path);
		return intent;
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_preview);

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
			}
			@Override
			public void onBottomThreshold() {
				finishActivity();
			}
		});
		//Way to set custom attacher to PhotoView
		PhotoViewAttacher a = photoView.getAttacher();
		a = attacher;

		Glide.with(getApplicationContext())
				.load(getIntent().getStringExtra(EXTRAS_KEY_IMAGE_PATH))
				.into(photoView);

		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setTitle("");
		}

		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// Set the padding to match the Status Bar height
			toolbar.setPadding(0, AndroidUtils.getStatusBarHeight(getApplicationContext()), 0, 0);
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
	public void onBackPressed() {
		super.onBackPressed();
		container.setBackgroundResource(android.R.color.transparent);
	}
}
