package app.drool.respite.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.decoder.DecoderFactory;
import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder;
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import app.drool.respite.R;
import app.drool.respite.asyncloaders.PicassoDecoder;
import app.drool.respite.asyncloaders.PicassoRegionDecoder;
import okhttp3.OkHttpClient;


/**
 * Created by drool on 7/6/16.
 */

public class ImageViewActivity extends AppCompatActivity {
    private String url;


    private ProgressBar progressBar;
    private SubsamplingScaleImageView scaleImageView;
    private OkHttpClient client;
    private Picasso picasso;

    private boolean isSystemUIShown;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageview);

        url = getIntent().getStringExtra("url");

        client = new OkHttpClient();
        picasso = new Picasso.Builder(ImageViewActivity.this).downloader(new OkHttp3Downloader(client)).build();
        progressBar = (ProgressBar) findViewById(R.id.activity_imageview_progressbar); // Current am not doing anything with this.
        scaleImageView = (SubsamplingScaleImageView) findViewById(R.id.activity_imageview_image);
        scaleImageView.setBackgroundResource(android.R.color.transparent);

        scaleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSystemUIShown)
                    hideUI();
                else {
                    picasso.cancelTag(url);
                    finish();
                }
            }
        });

        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                isSystemUIShown = (visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0;
            }
        });

        getSupportActionBar().setTitle("");
        hideUI();
        loadImageByUrl(url);


    }


    private void loadImageByUrl(final String url) {

        scaleImageView.setBitmapDecoderFactory(new DecoderFactory<ImageDecoder>() {
            public ImageDecoder make() {
                return new PicassoDecoder(url, picasso);
            }
        });

        scaleImageView.setRegionDecoderFactory(new DecoderFactory<ImageRegionDecoder>() {
            @Override
            public ImageRegionDecoder make() throws IllegalAccessException, InstantiationException {
                return new PicassoRegionDecoder(client);
            }
        });

        scaleImageView.setImage(ImageSource.uri(url));

    }

    private void hideUI() {
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
        } else {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_imageview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_imageview_openexternal:
                try {
                    final Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Toast.makeText(this, "Could not launch external browser", Toast.LENGTH_LONG).show();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
