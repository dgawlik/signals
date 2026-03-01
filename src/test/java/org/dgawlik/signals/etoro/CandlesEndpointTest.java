package org.dgawlik.signals.etoro;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.dgawlik.signals.Frequency;

public class CandlesEndpointTest {

    @Test
    public void test_dot_env() {
        Dotenv e = Dotenv.load();

        Assertions.assertNotNull(e.get("ETORO_PUBLIC_KEY"));
        Assertions.assertNotNull(e.get("ETORO_API_KEY"));

        Assertions.assertNull(e.get("XXX"));
    }

    @Test
    public void fetch_one_day_30() {
        Dotenv e = Dotenv.load();

        var publicKey = e.get("ETORO_PUBLIC_KEY");
        var apiKey = e.get("ETORO_API_KEY");

        var endpoint = new CandlesEndpoint("https://public-api.etoro.com", publicKey, apiKey);

        var result = endpoint.fetch(Frequency.ONE_DAY, 30, "AAPL", "TSLA", "EURUSD", "EURGBP", "EURJPY", "EURCHF");

        Assertions.assertEquals(6, result.size());

        Assertions.assertEquals(30, result.get(0).events().size());
        Assertions.assertEquals(30, result.get(1).events().size());
        Assertions.assertEquals(30, result.get(2).events().size());
        Assertions.assertEquals(30, result.get(3).events().size());
        Assertions.assertEquals(30, result.get(4).events().size());
        Assertions.assertEquals(30, result.get(5).events().size());
    }
}
