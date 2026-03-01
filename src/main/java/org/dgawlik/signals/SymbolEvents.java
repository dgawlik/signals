package org.dgawlik.signals;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record SymbolEvents(List<? extends Event> events) {

    public SymbolEvents {

        if (events == null) {
            throw new IllegalArgumentException("Events cannot be null");
        }

        if (events.stream().filter(Objects::isNull).findAny().isPresent()) {
            throw new IllegalArgumentException("Events cannot contain null values");
        }

        if (events.isEmpty()) {
            throw new IllegalArgumentException("Events cannot be empty");
        }
    }

    public String symbol() {
        return events.get(0).symbol();
    }
}
