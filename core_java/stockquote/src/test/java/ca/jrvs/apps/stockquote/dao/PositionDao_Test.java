package ca.jrvs.apps.stockquote.dao;

import ca.jrvs.apps.stockquote.model.Position;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PositionDao_Test {

    private static PostgreSQLContainer<?> postgres;
    private Connection conn;
    private PositionDao positionDao;

    @BeforeAll
    static void startContainer() {
        // Initialize Postgres container
        postgres = new PostgreSQLContainer<>("postgres:15")
                .withDatabaseName("testdb")
                .withUsername("user")
                .withPassword("password");
        postgres.start();
    }

    @AfterAll
    static void stopContainer() {
        if (postgres != null) {
            postgres.stop();
        }
    }

    @BeforeAll
    void setUpDatabase() throws SQLException {
        conn = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );

        // Create table
        conn.createStatement().execute("""
                CREATE TABLE IF NOT EXISTS position(
                    symbol VARCHAR PRIMARY KEY,
                    number_of_shares INT,
                    value_paid DOUBLE PRECISION
                );
                """);

        positionDao = new PositionDao(conn);
    }

    @BeforeEach
    void cleanTable() {
        positionDao.deleteAll();
    }

    @AfterAll
    void tearDownDatabase() throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

    @Test
    void save_createsAndUpdatesPosition() {
        Position p = new Position("MSFT", 10, 1500.0);
        Position saved = positionDao.save(p);
        assertEquals(p.getSymbol(), saved.getSymbol());
        assertEquals(p.getNumberOfShares(), saved.getNumberOfShares());

        // Update same symbol
        Position updated = new Position("MSFT", 20, 3000.0);
        Position saved2 = positionDao.save(updated);
        assertEquals(20, saved2.getNumberOfShares());
        assertEquals(3000.0, saved2.getValuePaid());
    }

    @Test
    void findById_returnsPosition() {
        Position p = new Position("MSFT", 10, 1500.0);
        positionDao.save(p);

        Optional<Position> fetched = positionDao.findById("MSFT");
        assertTrue(fetched.isPresent());
        assertEquals(10, fetched.get().getNumberOfShares());
    }

    @Test
    void findById_returnsEmptyIfNotFound() {
        Optional<Position> fetched = positionDao.findById("NONEXIST");
        assertTrue(fetched.isEmpty());
    }

    @Test
    void findAll_returnsMultiplePositions() {
        Position p1 = new Position("MSFT", 10, 1500.0);
        Position p2 = new Position("AAPL", 5, 1000.0);
        positionDao.save(p1);
        positionDao.save(p2);

        Iterable<Position> all = positionDao.findAll();
        int count = 0;
        for (Position p : all) count++;
        assertEquals(2, count);
    }

    @Test
    void deleteById_removesPosition() {
        Position p = new Position("MSFT", 10, 1500.0);
        positionDao.save(p);

        positionDao.deleteById("MSFT");
        Optional<Position> fetched = positionDao.findById("MSFT");
        assertTrue(fetched.isEmpty());
    }

    @Test
    void deleteAll_removesAll() {
        Position p1 = new Position("MSFT", 10, 1500.0);
        Position p2 = new Position("AAPL", 5, 1000.0);
        positionDao.save(p1);
        positionDao.save(p2);

        positionDao.deleteAll();

        Iterable<Position> all = positionDao.findAll();
        int count = 0;
        for (Position p : all) count++;
        assertEquals(0, count);
    }
}
