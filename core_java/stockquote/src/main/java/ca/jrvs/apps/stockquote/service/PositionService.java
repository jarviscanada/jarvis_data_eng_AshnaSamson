package ca.jrvs.apps.stockquote.service;

import ca.jrvs.apps.stockquote.dao.PositionDao;
import ca.jrvs.apps.stockquote.dao.QuoteDao;
import ca.jrvs.apps.stockquote.model.Position;
import ca.jrvs.apps.stockquote.model.Quote;
import ca.jrvs.apps.stockquote.client.QuoteHTTPHelper;
import java.util.Scanner;
import java.util.Optional;

public class PositionService {
    PositionDao positionDao;
    QuoteDao quoteDao;
    QuoteHTTPHelper quoteClient;

    public PositionService(PositionDao positionDao, QuoteDao quoteDao, QuoteHTTPHelper quoteClient) {
        this.positionDao = positionDao;
        this.quoteDao = quoteDao;
        this.quoteClient = quoteClient;
    }

    /**
     * Processes a buy order and updates the database accordingly
     *
     * @param ticker
     * @param numberOfShares
     * @return The position in our database after processing the buy
     */

    public Position buy(String ticker, int numberOfShares) {
        if (ticker == null || ticker.isEmpty()) {
            throw new IllegalArgumentException("Ticker cannot be null or empty");
        }

        Optional<Quote> optionalQuote = quoteDao.findById(ticker);
        if (optionalQuote.isEmpty()) {
            throw new IllegalArgumentException("Ticker not found in the database.");
        }

        Quote quote = optionalQuote.get();

        if (numberOfShares <= 0 || numberOfShares > quote.getVolume()) {
            throw new IllegalArgumentException("Number of shares invalid");
        }
        double price = quote.getPrice();

        // Fetch current position if it exists
        Optional<Position> existing = positionDao.findById(ticker);

        Position newPosition;
        if (existing.isPresent()) {
            // Update existing position
            Position oldPos = existing.get();
            int updatedShares = oldPos.getNumberOfShares() + numberOfShares;
            double updatedValue = oldPos.getValuePaid() + (numberOfShares * price);
            newPosition = new Position(ticker, updatedShares, updatedValue);
        } else {
            // Create new position
            newPosition = new Position(ticker, numberOfShares, numberOfShares * price);
        }

        // Save or update in database
        return positionDao.save(newPosition);
    }


    /**
     * Sells all shares of the given ticker symbol
     *
     * @param ticker
     */

    public void sell(String ticker) {
        if (ticker == null || ticker.isEmpty()) {
            throw new IllegalArgumentException("Ticker cannot be null or empty");
        }

        Optional<Position> position = positionDao.findById(ticker);
        if (!position.isPresent()) {
            throw new IllegalArgumentException("No position found for ticker: " + ticker);
        }

        // Delete position from database
        positionDao.deleteById(ticker);
    }

}