package org.dgawlik.signals;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.dgawlik.signals.etoro.CandlesEndpoint;
import org.dgawlik.signals.indicator.Series;
import org.dgawlik.signals.portfolio.Portfolio;

import lombok.var;

public class Simulation {

    public record Context(Portfolio portfolio, Map<String, Object> data, List<Quote> lookbehind) {

    }

    private Portfolio portfolio;
    private int lookbehind = 10;
    private LocalDateTime from = LocalDateTime.MIN;
    private LocalDateTime to = LocalDateTime.MAX;

    private final List<SymbolEvents> events;
    private final Frequency frequency;

    private Simulation(Frequency frequency, String... intruments) {
        var candlesEndpoint = CandlesEndpoint.ETORO();
        this.events = candlesEndpoint.fetch(frequency, 1000, intruments);
        this.frequency = frequency;
    }

    public Simulation withPortfolio(double cashAmount, double feeAmount) {
        this.portfolio = Portfolio.create(cashAmount, feeAmount);
        return this;
    }

    public Simulation withIndicators(Series... indicators) {
        var aggregator = SeriesAggregator.create(0, frequency).addEvents(events);
        var quotes = aggregator.convertToQuotes();

        for (var indicator : indicators) {
            events.add(indicator.get(quotes));
        }
        return this;
    }

    public static Simulation forInstruments(Frequency frequency, String... intruments) {
        return new Simulation(frequency, intruments);
    }

    public Portfolio getResult() {
        return portfolio;
    }

    public Simulation withFrom(LocalDateTime from) {
        this.from = from;
        return this;
    }

    public Simulation withTo(LocalDateTime to) {
        this.to = to;
        return this;
    }

    public Simulation withLookbehind(int lookbehind) {
        this.lookbehind = lookbehind;
        return this;
    }

    public Simulation run(Consumer<Context> strategy) {
        if (portfolio == null) {
            throw new IllegalStateException("Portfolio is not set");
        }

        var aggregator = SeriesAggregator.create(0, frequency).addEvents(events);
        var quotes = aggregator.convertToQuotes().stream()
                .filter(q -> !q.time(frequency).isBefore(from) && !q.time(frequency).isAfter(to)).toList();
        var data = new HashMap<String, Object>();
        var lookbehindQueue = new LinkedList<Quote>();

        for (var quote : quotes) {
            if (lookbehindQueue.size() > lookbehind) {
                lookbehindQueue.removeFirst();
            }
            lookbehindQueue.addLast(quote);

            strategy.accept(new Context(portfolio, data, List.copyOf(lookbehindQueue)));
        }

        return this;
    }
}
