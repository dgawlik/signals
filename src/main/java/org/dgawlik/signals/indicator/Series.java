package org.dgawlik.signals.indicator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.dgawlik.signals.Event;
import org.dgawlik.signals.Quote;
import org.dgawlik.signals.SymbolEvents;

public abstract class Series {

    public abstract Event process(List<Quote> lookbehind) throws RuntimeException;

    public static class LogContinueException extends RuntimeException {
        public LogContinueException(String message) {
            super(message);
        }
    }

    public static class LogDrainContinueException extends RuntimeException {
        public LogDrainContinueException(String message) {
            super(message);
        }
    }

    public static class BreakException extends RuntimeException {
        public BreakException(String message) {
            super(message);
        }
    }

    protected final List<String> errors;
    protected final int maxLookBehind;

    public Series(int maxLookBehind) {
        this.maxLookBehind = maxLookBehind;
        this.errors = new ArrayList<>();
    }

    public SymbolEvents get(List<Quote> quotes) {

        var lookbehind = new LinkedList<Quote>();
        var events = new ArrayList<Event>();

        for (Quote quote : quotes) {

            if (lookbehind.size() > maxLookBehind) {
                lookbehind.removeFirst();
            }

            lookbehind.addLast(quote);

            try {
                var event = process(lookbehind);
                if (event == null) {
                    throw new RuntimeException("Event cannot be null");
                }
                events.add(event);
            } catch (LogContinueException e) {
                errors.add(e.getMessage());
            } catch (LogDrainContinueException e) {
                errors.add(e.getMessage());
                lookbehind.clear();
            } catch (BreakException e) {
                var err = List.copyOf(errors);
                throw new RuntimeException("Failed to process series: " + err.toString());
            }
        }

        return new SymbolEvents(events);
    }

    public List<String> getErrors() {
        return List.copyOf(errors);
    }
}
