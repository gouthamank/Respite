package app.drool.respite.asyncloaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

/**
 * @ https://gist.github.com/davemorrissey/e2781ba5b966c9e95539
 * Created by gokhanbarisaker on 8/30/15.
 */


public class PicassoDecoder implements ImageDecoder
{
    private String tag;
    private Picasso picasso;

    public PicassoDecoder(String tag, Picasso picasso) {
        this.tag = tag;
        this.picasso = picasso;
    }

    @Override
    public Bitmap decode(Context context, Uri uri) throws Exception {
        return picasso
                .load(uri)
                .tag(tag)
                .config(Bitmap.Config.RGB_565)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .get();
    }
}