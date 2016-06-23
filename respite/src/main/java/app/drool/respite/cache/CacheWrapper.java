package app.drool.respite.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by drool on 6/16/16.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class CacheWrapper {
    private static final String TAG = "CacheWrapper.java";

    public static void addPreview(File cacheDir, String submissionID, Bitmap preview) {
        if (hasPreview(cacheDir, submissionID))
            return;

        final String filename = submissionID + "_preview";
        try {
            File cacheFile = new File(cacheDir, filename);
            FileOutputStream outputStream = new FileOutputStream(cacheFile);
            preview.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "addPreview: I/O Exception when trying to cache submission", e);
        }
    }

    public static boolean hasPreview(File cacheDir, String submissionID) {
        final String filename = submissionID + "_preview";
        if (cacheDir.list() == null)
            return false;

        File cachedFile = new File(cacheDir, filename);
        return cachedFile.exists() && cachedFile.isFile();
    }

    public static Bitmap getPreview(File cacheDir, String submissionID) {
        final String filename = submissionID + "_preview";
        File cacheFile = new File(cacheDir, filename);
        if (hasPreview(cacheDir, submissionID)) {
            return BitmapFactory.decodeFile(cacheFile.getAbsolutePath());
        }
        return null;
    }

    public static void clearPreviewCache(File cacheDir) {
        if (cacheDir.exists() && cacheDir.isDirectory()) {
            for (File f : cacheDir.listFiles()) {
                if (f.isDirectory())
                    deleteDir(f);
                else
                    //noinspection ResultOfMethodCallIgnored
                    f.delete();
            }
        }
    }

    private static void deleteDir(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                if (f.isDirectory())
                    deleteDir(f);
                else
                    //noinspection ResultOfMethodCallIgnored
                    f.delete();
            }
        }
    }
}
