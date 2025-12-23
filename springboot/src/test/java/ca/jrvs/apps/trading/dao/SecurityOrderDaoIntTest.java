package ca.jrvs.apps.trading.dao;

import static org.junit.Assert.assertEquals;

import ca.jrvs.apps.trading.TestConfig;
import ca.jrvs.apps.trading.model.domain.Account;
import ca.jrvs.apps.trading.model.domain.Quote;
import ca.jrvs.apps.trading.model.domain.SecurityOrder;
import ca.jrvs.apps.trading.model.domain.Trader;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.LocalDate;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfig.class})
@Sql({"classpath:schema.sql"})
public class SecurityOrderDaoIntTest {

    @Autowired
    private SecurityOrderDao securityOrderDao;

    @Autowired
    private QuoteDao quoteDao;

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private TraderDao traderDao;

    private SecurityOrder savedSecurityOrder;
    private Quote savedQuote;
    private Account savedAccount;
    private Trader savedTrader;

    @Before
    public void addOne() {
        savedQuote = new Quote();
        savedQuote.setAskPrice(10d);
        savedQuote.setAskSize(10);
        savedQuote.setBidPrice(10d);
        savedQuote.setBidSize(10);
        savedQuote.setTicker("AAPL");
        savedQuote.setLastPrice(10.1d);
        quoteDao.save(savedQuote);

        savedTrader = new Trader();
        savedTrader.setFirstName("John");
        savedTrader.setLastName("Doe");
        savedTrader.setDob(LocalDate.of(2025, 12, 12));
        savedTrader.setCountry("Canada");
        savedTrader.setEmail("john@doe.ca");
        savedTrader = traderDao.save(savedTrader);

        savedAccount = new Account();
        savedAccount.setId(savedTrader.getId());
        savedAccount.setAmount(100.0);
        savedAccount = accountDao.save(savedAccount);

        savedSecurityOrder = new SecurityOrder();
        savedSecurityOrder.setAccountId(savedAccount.getId());
        savedSecurityOrder.setStatus("Sold");
        savedSecurityOrder.setTicker(savedQuote.getTicker());
        savedSecurityOrder.setOrderSize(12);
        savedSecurityOrder.setPrice(21.0);
        savedSecurityOrder.setNotes("Note");
        savedSecurityOrder = securityOrderDao.save(savedSecurityOrder);
    }

    @After
    public void deleteAll() {
        securityOrderDao.deleteAll();
        quoteDao.deleteById(savedQuote.getTicker());
        accountDao.deleteById(savedAccount.getId());
        traderDao.deleteById(savedTrader.getId());
    }

    @Test
    public void findAllById() {
        List<SecurityOrder> testList = Lists.newArrayList(securityOrderDao.findAllById(
                Arrays.asList(savedSecurityOrder.getId(), -1)));
        assertEquals(1, testList.size());
        assertEquals(savedSecurityOrder, testList.get(0));
    }

    @Test
    public void saveAll() {
        List<SecurityOrder> testList = new ArrayList<>();
        testList.add(new SecurityOrder());
        testList.get(0).setAccountId(savedAccount.getId());
        testList.get(0).setStatus("In Progress");
        testList.get(0).setTicker(savedQuote.getTicker());
        testList.get(0).setOrderSize(21);
        testList.get(0).setPrice(12.0);
        testList.get(0).setNotes("Note1");
        testList.add(new SecurityOrder());
        testList.get(1).setAccountId(savedAccount.getId());
        testList.get(1).setStatus("Waiting for Response");
        testList.get(1).setTicker(savedQuote.getTicker());
        testList.get(1).setOrderSize(47);
        testList.get(1).setPrice(74.0);
        testList.get(1).setNotes("Note2");
        securityOrderDao.saveAll(testList);
        assertEquals(testList.get(0), securityOrderDao.findById(testList.get(0).getId()).get());
        assertEquals(testList.get(1), securityOrderDao.findById(testList.get(1).getId()).get());
    }

    @Test
    public void findAllByAccountId() {
        List<SecurityOrder> testList = securityOrderDao.findAllByAccountId(savedAccount.getId());
        assertEquals(savedSecurityOrder, testList.get(0));
    }
}