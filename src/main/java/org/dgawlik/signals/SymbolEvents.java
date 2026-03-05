package org.dgawlik.signals;

import java.util.List;

import org.dgawlik.signals.utils.ArgumentValidator;

public record SymbolEvents(List<? extends Event> events) {

    public SymbolEvents {
        ArgumentValidator.VAL
                .requireNonNull(events)
                .requireNonEmpty(events)
                .requireDoesNotContainNull(events);
    }

    public String symbol() {
        return events.get(0).symbol();
    }
}
