package ca.jrvs.apps.stockquote.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import ca.jrvs.apps.stockquote.dto.QuoteResponse;
import ca.jrvs.apps.stockquote.model.Quote;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QuoteHTTPHelper {
    private static final String TS_BASE_URL = "https://alpha-vantage.p.rapidapi.com";
    private static final String QUOTE_BASE_URL = "https://www.alphavantage.co";
    private final String apiKey;
    private final OkHttpClient client;

    public QuoteHTTPHelper(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
    }

    /**
     * Fetch latest quote data from Alpha Vantage endpoint
     * @param symbol Ticker symbol used to query Alpha Vantage
     * @return Quote with latest data
     * @throws IllegalArgumentException - if no data was found for the given symbol
     */
    public Quote fetchQuoteInfo(String symbol) throws IllegalArgumentException {
        String queryParams = String.format(
                "?function=GLOBAL_QUOTE" +
                        "&symbol=%s" +
                        "&apikey=%s", symbol, apiKey);
        String url = QUOTE_BASE_URL + "/query" + queryParams;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-rapidapi-host", "alpha-vantage.p.rapidapi.com")
                .addHeader("x-rapidapi-key", apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            ObjectMapper objectMapper = new ObjectMapper();
            QuoteResponse qr = objectMapper.readValue(response.body().string(), QuoteResponse.class);
            return new Quote(qr.getQuoteData()); // Convert DTO to Entity



        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Alpha Vantage could not be queried with the passed arguments");
    }

}