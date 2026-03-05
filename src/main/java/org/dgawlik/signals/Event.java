package org.dgawlik.signals;

import java.time.LocalDateTime;
import java.util.List;

import org.dgawlik.signals.utils.ArgumentValidator;

public sealed interface Event {

    LocalDateTime time();

    String symbol();

    record Candle(String symbol, LocalDateTime time, double open, double high, double low, double close, double volume)
            implements Event {

        public Candle {
            if (open > high || open < low || close > high || close < low) {
                throw new IllegalArgumentException("Invalid candle data");
            }

            ArgumentValidator.VAL
                    .requireNotBlank(symbol)
                    .requireNonNull(time);
        }
    }

    record Indicator(String forSymbol, String symbol, LocalDateTime time, double val1, double val2, double val3,
            double val4)
            implements Event {

        public Indicator {
            ArgumentValidator.VAL
                    .requireNotBlank(forSymbol)
                    .requireNotBlank(symbol)
                    .requireNonNull(time);

            if (Double.isNaN(val1)) {
                throw new IllegalArgumentException();
            }

            if (Double.isNaN(val1)) {
                throw new IllegalArgumentException();
            }
        }
    }

    record Custom(String forSymbol, String symbol, LocalDateTime time, List<KeyValue> keyValues) implements Event {

        public Custom {
            ArgumentValidator.VAL
                    .requireNotBlank(forSymbol)
                    .requireNotBlank(symbol)
                    .requireNonNull(time)
                    .requireNonEmpty(keyValues)
                    .requireDoesNotContainNull(keyValues);

            for (KeyValue kv : keyValues) {
                ArgumentValidator.VAL
                        .requireNotBlank(kv.key())
                        .requireNonNegative(kv.value());
            }

            keyValues = List.copyOf(keyValues);
        }
    }

    record NoOp(String symbol, LocalDateTime time) {
    }

    record KeyValue(String key, double value) {
    }

}
