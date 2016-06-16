package app.drool.respite.asyncloaders;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.lang.ref.WeakReference;

/**
 * Created by drool on 6/16/16.
 */

public class AsyncDrawableURL extends BitmapDrawable {
    private final WeakReference<PreviewFromURLTask> previewFromURLTaskReference;

    public AsyncDrawableURL(Resources res, Bitmap bm, PreviewFromURLTask task) {
        super(res, bm);
        this.previewFromURLTaskReference = new WeakReference<>(task);
    }

    public PreviewFromURLTask getPreviewFromURLTask() {
        return previewFromURLTaskReference.get();
    }
}
