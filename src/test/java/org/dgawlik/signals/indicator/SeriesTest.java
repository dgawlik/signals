package org.dgawlik.signals.indicator;

import java.time.LocalDateTime;
import java.util.List;

import org.dgawlik.signals.Event;
import org.dgawlik.signals.Frequency;
import org.dgawlik.signals.Quote;
import org.dgawlik.signals.SeriesAggregator;
import org.dgawlik.signals.SymbolEvents;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SeriesTest {

    private static Event.Candle candle(String symbol, double close, LocalDateTime time) {
        return new Event.Candle(symbol, time, close, close, close, close, 1);
    }

    public static class ScanSeries extends Series {

        public ScanSeries(int maxLookBehind) {
            super(maxLookBehind);
        }

        private double counter = 0;

        @Override
        public Event process(List<Quote> lookbehind) throws RuntimeException {
            if (lookbehind.size() == 1) {
                counter = lookbehind.getLast().getCandle("TEST").close();
                throw new LogContinueException("First candle");
            } else {
                counter += lookbehind.getLast().getCandle("TEST").close();
            }
            var time = lookbehind.getLast().getCandle("TEST").time();
            return new Event.Indicator("TEST", "RESULT", time, counter, Double.NaN, Double.NaN, Double.NaN);
        }

    }

    @Test
    public void test_scan_series() {
        // Given
        var time = LocalDateTime.now();
        var quotes = SeriesAggregator.create(0, Frequency.TICK)
                .addEvents(
                        new SymbolEvents(List.of(candle("TEST", 100, time), candle("TEST", 200, time.plusSeconds(1)))))
                .convertToQuotes();

        var series = new ScanSeries(5);

        var result = series.get(quotes);

        // Then
        Assertions.assertEquals(300.0, ((Event.Indicator) result.events().getLast()).val1());
    }

}
