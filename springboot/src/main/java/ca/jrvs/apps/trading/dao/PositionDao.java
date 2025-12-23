package ca.jrvs.apps.trading.dao;

import ca.jrvs.apps.trading.model.domain.Position;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class PositionDao {

    private static final Logger logger = LoggerFactory.getLogger(SecurityOrderDao.class);

    private final String VIEW_NAME = "position";
    private final String ACCOUNT_ID = "account_id";
    private final String QUOTE_ID = "ticker";

    private JdbcTemplate jdbcTemplate;
    @Autowired
    public PositionDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    public Optional<Position> findById(Integer accountId, String ticker) {
        Optional<Position> entity = Optional.empty();
        String selectSql = "SELECT * FROM " + VIEW_NAME + " WHERE " + ACCOUNT_ID + "=? AND " + QUOTE_ID + "=?";
        logger.debug("accountId = " + accountId);
        logger.debug("ticker = " + ticker);
        try {
            BeanPropertyRowMapper<Position> f = BeanPropertyRowMapper.newInstance(Position.class);
            Position e = jdbcTemplate.queryForObject(selectSql, f, accountId, ticker);
            entity = Optional.ofNullable(e);
        } catch (IncorrectResultSizeDataAccessException ex) {
            logger.error("Cannot find position for account " + accountId + " and ticker " + ticker, ex);
        }
        return entity;
    }

    public boolean existsById(Integer accountId, String ticker) {
        try {
            findById(accountId, ticker).get();
            return true;
        } catch (NoSuchElementException ex) {
            return false;
        }
    }

    public List<Position> findAll() {
        String selectSql = "SELECT * FROM " + VIEW_NAME;
        List<Position> positions = jdbcTemplate
                .query(selectSql, BeanPropertyRowMapper.newInstance(Position.class));
        return positions;
    }

    public long count() {
        String countSql = "SELECT COUNT(*) FROM " + VIEW_NAME;
        return jdbcTemplate.queryForObject(countSql, Integer.class);
    }

    public List<Position> findAllByAccountId(Integer accountId) {
        String selectSql = "SELECT * FROM " + VIEW_NAME + " WHERE " + ACCOUNT_ID + "=?";
        List<Position> positions = jdbcTemplate
                .query(selectSql, BeanPropertyRowMapper.newInstance(Position.class), accountId);
        return positions;
    }
}