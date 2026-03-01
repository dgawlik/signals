package org.dgawlik.signals.indicator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;

import org.dgawlik.signals.Event;
import org.dgawlik.signals.Frequency;
import org.dgawlik.signals.SeriesAggregator;
import org.dgawlik.signals.SymbolEvents;
import org.dgawlik.signals.etoro.CandlesEndpoint;
import org.junit.jupiter.api.Test;

public class SeriesAggregatorTest {

        @Test
        public void test_convert_to_quotes_same_time() {
                // Given
                var symbolEvents = List.<SymbolEvents>of(
                                new SymbolEvents(List.of(
                                                new Event.Candle("BTC", LocalDateTime.of(2022, 1, 1, 10, 0), 100, 110,
                                                                90, 105, 10))),
                                new SymbolEvents(List.of(
                                                new Event.Candle("AAPL", LocalDateTime.of(2022, 1, 1, 10, 2), 100, 110,
                                                                90, 105, 10))));

                var aggregator = SeriesAggregator.create(1, Frequency.ONE_HOUR).addEvents(symbolEvents);

                // When
                var quotes = aggregator.convertToQuotes();

                // Then
                assertEquals(1, quotes.size());
                var quote = quotes.get(0);
                assertEquals(2, quote.events().size());
                assertEquals(105, quote.getCandle("BTC").close());
                assertEquals(105, quote.getCandle("AAPL").close());
        }

        @Test
        public void test_error_on_gap_too_large() {
                // Given

                var beginning = LocalDateTime.of(2022, 1, 1, 10, 0);

                var symbolEvents = List.<SymbolEvents>of(
                                new SymbolEvents(List.of(
                                                new Event.Candle("BTC", beginning, 100, 110, 90, 105, 10),
                                                new Event.Candle("BTC", beginning.plusHours(1), 100, 110, 90, 105, 10),
                                                new Event.Candle("BTC", beginning.plusHours(2), 100, 110, 90, 105,
                                                                10))),
                                new SymbolEvents(List.of(
                                                new Event.Candle("AAPL", beginning, 100, 110, 90, 105, 10),
                                                new Event.Candle("AAPL", beginning.plusHours(2), 100, 110, 90, 105,
                                                                10))));

                var maxGap = 0;
                var aggregator = SeriesAggregator.create(maxGap, Frequency.ONE_HOUR).addEvents(symbolEvents);

                // When
                assertThrows(IllegalArgumentException.class, () -> aggregator.convertToQuotes());
        }

        @Test
        public void test_fetched_from_etoro() {
                // Given
                var endpoint = CandlesEndpoint.ETORO();
                var symbolEvents = endpoint.fetch(Frequency.ONE_DAY, 30, "AAPL", "TSLA");

                // When
                var aggregator = SeriesAggregator.create(0, Frequency.ONE_DAY).addEvents(symbolEvents);
                var quotes = aggregator.convertToQuotes();

                // Then
                assertEquals(30, quotes.size());
        }

}
