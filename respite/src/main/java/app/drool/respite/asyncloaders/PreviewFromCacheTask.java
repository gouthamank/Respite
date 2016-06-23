package app.drool.respite.asyncloaders;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.File;
import java.lang.ref.WeakReference;

import app.drool.respite.cache.CacheWrapper;
import app.drool.respite.utils.Utilities;

/**
 * Created by drool on 6/16/16.
 */

public class PreviewFromCacheTask extends AsyncTask<Void, Void, Bitmap> {
    public final String submissionID;
    private final WeakReference<ImageView> weakReference;
    private final File cacheDir;

    public PreviewFromCacheTask(File cacheDir, String submissionID, ImageView preview) {
        this.cacheDir = cacheDir;
        this.submissionID = submissionID;
        this.weakReference = new WeakReference<>(preview);
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        return CacheWrapper.getPreview(cacheDir, submissionID);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled())
            bitmap = null;

        if (weakReference != null && bitmap != null) {
            final ImageView preview = weakReference.get();
            final PreviewFromCacheTask previewFromCacheTask = Utilities.getPreviewFromCacheTask(preview);
            if (this == previewFromCacheTask && preview != null) {
                weakReference.get().setImageBitmap(bitmap);
                weakReference.get().setBackgroundResource(android.R.color.transparent);
            }
        }
    }
}