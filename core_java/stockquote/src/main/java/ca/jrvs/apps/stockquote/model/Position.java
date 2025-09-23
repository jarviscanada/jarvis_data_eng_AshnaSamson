package ca.jrvs.apps.stockquote.model;

import java.util.Scanner;

public class Position {

    private String symbol;
    private int numberOfShares;
    private double valuePaid;
//    private Quote quote;

    public Position(String symbol, int numberOfShares, double valuePaid) {
        this.symbol = symbol;
        this.numberOfShares = numberOfShares;
        this.valuePaid = valuePaid;
//        this.quote = quote;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public int getNumberOfShares() { return numberOfShares; }
    public void setNumberOfShares(int numberOfShares) { this.numberOfShares = numberOfShares; }

    public double getValuePaid() { return valuePaid; }
    public void setValuePaid(double valuePaid) { this.valuePaid = valuePaid; }

//    public Quote getQuote() { return quote; }
//    public void setQuote(Quote quote) { this.quote = quote; }

    @Override
    public String toString() {
        return "Position{" +
                "symbol='" + symbol + '\'' +
                ", numberOfShares=" + numberOfShares +
                ", valuePaid=" + valuePaid +
//                ", quote=" + (quote != null ? quote.getSymbol() : "null") +
                '}';
    }
}
