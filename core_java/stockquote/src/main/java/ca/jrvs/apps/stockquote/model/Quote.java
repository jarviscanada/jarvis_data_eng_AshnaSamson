package ca.jrvs.apps.stockquote.model;

import ca.jrvs.apps.stockquote.dto.QuoteResponseData;

import java.util.Scanner;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Quote {

    private String symbol;
    private String ticker; //id
    private double open;
    private double high;
    private double low;
    private double price;
    private int volume;
    private Date latestTradingDay;
    private double previousClose;
    private double change;
    private String changePercent;
    private Timestamp timestamp; //time when the info was pulled

    public Quote(String symbol, double open, double high, double low,
                 double price, int volume, Date latestTradingDay,
                 double previousClose, double change, String changePercent,
                 Timestamp timestamp) {
        this.symbol = symbol;
        this.open = open;
        this.high = high;
        this.low = low;
        this.price = price;
        this.volume = volume;
        this.latestTradingDay = latestTradingDay;
        this.previousClose = previousClose;
        this.change = change;
        this.changePercent = changePercent;
        this.timestamp = timestamp;
    }

    public Quote(QuoteResponseData qr) {
        this.symbol = qr.getSymbol();
        this.open = qr.getOpen();
        this.high = qr.getHigh();
        this.low = qr.getLow();
        this.price = qr.getPrice();
        this.volume = qr.getVolume();
        this.latestTradingDay = qr.getLatestTradingDay();
        this.previousClose = qr.getPreviousClose();
        this.change = qr.getChange();
        this.changePercent = qr.getChangePercent();
        this.timestamp = Timestamp.valueOf(LocalDateTime.now());
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getOpen() { return open; }
    public void setOpen(double open) { this.open = open; }

    public double getHigh() { return high; }
    public void setHigh(double high) { this.high = high; }

    public double getLow() { return low; }
    public void setLow(double low) { this.low = low; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getVolume() { return volume; }
    public void setVolume(int volume) { this.volume = volume; }

    public Date getLatestTradingDay() { return latestTradingDay; }
    public void setLatestTradingDay(Date latestTradingDay) { this.latestTradingDay = latestTradingDay; }

    public double getPreviousClose() { return previousClose; }
    public void setPreviousClose(double previousClose) { this.previousClose = previousClose; }

    public double getChange() { return change; }
    public void setChange(double change) { this.change = change; }

    public String getChangePercent() { return changePercent; }
    public void setChangePercent(String changePercent) { this.changePercent = changePercent; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "Quote{" +
                "symbol='" + symbol + '\'' +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", price=" + price +
                ", volume=" + volume +
                ", latestTradingDay=" + latestTradingDay +
                ", previousClose=" + previousClose +
                ", change=" + change +
                ", changePercent='" + changePercent + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}