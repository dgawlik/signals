package org.dgawlik.signals.portfolio;

import java.util.ArrayList;
import java.util.List;

import org.dgawlik.signals.Quote;

public class Portfolio {

    public record Position(String symbol, double units, double price) {

        public Position {
            if (units < 0) {
                throw new IllegalArgumentException("Units cannot be negative");
            }

            if (price < 0) {
                throw new IllegalArgumentException("Price cannot be negative");
            }

            if (symbol == null || symbol.isBlank()) {
                throw new IllegalArgumentException("Symbol cannot be null or empty");
            }
        }
    }

    public record Valuation(double cash, List<Position> positions) {

        public Valuation {
            if (cash < 0) {
                throw new IllegalArgumentException("Cash cannot be negative");
            }

            if (positions == null) {
                throw new IllegalArgumentException("Positions cannot be null");
            }

            positions = List.copyOf(positions);
        }

        public Position getPosition(String symbol) {
            var found = positions.stream().filter(p -> p.symbol().equals(symbol)).findFirst();
            if (found.isEmpty()) {
                throw new IllegalArgumentException("Position not found for symbol: " + symbol);
            }
            return found.get();
        }

        public double totalValue() {
            return cash + positions.stream().mapToDouble(p -> p.units() * p.price()).sum();
        }
    }

    public class Ops {

        private final ArrayList<Position> positions;
        private final double cash;
        private final double transactionCosts;
        private final Quote quote;

        private Ops(List<Position> positions, double cash, double transactionCosts, Quote quote) {
            var updateValuationPositions = positions
                    .stream()
                    .map(p -> new Position(p.symbol(), p.units(), quote.getCandle(p.symbol()).close()))
                    .toList();

            this.positions = new ArrayList<>(updateValuationPositions);
            this.cash = cash;
            this.transactionCosts = transactionCosts;
            this.quote = quote;
        }

        public boolean canBuy(String symbol, double amount) {
            return amount + transactionCosts <= cash;
        }

        public boolean canSell(String symbol, double amount) {

            var found = positions.stream().filter(p -> p.symbol().equals(symbol)).findFirst();
            if (found.isEmpty()) {
                throw new IllegalArgumentException("Position not found for symbol: " + symbol);
            }
            var position = found.get();

            return transactionCosts <= cash + amount && amount <= position.units() * position.price();
        }

        public Ops buy(String symbol, double amount) {
            if (!canBuy(symbol, amount)) {
                throw new IllegalArgumentException("Cannot buy position");
            }

            var candle = quote.getCandle(symbol);
            var found = positions.stream().filter(p -> p.symbol().equals(symbol)).findFirst();

            var requestedPosition = new Position(symbol, amount / candle.close(), candle.close());
            if (!found.isEmpty()) {
                var position = found.get();
                requestedPosition = new Position(symbol, position.units() + requestedPosition.units(), candle.close());
            }

            positions.removeIf(p -> p.symbol().equals(symbol));
            positions.add(requestedPosition);

            var newCash = cash - amount - transactionCosts;

            return new Ops(positions, newCash, transactionCosts, quote);
        }

        public Ops sell(String symbol, double amount) {
            if (!canSell(symbol, amount)) {
                throw new IllegalArgumentException("Cannot sell position");
            }

            var candle = quote.getCandle(symbol);
            var found = positions.stream().filter(p -> p.symbol().equals(symbol)).findFirst();

            var requestedPosition = new Position(symbol, amount / candle.close(), candle.close());
            if (found.isEmpty()) {
                throw new IllegalArgumentException("Position not found for symbol: " + symbol);
            }
            var position = found.get();
            requestedPosition = new Position(symbol, position.units() - requestedPosition.units(), candle.close());

            positions.removeIf(p -> p.symbol().equals(symbol));
            positions.add(requestedPosition);

            var newCash = cash + amount - transactionCosts;

            return new Ops(positions, newCash, transactionCosts, quote);
        }

        public Ops close(String symbol) {
            var found = positions.stream().filter(p -> p.symbol().equals(symbol)).findFirst();
            var candle = quote.getCandle(symbol);

            if (found.isEmpty()) {
                throw new IllegalArgumentException("Position not found for symbol: " + symbol);
            }
            var position = found.get();

            var newPositions = positions.stream().filter(p -> !p.symbol().equals(symbol)).toList();
            var newCash = cash + position.units() * candle.close() - transactionCosts;

            if (newCash < 0) {
                throw new IllegalArgumentException("Insufficient cash to close position");
            }

            return new Ops(newPositions, newCash, transactionCosts, quote);
        }

        public void commit() {
            currentValuation = new Valuation(cash, positions);
            history.add(currentValuation);
        }

        public void evaluate() {
            commit();
        }
    }

    private Valuation currentValuation;
    private List<Valuation> history;
    private final double transactionCosts;

    private Portfolio(double cash, double transactionCosts) {
        this.currentValuation = new Valuation(cash, List.of());
        this.transactionCosts = transactionCosts;
        this.history = new ArrayList<>();
        this.history.add(currentValuation);
    }

    public static Portfolio create(double cash, double transactionCosts) {
        return new Portfolio(cash, transactionCosts);
    }

    public Ops ops(Quote quote) {
        return new Ops(currentValuation.positions(), currentValuation.cash(), transactionCosts, quote);
    }

    public List<Valuation> history() {
        return List.copyOf(history);
    }

    public Valuation currentValuation() {
        return currentValuation;
    }

}
