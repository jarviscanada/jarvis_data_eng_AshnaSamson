package ca.jrvs.apps.trading.dao;

import static org.junit.Assert.*;

import ca.jrvs.apps.trading.TestConfig;
import ca.jrvs.apps.trading.TestDataSourceProperties;
import ca.jrvs.apps.trading.model.domain.Quote;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(TestConfig.class)
@Sql({"classpath:schema.sql"})

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = {TestConfig.class})
//@Sql({"classpath:schema.sql"})
public class QuoteDaoIntTest {

    @Autowired
    private QuoteDao quoteDao;

    private Quote savedQuote;

    @Before
    public void insertOne() {
        savedQuote = new Quote();
        savedQuote.setTicker("MSFT");       // main ticker
        savedQuote.setAskPrice(10d);
        savedQuote.setAskSize(10);
        savedQuote.setBidPrice(10d);
        savedQuote.setBidSize(10);
        savedQuote.setLastPrice(10.1d);

        quoteDao.save(savedQuote);
    }

    @After
    public void deleteOne() {
        quoteDao.deleteById(savedQuote.getTicker());
    }

    @Test
    public void testSave() {
        Quote q = new Quote();
        q.setTicker("GOOGL");
        q.setAskPrice(11d);
        q.setAskSize(11);
        q.setBidPrice(11d);
        q.setBidSize(11);
        q.setLastPrice(11.1d);

        quoteDao.save(q);

        Quote retrieved = quoteDao.findById("GOOGL").get();
        assertEquals(q.getTicker(), retrieved.getTicker());
        assertEquals(q.getAskPrice(), retrieved.getAskPrice());
        assertEquals(q.getAskSize(), retrieved.getAskSize());
        assertEquals(q.getBidPrice(), retrieved.getBidPrice());
        assertEquals(q.getBidSize(), retrieved.getBidSize());
        assertEquals(q.getLastPrice(), retrieved.getLastPrice());

    }

    @Test
    public void testSaveAll() {
        Quote q1 = new Quote();
        q1.setTicker("NVDA");
        q1.setAskPrice(11d);
        q1.setAskSize(11);
        q1.setBidPrice(11d);
        q1.setBidSize(11);
        q1.setLastPrice(11.1d);

        Quote q2 = new Quote();
        q2.setTicker("AMZN");
        q2.setAskPrice(9d);
        q2.setAskSize(9);
        q2.setBidPrice(9d);
        q2.setBidSize(9);
        q2.setLastPrice(9.1d);

        quoteDao.saveAll(Arrays.asList(q1, q2));

        Quote retrieved1 = quoteDao.findById("NVDA").get();
        assertEquals(q1.getTicker(), retrieved1.getTicker());
        assertEquals(q1.getAskPrice(), retrieved1.getAskPrice());
        assertEquals(q1.getAskSize(), retrieved1.getAskSize());
        assertEquals(q1.getBidPrice(), retrieved1.getBidPrice());
        assertEquals(q1.getBidSize(), retrieved1.getBidSize());
        assertEquals(q1.getLastPrice(), retrieved1.getLastPrice());

        Quote retrieved2 = quoteDao.findById("AMZN").get();
        assertEquals(q2.getTicker(), retrieved2.getTicker());
        assertEquals(q2.getAskPrice(), retrieved2.getAskPrice());
        assertEquals(q2.getAskSize(), retrieved2.getAskSize());
        assertEquals(q2.getBidPrice(), retrieved2.getBidPrice());
        assertEquals(q2.getBidSize(), retrieved2.getBidSize());
        assertEquals(q2.getLastPrice(), retrieved2.getLastPrice());
    }

    @Test
    public void testExistsById() {
        assertTrue(quoteDao.existsById("MSFT"));
        assertFalse(quoteDao.existsById("INVALID_TICKER"));
    }

    @Test
    //checks if setup saved quote ticker exists in any of the quote db rows
    public void testFindAll() {
        Iterable<Quote> all = quoteDao.findAll();
        List<Quote> result = StreamSupport.stream(all.spliterator(), false).collect(Collectors.toList());
        assertTrue(result.stream().anyMatch(dbQuote -> savedQuote.getTicker().equals(dbQuote.getTicker())));
    }

    @Test
    public void testCount() {
        assertEquals(1, quoteDao.count());
    }

    @Test
    public void testDeleteById() {
        quoteDao.deleteById("MSFT");
        assertFalse(quoteDao.existsById("MSFT"));
    }

    @Test
    public void testDeleteAll() {
        quoteDao.deleteAll();
        assertEquals(0, quoteDao.count());
    }
}
