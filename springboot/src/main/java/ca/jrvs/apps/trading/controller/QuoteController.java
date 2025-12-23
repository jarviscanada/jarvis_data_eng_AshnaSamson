package ca.jrvs.apps.trading.controller;

import ca.jrvs.apps.trading.model.domain.AlphaQuote;
import ca.jrvs.apps.trading.model.domain.Quote;
import ca.jrvs.apps.trading.service.QuoteService;
import java.util.List;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/quote")
public class QuoteController {

    public QuoteService quoteService;

    @Autowired
    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    /**
     * GET /quote/av/ticker/{ticker}
     * Fetch a real-time quote from Alpha Vantage.
     */
    @ApiOperation(value = "Get Alpha Vantage Quote",
            notes = "Fetches real-time Global Quote data from Alpha Vantage for a given ticker."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Ticker not found in Alpha Vantage"),
            @ApiResponse(code = 200, message = "Successfully retrieved quote")
    })
    @GetMapping(path = "/av/ticker/{ticker}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public AlphaQuote getAlphaVantageQuote(@PathVariable String ticker) {
        try {
            return quoteService.findAlphaQuoteByTicker(ticker); //instance method, so call it on an injected service object, not the class name.
        } catch (Exception e) {
            throw ResponseExceptionUtil.getResponseStatusException(e);
        }
    }

    @ApiOperation(value = "Update quote table using Alpha Vantage Quote data",
            notes = "Update all quotes in the quote table. Use Alpha Vantage trading API as market data source.")
    @PutMapping(path = "/av/ticker/{ticker}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void updateMarketData() {
        try {
            quoteService.updateMarketData();
        } catch (Exception e) {
            throw ResponseExceptionUtil.getResponseStatusException(e);
        }
    }

    @ApiOperation(value = "Update a given quote in the quote table",
            notes = "Manually update a quote in the quote table using Alpha Vantage market data")
    @PutMapping(path = "/")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Quote putQuote(@RequestBody Quote quote) {
        try {
            return quoteService.saveQuote(quote);
        } catch (Exception e) {
            throw ResponseExceptionUtil.getResponseStatusException(e);
        }
    }

    @ApiOperation(value = "Add a new ticker to the daily list (quote table)",
            notes = "Add a new ticker/symbol to the quote table, making it tradable")
    @PostMapping(path = "/tickerId/{tickerId}")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Ticker not found in Alpha Vantage system")})
    @ResponseBody
    public Quote createQuote(@PathVariable String tickerId) {
        try {
            return quoteService.saveQuote(tickerId);
        } catch (Exception e) {
            throw ResponseExceptionUtil.getResponseStatusException(e);
        }
    }

    @ApiOperation(value = "Show the daily list",
            notes = "Show the daily list for the current trading system.")
    @GetMapping(path = "/dailyList")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<Quote> getDailyList() {
        try {
            return quoteService.findAllQuotes();
        } catch (Exception e) {
            throw ResponseExceptionUtil.getResponseStatusException(e);
        }
    }
}

