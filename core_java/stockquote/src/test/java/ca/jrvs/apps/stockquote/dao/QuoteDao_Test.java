package ca.jrvs.apps.stockquote.dao;

import ca.jrvs.apps.stockquote.model.Quote;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QuoteDao_Test {

    // Spin up a Postgres container for the tests
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("user")
                    .withPassword("password");

    private Connection connection;
    private QuoteDao quoteDao;

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

        // create the table
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS quote (" +
                    "symbol VARCHAR(10) PRIMARY KEY," +
                    "open DOUBLE PRECISION," +
                    "high DOUBLE PRECISION," +
                    "low DOUBLE PRECISION," +
                    "price DOUBLE PRECISION," +
                    "volume INT," +
                    "latest_trading_day DATE," +
                    "previous_close DOUBLE PRECISION," +
                    "change DOUBLE PRECISION," +
                    "change_percent VARCHAR(20)," +
                    "timestamp TIMESTAMP" +
                    ")");
        }

        // clear it before each test
        try (Statement st = connection.createStatement()) {
            st.execute("DELETE FROM quote");
        }

        quoteDao = new QuoteDao(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void testSaveAndFindById() {
        Quote quote = new Quote(
                "AAPL",
                170.0,
                175.0,
                168.0,
                172.0,
                1000000,
                Date.valueOf("2024-09-24"),
                169.5,
                2.5,
                "1.5%",
                new Timestamp(System.currentTimeMillis())
        );

        Quote saved = quoteDao.save(quote);
        assertEquals("AAPL", saved.getSymbol());

        Optional<Quote> found = quoteDao.findById("AAPL");
        assertTrue(found.isPresent());
        assertEquals(172.0, found.get().getPrice());
    }

    @Test
    void testFindAll() {
        // insert two quotes
        quoteDao.save(new Quote("GOOG", 100.0, 105.0, 95.0, 101.0, 2000,
                Date.valueOf("2024-09-24"), 99.0, 2.0, "2%", new Timestamp(System.currentTimeMillis())));
        quoteDao.save(new Quote("MSFT", 200.0, 205.0, 195.0, 201.0, 3000,
                Date.valueOf("2024-09-24"), 199.0, 2.0, "1%", new Timestamp(System.currentTimeMillis())));

        var all = quoteDao.findAll();
        assertTrue(all.iterator().hasNext());
        assertEquals(2, ((java.util.Collection<?>) all).size());
    }

    @Test
    void testDeleteById() {
        Quote quote = new Quote("TSLA", 700, 710, 690, 705, 5000,
                Date.valueOf("2024-09-24"), 695, 10, "1.4%", new Timestamp(System.currentTimeMillis()));
        quoteDao.save(quote);

        quoteDao.deleteById("TSLA");
        Optional<Quote> deleted = quoteDao.findById("TSLA");
        assertTrue(deleted.isEmpty());
    }

    @Test
    void testDeleteAll() {
        quoteDao.save(new Quote("META", 300, 310, 290, 305, 1000,
                Date.valueOf("2024-09-24"), 295, 10, "3%", new Timestamp(System.currentTimeMillis())));

        quoteDao.deleteAll();
        var all = quoteDao.findAll();
        assertFalse(all.iterator().hasNext());
    }
}
