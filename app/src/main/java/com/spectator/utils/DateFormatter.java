package com.spectator.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DateFormatter {

    public static final Locale RUSSIAN_LOCALE = new Locale("ru","RU");
    public static final int DATE_STYLE = DateFormat.SHORT;
    public static final int TIME_STYLE = DateFormat.MEDIUM;
    public static final String DATE_PATTERN = "dd.MM.yyyy";
    public static final String TIME_PATTERN = "HH:mm:ss";

    public static String formatDate(long timestamp) {
        return DateFormat.getDateInstance(DATE_STYLE, RUSSIAN_LOCALE).format(timestamp);
    }

    public static String formatDate(long timestamp, int dateStyle) {
        return DateFormat.getDateInstance(dateStyle, RUSSIAN_LOCALE).format(timestamp);
    }

    public static String formatDate(long timestamp, String pattern) {
        return new SimpleDateFormat(pattern, RUSSIAN_LOCALE).format(timestamp);
    }

    public static String formatDate(long timestamp, String pattern, Locale locale) {
        return new SimpleDateFormat(pattern, locale).format(timestamp);
    }

    public static String formatDateDefaultPattern(long timestamp) {
        return new SimpleDateFormat(DATE_PATTERN, RUSSIAN_LOCALE).format(timestamp);
    }

    public static String formatTime(long timestamp) {
        return DateFormat.getTimeInstance(TIME_STYLE, RUSSIAN_LOCALE).format(timestamp);
    }

    public static String formatTime(long timestamp, int timeStyle) {
        return DateFormat.getTimeInstance(timeStyle, RUSSIAN_LOCALE).format(timestamp);
    }

    public static String formatTime(long timestamp, String pattern) {
        return new SimpleDateFormat(pattern, RUSSIAN_LOCALE).format(timestamp);
    }

    public static String formatTime(long timestamp, String pattern, Locale locale) {
        return new SimpleDateFormat(pattern, locale).format(timestamp);
    }

    public static String formatTimeDefaultPattern(long timestamp) {
        return new SimpleDateFormat(TIME_PATTERN, RUSSIAN_LOCALE).format(timestamp);
    }

}
