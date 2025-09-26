package ca.jrvs.apps.stockquote.service;

import ca.jrvs.apps.stockquote.dao.PositionDao;
import ca.jrvs.apps.stockquote.dao.QuoteDao;
import ca.jrvs.apps.stockquote.model.Position;
import ca.jrvs.apps.stockquote.client.QuoteHTTPHelper;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PositionService_IntTest {

    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("user")
                    .withPassword("password");

    private Connection connection;
    private PositionDao positionDao;
    private QuoteDao quoteDao; // not used directly in this test but required by constructor
    private PositionService positionService;

    @BeforeAll
    void startContainer() {
        postgres.start();
    }

    @AfterAll
    void stopContainer() {
        postgres.stop();
    }

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );

        // Create the position table
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS position (" +
                    "symbol VARCHAR(10) PRIMARY KEY," +
                    "number_of_shares INT," +
                    "value_paid DOUBLE PRECISION" +
                    ")");
            st.execute("DELETE FROM position");
        }

        positionDao = new PositionDao(connection);
        quoteDao = new QuoteDao(connection);
        positionService = new PositionService(positionDao, quoteDao, new QuoteHTTPHelper("fakeapikey"));
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void testBuyCreatesNewPosition() {
        Position p = positionService.buy("AAPL", 10, 150.0);
        assertEquals("AAPL", p.getSymbol());
        assertEquals(10, p.getNumberOfShares());
        assertEquals(1500.0, p.getValuePaid());

        Optional<Position> dbPosition = positionDao.findById("AAPL");
        assertTrue(dbPosition.isPresent());
        assertEquals(1500.0, dbPosition.get().getValuePaid());
    }

    @Test
    void testBuyUpdatesExistingPosition() {
        // first buy then second buy (should add shares and value)
        positionService.buy("AAPL", 10, 150.0);
        Position updated = positionService.buy("AAPL", 5, 200.0);

        assertEquals(15, updated.getNumberOfShares());
        assertEquals(1500.0 + 5 * 200.0, updated.getValuePaid());

        Optional<Position> dbPosition = positionDao.findById("AAPL");
        assertTrue(dbPosition.isPresent());
        assertEquals(15, dbPosition.get().getNumberOfShares());
    }

    @Test
    void testSellRemovesPosition() {
        positionService.buy("AAPL", 10, 150.0);
        Optional<Position> beforeSell = positionDao.findById("AAPL");
        assertTrue(beforeSell.isPresent());

        positionService.sell("AAPL");
        Optional<Position> afterSell = positionDao.findById("AAPL");
        assertTrue(afterSell.isEmpty());
    }

    @Test
    void testSellThrowsForUnknownTicker() {
        assertThrows(IllegalArgumentException.class, () -> positionService.sell("MSFT"));
    }

    @Test
    void testBuyInvalidArgs() {
        assertThrows(IllegalArgumentException.class, () -> positionService.buy(null, 10, 100));
        assertThrows(IllegalArgumentException.class, () -> positionService.buy("", 10, 100));
        assertThrows(IllegalArgumentException.class, () -> positionService.buy("AAPL", -5, 100));
        assertThrows(IllegalArgumentException.class, () -> positionService.buy("AAPL", 5, -100));
    }
}
