package org.dgawlik.signals.indicator.counters;

public interface Counter {

    public double getValue();

    public void reset();

    public void add(double value);

    public boolean isReady();
}
