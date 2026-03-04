package org.dgawlik.signals.strategies;

import java.util.List;

import org.dgawlik.signals.Quote;
import org.dgawlik.signals.portfolio.Portfolio;

public abstract class Strategy {

    protected boolean active;

    public abstract void activateOn(List<Quote> lookbehind);

    public abstract void deactivateOn(Portfolio portfolio, List<Quote> lookbehind);

    public abstract void act(Portfolio portfolio, List<Quote> lookbehind);

    public void step(Portfolio portfolio, List<Quote> lookbehind) {
        activateOn(lookbehind);

        if (active) {
            act(portfolio, lookbehind);
            deactivateOn(portfolio, lookbehind);
        }

    }
}
