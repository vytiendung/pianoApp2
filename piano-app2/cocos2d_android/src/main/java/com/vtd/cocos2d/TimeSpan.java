package com.vtd.cocos2d;

/**
 * Created by lag_nao on 9/24/2015.
 */
public class TimeSpan implements Comparable {
    private long fMilliseconds;

    public TimeSpan(long days, long hours, long minutes, long seconds, long milliseconds) {
        this.fMilliseconds = milliseconds;
        this.fMilliseconds += seconds * 1000L;
        this.fMilliseconds += minutes * 60000L;
        this.fMilliseconds += hours * 3600000L;
        this.fMilliseconds += days * 86400000L;
    }

    public int getDays() {
        return (int) (this.fMilliseconds / 86400000L);
    }

    public int getHours() {
        return (int) (this.fMilliseconds / 3600000L) % 24;
    }

    public int getMinutes() {
        return (int) (this.fMilliseconds / 60000L) % 60;
    }

    public int getSeconds() {
        return (int) (this.fMilliseconds / 1000L) % 60;
    }

    public int getMilliseconds() {
        return (int) (this.fMilliseconds % 1000L);
    }

    public double getTotalDays() {
        return this.fMilliseconds / 86400000.0D;
    }

    public double getTotalHours() {
        return this.fMilliseconds / 3600000.0D;
    }

    public double getTotalMinutes() {
        return this.fMilliseconds / 60000.0D;
    }

    public double getTotalSeconds() {
        return this.fMilliseconds / 1000.0D;
    }

    public long getTotalMilliseconds() {
        return this.fMilliseconds;
    }

    public boolean equals(TimeSpan value) {
        if (value == null) {
            return false;
        }
        return this.fMilliseconds == value.fMilliseconds;
    }

    public boolean equals(Object value) {
        if (!(value instanceof TimeSpan)) {
            return false;
        }
        return equals((TimeSpan) value);
    }

    public int compareTo(TimeSpan value) {
        if (value == null) {
            return 1;
        }

        long result = this.fMilliseconds - value.fMilliseconds;
        if (result < 0L) {
            return -1;
        }
        if (result == 0L) {
            return 0;
        }
        return 1;
    }

    public int compareTo(Object value) {
        if (!(value instanceof TimeSpan)) {
            return 1;
        }

        return compareTo((TimeSpan) value);
    }

    public int hashCode() {
        return (int) (this.fMilliseconds ^ this.fMilliseconds >>> 32);
    }

    public static TimeSpan fromMilliseconds(long value) {
        return new TimeSpan(0L, 0L, 0L, 0L, value);
    }

    public static TimeSpan fromSeconds(long value) {
        return new TimeSpan(0L, 0L, 0L, value, 0L);
    }

    public static TimeSpan fromMinutes(long value) {
        return new TimeSpan(0L, 0L, value, 0L, 0L);
    }
}
