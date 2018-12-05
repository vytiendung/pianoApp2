package com.vtd.cocos2d;

import android.os.Handler;

/**
 * Created by lag_nao on 9/24/2015.
 */
public class MessageBasedTimer {
    private Handler fHandler;
    private Runnable fRunnable;
    private Listener fListener;
    private TimeSpan fInterval;
    private Ticks fNextElapseTimeInTicks;
    private boolean fIsRunning;
    public static final TimeSpan MIN_INTERVAL = TimeSpan.fromMilliseconds(5L);

    public MessageBasedTimer() {
        this.fHandler = null;
        this.fRunnable = null;
        this.fListener = null;
        this.fInterval = TimeSpan.fromMinutes(1L);
        this.fNextElapseTimeInTicks = Ticks.fromCurrentTime();
        this.fIsRunning = false;
    }

    public void setHandler(Handler handler) {
        if (handler == this.fHandler) {
            return;
        }

        boolean wasRunning = this.fIsRunning;
        if (this.fIsRunning) {
            stop();
        }

        this.fHandler = handler;

        if ((wasRunning) && (handler != null))
            start();
    }

    public Handler getHandler() {
        return this.fHandler;
    }

    public void setListener(Listener listener) {
        this.fListener = listener;
    }

    public Listener getListener() {
        return this.fListener;
    }

    public void setInterval(TimeSpan interval) {
        if (interval == null) {
            throw new NullPointerException();
        }

        if (interval.compareTo(MIN_INTERVAL) <= 0) {
            interval = MIN_INTERVAL;
        }

        if (interval.equals(this.fInterval)) {
            return;
        }

        this.fInterval = interval;
    }

    public TimeSpan getInterval() {
        return this.fInterval;
    }

    public boolean isRunning() {
        return this.fIsRunning;
    }

    public boolean isNotRunning() {
        return !this.fIsRunning;
    }

    public void start() {
        if (this.fIsRunning) {
            return;
        }

        if (this.fHandler == null) {
            return;
        }

        this.fRunnable = new Runnable() {
            public void run() {
                MessageBasedTimer.this.onElapsed();
            }
        };
        this.fIsRunning = true;
        this.fNextElapseTimeInTicks = Ticks.fromCurrentTime().add(this.fInterval);
        this.fHandler.postDelayed(this.fRunnable, this.fInterval.getTotalMilliseconds());
    }

    public void stop() {
        if (!this.fIsRunning) {
            return;
        }

        if ((this.fHandler != null) && (this.fRunnable != null)) {
            this.fHandler.removeCallbacks(this.fRunnable);
            this.fRunnable = null;
        }
        this.fIsRunning = false;
    }

    private void onElapsed() {
        if (!this.fIsRunning) {
            return;
        }

        if (this.fListener != null) {
            this.fListener.onTimerElapsed();
        }

        if (!this.fIsRunning) {
            return;
        }

        Ticks currentTimeInTicks = Ticks.fromCurrentTime();
        do {
            this.fNextElapseTimeInTicks = this.fNextElapseTimeInTicks.add(this.fInterval);
        }while (this.fNextElapseTimeInTicks.compareTo(currentTimeInTicks) <= 0);
        long delayInMilliseconds = this.fNextElapseTimeInTicks.subtract(currentTimeInTicks).getTotalMilliseconds();
        this.fHandler.postDelayed(this.fRunnable, delayInMilliseconds);
    }

    public static abstract interface Listener {
        public abstract void onTimerElapsed();
    }
}
