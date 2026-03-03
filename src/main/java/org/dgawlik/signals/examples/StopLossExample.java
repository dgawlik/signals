package org.dgawlik.signals.examples;

import org.dgawlik.signals.Frequency;
import org.dgawlik.signals.Simulation;
import org.dgawlik.signals.indicator.counters.TrailingStopLoss;
import org.dgawlik.signals.indicator.extensions.EMA;

public class StopLossExample {

    public static void main(String[] args) {
        var finalPortfolio = Simulation.forInstruments(Frequency.ONE_DAY, "AAPL", "TSLA")
                .withPortfolio(1_000_000, 2.5)
                .withIndicators(new EMA("AAPL", 7), new EMA("AAPL", 24), new EMA("TSLA", 7), new EMA("TSLA", 24))
                .run(() -> {

                    var isAaplOn = new Boolean[] { false };
                    var isTslaOn = new Boolean[] { false };

                    var slAapl = new TrailingStopLoss();
                    var slTsla = new TrailingStopLoss();

                    return (portfolio, lookbehind) -> {
                        if (lookbehind.size() > 1) {
                            var prevQuote = lookbehind.get(lookbehind.size() - 2);
                            var quote = lookbehind.getLast();

                            var prevEma7Aapl = prevQuote.getIndicator("EMA7.AAPL").val1();
                            var ema7Aapl = quote.getIndicator("EMA7.AAPL").val1();

                            var prevEma24Aapl = prevQuote.getIndicator("EMA24.AAPL").val1();
                            var ema24Aapl = quote.getIndicator("EMA24.AAPL").val1();

                            var prevEma7Tsla = prevQuote.getIndicator("EMA7.TSLA").val1();
                            var ema7Tsla = quote.getIndicator("EMA7.TSLA").val1();

                            var prevEma24Tsla = prevQuote.getIndicator("EMA24.TSLA").val1();
                            var ema24Tsla = quote.getIndicator("EMA24.TSLA").val1();

                            try {
                                if (prevEma7Aapl < prevEma24Aapl && ema7Aapl > ema24Aapl) {
                                    var cash = portfolio.currentValuation().cash();
                                    portfolio.ops(quote).buy("AAPL", cash / 2 - 10_000).commit();
                                    isAaplOn[0] = true;
                                    slAapl.setUp(0.05, quote.getCandle("AAPL").close());
                                }

                                if (prevEma7Tsla < prevEma24Tsla && ema7Tsla > ema24Tsla) {
                                    var restOfCash = portfolio.currentValuation().cash();
                                    portfolio.ops(quote).buy("TSLA", restOfCash / 2 - 10_000).commit();
                                    isTslaOn[0] = true;
                                    slTsla.setUp(0.05, quote.getCandle("TSLA").close());
                                }

                                if (isAaplOn[0]) {
                                    if (slAapl.isHit(quote.getCandle("AAPL").close())) {
                                        portfolio.ops(quote).close("AAPL").commit();
                                        isAaplOn[0] = false;
                                    }
                                }

                                if (isTslaOn[0]) {
                                    if (slTsla.isHit(quote.getCandle("TSLA").close())) {
                                        portfolio.ops(quote).close("TSLA").commit();
                                        isTslaOn[0] = false;
                                    }
                                }

                            } catch (Exception e) {
                                System.err.println(e.getMessage());
                            }
                        }

                    };
                }).getResult();

        System.out.println(finalPortfolio.currentValuation().totalValue());
    }
}
