package org.dgawlik.signals.examples;

import java.util.List;
import java.util.stream.IntStream;

import org.dgawlik.signals.Event;
import org.dgawlik.signals.Frequency;
import org.dgawlik.signals.etoro.CandlesEndpoint;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

public class MultipleInstruments {

    public static void main(String[] args) {
        XYChart chart = new XYChartBuilder().width(800).height(600).title("EMA Series").xAxisTitle("Time")
                .yAxisTitle("Price").build();

        var candlesEndpoint = CandlesEndpoint.ETORO();

        var candles = candlesEndpoint.fetch(Frequency.ONE_MINUTE, 100, "AAPL");

        for (var candle : candles) {
            var xData = IntStream.range(0, candle.events().size()).mapToObj(Integer::valueOf).toList();
            var yData = candle.events().stream().map(e -> ((Event.Candle) e).close()).toList();
            chart.addSeries(candle.symbol(), xData, yData);
        }
        new SwingWrapper<>(chart).displayChart();
    }
}
