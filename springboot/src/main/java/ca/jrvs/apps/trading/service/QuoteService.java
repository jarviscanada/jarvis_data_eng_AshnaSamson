package ca.jrvs.apps.trading.service;

import ca.jrvs.apps.trading.dao.MarketDataDao;
import ca.jrvs.apps.trading.dao.QuoteDao;
import ca.jrvs.apps.trading.model.domain.AlphaQuote;
import ca.jrvs.apps.trading.model.domain.Quote;
import ca.jrvs.apps.trading.model.domain.QuoteResponseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataAccessException;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuoteService {

    private static final Logger logger = LoggerFactory.getLogger(QuoteService.class);

    private final MarketDataDao marketDataDao;
    private final QuoteDao quoteDao;


    public QuoteService(MarketDataDao marketDataDao, QuoteDao quoteDao) {
        this.marketDataDao = marketDataDao;
        this.quoteDao = quoteDao;
    }

    /**
     * Find an AlphaVantage quote by ticker
     *
     * @param ticker stock symbol
     * @return AlphaQuote
     * @throws IllegalArgumentException if ticker is invalid or not found
     */
    public AlphaQuote findAlphaQuoteByTicker(String ticker) {

        logger.info("Requesting Alpha Vantage quote for ticker: {}", ticker);

        if (ticker == null || ticker.trim().isEmpty()) {
            logger.error("Ticker cannot be null or empty");
            throw new IllegalArgumentException("Ticker cannot be empty");
        }

        return marketDataDao
                .findById(ticker.trim().toUpperCase())
                .orElseThrow(() -> {
                    logger.error("Quote not found for ticker: {}", ticker);
                    return new IllegalArgumentException("Invalid ticker: " + ticker);
                });
    }

    /**
     * Update quote table from Alpha Vantage source
     *
     * @throws DataAccessException if unable to retrieve or save data
     * @throws IllegalArgumentException if ticker is invalid
     */
    public void updateMarketData() {
        Iterable<Quote> quoteList = quoteDao.findAll();

        quoteList.forEach(q -> {
            AlphaQuote alphaQuote = marketDataDao
                    .findById(q.getTicker())
                    .orElseThrow(() -> new IllegalArgumentException("ERROR: Ticker " + q.getTicker() + " is invalid"));

            Quote updatedQuote = buildQuoteFromAlphaQuote(alphaQuote);
            quoteDao.save(updatedQuote);
        });
    }

    /**
     * Helper function which converts an AlphaQuote into a Quote
     */
    protected static Quote buildQuoteFromAlphaQuote(AlphaQuote alphaQuote) {
        Quote quote = new Quote();
        QuoteResponseData gq = alphaQuote.getQuoteData();

        if (gq == null) {
            throw new IllegalArgumentException("AlphaQuote is missing Global Quote object");
        }

        quote.setTicker(gq.getSymbol());
        quote.setLastPrice(gq.getPrice());
        quote.setBidPrice(-1d);  // AlphaVantage does NOT provide bid
        quote.setAskPrice(-1d);  // AlphaVantage does NOT provide ask
        quote.setBidSize(-1);   // AlphaVantage does NOT provide bid size
        quote.setAskSize(-1);   // AlphaVantage does NOT provide ask size

        return quote;
    }

    /**
     * Utility: safely parse alpha vantage numeric strings
     */
    private static double parseDoubleOrDefault(String value, double defaultValue) {
        try {
            return (value != null) ? Double.parseDouble(value) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Save a given Quote entity directly to DB (no validation)
     */
    public Quote saveQuote(Quote quote) {
        return quoteDao.save(quote);
    }

    /**
     * Validate against Alpha Vantage and save each ticker to the quote table
     *
     * @param tickers - list of ticker strings
     * @throws IllegalArgumentException if any ticker is not found
     */
    public List<Quote> saveQuotes(List<String> tickers) {
        List<Quote> quoteList = new ArrayList<>();

        tickers.forEach(t -> {
            try {
                quoteList.add(saveQuote(t));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("ERROR: Ticker " + t + " is invalid");
            }
        });

        return quoteList;
    }

    /**
     * Validate ticker via Alpha Vantage and save it
     */
    public Quote saveQuote(String ticker) {
        AlphaQuote alphaQuote = findAlphaQuoteByTicker(ticker);
        Quote quote = buildQuoteFromAlphaQuote(alphaQuote);
        return saveQuote(quote);
    }

    /**
     * Get all quotes from DB
     */
    public List<Quote> findAllQuotes() {
        return (List<Quote>) quoteDao.findAll();
    }

}
