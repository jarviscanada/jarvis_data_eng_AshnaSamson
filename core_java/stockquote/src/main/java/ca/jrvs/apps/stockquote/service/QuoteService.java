package ca.jrvs.apps.stockquote.service;

import ca.jrvs.apps.stockquote.client.QuoteHTTPHelper;
import ca.jrvs.apps.stockquote.dao.QuoteDao;
import ca.jrvs.apps.stockquote.model.Quote;

import java.util.Optional;

public class QuoteService {

    private QuoteDao dao;
    private QuoteHTTPHelper httpHelper;

    public QuoteService (QuoteDao quoteDao, QuoteHTTPHelper httpHelper) {
        this.dao = quoteDao;
        this.httpHelper = httpHelper;
    }

    /**
     * Fetches latest quote data from endpoint
     * @param ticker
     * @return Latest quote information or empty optional if ticker symbol not found
     */
    public Optional<Quote> fetchQuoteDataFromAPI(String ticker) {
        String symbol = ticker;
        String apiKey = "c4ab4d07cemsh83cb82e76e49b53p18cb68jsn7c0c0b0bdb9d";

        try {
            Quote quote = httpHelper.fetchQuoteInfo(symbol);
            dao.save(quote);
            System.out.println(quote);
            if (quote.getSymbol() == null || quote.getSymbol().isEmpty()) {
                return Optional.empty(); // API response doesn't have existing symbol
            } else {
                return Optional.of(quote); // API response has existing symbol
            }

        } catch (Exception e) {
            System.out.println("Ticker symbol not found: " + e.getMessage());
            return Optional.empty();
        }

    }

}