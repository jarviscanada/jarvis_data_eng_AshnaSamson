package ca.jrvs.apps.trading.dao;

import ca.jrvs.apps.trading.model.domain.Account;
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
public class AccountDao extends JdbcCrudDao<Account> {

    private static final Logger logger = LoggerFactory.getLogger(AccountDao.class);

    private static final String TABLE_NAME = "account";
    private static final String ID_COLUMN = "id";
    private static final String TRADER_COLUMN = "trader_id";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleInsert;

    @Autowired
    public AccountDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleInsert = new SimpleJdbcInsert(dataSource)
                .withTableName(TABLE_NAME)
                .usingGeneratedKeyColumns(ID_COLUMN);
    }

    @Override
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @Override
    public SimpleJdbcInsert getSimpleJdbcInsert() {
        return simpleInsert;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getIdColumnName() {
        return ID_COLUMN;
    }

    @Override
    Class<Account> getEntityClass() {
        return Account.class;
    }

    /**
     * Update an Account's amount
     */
    @Override
    public int updateOne(Account entity) {
        final String updateSql =
                "UPDATE " + TABLE_NAME + " SET amount = ? WHERE " + ID_COLUMN + " = ?";
        return jdbcTemplate.update(updateSql, entity.getAmount(), entity.getId());
    }

    @Override
    public <S extends Account> Iterable<S> saveAll(Iterable<S> iterable) {
        iterable.forEach(this::save);
        return iterable;
    }

    @Override
    public void delete(Account account) {
        throw new UnsupportedOperationException("Delete by Account not implemented");
    }

    @Override
    public void deleteAll(Iterable<? extends Account> iterable) {
        throw new UnsupportedOperationException("Bulk delete not implemented");
    }

    /**
     * Find an Account by trader ID
     */
    public Optional<Account> findByTraderId(Integer traderId) {
        final String selectSql =
                "SELECT * FROM " + TABLE_NAME + " WHERE " + TRADER_COLUMN + " = ?";

        Optional<Account> entity = Optional.empty();
        try {
            entity = Optional.ofNullable(
                    jdbcTemplate.queryForObject(
                            selectSql,
                            BeanPropertyRowMapper.newInstance(Account.class),
                            traderId
                    )
            );
        } catch (IncorrectResultSizeDataAccessException ex) {
            logger.warn("Account not found for trader ID: " + traderId);
        }

        return entity;
    }
}

