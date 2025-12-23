package ca.jrvs.apps.trading.dao;

import static org.junit.Assert.assertEquals;

import ca.jrvs.apps.trading.TestConfig;
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
public class TraderDaoIntTest {

    @Autowired
    private TraderDao traderDao;

    private Trader savedTrader;

    @Before
    public void insertOne() {
        savedTrader = new Trader();
        savedTrader.setFirstName("John");
        savedTrader.setLastName("Doe");
        savedTrader.setDob(LocalDate.of(2025, 12, 12));
        savedTrader.setCountry("Canada");
        savedTrader.setEmail("john@doe.ca");
        savedTrader = traderDao.save(savedTrader);
    }

    @After
    public void deleteAll() {
        traderDao.deleteAll();
    }

    @Test
    public void findAllById() {
        List<Trader> testList = Lists.newArrayList(traderDao.findAllById(
                Arrays.asList(savedTrader.getId(), -1)));
        assertEquals(1, testList.size());
        assertEquals(savedTrader, testList.get(0));
    }

    @Test
    public void save() {
        Trader testTrader = new Trader();
        testTrader.setFirstName("Bob");
        testTrader.setLastName("Doe");
        testTrader.setDob(LocalDate.of(2025, 12, 12));
        testTrader.setCountry("Canada");
        testTrader.setEmail("bob@doe.ca");
        testTrader = traderDao.save(testTrader);
        assertEquals(testTrader, traderDao.findById(testTrader.getId()).get());
    }

    @Test
    public void saveAll() {
        List<Trader> testList = new ArrayList<>();
        testList.add(new Trader());
        testList.get(0).setFirstName("Bob");
        testList.get(0).setLastName("Doe");
        testList.get(0).setDob(LocalDate.of(2020, 7, 7));
        testList.get(0).setCountry("Canada");
        testList.get(0).setEmail("bob@doe.ca");
        testList.add(new Trader());
        testList.get(1).setFirstName("Katie");
        testList.get(1).setLastName("Doe");
        testList.get(1).setDob(LocalDate.of(2020, 7, 7));
        testList.get(1).setCountry("Canada");
        testList.get(1).setEmail("katie@doe.ca");
        testList = (List) traderDao.saveAll(testList);
        assertEquals(testList.get(0), traderDao.findById(testList.get(0).getId()).get());
        assertEquals(testList.get(1), traderDao.findById(testList.get(1).getId()).get());
    }
}