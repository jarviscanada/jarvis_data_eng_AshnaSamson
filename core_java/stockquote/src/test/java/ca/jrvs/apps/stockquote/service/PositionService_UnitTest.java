package ca.jrvs.apps.stockquote.service;

import ca.jrvs.apps.stockquote.dao.PositionDao;
import ca.jrvs.apps.stockquote.dao.QuoteDao;
import ca.jrvs.apps.stockquote.model.Position;
import ca.jrvs.apps.stockquote.client.QuoteHTTPHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PositionService_UnitTest {
    private PositionDao positionDao;
    private QuoteDao quoteDao;
    private QuoteHTTPHelper quoteHTTPHelper;
    private PositionService positionService;

    @BeforeEach
    void setUp() {
        // use classic mock maker if inline fails (see previous answer)
        positionDao = mock(PositionDao.class);
        quoteDao = mock(QuoteDao.class);
        quoteHTTPHelper = mock(QuoteHTTPHelper.class);
        positionService = new PositionService(positionDao, quoteDao, quoteHTTPHelper);
    }

    @Test
    void buy_createsNewPositionWhenNoneExists() {
        String ticker = "APPL";
        int shares = 10;
        double price = 300.0;

        // no existing position
        when(positionDao.findById(ticker)).thenReturn(Optional.empty());

        // capture what gets saved
        ArgumentCaptor<Position> captor = ArgumentCaptor.forClass(Position.class);
        when(positionDao.save(any(Position.class))).thenAnswer(inv -> inv.getArgument(0));

        Position saved = positionService.buy(ticker, shares, price);

        // verify
        verify(positionDao).findById(ticker);
        verify(positionDao).save(captor.capture());

        Position p = captor.getValue();
        assertEquals(ticker, p.getSymbol());
        assertEquals(shares, p.getNumberOfShares());
        assertEquals(shares * price, p.getValuePaid());

        // returned value matches
        assertEquals(saved.getSymbol(), ticker);
        assertEquals(saved.getNumberOfShares(), shares);
    }

    @Test
    void buy_updatesExistingPosition() {
        String ticker = "APPL";
        int shares = 5;
        double price = 200.0;

        Position existing = new Position(ticker, 10, 2000.0);
        when(positionDao.findById(ticker)).thenReturn(Optional.of(existing));

        when(positionDao.save(any(Position.class))).thenAnswer(inv -> inv.getArgument(0));

        Position updated = positionService.buy(ticker, shares, price);

        verify(positionDao).findById(ticker);
        verify(positionDao).save(any(Position.class));

        assertEquals(ticker, updated.getSymbol());
        // old 10 + new 5
        assertEquals(15, updated.getNumberOfShares());
        // old 2000 + (5*200)
        assertEquals(2000.0 + 1000.0, updated.getValuePaid());
    }

    @Test
    void buy_throwsForInvalidInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> positionService.buy("", 10, 100.0));
        assertThrows(IllegalArgumentException.class,
                () -> positionService.buy("APPL", 0, 100.0));
        assertThrows(IllegalArgumentException.class,
                () -> positionService.buy("APPL", 10, -5.0));
    }

    @Test
    void sell_deletesExistingPosition() {
        String ticker = "APPL";
        when(positionDao.findById(ticker)).thenReturn(Optional.of(new Position(ticker, 10, 1000.0)));

        positionService.sell(ticker);

        verify(positionDao).findById(ticker);
        verify(positionDao).deleteById(ticker);
    }

    @Test
    void sell_throwsWhenPositionNotFound() {
        when(positionDao.findById("APPL")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> positionService.sell("APPL"));

        verify(positionDao).findById("APPL");
        verify(positionDao, never()).deleteById(anyString());
    }

    @Test
    void sell_throwsForInvalidTicker() {
        assertThrows(IllegalArgumentException.class,
                () -> positionService.sell(""));
    }
}