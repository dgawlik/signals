package org.dgawlik.signals.portfolio;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PnLTest {

    @Test
    public void test_pnl_basic() {
        var valuations = List.of(
                new Portfolio.Valuation(1000, List.of(new Portfolio.Position("AAPL", 10, 100))),
                new Portfolio.Valuation(1100, List.of(new Portfolio.Position("AAPL", 10, 110))));

        var results = PnL.of(valuations);

        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("AAPL", results.get(0).symbol());
        Assertions.assertEquals(100, results.get(0).differenceCash());
        Assertions.assertEquals(10, results.get(0).differencePercent());
    }

    @Test
    public void test_pnl_two_buys() {
        var valuations = List.of(
                new Portfolio.Valuation(1000, List.of(new Portfolio.Position("AAPL", 10, 100))),
                new Portfolio.Valuation(1100, List.of(new Portfolio.Position("AAPL", 15, 110))));

        var results = PnL.of(valuations);

        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("AAPL", results.get(0).symbol());
        Assertions.assertEquals(100, results.get(0).differenceCash());
        Assertions.assertEquals(10, results.get(0).differencePercent());
    }

    @Test
    public void test_pnl_buy_and_sell() {
        var valuations = List.of(
                new Portfolio.Valuation(1000, List.of(new Portfolio.Position("AAPL", 10, 100))),
                new Portfolio.Valuation(1100, List.of(new Portfolio.Position("AAPL", 5, 110))));

        var results = PnL.of(valuations);

        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals("AAPL", results.get(0).symbol());
        Assertions.assertEquals(450, results.get(0).differenceCash());
        Assertions.assertEquals(81.81818181818183, results.get(0).differencePercent());
    }
}
