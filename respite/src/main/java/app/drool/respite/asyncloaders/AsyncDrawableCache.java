package app.drool.respite.asyncloaders;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.lang.ref.WeakReference;

/**
 * Created by drool on 6/16/16.
 */

public class AsyncDrawableCache extends BitmapDrawable {
    private final WeakReference<PreviewFromCacheTask> previewFromCacheTaskReference;

    public AsyncDrawableCache(Resources res, Bitmap bm, PreviewFromCacheTask task) {
        super(res, bm);
        this.previewFromCacheTaskReference = new WeakReference<>(task);
    }

    public PreviewFromCacheTask getPreviewFromCacheTask() {
        return previewFromCacheTaskReference.get();
    }
}
