package ca.jrvs.apps.trading.dao;

import ca.jrvs.apps.trading.model.domain.AlphaQuote;
import ca.jrvs.apps.trading.util.JsonUtil;
import ca.jrvs.apps.trading.config.MarketDataConfig;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.*;

@Repository
public class MarketDataDao {

    private final String ALPHA_URL_TEMPLATE;

    private final Logger logger = LoggerFactory.getLogger(ca.jrvs.apps.trading.dao.MarketDataDao.class);
    private final HttpClientConnectionManager httpClientConnectionManager;
    private final String apiKey;

    @Autowired
    public MarketDataDao(MarketDataConfig marketDataConfig) {
        this.httpClientConnectionManager = new PoolingHttpClientConnectionManager();
        this.apiKey = marketDataConfig.getToken();
        this.ALPHA_URL_TEMPLATE = marketDataConfig.getHost();
    }

    /**
     * Get an AlphaVantageQuote
     *
     * @param ticker
     * @throws IllegalArgumentException if a given ticker is invalid
     * @throws DataRetrievalFailureException if HTTP request failed
     */

    public Optional<AlphaQuote> findById(String ticker) {
        List<String> list = Collections.singletonList(ticker);
        List<AlphaQuote> quotes = findAllById(list);

        if (quotes.isEmpty()) {
            return Optional.empty();
        } else if (quotes.size() == 1) {
            return Optional.of(quotes.get(0));
        } else {
            throw new DataRetrievalFailureException("Unexpected number of quotes returned");
        }
    }

    /**
     * Get quotes from Alpha Vantage
     * @param tickers is a list of tickers
     * @return a list of AlphaVantageQuote objects
     * @throws IllegalArgumentException if a given ticker is invalid
     * @throws DataRetrievalFailureException if HTTP request failed
     */
    public List<AlphaQuote> findAllById(Iterable<String> tickers) {
        List<AlphaQuote> results = new ArrayList<>();

        for (String ticker : tickers) {
            if (ticker == null || ticker.isEmpty()) {
                throw new IllegalArgumentException("Invalid ticker: " + ticker);
            }

            String url = String.format(ALPHA_URL_TEMPLATE, ticker, apiKey);
            Optional<String> response = executeHttpGet(url);

            if (!response.isPresent()) {
                throw new IllegalArgumentException("Ticker not found: " + ticker);
            }

            JSONObject root = new JSONObject(response.get());
            if (!root.has("Global Quote")) {
                throw new IllegalArgumentException("Ticker not found: " + ticker);
            }

            JSONObject quoteJson = root.getJSONObject("Global Quote");

            try {
                AlphaQuote quote = JsonUtil.toObjectFromJson(root.toString(), AlphaQuote.class);
                results.add(quote);
            } catch (RuntimeException e) {
                logger.error("Failed to parse AlphaQuote JSON", e);
            }
        }
        return results;
    }



    /**
     * Execute a GET request and return http entity/body as a string
     * Tip: use EntitiyUtils.toString to process HTTP entity
     *
     * @param url resource URL
     * @return http response body or Optional.empty for 404 response
     * @throws DataRetrievalFailureException if HTTP failed or status code is unexpected
     */
    private Optional<String> executeHttpGet(String url) {
        CloseableHttpClient httpClient = getHttpClient();
        HttpUriRequest request = new HttpGet(url);

        try {
            HttpResponse response = httpClient.execute(request);
            int status = response.getStatusLine().getStatusCode();

            if (status == 404) {
                return Optional.empty();
            } else if (status == 200) {
                return Optional.of(EntityUtils.toString(response.getEntity()));
            } else {
                throw new DataRetrievalFailureException("Unexpected HTTP Status: " + status);
            }

        } catch (IOException e) {
            throw new DataRetrievalFailureException("HTTP request failed: " + e.getMessage());
        }
    }

    /**
     * Borrow a HTTP client from the HttpClientConnectionManager
     * @return a HttpClient
     */
    private CloseableHttpClient getHttpClient() {
        return HttpClients.custom()
                .setConnectionManager(httpClientConnectionManager)
                .setConnectionManagerShared(true)
                .build();
    }

}