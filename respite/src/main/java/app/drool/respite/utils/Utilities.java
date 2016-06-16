package app.drool.respite.utils;

import java.util.Date;

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
}
