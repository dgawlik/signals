package org.dgawlik.signals.examples;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dgawlik.signals.Frequency;
import org.dgawlik.signals.Quote;
import org.dgawlik.signals.Simulation;
import org.dgawlik.signals.indicator.counters.TrailingStopLoss;
import org.dgawlik.signals.indicator.extensions.EMA;
import org.dgawlik.signals.portfolio.Portfolio;
import org.dgawlik.signals.strategies.Strategy;

public class StopLossExample {

    public static void main(String[] args) {
        var finalPortfolio = Simulation.forInstruments(Frequency.ONE_DAY, "AAPL", "TSLA", "GOOG", "MSFT")
                .withPortfolio(100_000, 2.5)
                .withIndicators(
                        new EMA("AAPL", 7),
                        new EMA("AAPL", 24),
                        new EMA("TSLA", 7),
                        new EMA("TSLA", 24),
                        new EMA("GOOG", 7),
                        new EMA("GOOG", 24),
                        new EMA("MSFT", 7),
                        new EMA("MSFT", 24))
                .run(() -> {

                    class MACDAndStopLossStrategy extends Strategy {

                        private final String symbol;
                        private final List<String> allSymbols;
                        private TrailingStopLoss sl;

                        public MACDAndStopLossStrategy(String symbol, List<String> allSymbols) {
                            this.symbol = symbol;
                            this.allSymbols = allSymbols;
                        }

                        @Override
                        public void activateOn(List<Quote> lookbehind) {

                            if (lookbehind.size() > 1) {
                                var prevQuote = lookbehind.get(lookbehind.size() - 2);
                                var quote = lookbehind.getLast();

                                var prevEma7 = prevQuote.getIndicator("EMA7." + symbol).val1();
                                var ema7 = quote.getIndicator("EMA7." + symbol).val1();

                                var prevEma24 = prevQuote.getIndicator("EMA24." + symbol).val1();
                                var ema24 = quote.getIndicator("EMA24." + symbol).val1();

                                if (prevEma7 < prevEma24 && ema7 > ema24) {
                                    active = true;
                                    sl = new TrailingStopLoss();
                                    sl.setUp(0.05, quote.getCandle(symbol).close());
                                }
                            }
                        }

                        @Override
                        public void deactivateOn(Portfolio portfolio, List<Quote> lookbehind) {
                            var quote = lookbehind.getLast();

                            if (sl.isHit(quote.getCandle(symbol).close())) {
                                portfolio.ops(quote).close(symbol).commit();
                                active = false;
                            }
                        }

                        @Override
                        public void act(Portfolio portfolio, List<Quote> lookbehind) {
                            var numPositions = portfolio.currentValuation().positions().size();
                            var quote = lookbehind.getLast();

                            if (numPositions == 0) {
                                portfolio.ops(quote).rebalance(Map.of(symbol, 0.9)).commit();
                            } else {
                                portfolio.ops(quote)
                                        .rebalanceEqualWeights(0.95, allSymbols)
                                        .commit();
                            }
                        }
                    }

                    var strategyAapl = new MACDAndStopLossStrategy("AAPL", List.of("AAPL", "TSLA", "GOOG", "MSFT"));
                    var strategyTsla = new MACDAndStopLossStrategy("TSLA", List.of("AAPL", "TSLA", "GOOG", "MSFT"));
                    var strategyGoog = new MACDAndStopLossStrategy("GOOG", List.of("AAPL", "TSLA", "GOOG", "MSFT"));
                    var strategyMsft = new MACDAndStopLossStrategy("MSFT", List.of("AAPL", "TSLA", "GOOG", "MSFT"));

                    return (portfolio, lookbehind) -> {
                        strategyAapl.step(portfolio, lookbehind);
                        strategyTsla.step(portfolio, lookbehind);
                        strategyGoog.step(portfolio, lookbehind);
                        strategyMsft.step(portfolio, lookbehind);
                    };
                }).getResult();

        System.out.println(finalPortfolio.currentValuation().totalValue());
    }
}
