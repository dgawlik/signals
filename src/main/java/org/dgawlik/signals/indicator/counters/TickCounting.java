package org.dgawlik.signals.indicator.counters;

public class TickCounting implements Counter {

    private int count;

    public TickCounting() {
        this.count = 0;
    }

    public void add(double value) {
        count++;
    }

    public double getValue() {
        return count;
    }

    public void reset() {
        count = 0;
    }

    public boolean isReady() {
        return true;
    }

}
