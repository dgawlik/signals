package org.dgawlik.signals.portfolio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PnL {

    public record DifferencePair(Portfolio.Position start, Portfolio.Position end) {
        public DifferencePair {
            if (start.symbol() != end.symbol()) {
                throw new IllegalArgumentException("Symbols must match");
            }
        }
    }

    public record Result(String symbol, double differenceCash, double differencePercent) {
        public Result {
            if (symbol == null || symbol.isBlank()) {
                throw new IllegalArgumentException("Symbol cannot be null or empty");
            }
        }
    }

    public static List<Result> of(List<Portfolio.Valuation> valuations) {
        if (valuations.size() < 2) {
            throw new IllegalArgumentException("At least two valuations are required");
        }

        var unchanged = new HashMap<String, Portfolio.Position>();
        var intermediates = new HashMap<String, List<DifferencePair>>();

        var prevValuation = valuations.get(0);

        for (var pos : prevValuation.positions()) {
            unchanged.put(pos.symbol(), pos);
        }

        for (int i = 1; i < valuations.size(); i++) {
            var currentValuation = valuations.get(i);

            for (var pos : currentValuation.positions()) {
                var prevPos = unchanged.get(pos.symbol());

                if (prevPos == null) {
                    unchanged.put(pos.symbol(), pos);
                } else if (prevPos.units() != pos.units()) {
                    if (prevPos.units() < pos.units()) {
                        var slice = new Portfolio.Position(pos.symbol(), prevPos.units(), pos.price());
                        intermediates.computeIfAbsent(pos.symbol(), k -> new ArrayList<>())
                                .add(new DifferencePair(prevPos, slice));
                        unchanged.put(pos.symbol(), pos);
                    } else {
                        var slice = new Portfolio.Position(pos.symbol(), prevPos.units() - pos.units(), pos.price());
                        intermediates.computeIfAbsent(pos.symbol(), k -> new ArrayList<>())
                                .add(new DifferencePair(slice, prevPos));
                        unchanged.put(pos.symbol(), pos);
                    }
                }
            }
        }

        var lastValuation = valuations.get(valuations.size() - 1);
        for (var pos : lastValuation.positions()) {
            var prevPos = unchanged.get(pos.symbol());
            if (prevPos != null) {
                intermediates.computeIfAbsent(pos.symbol(), k -> new ArrayList<>())
                        .add(new DifferencePair(prevPos, pos));
            }
        }

        var results = new ArrayList<Result>();

        for (var entry : intermediates.entrySet()) {
            var symbol = entry.getKey();
            var pairs = entry.getValue();

            var differenceCash = 0.0;
            var differencePercent = 0.0;

            for (var pair : pairs) {
                differenceCash += pair.end().units() * pair.end().price() - pair.start().units() * pair.start().price();
                differencePercent += (pair.end().units() * pair.end().price()
                        - pair.start().units() * pair.start().price()) / (pair.start().units() * pair.start().price())
                        * 100;
            }

            results.add(new Result(symbol, differenceCash, differencePercent));
        }

        return results;
    }

}
