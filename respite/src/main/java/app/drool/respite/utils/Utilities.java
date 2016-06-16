package app.drool.respite.utils;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import java.util.Date;

import app.drool.respite.asyncloaders.AsyncDrawableCache;
import app.drool.respite.asyncloaders.AsyncDrawableURL;
import app.drool.respite.asyncloaders.PreviewFromCacheTask;
import app.drool.respite.asyncloaders.PreviewFromURLTask;

/**
 * Created by drool on 6/16/16.
 */

public class Utilities {
    public static String getReadableCreationTime(Date createdTime) {
        Date currentTime = new Date();
        long diffMs = currentTime.getTime() - createdTime.getTime();

        if(diffMs < 0)
            return "in the future";

        long diffMinutes = diffMs / (1000 * 60);
        int diffMinutesInt = (int) diffMinutes;

        if(diffMinutesInt < 2)
            return "just now";

        if(diffMinutesInt < 60)
            return String.valueOf(diffMinutesInt) + "m";

        long diffHours = (long) Math.ceil(diffMinutes / 60);
        int diffHoursInt = (int) diffHours;

        if(diffHoursInt < 24)
            return String.valueOf(diffHoursInt) + "h";

        long diffDays = (long) Math.ceil(diffHours / 24);
        int diffDaysInt = (int) diffDays;

        if(diffDaysInt < 365)
            return String.valueOf(diffDays) + "d";

        return "";
    }

    public static PreviewFromCacheTask getPreviewFromCacheTask(ImageView preview) {
        final Drawable drawable = preview.getDrawable();
        if(drawable instanceof AsyncDrawableCache) {
            final AsyncDrawableCache asyncDrawableCache = (AsyncDrawableCache) drawable;
            return asyncDrawableCache.getPreviewFromCacheTask();
        }
        return null;
    }

    public static PreviewFromURLTask getPreviewFromURLTask(ImageView preview) {
        final Drawable drawable = preview.getDrawable();
        if(drawable instanceof AsyncDrawableURL) {
            final AsyncDrawableURL asyncDrawable = (AsyncDrawableURL) drawable;
            return asyncDrawable.getPreviewFromURLTask();
        }
        return null;
    }
}
