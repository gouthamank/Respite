package app.drool.respite.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageview);

        url = getIntent().getStringExtra("url");

        progressBar = (ProgressBar) findViewById(R.id.activity_imageview_progressbar);
        scaleImageView = (SubsamplingScaleImageView) findViewById(R.id.activity_imageview_image);
        scaleImageView.setBackgroundResource(android.R.color.transparent);

        scaleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        loadImageByUrl(url);
    }


    private void loadImageByUrl(final String url) {
        final OkHttpClient client = new OkHttpClient();
        final Picasso picasso = new Picasso.Builder(ImageViewActivity.this).downloader(new OkHttp3Downloader(client)).build();

        scaleImageView.setBitmapDecoderFactory(new DecoderFactory<ImageDecoder>() {
            public ImageDecoder make() {
                return new PicassoDecoder(url, picasso);
            }});

        scaleImageView.setRegionDecoderFactory(new DecoderFactory<ImageRegionDecoder>() {
            @Override
            public ImageRegionDecoder make() throws IllegalAccessException, InstantiationException {
                return new PicassoRegionDecoder(client);
            }
        });

        scaleImageView.setImage(ImageSource.uri(url));

    }
}
