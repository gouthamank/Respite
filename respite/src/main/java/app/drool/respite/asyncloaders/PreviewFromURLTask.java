package app.drool.respite.asyncloaders;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;

import app.drool.respite.cache.CacheWrapper;
import app.drool.respite.utils.Utilities;

/**
 * Created by drool on 6/16/16.
 */

public class PreviewFromURLTask extends AsyncTask<Void, Void, Bitmap> {
    public final String submissionID;
    private final WeakReference<ImageView> weakReference;
    private final File cacheDir;
    private final String thumbnailLocation;

    public PreviewFromURLTask(File cacheDir, String submissionID, ImageView preview, String thumbnailLocation) {
        this.cacheDir = cacheDir;
        this.submissionID = submissionID;
        this.weakReference = new WeakReference<>(preview);
        this.thumbnailLocation = thumbnailLocation;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            URL thumbnailURL = new URL(thumbnailLocation);
            return BitmapFactory.decodeStream(thumbnailURL.openStream());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled())
            bitmap = null;

        if (weakReference != null && bitmap != null) {
            final ImageView preview = weakReference.get();
            final PreviewFromURLTask previewFromURLTask = Utilities.getPreviewFromURLTask(preview);
            if (this == previewFromURLTask && preview != null) {
                weakReference.get().setImageBitmap(bitmap);
                weakReference.get().setBackgroundResource(android.R.color.transparent);
                CacheWrapper.addPreview(cacheDir, submissionID, bitmap);
            }
        }
    }
}