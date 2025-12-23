package ca.jrvs.apps.trading.model.domain;
import com.fasterxml.jackson.annotation.JsonProperty;


public class AlphaQuote {
    @JsonProperty(value = "Global Quote")
    QuoteResponseData quoteData;

    public AlphaQuote() {
    }

    public QuoteResponseData getQuoteData() {
        return quoteData;
    }

    public void setQuoteData(QuoteResponseData quoteData) {
        this.quoteData = quoteData;
    }

    @Override
    public String toString() {
        return quoteData.toString();
    }
}
