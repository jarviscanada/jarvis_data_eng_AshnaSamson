package ca.jrvs.apps.trading.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import ca.jrvs.apps.trading.TestConfig;
import ca.jrvs.apps.trading.model.domain.Account;
import ca.jrvs.apps.trading.model.domain.Trader;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(TestConfig.class)
@Sql({"classpath:schema.sql"})
public class AccountDaoIntTest {

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private TraderDao traderDao;

    private Trader savedTrader;
    private Account savedAccount;

    @Before
    public void setup() {
        Trader trader = new Trader();
        trader.setFirstName("John");
        trader.setLastName("Doe");
        trader.setDob(LocalDate.of(2025, 12, 12));
        trader.setCountry("Canada");
        trader.setEmail("john@doe.ca");

        savedTrader = traderDao.save(trader);
        assertNotNull(savedTrader.getId());

        Account account = new Account();
        account.setTraderId(savedTrader.getId());
        account.setAmount(100.0);

        savedAccount = accountDao.save(account);
        assertNotNull(savedAccount.getId());
    }

    @Before
    public void addOne() {
        savedTrader = new Trader();
        savedTrader.setFirstName("John");
        savedTrader.setLastName("Doe");
        savedTrader.setDob(LocalDate.of(2025, 12, 12));
        savedTrader.setCountry("Canada");
        savedTrader.setEmail("john@doe.ca");
        savedTrader = traderDao.save(savedTrader);

        savedAccount = new Account();
        savedAccount.setTraderId(savedTrader.getId());
        savedAccount.setAmount(100.0);
        savedAccount = accountDao.save(savedAccount);
    }


    @After
    public void cleanup() {
        accountDao.deleteAll();
        traderDao.deleteById(savedTrader.getId());
    }

    @Test
    public void findAllById() {
        List<Account> result = Lists.newArrayList(
                accountDao.findAllById(Arrays.asList(savedAccount.getId(), -1))
        );

        assertEquals(1, result.size());

        Account r = result.get(0);
        assertEquals(savedAccount.getId(), r.getId());
        assertEquals(savedAccount.getTraderId(), r.getTraderId());
        assertEquals(savedAccount.getAmount(), r.getAmount());
    }

    @Test
    public void saveAll() {
        List<Account> accounts = new ArrayList<>();

        Account a1 = new Account();
        a1.setTraderId(savedTrader.getId());
        a1.setAmount(200.0);

        Account a2 = new Account();
        a2.setTraderId(savedTrader.getId());
        a2.setAmount(300.0);

        accounts.add(a1);
        accounts.add(a2);

        accountDao.saveAll(accounts);

        Account r1 = accountDao.findById(a1.getId()).get();
        assertEquals(a1.getTraderId(), r1.getTraderId());
        assertEquals(a1.getAmount(), r1.getAmount());

        Account r2 = accountDao.findById(a2.getId()).get();
        assertEquals(a2.getTraderId(), r2.getTraderId());
        assertEquals(a2.getAmount(), r2.getAmount());
    }

    @Test
    public void findByTraderId() {
        Account r = accountDao.findByTraderId(savedTrader.getId()).get();

        assertEquals(savedAccount.getId(), r.getId());
        assertEquals(savedAccount.getTraderId(), r.getTraderId());
        assertEquals(savedAccount.getAmount(), r.getAmount());
    }
}
