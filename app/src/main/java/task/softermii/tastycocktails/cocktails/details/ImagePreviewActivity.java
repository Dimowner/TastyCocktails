package task.softermii.tastycocktails.cocktails.details;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import task.softermii.tastycocktails.R;
import task.softermii.tastycocktails.util.AndroidUtils;
import task.softermii.tastycocktails.util.AnimationUtil;

/**
 * Created on 27.09.2017.
 * @author Dimowner
 */
public class ImagePreviewActivity extends AppCompatActivity {

	public static final String EXTRAS_KEY_IMAGE_PATH = "image_path";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_preview);

		Toolbar toolbar = findViewById(R.id.toolbar);

		PhotoView photoView = findViewById(R.id.photo_view);
		photoView.setImageResource(R.drawable.loadscreen4);

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

		AnimationUtil.physBasedRevealAnimation(toolbar.getChildAt(0));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (AndroidUtils.isAndroid5()) {
					finishAfterTransition();
				} else {
					finish();
				}
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
