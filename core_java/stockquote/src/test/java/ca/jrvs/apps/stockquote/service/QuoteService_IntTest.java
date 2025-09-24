package ca.jrvs.apps.stockquote.service;

import ca.jrvs.apps.stockquote.client.QuoteHTTPHelper;
import ca.jrvs.apps.stockquote.dao.QuoteDao;
import ca.jrvs.apps.stockquote.model.Quote;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QuoteService_IntTest {

    // Spin up Postgres container automatically
    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.3")
            .withDatabaseName("stockquote")
            .withUsername("test")
            .withPassword("test");

    private Connection connection;
    private QuoteDao quoteDao;
    private QuoteService quoteService;

    @BeforeAll
    void setUpDatabase() throws Exception {
        // Connect to container’s Postgres
        connection = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );

        // Create table for QuoteDao
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS quote (" +
                            "symbol VARCHAR(10) PRIMARY KEY," +
                            "open NUMERIC," +
                            "high NUMERIC," +
                            "low NUMERIC," +
                            "price NUMERIC," +
                            "volume BIGINT," +
                            "latest_trading_day DATE," +
                            "previous_close NUMERIC," +
                            "change NUMERIC," +
                            "change_percent VARCHAR(20)," +
                            "timestamp TIMESTAMP" +
                            ")"
            );
        }

        // Instantiate DAO with the Connection using a fake API
        quoteDao = new QuoteDao(connection);
        quoteService = new QuoteService(quoteDao, new QuoteHTTPHelper("fakeapikey"));
    }

    @AfterAll
    void tearDownDatabase() throws Exception {
        if (connection != null) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS quote");
            }
            connection.close();
        }
    }

    @Test
    void testFetchQuoteDataFromAPI_validTicker() {
        Optional<Quote> quoteOpt = quoteService.fetchQuoteDataFromAPI("MSFT");
        assertTrue(quoteOpt.isPresent(), "Quote should be present for MSFT");
        Quote quote = quoteOpt.get();
        assertEquals("MSFT", quote.getSymbol());
    }

    @Test
    void testFetchQuoteDataFromAPI_invalidTicker() {
        Optional<Quote> quoteOpt = quoteService.fetchQuoteDataFromAPI("INVALIDTICKERXYZ");
        assertTrue(quoteOpt.isEmpty(), "Quote should be empty for invalid ticker");
    }
}
