package org.dgawlik.signals.indicator.extensions;

import java.util.List;
import java.util.stream.DoubleStream;

import org.dgawlik.signals.Event;
import org.dgawlik.signals.Quote;
import org.dgawlik.signals.indicator.Series;

public class EMA extends Series {

    private final String forSymbol;

    public EMA(String forSymbol, int maxLookBehind) {
        super(maxLookBehind);
        this.forSymbol = forSymbol;
    }

    @Override
    public Event process(List<Quote> lookbehind) throws RuntimeException {

        if (lookbehind.size() == 1) {
            var candle = lookbehind.get(0).getCandle(forSymbol);
            return new Event.Indicator(forSymbol, "EMA" + maxLookBehind + "." + forSymbol, candle.time(),
                    candle.close(), Double.NaN, Double.NaN, Double.NaN);
        } else {
            var prevCloses = lookbehind.subList(0, lookbehind.size() - 1)
                    .stream()
                    .map(q -> q.getCandle(forSymbol))
                    .mapToDouble(Event.Candle::close)
                    .toArray();

            var prevEma = DoubleStream.of(prevCloses).average().orElseThrow();

            var currentClose = lookbehind.get(lookbehind.size() - 1).getCandle(forSymbol).close();

            var ema = (prevEma * DoubleStream.of(prevCloses).count() + currentClose)
                    / (DoubleStream.of(prevCloses).count() + 1);

            return new Event.Indicator(forSymbol, "EMA" + maxLookBehind + "." + forSymbol,
                    lookbehind.get(lookbehind.size() - 1).getCandle(forSymbol).time(), ema, Double.NaN, Double.NaN,
                    Double.NaN);
        }

    }
}
