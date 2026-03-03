package org.dgawlik.signals.indicator.counters;

public class TrailingStopLoss {

    private double stopLossPercent;
    private double stopLoss;
    private double currentPrice;

    public void setUp(double stopLossPercent, double currentPrice) {
        this.stopLossPercent = stopLossPercent;
        this.currentPrice = currentPrice;
        this.stopLoss = currentPrice * (1 - stopLossPercent);
    }

    private void update(double currentPrice) {
        if (this.currentPrice < currentPrice) {
            stopLoss = currentPrice * (1 - stopLossPercent);
            this.currentPrice = currentPrice;
        }
    }

    public boolean isHit(double currentPrice) {
        update(currentPrice);
        return currentPrice <= stopLoss;
    }

}
