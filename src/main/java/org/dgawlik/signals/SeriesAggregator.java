package org.dgawlik.signals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SeriesAggregator {

    private record EventsByTime(LocalDateTime time, List<Event> events) {
    }

    private final Map<String, List<? extends Event>> events;
    private final int maxGap;
    private final Frequency frequency;

    public SeriesAggregator(int maxGap, Frequency frequency) {
        this.events = new HashMap<>();
        this.maxGap = maxGap;
        this.frequency = frequency;
    }

    public SeriesAggregator addEvents(SymbolEvents symbolEvents) {
        this.events.put(symbolEvents.symbol(), symbolEvents.events());
        return this;
    }

    public SeriesAggregator addEvents(List<SymbolEvents> symbolEvents) {
        symbolEvents.forEach(this::addEvents);
        return this;
    }

    public static SeriesAggregator create(int maxGap, Frequency frequency) {
        return new SeriesAggregator(maxGap, frequency);
    }

    public List<Quote> convertToQuotes() {
        var allEvents = new ArrayList<Event>();
        allEvents.addAll(events.values().stream().flatMap(List::stream).toList());

        List<EventsByTime> eventsByTime = allEvents.stream()
                .collect(Collectors.groupingBy(e -> discreteTime(e.time(), frequency), Collectors.toList()))
                .entrySet().stream()
                .map(entry -> new EventsByTime(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(EventsByTime::time))
                .toList();

        var quotes = eventsByTime.stream().map(ebt -> {

            Map<String, Event> eventsMap = new HashMap<>();

            for (Event e : ebt.events()) {
                if (eventsMap.containsKey(e.symbol())) {
                    throw new IllegalArgumentException("Multiple events for the same time");
                }
                eventsMap.put(e.symbol(), e);
            }

            return new Quote(eventsMap);
        }).toList();

        validate(quotes);
        return quotes;
    }

    public static LocalDateTime discreteTime(LocalDateTime time, Frequency frequency) {
        return switch (frequency) {
            case TICK -> time.withNano(0);
            case ONE_MINUTE -> time.withSecond(0).withNano(0);
            case FIVE_MINUTES -> time.withMinute((time.getMinute() / 5) * 5).withSecond(0).withNano(0);
            case TEN_MINUTES -> time.withMinute((time.getMinute() / 10) * 10).withSecond(0).withNano(0);
            case FIFTEEN_MINUTES -> time.withMinute((time.getMinute() / 15) * 15).withSecond(0).withNano(0);
            case THIRTY_MINUTES -> time.withMinute((time.getMinute() / 30) * 30).withSecond(0).withNano(0);
            case ONE_HOUR -> time.withMinute(0).withSecond(0).withNano(0);
            case FOUR_HOURS -> time.withHour(time.getHour() / 4 * 4).withMinute(0).withSecond(0).withNano(0);
            case ONE_DAY -> time.withHour(time.getHour()).withMinute(0).withSecond(0).withNano(0);
        };
    }

    private void validate(List<Quote> quotes) {
        Map<String, Integer> consecutiveMissing = new HashMap<>();

        for (Quote quote : quotes) {
            for (String symbol : events.keySet()) {
                if (quote.events().containsKey(symbol)) {
                    consecutiveMissing.put(symbol, 0);
                } else {
                    consecutiveMissing.put(symbol, consecutiveMissing.getOrDefault(symbol, 0) + 1);
                    if (consecutiveMissing.get(symbol) > maxGap) {
                        throw new IllegalArgumentException("Too many missing candles for symbol: " + symbol);
                    }
                }
            }
        }
    }
}
