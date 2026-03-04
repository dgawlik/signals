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
        var finalPortfolio = Simulation.forInstruments(Frequency.ONE_DAY, "AAPL", "TSLA")
                .withPortfolio(100_000, 2.5)
                .withIndicators(new EMA("AAPL", 7), new EMA("AAPL", 24), new EMA("TSLA", 7), new EMA("TSLA", 24))
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

                            var count = allSymbols.size();
                            var weight = (1.0 / count) * 0.9;

                            var rebalanceMap = new HashMap<String, Double>();
                            for (String s : allSymbols) {
                                rebalanceMap.put(s, weight);
                            }

                            if (numPositions == 0) {
                                portfolio.ops(quote).rebalance(Map.of(symbol, 0.9)).commit();
                            } else {
                                portfolio.ops(quote)
                                        .rebalance(rebalanceMap)
                                        .commit();
                            }
                        }
                    }

                    var strategyAapl = new MACDAndStopLossStrategy("AAPL", List.of("AAPL", "TSLA"));
                    var strategyTsla = new MACDAndStopLossStrategy("TSLA", List.of("AAPL", "TSLA"));

                    return (portfolio, lookbehind) -> {
                        strategyAapl.step(portfolio, lookbehind);
                        strategyTsla.step(portfolio, lookbehind);
                    };
                }).getResult();

        System.out.println(finalPortfolio.currentValuation().totalValue());
    }
}
