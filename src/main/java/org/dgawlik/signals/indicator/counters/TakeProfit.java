package org.dgawlik.signals.indicator.counters;

public class TakeProfit {
    private double takeProfit;

    public void setUp(double takeProfit) {
        this.takeProfit = takeProfit;
    }

    public boolean isHit(double currentPrice) {
        return currentPrice >= takeProfit;
    }
}
