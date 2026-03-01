package org.dgawlik.signals.portfolio;

import java.time.LocalDateTime;
import java.util.Map;

import org.dgawlik.signals.Event;
import org.dgawlik.signals.Quote;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PortfolioTests {

        @Test
        public void test_positive() {
                var candle = new Event.Candle("AAPL", LocalDateTime.now(), 1, 1, 1, 1, 1);
                var candle2 = new Event.Candle("TSLA", LocalDateTime.now(), 1, 1, 1, 1, 1);

                var quote = new Quote(Map.of("AAPL", candle, "TSLA", candle2));

                var portfolio = Portfolio.create(1000, 2.5);

                portfolio.ops(quote)
                                .buy("AAPL", 10)
                                .buy("TSLA", 10)
                                .commit();

                var current = portfolio.currentValuation();
                Assertions.assertEquals(975, current.cash());

                Assertions.assertEquals(10,
                                current.getPosition("AAPL").units());
                Assertions.assertEquals(10,
                                current.getPosition("TSLA").units());

        }

        @Test
        public void test_too_much_buy() {
                var candle = new Event.Candle("AAPL", LocalDateTime.now(), 1, 1, 1, 1, 1);
                var candle2 = new Event.Candle("TSLA", LocalDateTime.now(), 1, 1, 1, 1, 1);

                var quote = new Quote(Map.of("AAPL", candle, "TSLA", candle2));

                var portfolio = Portfolio.create(1000, 2.5);

                Assertions.assertThrows(IllegalArgumentException.class, () -> portfolio.ops(quote)
                                .buy("AAPL", 997.5)
                                .buy("TSLA", 10)
                                .commit());

        }

        @Test
        public void test_buy_sell() {
                var candle = new Event.Candle("AAPL", LocalDateTime.now(), 1, 1, 1, 1, 1);
                var candle2 = new Event.Candle("TSLA", LocalDateTime.now(), 1, 1, 1, 1, 1);

                var quote = new Quote(Map.of("AAPL", candle, "TSLA", candle2));

                var portfolio = Portfolio.create(1000, 2.5);

                portfolio.ops(quote)
                                .buy("AAPL", 997.5)
                                .commit();

                portfolio.ops(quote)
                                .sell("AAPL", 100)
                                .commit();

                var current = portfolio.currentValuation();
                Assertions.assertEquals(97.5, current.cash());

                Assertions.assertEquals(897.5,
                                current.getPosition("AAPL").units());
        }

        @Test
        public void test_buy_close() {
                var candle = new Event.Candle("AAPL", LocalDateTime.now(), 1, 1, 1, 1, 1);
                var candle2 = new Event.Candle("TSLA", LocalDateTime.now(), 1, 1, 1, 1, 1);

                var quote = new Quote(Map.of("AAPL", candle, "TSLA", candle2));

                var portfolio = Portfolio.create(1000, 2.5);

                portfolio.ops(quote)
                                .buy("AAPL", 997.5)
                                .commit();

                portfolio.ops(quote)
                                .close("AAPL")
                                .commit();

                var current = portfolio.currentValuation();
                Assertions.assertEquals(995, current.cash());
        }

        @Test
        public void test_sell_with_profit() {
                var candle = new Event.Candle("AAPL", LocalDateTime.now(), 1, 1, 1, 1, 1);
                var candle2 = new Event.Candle("TSLA", LocalDateTime.now(), 1, 1, 1, 1, 1);

                var quote = new Quote(Map.of("AAPL", candle, "TSLA", candle2));

                var portfolio = Portfolio.create(1010, 2.5);

                portfolio.ops(quote)
                                .buy("AAPL", 500)
                                .buy("TSLA", 500)
                                .commit();

                var candle3 = new Event.Candle("AAPL", LocalDateTime.now(), 2, 2, 2, 2, 2);
                var candle4 = new Event.Candle("TSLA", LocalDateTime.now(), 2, 2, 2, 2, 2);

                var quote2 = new Quote(Map.of("AAPL", candle3, "TSLA", candle4));

                portfolio.ops(quote2)
                                .close("AAPL")
                                .close("TSLA")
                                .commit();

                var current = portfolio.currentValuation();
                Assertions.assertEquals(2000, current.cash());
        }

        @Test
        public void try_sell_non_existent() {
                var candle = new Event.Candle("AAPL", LocalDateTime.now(), 1, 1, 1, 1, 1);
                var candle2 = new Event.Candle("TSLA", LocalDateTime.now(), 1, 1, 1, 1, 1);

                var quote = new Quote(Map.of("AAPL", candle, "TSLA", candle2));

                var portfolio = Portfolio.create(1010, 2.5);

                portfolio.ops(quote)
                                .buy("AAPL", 500)
                                .buy("TSLA", 500)
                                .commit();

                Assertions.assertThrows(IllegalArgumentException.class, () -> portfolio.ops(quote)
                                .sell("XXX", 600)
                                .commit());
        }

        @Test
        public void try_buy_lacking_quote() {
                var candle = new Event.Candle("AAPL", LocalDateTime.now(), 1, 1, 1, 1, 1);
                var candle2 = new Event.Candle("TSLA", LocalDateTime.now(), 1, 1, 1, 1, 1);

                var quote = new Quote(Map.of("AAPL", candle, "TSLA", candle2));

                var portfolio = Portfolio.create(1010, 2.5);

                Assertions.assertThrows(IllegalArgumentException.class, () -> portfolio.ops(quote)
                                .buy("AAPL", 500)
                                .buy("XXX", 500)
                                .commit());
        }
}
