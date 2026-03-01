package org.dgawlik.signals.examples;

import java.util.List;
import java.util.stream.IntStream;

import org.dgawlik.signals.Event;
import org.dgawlik.signals.Frequency;
import org.dgawlik.signals.SeriesAggregator;
import org.dgawlik.signals.etoro.CandlesEndpoint;
import org.dgawlik.signals.indicator.Series;
import org.dgawlik.signals.indicator.extensions.EMA;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

public class MACD {

    public static void main(String[] args) {
        XYChart chart = new XYChartBuilder().width(800).height(600).title("EMA Series").xAxisTitle("Time")
                .yAxisTitle("Price").build();

        var candlesEndpoint = CandlesEndpoint.ETORO();

        var candles = candlesEndpoint.fetch(Frequency.ONE_DAY, 100, "AAPL");
        var aggregator = SeriesAggregator.create(0, Frequency.ONE_DAY).addEvents(candles);

        var ema7 = new EMA("AAPL", 7).get(aggregator.convertToQuotes());
        var ema24 = new EMA("AAPL", 24).get(aggregator.convertToQuotes());

        for (var candle : candles) {
            var xData = IntStream.range(0,
                    candle.events().size()).mapToObj(Integer::valueOf).toList();
            var yData = candle.events().stream().map(e -> ((Event.Candle) e).close()).toList();
            chart.addSeries(candle.symbol(), xData, yData);
        }

        var xData = IntStream.range(0, ema7.events().size()).mapToObj(Integer::valueOf).toList();
        var yData = ema7.events().stream().map(e -> ((Event.Indicator) e).val1()).toList();
        chart.addSeries("EMA7", xData, yData);

        xData = IntStream.range(0,
                ema24.events().size()).mapToObj(Integer::valueOf).toList();
        yData = ema24.events().stream().map(e -> ((Event.Indicator) e).val1()).toList();
        chart.addSeries("EMA24", xData, yData);

        new SwingWrapper<>(chart).displayChart();
    }
}
