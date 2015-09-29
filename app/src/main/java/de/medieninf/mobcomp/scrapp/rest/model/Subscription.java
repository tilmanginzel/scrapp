package de.medieninf.mobcomp.scrapp.rest.model;

import java.util.Date;

/**
 * POJO for Subscription.
 */
public class Subscription {
    private Date startTime = new Date();
    private int interval = -1;

    public Subscription() {
    }

    public Subscription(Subscription r) {
        this.startTime = r.startTime;
        this.interval = r.interval;
    }

    public int getInterval() { return interval; }

    public void setInterval(int interval) { this.interval = interval; }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
}
