package com.vtd.cocos2d;

import android.os.SystemClock;

/**
 * Created by lag_nao on 9/24/2015.
 */
public class Ticks
        implements Comparable {
    private long fMilliseconds;

    public Ticks(Ticks value) {
        if (value != null)
            this.fMilliseconds = value.fMilliseconds;
    }

    private Ticks(long value) {
        this.fMilliseconds = value;
    }

    public Ticks add(TimeSpan value) {
        if (value == null) {
            return this;
        }
        return new Ticks(this.fMilliseconds + value.getTotalMilliseconds());
    }

    public Ticks addMilliseconds(long value) {
        if (value == 0L) {
            return this;
        }
        return new Ticks(this.fMilliseconds + value);
    }

    public Ticks addSeconds(long value) {
        if (value == 0L) {
            return this;
        }
        return new Ticks(this.fMilliseconds + value * 1000L);
    }

    public TimeSpan subtract(Ticks value) {
        long milliseconds = 0L;
        if (value != null) {
            milliseconds = value.fMilliseconds;
        }

        if (milliseconds == -9223372036854775808L) {
            milliseconds += 1L;
        }

        return TimeSpan.fromMilliseconds(this.fMilliseconds - milliseconds);
    }

    public boolean equals(Ticks value) {
        if (value == null) {
            return false;
        }
        return this.fMilliseconds == value.fMilliseconds;
    }

    public boolean equals(Object value) {
        if (!(value instanceof Ticks)) {
            return false;
        }
        return equals((Ticks) value);
    }

    public int compareTo(Ticks value) {
        if (value == null) {
            return 1;
        }

        long milliseconds = value.fMilliseconds;

        if (milliseconds == -9223372036854775808L) {
            milliseconds += 1L;
        }

        long result = this.fMilliseconds - milliseconds;
        if (result < 0L) {
            return -1;
        }
        if (result == 0L) {
            return 0;
        }
        return 1;
    }

    public int compareTo(Object value) {
        if (!(value instanceof Ticks)) {
            return 1;
        }

        return compareTo((Ticks) value);
    }

    public int hashCode() {
        return (int) (this.fMilliseconds ^ this.fMilliseconds >>> 32);
    }

    public String toString() {
        return Long.toString(this.fMilliseconds);
    }

    public long toLong() {
        return this.fMilliseconds;
    }

    public static Ticks fromLong(long value) {
        return new Ticks(value);
    }

    public static Ticks fromCurrentTime() {
        return new Ticks(SystemClock.elapsedRealtime());
    }
}
