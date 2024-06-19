package com.mrikso.anitube.app.utils;

import android.content.Context;
import android.content.res.Resources;

import com.mrikso.anitube.app.R;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class ReadableTime {

    public static final long SECOND_MILLIS = 1000;
    public static final long MINUTE_MILLIS = 60 * SECOND_MILLIS;
    public static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;
    public static final long DAY_MILLIS = 24 * HOUR_MILLIS;
    public static final long WEEK_MILLIS = 7 * DAY_MILLIS;
    public static final long YEAR_MILLIS = 365 * DAY_MILLIS;
    public static final long[] MULTIPLES = {YEAR_MILLIS, DAY_MILLIS, HOUR_MILLIS, MINUTE_MILLIS, SECOND_MILLIS};
    public static final int SIZE = 5;
    public static final int[] UNITS = {R.plurals.year, R.plurals.day, R.plurals.hour, R.plurals.minute, R.plurals.second
    };
    private static final Object sCalendarLock = new Object();
    private static final DateTimeFormatter DATE_FORMAT_WITHOUT_YEAR = DateTimeFormatter.ofPattern("MMM d");
    private static final DateTimeFormatter DATE_FORMAT_WITH_YEAR = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private static final DateTimeFormatter FILENAMABLE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS");
    private static final Object sDateFormatLock = new Object();

    public static String getTimeAgo(long time, Context context) {
        Resources resources = context.getResources();

        long now = System.currentTimeMillis();

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return resources.getString(R.string.just_now);
        } else if (diff < 2 * MINUTE_MILLIS) {
            return resources.getQuantityString(R.plurals.some_minutes_ago, 1, 1);
        } else if (diff < 50 * MINUTE_MILLIS) {
            int minutes = (int) (diff / MINUTE_MILLIS);
            return resources.getQuantityString(R.plurals.some_minutes_ago, minutes, minutes);
        } else if (diff < 90 * MINUTE_MILLIS) {
            return resources.getQuantityString(R.plurals.some_hours_ago, 1, 1);
        } else if (diff < 24 * HOUR_MILLIS) {
            int hours = (int) (diff / HOUR_MILLIS);
            return resources.getQuantityString(R.plurals.some_hours_ago, hours, hours);
        } else if (diff < 48 * HOUR_MILLIS) {
            return resources.getString(R.string.yesterday);
        } else if (diff < WEEK_MILLIS) {
            int days = (int) (diff / DAY_MILLIS);
            return resources.getQuantityString(R.plurals.some_days_ago, days, days);
        } else {
            synchronized (sCalendarLock) {
                OffsetDateTime nowDate = Instant.ofEpochMilli(now).atOffset(ZoneOffset.UTC);
                OffsetDateTime timeDate = Instant.ofEpochMilli(time).atOffset(ZoneOffset.UTC);
                int nowYear = nowDate.getYear();
                int timeYear = timeDate.getYear();

                if (nowYear == timeYear) {
                    return DATE_FORMAT_WITHOUT_YEAR.format(timeDate.toInstant().atOffset(ZoneOffset.UTC));
                } else {
                    return DATE_FORMAT_WITH_YEAR.format(timeDate.toInstant().atOffset(ZoneOffset.UTC));
                }
            }
        }
    }

    public static String getShortTimeInterval(long time, Context context) {
        StringBuilder sb = new StringBuilder();
        Resources resources = context.getResources();

        for (int i = 0; i < SIZE; i++) {
            long multiple = MULTIPLES[i];
            long quotient = time / multiple;
            if (time > multiple * 1.5 || i == SIZE - 1) {
                sb.append(quotient).append(" ").append(resources.getQuantityString(UNITS[i], (int) quotient));
                break;
            }
        }

        return sb.toString();
    }

    public static String getFilenamableTime(long time) {
        synchronized (sDateFormatLock) {
            return FILENAMABLE_DATE_FORMAT.format(Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()));
        }
    }

    public static String generateTime(long position) {
        int totalSeconds = (int) (position / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        if (hours > 0) {
            return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
        }
    }
}
