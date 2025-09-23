package ca.jrvs.apps.stockquote.dto;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QuoteResponse {
    @JsonProperty(value = "Global Quote")
    QuoteResponseData quoteData;

    public QuoteResponse() {
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
