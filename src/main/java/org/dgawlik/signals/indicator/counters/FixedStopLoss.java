package org.dgawlik.signals.indicator.counters;

public class FixedStopLoss {

    private double stopLoss;

    public void setUp(double stopLoss) {
        this.stopLoss = stopLoss;
    }

    public boolean isHit(double currentPrice) {
        return currentPrice <= stopLoss;
    }
}
