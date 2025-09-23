package ca.jrvs.apps.stockquote.service;

import ca.jrvs.apps.stockquote.client.QuoteHTTPHelper;
import ca.jrvs.apps.stockquote.dao.QuoteDao;
import ca.jrvs.apps.stockquote.model.Quote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuoteService_UnitTest {
    private QuoteHTTPHelper mockHelper;
    private QuoteDao mockDao;
    private QuoteService service;

    @BeforeEach
    void setUp() {
        mockDao = mock(QuoteDao.class);
        mockHelper = mock(QuoteHTTPHelper.class);
        service = new QuoteService(mockDao, mockHelper); // no need for spy
    }

    @Test
    void testFetchQuoteDataFromAPI_validSymbol() throws Exception {
        // Arrange
        Quote fakeQuote = new Quote();
        fakeQuote.setSymbol("AAPL");
        when(mockHelper.fetchQuoteInfo("AAPL")).thenReturn(fakeQuote);

        // Act
        Optional<Quote> result = service.fetchQuoteDataFromAPI("AAPL");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("AAPL", result.get().getSymbol());

        // Verify DAO save was called
        ArgumentCaptor<Quote> captor = ArgumentCaptor.forClass(Quote.class);
        verify(mockDao, times(1)).save(captor.capture());
        assertEquals("AAPL", captor.getValue().getSymbol());
    }

    @Test
    void testFetchQuoteDataFromAPI_invalidSymbol() throws Exception {
        // Arrange
        when(mockHelper.fetchQuoteInfo("BAD")).thenThrow(new RuntimeException("Not found"));

        // Act
        Optional<Quote> result = service.fetchQuoteDataFromAPI("BAD");

        // Assert
        assertFalse(result.isPresent());
        verify(mockDao, never()).save(any());
    }
}