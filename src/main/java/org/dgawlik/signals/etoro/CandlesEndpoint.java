package org.dgawlik.signals.etoro;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;

import org.dgawlik.signals.Event;
import org.dgawlik.signals.Frequency;
import org.dgawlik.signals.SymbolEvents;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.SneakyThrows;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import tools.jackson.databind.ObjectMapper;

public class CandlesEndpoint {

    record SymbolInstrumentId(String symbol, Integer instrumentId) {

    }

    private final String baseUrl;
    private final String etoroApiKey;
    private final String etoroUserKey;
    private final ObjectMapper om;

    /**
     * Statically factory constructs a default endpoint instance reading API/Host
     * keys from dotEnv variables.
     *
     * @return the configured historical endpoint connection.
     */
    public static CandlesEndpoint ETORO() {
        Dotenv e = Dotenv.load();

        var publicKey = e.get("ETORO_PUBLIC_KEY");
        var apiKey = e.get("ETORO_API_KEY");

        return new CandlesEndpoint("https://public-api.etoro.com", publicKey, apiKey);
    }

    public CandlesEndpoint(String baseUrl, String etoroApiKey, String etoroUserKey) {
        this.baseUrl = baseUrl;

        this.etoroApiKey = etoroApiKey;
        this.etoroUserKey = etoroUserKey;

        if (this.etoroApiKey == null || this.etoroUserKey == null) {
            throw new IllegalArgumentException("Etoro envirnoment keys not set");
        }

        this.om = new ObjectMapper();
    }

    @SneakyThrows
    public List<SymbolEvents> fetch(Frequency frequency, int count, String... symbols) {

        var errorneusSymbols = new ArrayList<String>();

        Scheduler vtScheduler = Schedulers.fromExecutor(Executors.newVirtualThreadPerTaskExecutor());

        var result = Flux.fromArray(symbols)
                .flatMap(symbol -> Mono.fromCallable(() -> {
                    SymbolInstrumentId instrId = lookupInstrumentId(symbol)
                            .orElseThrow(() -> new IllegalStateException("Instrument not found"));
                    return doFetch(frequency, count, instrId);
                })
                        .subscribeOn(vtScheduler)
                        .onErrorResume(error -> {
                            errorneusSymbols.add(symbol);
                            System.err.println("Failed on " + symbol + ": " + error.getMessage());
                            return Mono.empty();
                        }))
                .collectList()
                .block();

        if (!errorneusSymbols.isEmpty()) {
            throw new IllegalStateException("Failed to fetch candles for symbols: " + errorneusSymbols);
        }

        return result;
    }

    @SneakyThrows
    private SymbolEvents doFetch(Frequency frequency, int count, SymbolInstrumentId symbolInstrId) {
        var url = "{baseUrl}/api/v1/market-data/instruments/{instrumentId}/history/candles/asc/{interval}/{count}"
                .replace("{baseUrl}", this.baseUrl)
                .replace("{instrumentId}", symbolInstrId.instrumentId().toString())
                .replace("{interval}", frequency.getName())
                .replace("{count}", count + "");

        var client = HttpClient.newHttpClient();

        var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-api-key", this.etoroApiKey)
                .header("x-user-key", this.etoroUserKey)
                .header("x-request-id", UUID.randomUUID().toString())
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        try {
            var map = om.readValue(response.body(), Map.class);
            var unwrap1 = (List<Map<String, Object>>) map.get("candles");
            var unwrap2 = (List<Map<String, Object>>) unwrap1.get(0).get("candles");

            return new SymbolEvents(unwrap2.stream()
                    .map(m -> new Event.Candle(
                            symbolInstrId.symbol(),
                            LocalDateTime.parse(((String) m.get("fromDate")).replace("Z", "")),
                            (Double) m.get("open"),
                            (Double) m.get("high"),
                            (Double) m.get("low"),
                            (Double) m.get("close"),
                            m.get("volume") != null
                                    ? (Double) m.get("volume")
                                    : Double.NaN))
                    .toList());
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @SneakyThrows
    Optional<SymbolInstrumentId> lookupInstrumentId(String symbol) {
        var client = HttpClient.newHttpClient();

        var request = HttpRequest.newBuilder()
                .uri(new URI("{baseUrl}/api/v1/market-data/search?internalSymbolFull={internalSymbolFull}"
                        .replace("{baseUrl}", this.baseUrl)
                        .replace("{internalSymbolFull}", symbol)))
                .header("x-api-key", this.etoroApiKey)
                .header("x-user-key", this.etoroUserKey)
                .header("x-request-id", UUID.randomUUID().toString())
                .build();

        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            var map = om.readValue(response.body(), Map.class);
            var unwrap1 = (List<Map<String, Object>>) map.get("items");
            return Optional.of(new SymbolInstrumentId(symbol, (Integer) unwrap1.get(0).get("internalInstrumentId")));
        } catch (Exception e) {
            return Optional.empty();
        }

    }
}
