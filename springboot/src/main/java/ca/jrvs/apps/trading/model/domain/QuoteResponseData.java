package ca.jrvs.apps.trading.model.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.Scanner;
import java.sql.Date;

@JsonRootName(value = "Global Quote")
public class QuoteResponseData {

    @JsonProperty("01. symbol")
    private String symbol;
    @JsonProperty("02. open")
    private double open;
    @JsonProperty("03. high")
    private double high;
    @JsonProperty("04. low")
    private double low;
    @JsonProperty("05. price")
    private double price;
    @JsonProperty("06. volume")
    private int volume;
    @JsonProperty("07. latest trading day")
    private Date latestTradingDay;
    @JsonProperty("08. previous close")
    private double previousClose;
    @JsonProperty("09. change")
    private double change;
    @JsonProperty("10. change percent")
    private String changePercent;

    public QuoteResponseData() {
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
                '}';
    }
}

