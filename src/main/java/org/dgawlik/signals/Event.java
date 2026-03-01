package org.dgawlik.signals;

import java.time.LocalDateTime;
import java.util.List;

public sealed interface Event {

    LocalDateTime time();

    String symbol();

    record Candle(String symbol, LocalDateTime time, double open, double high, double low, double close, double volume)
            implements Event {

        public Candle {
            if (open > high || open < low || close > high || close < low) {
                throw new IllegalArgumentException("Invalid candle data");
            }

            if (symbol == null || symbol.isBlank()) {
                throw new IllegalArgumentException("Symbol cannot be null or empty");
            }

            if (time == null) {
                throw new IllegalArgumentException("Time cannot be null");
            }
        }
    }

    record Indicator(String forSymbol, String symbol, LocalDateTime time, double val1, double val2, double val3,
            double val4)
            implements Event {

        public Indicator {
            if (forSymbol == null || forSymbol.isBlank()) {
                throw new IllegalArgumentException("For symbol cannot be null or empty");
            }

            if (symbol == null || symbol.isBlank()) {
                throw new IllegalArgumentException("Symbol cannot be null or empty");
            }

            if (Double.isNaN(val1)) {
                throw new IllegalArgumentException();
            }
        }
    }

    record Custom(String forSymbol, String symbol, LocalDateTime time, List<KeyValue> keyValues) implements Event {

        public Custom {
            if (forSymbol == null || forSymbol.isBlank()) {
                throw new IllegalArgumentException("For symbol cannot be null or empty");
            }

            if (symbol == null || symbol.isBlank()) {
                throw new IllegalArgumentException("Symbol cannot be null or empty");
            }

            if (keyValues == null || keyValues.isEmpty()) {
                throw new IllegalArgumentException("Key values cannot be null or empty");
            }

            for (KeyValue kv : keyValues) {
                if (kv.key() == null || kv.key().isBlank()) {
                    throw new IllegalArgumentException("Key cannot be null or empty");
                }

                if (Double.isNaN(kv.value())) {
                    throw new IllegalArgumentException("Value cannot be NaN");
                }
            }

            keyValues = List.copyOf(keyValues);
        }
    }

    record NoOp(String symbol, LocalDateTime time) {
    }

    record KeyValue(String key, double value) {
    }

}
