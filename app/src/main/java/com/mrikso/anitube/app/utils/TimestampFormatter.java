package com.mrikso.anitube.app.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimestampFormatter {

    public static String formatTimestamp(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        Calendar currentCalendar = Calendar.getInstance();

        SimpleDateFormat dateFormat;
        String formattedTimestamp;

        if (isToday(calendar, currentCalendar)) {
            dateFormat = new SimpleDateFormat("'сьогодні в' HH:mm", Locale.getDefault());
            formattedTimestamp = dateFormat.format(new Date(timestamp));
        } else if (isSameYear(calendar, currentCalendar)) {
            dateFormat = new SimpleDateFormat("d MMMM 'в' HH:mm", Locale.getDefault());
            formattedTimestamp = dateFormat.format(new Date(timestamp));
        } else {
            dateFormat = new SimpleDateFormat("d MMMM 'в' HH:mm", Locale.getDefault());
            formattedTimestamp = dateFormat.format(new Date(timestamp));
        }

        return formattedTimestamp;
    }

    private static boolean isToday(Calendar calendar, Calendar currentCalendar) {
        return calendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)
                && calendar.get(Calendar.DAY_OF_YEAR) == currentCalendar.get(Calendar.DAY_OF_YEAR);
    }

    private static boolean isSameYear(Calendar calendar, Calendar currentCalendar) {
        return calendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR);
    }
}
