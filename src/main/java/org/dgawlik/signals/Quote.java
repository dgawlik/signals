package org.dgawlik.signals;

import java.time.LocalDateTime;
import java.util.Map;

public record Quote(
        Map<String, ? extends Event> events) {

    public Quote {
        if (events == null) {
            throw new IllegalArgumentException("Events cannot be null");
        }

        events = Map.copyOf(events);
    }

    public LocalDateTime time(Frequency frequency) {
        return SeriesAggregator.discreteTime(events.values().stream().findFirst().get().time(), frequency);
    }

    public Event.Candle getCandle(String symbol) {
        var result = events.get(symbol);
        if (result == null) {
            throw new IllegalArgumentException("Candle not found for symbol: " + symbol);
        }

        if (result instanceof Event.Candle candle) {
            return candle;
        }

        throw new IllegalArgumentException("Event is not a candle: " + symbol);
    }

    public Event.Indicator getIndicator(String symbol) {
        var result = events.get(symbol);
        if (result == null) {
            throw new IllegalArgumentException("Indicator not found for symbol: " + symbol);
        }

        if (result instanceof Event.Indicator indicator) {
            return indicator;
        }

        throw new IllegalArgumentException("Event is not an indicator: " + symbol);
    }

    public Event.Custom getCustom(String symbol) {
        var result = events.get(symbol);
        if (result == null) {
            throw new IllegalArgumentException("Custom not found for symbol: " + symbol);
        }

        if (result instanceof Event.Custom custom) {
            return custom;
        }

        throw new IllegalArgumentException("Event is not a custom event: " + symbol);
    }
}
